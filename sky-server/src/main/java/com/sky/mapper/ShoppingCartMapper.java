package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    List<ShoppingCart> getByDishIdAndSetmealId(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumber(ShoppingCart cart1);

    @Insert("insert into shopping_cart (user_id, dish_id, setmeal_id, dish_flavor, name, image, number,amount, create_time) values (#{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{name}, #{image},#{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    List<ShoppingCart> list(Long currentId);

    void delete(Long currentId);

    @Delete("delete from shopping_cart where id = #{id}")
    void deletebyId(Long id);
}
