package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //向套餐表插入1条数据
        setmealMapper.insert(setmeal);

        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && setmealDishes.size() > 0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            //向套餐菜品关系表中插入n条数据
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 套餐批量删除
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                //起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除套餐数据
        //根据套餐id集合批量删除套餐数据
        //sql: delete from setmeal where id in (?,?,?)
        setmealMapper.deleteByIds(ids);

        //删除套餐id批量删除套餐菜品关联表数据
        //sql: delete from setmeal_dish where setmeal_id in (?,?,?)
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 根据id查询套餐和关联的菜品数据
     * @param id
     * @return
     */
    public SetmealVO getByIdWithDish(Long id) {
        //根据id查询套餐数据
        Setmeal setmeal = setmealMapper.getById(id);
        //根据套餐id查询菜品数据
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        //将查询到数据封装到VO
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 根据id修改套餐基本信息和对应的菜品信息
     * @param setmealDTO
     */
    @Transactional
    public void updateWithDish(SetmealDTO setmealDTO){
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //修改套餐表基本信息
        setmealMapper.update(setmeal);

        //删除原有的菜品数据
        setmealDishMapper.deleteBySetmealId(setmeal.getId());

        //重新插入菜品数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && setmealDishes.size() > 0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            //重新向套餐菜品关系表中插入n条数据
            setmealDishMapper.insertBatch(setmealDishes);
        }


    }
}
