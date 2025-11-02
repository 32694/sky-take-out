package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);


    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);


    // 查询订单详情
    OrderVO details(Long id);

    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);


    OrderStatisticsVO statistics();

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusandTime(Integer status, LocalDateTime orderTime);


    // 测试用，不是商户支付功能无法实现
    @Select("select id from orders where number = #{outTradeNo}")
    Long getIdByOutTradeNo(String outTradeNo);

    Integer countByMap(Map map);
}
