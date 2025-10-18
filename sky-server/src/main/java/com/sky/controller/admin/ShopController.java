package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("AdminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {

    public static final String SHOP_STATUS = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;
    @PutMapping("/{status}")
    @ApiOperation("起售/禁售店铺")
    public Result startOrStopShop(@PathVariable Integer status) {
        log.info("设置店铺状态（1为营业，否则为打烊）：{}", status);
        redisTemplate.opsForValue().set(SHOP_STATUS, status);
        return Result.success();
    }


    @GetMapping("/status")
    @ApiOperation("查询店铺状态")
    public Result<Integer> getShopStatus() {
        log.info("查询店铺状态");
        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);
        return Result.success(shopStatus);
    }
}
