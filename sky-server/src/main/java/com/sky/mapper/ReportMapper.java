package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper
{


    @Select("select sum(amount) from orders where order_time between #{beginTime} and #{endTime} and status=5")
    Double getTurnoverStatistics(LocalDateTime beginTime, LocalDateTime endTime);

    @Select("select count(id) from user where create_time < #{endTime} ")
    Integer getUserStatistics(LocalDateTime endTime);

    @Select("select count(id) from user where create_time between #{beginTime} and #{endTime}")
    Integer getNewUser(LocalDateTime beginTime, LocalDateTime endTime);

    @Select("select count(id) from orders where order_time between #{beginTime} and #{endTime}")
    Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime);

    @Select("select count(id) from orders where order_time between #{beginTime} and #{endTime} and status=5")
    Integer getValidOrderCount(LocalDateTime beginTime, LocalDateTime endTime);

    @Select("SELECT od.name, COALESCE(SUM(od.number), 0) AS total " +
            "FROM order_detail od " +
            "INNER JOIN orders o ON od.order_id = o.id " +
            "WHERE o.order_time BETWEEN #{begin} AND #{end} " +
            "AND o.status = 5 " +
            "GROUP BY od.name " +
            "HAVING total > 0 " +
            "ORDER BY total DESC " +
            "LIMIT 10")
    List<Map<String, Object>> getTop10(LocalDate begin, LocalDate end);
}
