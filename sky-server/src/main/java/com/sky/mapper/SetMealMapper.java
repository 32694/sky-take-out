package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealMapper {
    @AutoFill(value = OperationType.INSERT)
    void add(Setmeal setmeal);

    void addSetmealDish(List<SetmealDish> setmealDishes);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);


    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);
    SetmealVO getById(Long id);



    // 根据菜品id删除
    void deleteByDishId(Long Id);

    //查询套餐中的菜品
    List<SetmealDish> getListDish(Long setmealId);

    void delete(List<Long> ids);
}
