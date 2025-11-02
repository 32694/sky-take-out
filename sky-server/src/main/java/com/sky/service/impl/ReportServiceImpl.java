package com.sky.service.impl;

import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public void export(HttpServletResponse response) throws IOException {
        // 查询总数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDateTime beginTime =LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        BusinessDataVO businessDataVO =  workspaceService.getBusinessData(beginTime, endTime);
        // 创建工作簿
        //基于模版创建工作簿

//        this.getClass() - 获取当前类的Class对象
//        .getClassLoader() - 获取当前类的类加载器
//        .getResourceAsStream() - 类加载器的方法，用于从类路径加载资源为输入流
//        "template/运营数据报表模板.xlsx" - 资源文件的路径
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        XSSFWorkbook excel = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = excel.getSheet("Sheet1");
        // 设置时间
        sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);
        //概览数据设置
        sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
        sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
        sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
        sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
        sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());
        //明细数据设置
        for (int i = 0; i < 30; i++)
        {
            LocalDate date = begin.plusDays(i);
            BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
            sheet.getRow(7 + i).getCell(1).setCellValue(date.toString());
            sheet.getRow(7 + i).getCell(2).setCellValue(data.getTurnover());
            sheet.getRow(7 + i).getCell(3).setCellValue(data.getValidOrderCount());
            sheet.getRow(7 + i).getCell(4).setCellValue(data.getOrderCompletionRate());
            sheet.getRow(7 + i).getCell(5).setCellValue(data.getUnitPrice());
            sheet.getRow(7 + i).getCell(6).setCellValue(data.getNewUsers());

        }
        //写出数据
        ServletOutputStream out = response.getOutputStream();
        excel.write(out);
        out.close();
        excel.close();
    }
}
