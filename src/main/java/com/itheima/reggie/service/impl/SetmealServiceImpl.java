package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {

    @Resource
    private SetmealService setmealService;

    @Resource
    private SetmealDishService setmealDishService;


    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto){
        setmealService.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().forEach((item->{
            item.setSetmealId(setmealDto.getId());
            setmealDishService.save(item);
        }));
    }

    @Transactional
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        //先修改setmeal表
        setmealService.updateById(setmealDto);
        //删除所有setmealDish的内容
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //添加新的setmealDish的内容
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().forEach((item)->{
            item.setSetmealId(setmealDto.getId());
        });
        setmealDishService.saveBatch(setmealDishes);


    }

    @Transactional
    @Override
    public void removeWithDish(long id){
        //移除setmealDish
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        setmealDishService.remove(queryWrapper);

        //移除setmeal
        setmealService.removeById(id);
    }
}
