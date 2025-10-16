package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.sky.dto.DishPageQueryDTO;

import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;
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
        PageResult pageResult = new PageResult();
        pageResult.setTotal(pagedata.getTotal());
        pageResult.setRecords(pagedata.getResult());
        return pageResult;
    }

    @Override
    public void add(Dish dish) {
        dishMapper.add(dish);
    }


    @Override
    public DishVO getById(Long id) {
        DishVO dishVO = dishMapper.findById(id);
        List<DishFlavor> dishFlavor = dishFlavorMapper.list(id);
        dishVO.setFlavors(dishFlavor);
        return dishVO;
    }
}
