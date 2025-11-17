package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.sky.dto.DishPageQueryDTO;

import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishSalesVO;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    // 缓存时间常量
    private static final long DISH_CACHE_TIME = 120; // 小时
    private static final long SALES_CACHE_TIME = 5; // 分钟
    private static final String DISH_KEY_PREFIX = "dish_";
    private static final String SALES_KEY = "dish_sales";
    @Override
    public PageResult pagequery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> pagedata=dishMapper.pagequery(dishPageQueryDTO);

        //获取菜品
        List<DishVO> list = pagedata.getResult();

        list.forEach(dishVO -> {
            //定义口味集合
            List<DishFlavor> dishFlavor=dishFlavorMapper.list(dishVO.getId());
            dishVO.setFlavors(dishFlavor);
        });
        list = listByDishId(list);
        PageResult pageResult = new PageResult();
        pageResult.setTotal(pagedata.getTotal());
        pageResult.setRecords(list);
        return pageResult;
    }


    @Override
    public void add(Dish dish) {
        dishMapper.add(dish);
    }


    @Transactional
    @Override
    public DishVO getById(Long id) {
        DishVO dishVO = dishMapper.findById(id);
        List<DishFlavor> dishFlavor = dishFlavorMapper.list(id);
        dishVO.setFlavors(dishFlavor);
        return dishVO;
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder().id(id).status(status).build();
        dishMapper.update(dish);
    }

    @Override
    public void update(Dish dish) {
        dishMapper.update(dish);
    }

    @Override
    public List<DishVO> list(Long categoryId) {
        List<DishVO> dishList = dishMapper.list(categoryId);
        dishList = listByDishId(dishList);
        return dishList;
    }

    @Transactional
    @Override
    public void deletebatch(List<Long> ids) {
        dishMapper.deletebatch(ids);
        ids.forEach(id -> dishFlavorMapper.deleteByDishId(id));
    }

    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        Long categoryId = dish.getCategoryId();
        log.info("查询分类{}的菜品列表，包含实时销量", categoryId);

        try {
            return getDishesWithRealTimeSales(categoryId);
        } catch (Exception e) {
            log.error("获取菜品列表异常，降级到直接查询数据库", e);
            // 降级方案：直接查询数据库
            return getDishesFromDatabase(categoryId);
        }
    }



    @Override
    public List<DishVO> getPopularBySales(int count) {
        log.info("获取大众推荐菜品，数量: {}", count);

        try {
            return getPopularDishesBySales(count);
        } catch (Exception e) {
            log.error("获取大众推荐失败，使用降级方案", e);
            return getFallbackPopularDishes(count);
        }
    }

    @Override
    public List<DishVO> guessYouLike(Long userId) {
        return null;
    }


    //查询菜品销售数据
    // 这个方法现在只在分页查询等场景使用，不用于缓存
    public List<DishVO> listByDishId(List<DishVO> dishList) {
        // 直接查询实时销量，不经过缓存
        List<DishSalesVO> salesList = orderDetailMapper.getDishMonthlySales();
        Map<Long, Integer> salesMap = salesList.stream()
                .collect(Collectors.toMap(DishSalesVO::getDishId, DishSalesVO::getNum));

        for (DishVO dish : dishList) {
            Long dishId = dish.getId();
            Integer num = salesMap.get(dishId);
            dish.setNum(num != null ? num : 0);
        }
        return dishList;
    }



    /**
     * 获取菜品数据（基本信息+实时销量）
     */
    private List<DishVO> getDishesWithRealTimeSales(Long categoryId) {
        // 1. 获取菜品基本信息（长缓存）
        List<DishVO> dishes = getCachedDishes(categoryId);

        // 2. 获取实时销量数据（短缓存）
        Map<Long, Integer> salesMap = getCachedSales();

        // 3. 合并数据
        return mergeDishesWithSales(dishes, salesMap);
    }

    /**
     * 获取菜品缓存数据
     */
    private List<DishVO> getCachedDishes(Long categoryId) {
        String dishKey = DISH_KEY_PREFIX + categoryId;

        // 尝试从缓存获取
        List<DishVO> cachedDishes = (List<DishVO>) redisTemplate.opsForValue().get(dishKey);

        if (cachedDishes != null && !cachedDishes.isEmpty()) {
            log.debug("缓存命中: {}", dishKey);
            return cachedDishes;
        }

        log.info("缓存未命中，从数据库查询并缓存: {}", dishKey);
        // 缓存未命中，从数据库查询
        List<DishVO> dbDishes = getDishesFromDatabase(categoryId);

        // 缓存前将销量设为0（因为销量要实时获取）
        dbDishes.forEach(dish -> dish.setNum(0));

        // 存入缓存（1小时）
        try {
            redisTemplate.opsForValue().set(
                    dishKey,
                    dbDishes,
                    DISH_CACHE_TIME,
                    TimeUnit.HOURS
            );
            log.info("菜品数据缓存成功: {}", dishKey);
        } catch (Exception e) {
            log.error("缓存菜品数据失败: {}", dishKey, e);
        }

        return dbDishes;
    }

    /**
     * 从数据库获取菜品数据（基础版本）
     */
    private List<DishVO> getDishesFromDatabase(Long categoryId) {
        List<DishVO> dishList = dishMapper.list(categoryId);

        List<DishVO> result = new ArrayList<>();
        for (DishVO dishVO : dishList) {
            DishVO vo = new DishVO();
            BeanUtils.copyProperties(dishVO, vo);

            // 查询口味信息
            List<DishFlavor> flavors = dishFlavorMapper.list(dishVO.getId());
            vo.setFlavors(flavors);

            result.add(vo);
        }

        return result;
    }

    /**
     * 获取销量缓存数据
     */
    private Map<Long, Integer> getCachedSales() {
        // 尝试从缓存获取销量数据
        Map<Long, Integer> salesMap = (Map<Long, Integer>) redisTemplate.opsForValue().get(SALES_KEY);

        if (salesMap != null) {
            log.debug("销量缓存命中");
            return salesMap;
        }

        log.info("销量缓存未命中，从数据库查询");
        // 从数据库查询销量数据
        List<DishSalesVO> salesList = orderDetailMapper.getDishMonthlySales();
        salesMap = salesList.stream()
                .collect(Collectors.toMap(DishSalesVO::getDishId, DishSalesVO::getNum));

        // 存入缓存（5分钟）
        try {
            redisTemplate.opsForValue().set(
                    SALES_KEY,
                    salesMap,
                    SALES_CACHE_TIME,
                    TimeUnit.MINUTES
            );
            log.info("销量数据缓存成功");
        } catch (Exception e) {
            log.error("缓存销量数据失败", e);
        }

        return salesMap;
    }

    /**
     * 合并菜品数据和销量数据
     */
    private List<DishVO> mergeDishesWithSales(List<DishVO> dishes, Map<Long, Integer> salesMap) {
        for (DishVO dish : dishes) {
            Integer sales = salesMap.get(dish.getId());
            dish.setNum(sales != null ? sales : 0);
        }
        return dishes;
    }




    /**
     * 基于销量的热门推荐
     */
    private List<DishVO> getPopularDishesBySales(int count) {
        // 1. 获取所有启用的菜品
        List<DishVO> allDishes = getAllEnabledDishesWithFlavors();

        // 2. 获取销量数据
        Map<Long, Integer> salesMap = getCachedSales();

        // 3. 设置销量并排序
        allDishes.forEach(dish -> {
            Integer sales = salesMap.get(dish.getId());
            dish.setNum(sales != null ? sales : 0);
        });

        // 4. 按销量降序排序
        List<DishVO> sortedDishes = allDishes.stream()
                .sorted((d1, d2) -> Integer.compare(d2.getNum(), d1.getNum()))
                .collect(Collectors.toList());

        // 5. 返回前count个菜品
        return sortedDishes.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有启用的菜品（包含口味信息）
     */
    private List<DishVO> getAllEnabledDishesWithFlavors() {
        // 查询所有启用的菜品
        List<DishVO> dishes = dishMapper.listAllEnabled();

        // 设置口味信息
        for (DishVO dish : dishes) {
            List<DishFlavor> flavors = dishFlavorMapper.list(dish.getId());
            dish.setFlavors(flavors);
        }

        return dishes;
    }

    /**
     * 降级方案：如果销量数据获取失败，返回最近上架的菜品
     */
    private List<DishVO> getFallbackPopularDishes(int count) {
        log.warn("使用降级方案：返回最近上架的菜品");

        List<DishVO> recentDishes = dishMapper.listRecentEnabled(count);
        for (DishVO dish : recentDishes) {
            List<DishFlavor> flavors = dishFlavorMapper.list(dish.getId());
            dish.setFlavors(flavors);
            dish.setNum(0); // 降级方案中销量设为0
        }

        return recentDishes;
    }
}
