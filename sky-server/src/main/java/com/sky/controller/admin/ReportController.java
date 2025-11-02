package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "图表相关接口")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @ApiOperation("统计营业额数据")
    @GetMapping("/turnoverStatistics")
    public Result turnOverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("统计营业额数据：{}到{}", begin, end);
        TurnoverReportVO turnoverReportVO = reportService.turnoverStatistics(begin, end);
        return Result.success(turnoverReportVO);
    }
    @ApiOperation("统计用户数据")
    @GetMapping("/userStatistics")
    public Result userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("统计用户数据：{}到{}", begin, end);
        return Result.success(reportService.userStatistics(begin, end));
    }
    @ApiOperation("统计订单数据")
    @GetMapping("/ordersStatistics")
    public Result ordersStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("统计订单数据：{}到{}", begin, end);
        return Result.success(reportService.ordersStatistics(begin, end));
    }
    @ApiOperation("统计销量top10")
    @GetMapping("/top10")
    public Result top10(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("统计销量top10：{}到{}", begin, end);
        return Result.success(reportService.top10(begin, end));
    }
}
