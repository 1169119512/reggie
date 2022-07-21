package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Resource
    private SetmealService setmealService;

    @Resource
    private SetmealDishService setmealDishService;
    @Resource
    private CategoryService categoryService;

    @Resource
    private DishService dishService;

    @GetMapping("/page")
    @ApiOperation(value = "套餐分页接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value = "页码",required = true),
            @ApiImplicitParam(name="pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name="name",value = "套餐包含的字",required = false),
    })
    public R<Page> page(int page, int pageSize, String name){
        //把返回对象由Setmeal->SetmealDto
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page,pageSize);
        //查询Setmeal表获取数据
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq((name!= null),Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);
        //把除了records的数据由Setmeal复制到SetmealDto
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        List<Setmeal> setmeals = pageInfo.getRecords();
        //把Setmeal的records数据弄到setmealDtos里
        List<SetmealDto> setmealDtos=setmeals.stream().map((item)->{
            //！！！！！！！！！注意：收集起来的setmealDto对象并不是同一个，不像是list.add(setmealDto)然后setmealDto = new SetmealDto();后list添加的会指向new的新指针
            SetmealDto setmealDto = new SetmealDto();
            //复制setmeals的每一项给SetmealDto
            BeanUtils.copyProperties(item,setmealDto);
            //添加categoryName到setmealDto上
        Category category = categoryService.getById(item.getCategoryId());
        setmealDto.setCategoryName(category.getName());
        //返回setmealDto把它收集起来
        return setmealDto;
        }).collect(Collectors.toList());
        //设置list数据
        setmealDtoPage.setRecords(setmealDtos);
        return R.success(setmealDtoPage);
    }

    //删除所有套餐缓存
    @CacheEvict(value = "setmealCache",allEntries = true)
    @Transactional
    @PostMapping
    @ApiOperation(value = "新增套餐接口")
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐添加:{}",setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("成功添加套餐");
    }

    @Transactional
    @GetMapping("/{id}")
    @ApiOperation(value = "根据id显示套餐接口")
    public R<SetmealDto> getById(@PathVariable long id){
        log.info("套餐回显");
        Setmeal setmeal = setmealService.getById(id);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        SetmealDto setmealDto = new SetmealDto();
        setmealDto.setSetmealDishes(setmealDishes);
        BeanUtils.copyProperties(setmeal,setmealDto);
        Category category = categoryService.getById(setmeal.getCategoryId());
        setmealDto.setCategoryName(category.getName());
        return R.success(setmealDto);
    }

    //删除所有套餐缓存
    @CacheEvict(value = "setmealCache",allEntries = true)
    @PutMapping
    @ApiOperation(value = "修改套餐接口")
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("修改套餐");
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }

    //删除所有套餐缓存
    @CacheEvict(value = "setmealCache",allEntries = true)
    @Transactional
    @PostMapping("/status/{status}")
    @ApiOperation(value = "修改套餐状态接口")
    public R<String> updateSetmealToStopSelling(@PathVariable int status, @RequestParam List<Long> ids){
        log.info("修改套餐状态为启售或停售");
        ids.stream().forEach((id)->{
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        });

        return R.success("成功修改套餐状态");
    }

    //删除所有套餐缓存
    @CacheEvict(value = "setmealCache",allEntries = true)
    @Transactional
    @DeleteMapping
    @ApiOperation(value = "移除对应id套餐接口")
    @ApiImplicitParam(name="ids",value = "所有要删除的套餐主键",required = true)
    public R<String> removeById(@RequestParam List<Long> ids){
        log.info("删除套餐，套餐id为{}",ids);
        ids.stream().forEach((id)->{
            setmealService.removeWithDish(id);
        });
        return R.success("删除套餐成功");
    }

//当status为空时，这个请求就会报错不能进来这个方法内:会报500错误，因此使用实体类封装更好
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId+'_'+#setmeal.status")
    @GetMapping("/list")
    @ApiOperation(value = "展示套餐list接口")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq((Integer)setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        List<Setmeal> setmeals = setmealService.list(queryWrapper);
        return R.success(setmeals);
    }


    @GetMapping("/dish/{id}")
    @ApiOperation(value = "根据id展示套餐下所有菜品接口")
    public R<List<Dish>> showDish(@PathVariable long id){
//        SetmealDto setmealDto = new SetmealDto();
//        Setmeal setmeal = setmealService.getById(id);
//        Category category = categoryService.getById(setmeal.getCategoryId());
//        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
//        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
//        BeanUtils.copyProperties(setmeal,setmealDto);
//        setmealDto.setCategoryName(category.getName());
//        setmealDto.setSetmealDishes(setmealDishes);
//        return R.success(setmealDto);

        Setmeal setmeal = setmealService.getById(id);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        List<Dish> dishes = setmealDishes.stream().map((item) -> {
            Dish dish = dishService.getById(item.getDishId());
            return dish;
        }).collect(Collectors.toList());
        return R.success(dishes);

    }
}
