package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户订单接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private WebSocketServer webSocketServer;
    @ApiOperation("用户下单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);


    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    //订单支付这个接口由于没有商户号，没法正常使用
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);




        //fy修改模拟订单支付成功
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        //通过webSocket发送信息给客户端
        Map map = new HashMap();
        // 通过订单号查询订单ID
        String outTradeNo = ordersPaymentDTO.getOrderNumber();
        log.info("开始查询订单ID，订单号：{}", outTradeNo);
        Long orderId = orderService.getIdByOutTradeNo(outTradeNo);
        log.info("查询到的订单ID：{}", orderId);
        //1表示新订单
        map.put("type", 1);
        map.put("orderId", orderId);
        map.put("content", "订单号：" + outTradeNo);
        String json = JSON.toJSONString(map);
        log.info("准备发送WebSocket消息：{}", json);
        webSocketServer.sendToAllClient(json);
        log.info("WebSocket消息发送成功");






        return Result.success(orderPaymentVO);
    }
    @GetMapping("details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id){
        log.info("查询订单详情，订单id为：{}", id);
        OrderVO orderVO = orderService.details(id);
        log.info("查询结果：{}", orderVO);
        return Result.success(orderVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    public Result historyOrders( @RequestParam Integer page,
                                 @RequestParam Integer pageSize,
                                 @RequestParam(required = false) Integer status){
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setPage(page);
        ordersPageQueryDTO.setPageSize(pageSize);
        ordersPageQueryDTO.setStatus(status);
        log.info("查询历史订单：{}", ordersPageQueryDTO);
        return Result.success(orderService.pageQuery(ordersPageQueryDTO,true));
    }
    @PostMapping("repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable("id") Long id){
        log.info("再来一单，订单id为：{}", id);
        orderService.repetition(id);
        return Result.success();
    }



    // 取消订单是修改订单状态，而不是删除订单！！！
    @PutMapping("cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable("id") Long id){
        log.info("取消订单，订单id为：{}", id);
        OrdersCancelDTO ordersCancelDTO = new OrdersCancelDTO();
        ordersCancelDTO.setId(id);
        ordersCancelDTO.setCancelReason("用户取消");
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    //催单，但是不知道自己做的对不对
    @GetMapping("reminder/{id}")
    @ApiOperation("催单")
    public Result reminder(@PathVariable("id") Long id){
        log.info("催单，订单id为：{}", id);
        orderService.reminder(id);
        return Result.success();
    }



}
