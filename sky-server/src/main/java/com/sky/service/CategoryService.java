package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;


public interface CategoryService {

    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    void add(Category category);

    void delete(Long id);

    void update(Category category);

    void startOrStop(Integer status, Long id);

    List<Category> list(Long type);
}
