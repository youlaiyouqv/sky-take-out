package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface SetmealService {

    /**
     * 新增套餐，同时需要保存套餐和菜品的关系
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);


    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 套餐批量删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
