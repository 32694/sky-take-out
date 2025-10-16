package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;

import com.sky.dto.DishPageQueryDTO;

import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper {

    Page<DishVO> pagequery(DishPageQueryDTO dishPageQueryDTO);

    @AutoFill(value = OperationType.INSERT)
    void add(Dish dish);

    DishVO findById(Long id);
}
