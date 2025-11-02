package com.sky.service.impl;

import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private ReportMapper reportMapper;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        List<Double> turnoverList = new ArrayList<>();
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(StringUtils.join(dateList, ","));
        for (LocalDate localDate : dateList) {
           LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
           LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
           Double turnover =reportMapper.getTurnoverStatistics(beginTime, endTime);
           if(turnover == null)
               turnover = 0.0;
           turnoverList.add(turnover);
        }
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList, ","));
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(StringUtils.join(dateList, ","));
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Integer totalUser = reportMapper.getUserStatistics(endTime);
            totalUserList.add(totalUser);
            Integer newUser = reportMapper.getNewUser(beginTime, endTime);
            newUserList.add(newUser);
        }
        userReportVO.setNewUserList(StringUtils.join(newUserList, ","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserList, ","));
        return userReportVO;
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        OrderReportVO orderReportVO = new OrderReportVO();
        orderReportVO.setDateList(StringUtils.join(dateList, ","));
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Integer orderCount = reportMapper.getOrderCount(beginTime, endTime);
            orderCountList.add(orderCount);
            Integer validOrderCount = reportMapper.getValidOrderCount(beginTime, endTime);
            validOrderCountList.add(validOrderCount);
        }
        orderReportVO.setOrderCountList(StringUtils.join(orderCountList, ","));
        orderReportVO.setValidOrderCountList(StringUtils.join(validOrderCountList, ","));
        orderReportVO.setTotalOrderCount(orderCountList.stream().mapToInt(Integer::intValue).sum());
        orderReportVO.setValidOrderCount(validOrderCountList.stream().mapToInt(Integer::intValue).sum());
        orderReportVO.setOrderCompletionRate(orderReportVO.getValidOrderCount() * 1.0 / orderReportVO.getTotalOrderCount());
        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        // 执行查询
        List<Map<String, Object>> result = reportMapper.getTop10(begin, end);

        // 创建 VO 对象
        SalesTop10ReportVO vo = new SalesTop10ReportVO();

        // 使用 StringJoiner 构建字符串
        StringJoiner nameJoiner = new StringJoiner(",");
        StringJoiner numberJoiner = new StringJoiner(",");

        // 遍历结果集
        for (Map<String, Object> row : result) {
            // 获取菜品名称
            String name = (String) row.get("name");
            nameJoiner.add(name);

            // 获取销量，确保转换为字符串
            Object totalObj = row.get("total");
            String numberStr = totalObj == null ? "0" : totalObj.toString();
            numberJoiner.add(numberStr);
        }

        // 设置到 VO 对象
        vo.setNameList(nameJoiner.toString());
        vo.setNumberList(numberJoiner.toString());

        return vo;

    }

    public List<LocalDate> getDateList(LocalDate begin, LocalDate end){
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate temp = begin;
        while (!temp.equals(end)){
            dateList.add(temp);
            temp = temp.plusDays(1);
        }
        dateList.add(end);
        return dateList;
    }
}
