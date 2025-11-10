package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController("UserRecommendController")
@RequestMapping("/user/recommend")
@Api(tags = "个性化推荐接口")
@Slf4j
public class RecommendController {
    @GetMapping("/hot")
    @ApiOperation("大众推荐")
    public Result hot() {
        log.info("获取推荐菜品");
        //结合AI算法
        List<DishVO> list=new ArrayList<>();
        return Result.success(list);
    }
    @GetMapping("/guess")
    @ApiOperation("猜你喜欢")
    public Result guess() {
        Long userId= BaseContext.getCurrentId();
        log.info("猜你喜欢,用户id为 {}", userId);
        List<DishVO> list=new ArrayList<>();
        //结合AI算法
        return Result.success(list);
    }
}
