package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper
{
    List<DishFlavor> list(Long dishId);

    void insertBatch(List<DishFlavor> dishFlavorList);

    void deleteByDishId(Long categoryId);
}
