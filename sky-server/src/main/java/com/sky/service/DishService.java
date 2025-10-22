package com.sky.service;


import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    PageResult pagequery(DishPageQueryDTO dishPageQueryDTO);

    void add(Dish dish);

    DishVO getById(Long id);

    void startOrStop(Integer status, Long id);

    void update(Dish dish);

    List<DishVO> list(Long categoryId);

    void deletebatch(List<Long> ids);


    List<DishVO> listWithFlavor(Dish dish);
}
