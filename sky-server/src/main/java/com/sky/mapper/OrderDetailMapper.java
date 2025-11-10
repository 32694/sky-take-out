package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.vo.DishSalesVO;
import com.sky.vo.SetmealSalesVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    void insertBatch(List<OrderDetail> orderDetails);

    List<OrderDetail> getByOrderId(Long orderId);

    //查询菜品销量
    @Select(" SELECT od.dish_id AS dishId, SUM(od.number) AS num FROM order_detail od JOIN orders o ON od.order_id = o.id WHERE o.order_time >= DATE_SUB(NOW(), INTERVAL 1 MONTH) GROUP BY od.dish_id ")
    List<DishSalesVO> getDishMonthlySales();

    //查询套餐销量
    @Select("SELECT od.setmeal_id AS setmealId, SUM(od.number) AS num " +
            "FROM order_detail od " +
            "JOIN orders o ON od.order_id = o.id " +
            "WHERE o.order_time >= DATE_SUB(NOW(), INTERVAL 1 MONTH) " +
            "AND od.setmeal_id IS NOT NULL " +
            "GROUP BY od.setmeal_id")
    List<SetmealSalesVO> getSetmealMonthlySales();

}
