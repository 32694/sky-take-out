package com.sky.service.impl;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired private ShoppingCartMapper shoppingCartMapper;
@Autowired private DishMapper dishMapper;
@Autowired private SetMealMapper setMealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart.setDishId(shoppingCartDTO.getDishId());
        shoppingCart.setSetmealId(shoppingCartDTO.getSetmealId());
        shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
        //查询该用户的某个套餐或菜品是否存在
        //1.如果存在，数量加1
        List<ShoppingCart> cart = shoppingCartMapper.getByDishIdAndSetmealId(shoppingCart);
        if(cart != null && cart.size() > 0){
            ShoppingCart cart1 = cart.get(0);
            cart1.setNumber(cart1.getNumber() + 1);
            shoppingCartMapper.updateNumber(cart1);
            return;
        }
        //2.如果不存在，添加
        if(shoppingCart.getDishId() != null)
        {
           DishVO dish = dishMapper.findById(shoppingCart.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        }
        if(shoppingCart.getSetmealId() != null)
        {
            SetmealVO setmeal = setMealMapper.getById(shoppingCart.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    public List<ShoppingCart> list(Long currentId) {
        return shoppingCartMapper.list(currentId);
    }

    @Override
    public void clean(Long currentId) {
        shoppingCartMapper.delete(currentId);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart.setDishId(shoppingCartDTO.getDishId());
        shoppingCart.setSetmealId(shoppingCartDTO.getSetmealId());
        shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
        List<ShoppingCart> cart = shoppingCartMapper.getByDishIdAndSetmealId(shoppingCart);
        if(cart != null && cart.size() > 0){
            ShoppingCart cart1 = cart.get(0);
            if(cart1.getNumber() == 1){
                shoppingCartMapper.deletebyId(cart1.getId());
            }else{
                cart1.setNumber(cart1.getNumber() - 1);
                shoppingCartMapper.updateNumber(cart1);
            }
        }

    }
}
