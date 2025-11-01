package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "订单相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @ApiOperation("查询订单详情")
    @GetMapping("/details/{id}")
    public Result details(@PathVariable("id") Long id){
        log.info("查询订单详情：{}", id);
        return Result.success(orderService.details(id));
    }

    @ApiOperation("订单搜索")
    @GetMapping("/conditionSearch")
    public Result conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索：{}", ordersPageQueryDTO);
        return Result.success(orderService.pageQuery(ordersPageQueryDTO,false));
    }

    @ApiOperation("订单状态数量统计")
    @GetMapping("/statistics")
    public Result statistics(){
        log.info("订单状态数量统计");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    @ApiOperation("接单")
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接单：{}", ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }
    @ApiOperation("取消订单")
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO)
     {
        log.info("取消订单：{}", ordersCancelDTO);
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    @ApiOperation("拒单")
    @PutMapping("/rejection")
    public Result reject(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒单：{}", ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    @ApiOperation("派单")
    @PutMapping("delivery/{id}")
    public Result delivery(@PathVariable("id") Long id){
        log.info("派单：{}", id);
        orderService.delivery(id);
        return Result.success();
    }

    @ApiOperation("订单完成")
    @PutMapping("complete/{id}")
    public Result complete(@PathVariable("id") Long id){
        log.info("订单完成：{}", id);
        orderService.complete(id);
        return Result.success();
    }
}
