package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Resource
    private DishFlavorService dishFlavorService;

    @Resource
    private SetmealDishService setmealDishService;


    /**
     * 新增菜品同时保存对应的口味数据
     *
     *
     * @param dishDto
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        //保存之后会生成id
        Long dishId = dishDto.getId();

        //将id附给flavor的每一项
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item->{
            item.setDishId(dishId);
            return item;
        })).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);


        }

    @Transactional
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //修改菜品信息
        this.updateById(dishDto);

        //修改菜品口味信息flavors
        List<DishFlavor> flavors = dishDto.getFlavors();
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
            dishFlavorService.remove(queryWrapper);


        if(flavors != null){
            //保存所有flavor
            flavors.stream().forEach((item)->{
                item.setDishId(dishDto.getId());
                dishFlavorService.save(item);
            });
            //还需要给每个flavor附上dishId值
//        dishFlavorService.saveBatch(flavors);
        }


    }

    @Transactional
    @Override
public void remove(long id){
        //先判断菜品是否关联套餐
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getDishId,id);
        int count = setmealDishService.count(queryWrapper);
        //关联套餐则不能删除
        if(count > 0){
            throw new CustomException("当前菜品包含已关联套餐，请在删除与此菜品关联的套餐后重试！");
        }
        else {
            this.removeById(id);
        }
    }


}

