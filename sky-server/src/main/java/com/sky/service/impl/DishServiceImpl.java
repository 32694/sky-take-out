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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;
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
        List<DishVO> dishList = dishMapper.list(dish.getCategoryId());

        List<DishVO> dishVOList = new ArrayList<>();

        for (DishVO d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.list(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        dishVOList = listByDishId(dishVOList);
        return dishVOList;
    }


    //查询菜品销售数据
    public List<DishVO> listByDishId(List<DishVO> dishList) {
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
}
