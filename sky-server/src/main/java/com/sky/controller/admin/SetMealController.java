package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetMealController {
    @Autowired
    private SetMealService setMealService;

    @ApiOperation("新增套餐")
    @PostMapping
    @CacheEvict(cacheNames = "setmealCache",key="#setmealDTO.categoryId")
    public Result add(@RequestBody SetmealDTO setmealDTO)
    {
        log.info("新增套餐",setmealDTO);
        setMealService.add(setmealDTO);
        return Result.success();
    }

    @ApiOperation("套餐分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO)
        {
        log.info("分页查询",setmealPageQueryDTO);
        PageResult pageResult = setMealService.pagequery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("修改套餐状态")
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status,@RequestParam Long id)
    {
        log.info("修改套餐状态",status);
        setMealService.startOrStop(status,id);
        return Result.success();
    }
    @ApiOperation("根据id查询套餐")
    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id)
        {
        log.info("根据id查询套餐");
        SetmealVO setmealVO = setMealService.getById(id);
        return Result.success(setmealVO);
    }
    @ApiOperation("修改套餐")
    @PutMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO)
        {
            log.info("修改套餐",setmealDTO);
            setMealService.update(setmealDTO);
            return Result.success();
        }
    @ApiOperation("批量删除套餐")
    @DeleteMapping()
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids)
        {
            log.info("批量删除套餐",ids);
            setMealService.delete(ids);
            return Result.success();
        }
}
