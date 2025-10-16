package com.sky.service;


import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

public interface DishService {

    PageResult pagequery(DishPageQueryDTO dishPageQueryDTO);

    void add(Dish dish);

    DishVO getById(Long id);
}
