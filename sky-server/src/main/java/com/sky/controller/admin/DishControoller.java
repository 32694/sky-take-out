package com.sky.controller.admin;



import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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

    @ApiOperation("禁用或启用状态")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status,@RequestParam Long id)
    {
        log.info("禁用或启用状态",status);
        dishService.startOrStop(status,id);
        return Result.success();
    }

    @ApiOperation("修改菜品")
    @PutMapping
    @Transactional
    public Result update(@RequestBody DishDTO dishDTO)
    {
        log.info("修改菜品",dishDTO);
        Dish dish=Dish.builder().
                id(dishDTO.getId()).
                name(dishDTO.getName()).
                categoryId(dishDTO.getCategoryId()).
                price(dishDTO.getPrice()).
                image(dishDTO.getImage()).
                description(dishDTO.getDescription()).
                status(dishDTO.getStatus()).
                build();
        dishService.update(dish);
        List<DishFlavor> dishFlavorList=dishDTO.getFlavors();
        if(dishFlavorList!=null&&dishFlavorList.size()>0)
        {
            dishFlavorMapper.deleteByDishId(dish.getId());
            for(DishFlavor dishFlavor:dishFlavorList)
            {
                dishFlavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(dishDTO.getFlavors());
        }
        return Result.success();
    }
    @ApiOperation("根据ID查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id)
    {
        log.info("根据ID查询菜品");
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }
    @ApiOperation("根据分类ID查询菜品")
    @GetMapping("/list")
    public Result<List<DishVO>> list(@RequestParam Long categoryId)
    {
        log.info("根据分类ID查询菜品");
        List<DishVO> list = dishService.list(categoryId);
        list.forEach(dishVO->dishVO.setFlavors(dishFlavorMapper.list(dishVO.getId())));
        return Result.success(list);
    }
    @ApiOperation("批量删除")
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids)
    {
        log.info("批量删除",ids);
        dishService.deletebatch(ids);
        return Result.success();
    }
}

