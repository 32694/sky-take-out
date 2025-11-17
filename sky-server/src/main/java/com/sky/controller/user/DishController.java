package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired private RedisTemplate redisTemplate;
    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        String key="dish_"+categoryId;

        //查询redis里面是否存在数据
        List<DishVO> list= (List<DishVO>) redisTemplate.opsForValue().get(key);
        //存在
        if(list!=null&&list.size()>0)
        {
            return Result.success(list);
        }
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
        //不存在查询数据库
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key,list);
        return Result.success(list);
    }




    @GetMapping("/hot")
    @ApiOperation("大众推荐")
    public Result<List<DishVO>> hot() {
        log.info("获取大众推荐菜品");

        // 使用缓存键
        String cacheKey = "recommend:popular:by_sales";

        // 先尝试从缓存获取
        List<DishVO> cachedRecommendations = (List<DishVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedRecommendations != null && !cachedRecommendations.isEmpty()) {
            log.info("大众推荐缓存命中");
            return Result.success(cachedRecommendations);
        }

        // 缓存未命中，实时计算
        List<DishVO> recommendations = dishService.getPopularBySales(10); // 返回前10个

        // 缓存结果，5分钟过期
        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    recommendations,
                    5,
                    TimeUnit.MINUTES
            );
            log.info("大众推荐结果缓存成功");
        } catch (Exception e) {
            log.error("缓存大众推荐结果失败", e);
        }

        log.info("获取大众推荐菜品成功，共{}个", recommendations.size());
        return Result.success(recommendations);
    }



    //猜你喜欢
    @GetMapping("/guess")
    @ApiOperation("猜你喜欢")
    public Result guess() {
        return Result.success();
    }

}
