package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SetMealServiceImpl implements SetMealService {
    @Autowired
    private SetMealMapper setMealMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Transactional
    @Override
    public void add(SetmealDTO setmealDTO) {
        Setmeal setmeal= Setmeal.builder().
                name(setmealDTO.getName()).
                categoryId(setmealDTO.getCategoryId()).
                price(setmealDTO.getPrice()).
                image(setmealDTO.getImage()).
                description(setmealDTO.getDescription()).
                status(setmealDTO.getStatus()).
                build();
        setMealMapper.add(setmeal);
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        if(setmealDishes!=null&&setmealDishes.size()>0)
        {
            for(SetmealDish setmealDish:setmealDishes)
            {
                setmealDish.setSetmealId(setmeal.getId());
            }
            setMealMapper.addSetmealDish(setmealDishes);
        }
    }

    @Override
    public PageResult pagequery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> pagedata=setMealMapper.pageQuery(setmealPageQueryDTO);

        List<SetmealVO> list = pagedata.getResult();
        list.forEach(setmealVO -> {
            //定义口味集合
            List<SetmealDish> setmealDish=setMealMapper.getListDish(setmealVO.getId());
            setmealVO.setSetmealDishes(setmealDish);
        });
        list = listBySetMealId(list);
        return new PageResult(pagedata.getTotal(),list);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal=Setmeal.builder().
                id(id).
                status(status).
                build();
        setMealMapper.update(setmeal);
    }

    @Transactional
    @Override
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO = setMealMapper.getById(id);
        setmealVO.setSetmealDishes(setMealMapper.getListDish(id));
        return setmealVO;
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal=Setmeal.builder().
                id(setmealDTO.getId()).
                name(setmealDTO.getName()).
                categoryId(setmealDTO.getCategoryId()).
                price(setmealDTO.getPrice()).
                image(setmealDTO.getImage()).
                description(setmealDTO.getDescription()).
                status(setmealDTO.getStatus()).
                build();
        setMealMapper.update(setmeal);
       List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
       if(setmealDishes!=null&&setmealDishes.size()>0)
       {
           setMealMapper.deleteByDishId(setmeal.getId());
           setmealDishes.forEach(setmealDish -> setMealMapper.deleteByDishId(setmealDish.getId()));
           setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
           setMealMapper.addSetmealDish(setmealDishes);
       }
    }

    @Transactional
    @Override
    public void delete(List<Long> ids) {
        setMealMapper.delete(ids);
        ids.forEach(id -> setMealMapper.deleteByDishId(id));
    }

    @Override
    public List<SetmealVO> list(Setmeal setmeal) {
        List<SetmealVO> list = setMealMapper.list(setmeal);
        list = listBySetMealId(list);
        return list;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setMealMapper.getDishItemBySetmealId(id);
    }


    //查询套餐销售数据
    public List<SetmealVO> listBySetMealId(List<SetmealVO>  setmealList) {
        List<SetmealSalesVO> sales = orderDetailMapper.getSetmealMonthlySales();
        Map<Long, Integer> salesMap = sales.stream()
                .collect(Collectors.toMap(SetmealSalesVO::getSetmealId, SetmealSalesVO::getNum));

        setmealList.forEach(item -> item.setNum(salesMap.getOrDefault(item.getId(), 0)));

        return setmealList;
    }

}
