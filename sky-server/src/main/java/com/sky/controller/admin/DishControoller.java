package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.impl.SalesCacheService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishControoller {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    // ✅ 新增：注入SalesCacheService
    @Autowired
    private SalesCacheService salesCacheService;

    // 缓存键常量
    private static final String DISH_KEY_PREFIX = "dish_";

    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result pagequery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品");
        PageResult pageResult = dishService.pagequery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("菜品增加")
    @PostMapping()
    public Result add(@RequestBody DishDTO dishDTO) {
        log.info("菜品增加: {}", dishDTO);

        Dish dish = Dish.builder()
                .name(dishDTO.getName())
                .categoryId(dishDTO.getCategoryId())
                .price(dishDTO.getPrice())
                .image(dishDTO.getImage())
                .description(dishDTO.getDescription())
                .status(dishDTO.getStatus())
                .build();

        dishService.add(dish);

        List<DishFlavor> dishFlavorList = dishDTO.getFlavors();
        if (dishFlavorList != null && dishFlavorList.size() > 0) {
            for (DishFlavor dishFlavor : dishFlavorList) {
                dishFlavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(dishFlavorList);
        }

        // 清理相关缓存
        clearRelatedCaches(dish.getCategoryId(), null);
        return Result.success();
    }

    @ApiOperation("禁用或启用状态")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("禁用或启用状态: {}", status);

        // 先获取菜品信息，知道原来的分类ID
        DishVO originalDish = dishService.getById(id);
        Long originalCategoryId = originalDish.getCategoryId();

        dishService.startOrStop(status, id);

        // 清理相关缓存
        clearRelatedCaches(originalCategoryId, id);
        return Result.success();
    }

    @ApiOperation("修改菜品")
    @PutMapping
    @Transactional
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品: {}", dishDTO);

        // 先获取原来的菜品信息，用于比较分类是否变化
        DishVO originalDish = dishService.getById(dishDTO.getId());
        Long originalCategoryId = originalDish.getCategoryId();
        Long newCategoryId = dishDTO.getCategoryId();

        Dish dish = Dish.builder()
                .id(dishDTO.getId())
                .name(dishDTO.getName())
                .categoryId(newCategoryId)
                .price(dishDTO.getPrice())
                .image(dishDTO.getImage())
                .description(dishDTO.getDescription())
                .status(dishDTO.getStatus())
                .build();

        dishService.update(dish);

        List<DishFlavor> dishFlavorList = dishDTO.getFlavors();
        if (dishFlavorList != null && dishFlavorList.size() > 0) {
            dishFlavorMapper.deleteByDishId(dish.getId());
            for (DishFlavor dishFlavor : dishFlavorList) {
                dishFlavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(dishDTO.getFlavors());
        }

        // 清理相关缓存
        clearRelatedCaches(originalCategoryId, dishDTO.getId());

        // 如果分类改变了，还需要清理新分类的缓存
        if (!originalCategoryId.equals(newCategoryId)) {
            clearRelatedCaches(newCategoryId, dishDTO.getId());
        }

        return Result.success();
    }

    @ApiOperation("根据ID查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据ID查询菜品");
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    @ApiOperation("根据分类ID查询菜品")
    @GetMapping("/list")
    public Result<List<DishVO>> list(@RequestParam Long categoryId) {
        log.info("根据分类ID查询菜品");
        List<DishVO> list = dishService.list(categoryId);
        list.forEach(dishVO -> dishVO.setFlavors(dishFlavorMapper.list(dishVO.getId())));
        return Result.success(list);
    }

    @ApiOperation("批量删除")
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除: {}", ids);

        // 先获取所有要删除的菜品信息，记录分类ID
        Set<Long> affectedCategoryIds = new HashSet<>();
        for (Long id : ids) {
            DishVO dish = dishService.getById(id);
            if (dish != null) {
                affectedCategoryIds.add(dish.getCategoryId());
            }
        }

        dishService.deletebatch(ids);

        // 清理所有受影响分类的缓存
        for (Long categoryId : affectedCategoryIds) {
            clearRelatedCaches(categoryId, null);
        }

        return Result.success();
    }

    /**
     * 清理相关缓存
     * @param categoryId 分类ID
     * @param dishId 菜品ID（可选，用于更精确的清理）
     */
    private void clearRelatedCaches(Long categoryId, Long dishId) {
        try {
            // 1. 清理菜品基本信息缓存
            if (categoryId != null) {
                String dishKey = DISH_KEY_PREFIX + categoryId;
                redisTemplate.delete(dishKey);
                log.info("清理菜品缓存: {}", dishKey);
            } else {
                // 如果不知道具体分类，清理所有菜品缓存
                Set<String> dishKeys = redisTemplate.keys(DISH_KEY_PREFIX + "*");
                if (dishKeys != null && !dishKeys.isEmpty()) {
                    redisTemplate.delete(dishKeys);
                    log.info("清理所有菜品缓存: {}个键", dishKeys.size());
                }
            }

            // 2. ✅ 修改：使用SalesCacheService清理销量缓存
            salesCacheService.refreshSalesCache();
            log.info("通过SalesCacheService清理销量缓存");

            // 3. ✅ 新增：清理大众推荐缓存
            String popularRecommendKey = "recommend:popular:by_sales";
            redisTemplate.delete(popularRecommendKey);
            log.info("清理大众推荐缓存");

        } catch (Exception e) {
            log.error("清理缓存失败", e);
            // 不抛出异常，避免影响主要业务逻辑
        }
    }

    /**
     * 原来的清理方法，保留用于兼容
     */
    private void deleteCache(String pattern) {
        try {
            Set keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清理缓存模式: {}, 共{}个键", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("清理缓存失败: {}", pattern, e);
        }
    }
}