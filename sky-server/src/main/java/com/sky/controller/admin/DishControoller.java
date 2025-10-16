package com.sky.controller.admin;


import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.mapper.DishFlavorMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishControoller {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result pagequery(DishPageQueryDTO dishPageQueryDTO)
    {
        log.info("分页查询菜品");
        PageResult pageResult = dishService.pagequery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("菜品增加")
    @PostMapping()
    public Result add(@RequestBody  DishDTO dishDTO)
    {
        log.info("菜品增加",dishDTO);

        Dish dish=Dish.builder().
                name(dishDTO.getName()).
                categoryId(dishDTO.getCategoryId()).
                price(dishDTO.getPrice()).
                image(dishDTO.getImage()).
                description(dishDTO.getDescription()).
                status(dishDTO.getStatus()).
                build();

        dishService.add(dish);

        List<DishFlavor> dishFlavorList=dishDTO.getFlavors();
        if(dishFlavorList!=null&&dishFlavorList.size()>0)
        {
            for(DishFlavor dishFlavor:dishFlavorList)
            {
                dishFlavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(dishFlavorList);
        }
        return Result.success();
    }

}

