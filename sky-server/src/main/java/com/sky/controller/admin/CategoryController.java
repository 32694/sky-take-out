package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "分类相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @ApiOperation("分类分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分页查询",categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("新增分类")
    @PostMapping()
    public Result add(@RequestBody CategoryDTO categoryDTO)
        {
            log.info("新增分类",categoryDTO);
            Category category=Category.builder().
                    name(categoryDTO.getName()).
                    sort(categoryDTO.getSort()).
                    type(categoryDTO.getType()).
                    build();
            categoryService.add(category);
            return Result.success();
        }

        @ApiOperation("删除分类")
        @DeleteMapping
        public Result delete(@RequestParam Long id)
        {
            log.info("删除分类",id);
            categoryService.delete(id);
            return Result.success();
        }
        @ApiOperation("修改分类")
        @PutMapping
        public Result update(@RequestBody CategoryDTO categoryDTO)
            {
            log.info("修改分类",categoryDTO);
            Category category=Category.builder().
                    id(categoryDTO.getId()).
                    name(categoryDTO.getName()).
                    sort(categoryDTO.getSort()).
                    type(categoryDTO.getType()).
                    build();
            categoryService.update(category);
            return Result.success();
        }
    @ApiOperation("禁用或启用状态")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status,@RequestParam Long id)
    {
        log.info("禁用或启用状态",status);
        categoryService.startOrStop(status,id);
        return Result.success();
    }
    @ApiOperation("查询分类")
    @GetMapping("/list")
    public Result<List<Category>> list(Long type)
    {
            log.info("查询分类",type);
           List<Category> list= categoryService.list(type);
            return Result.success(list);
    }
}
