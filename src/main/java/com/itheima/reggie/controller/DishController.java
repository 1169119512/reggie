package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("dish")
@Api(tags = "菜品相关接口")
public class DishController{
    @Resource
 private DishService dishService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private DishFlavorService dishFlavorService;

    /**
     * 缓存两种方案：
     * 方案1；添加删除修改启售停售时删除所有缓存
     * 方案2：添加删除修改启售停售要删除对应菜品分类的缓存（修改要删除修改前和修改后的缓存）
     *
     */
    @Resource
    private RedisTemplate redisTemplate;


    @PostMapping
    @ApiOperation(value = "保存菜品接口")
    public R<String> save(@RequestBody DishDto dishDto){
       log.info(dishDto.toString());
        //如果keys=*,那么就是当菜品更新时就删除所有缓存
        //       Set Keys = redisTemplate.keys("dish_*");
        Set keys = redisTemplate.keys(dishDto.getCategoryId() + "_1" );
        redisTemplate.delete(keys);
        dishService.saveWithFlavor(dishDto);
       return R.success("新增菜品成功");

   }


   @GetMapping("/page")
   @ApiOperation(value = "菜品分类分页接口")
   @ApiImplicitParams({
           @ApiImplicitParam(name="page",value = "页码",required = true),
           @ApiImplicitParam(name="pageSize",value = "每页记录数",required = true),
           @ApiImplicitParam(name="name",value = "要搜索的菜品名字",required = false),
   })
   public R<Page> page(int page, int pageSize, String name){
        log.info("具体菜类信息查询");
        //构建分页构建器
        Page<Dish> pageInfo = new Page<Dish>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page,pageSize);
        //构建查询构建器
       LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
       //添加过滤条件
       queryWrapper.like((name != null),Dish::getName,name);

       //进行分页查询
       dishService.page(pageInfo,queryWrapper);

       //把Dish列表弄到DishDtos里，因为DishDtos还包含了需要显示的菜品分类
       List<Dish> records = pageInfo.getRecords();
       List<DishDto> dishDtos = null;

       //根据Dish列表的category_id查询category表的菜品分类，并添加到dishDto里，然后收集到DishDtos里
       dishDtos = records.stream().map((item)->{
           DishDto dishDto = new DishDto();
           BeanUtils.copyProperties(item,dishDto);
           Long categoryId = item.getCategoryId();
           Category category = categoryService.getById(categoryId);
           if(category != null){
               String categoryName = category.getName();
               dishDto.setCategoryName(categoryName);
           }
           return dishDto;
       }).collect(Collectors.toList());

       //把除了records的信息复制到dishDto里
       BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
       //设置Page的dishDtos
       dishDtoPage.setRecords(dishDtos);


       return R.success(dishDtoPage);
   }

   @GetMapping("/{id}")
   @ApiOperation(value = "展示菜品接口")
   public R<DishDto> getById(@PathVariable Long id){
        log.info("进行菜品回显");
       Dish dish = dishService.getById(id);
       DishDto dishDto = new DishDto();
       if(dish == null){
           return R.error("没有此菜品");
       }
       else {
           Long categoryId = dish.getCategoryId();
           Category category = categoryService.getById(categoryId);
           if(category.getName() != null){
               dishDto.setCategoryName(category.getName());
           }
           LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
           queryWrapper.eq(DishFlavor::getDishId,dish.getId());
           List<DishFlavor> list = dishFlavorService.list(queryWrapper);
           dishDto.setFlavors(list);
           BeanUtils.copyProperties(dish,dishDto);

           return R.success(dishDto);
       }
   }

    //修改菜品同时删除缓存,要删除修改前和修改后的菜品缓存
   @PutMapping
   @ApiOperation(value = "修改菜品分类接口")
   public R<String> update(@RequestBody DishDto dishDto){
        log.info("修改菜品");
        log.info(dishDto.toString());
       //如果keys=*,那么就是当菜品更新时就删除所有缓存
       Dish dish = dishService.getById(dishDto);
//       Set Keys = redisTemplate.keys("dish_*");
       Set beforeKeys = redisTemplate.keys(dish.getCategoryId() + "_1"  );
       Set afterKeys = redisTemplate.keys(dishDto.getCategoryId() + "_1" );
       redisTemplate.delete(beforeKeys);
       redisTemplate.delete(afterKeys);

       dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
   }

   //使用redis缓存菜品数据，status不能为空，因此用Dish类封装数据,不需要使用requestbody,因为数值在url里
   @GetMapping("/list")
   @ApiOperation(value = "展示菜品list接口")
   public R<List<DishDto>> getById(Dish dish){
        log.info("根据菜品分类获取所有的菜品");
       ValueOperations valueOperations = redisTemplate.opsForValue();
       String key = null;
       //当没有status时就是在添加套餐时获取数据，不需要写入redis里
       if(dish.getStatus() != null)
       key = dish.getCategoryId()+"_"+dish.getStatus();
       else
           key = dish.getCategoryId()+"_";
       List<DishDto> dishDtos = null;
       if(valueOperations.get(key)!=null){
           dishDtos = (List<DishDto>) valueOperations.get(key);
       }
       else {
           LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
           queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
           queryWrapper.orderByDesc(Dish::getUpdateTime);
           queryWrapper.eq((Integer)dish.getStatus()!= null,Dish::getStatus,dish.getStatus());
           List<Dish> dishes = dishService.list(queryWrapper);
           dishDtos = dishes.stream().map((item)->{
               DishDto dishDto = new DishDto();
               BeanUtils.copyProperties(item,dishDto);
               return dishDto;
           }).collect(Collectors.toList());


           dishDtos.stream().forEach((item)->{
               LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
               queryWrapper1.eq(DishFlavor::getDishId,item.getId());
               List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper1);
               item.setFlavors(dishFlavors);
               Category category = categoryService.getById(item.getCategoryId());
               item.setCategoryName(category.getName());
           });
           valueOperations.set(dish.getCategoryId()+"_"+dish.getStatus(),dishDtos,60, TimeUnit.MINUTES);
       }

       return R.success(dishDtos);
   }




   @Transactional
    @PostMapping("/status/{status}")
   @ApiOperation(value = "修改菜品状态接口")
   public R<String> updateSetmealTobeiginSelling(@PathVariable("status") int status ,@RequestParam List<Long> ids){
        log.info("修改菜品状态为启售或停售");
        ids.stream().forEach((id)->{
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            //如果keys=*,那么就是当菜品更新时就删除所有缓存
            //       Set Keys = redisTemplate.keys("dish_*");

            Set keys = redisTemplate.keys(dish.getCategoryId()+"_1");
            redisTemplate.delete(keys);
            dishService.updateById(dish);
        });
        return R.success("成功修改套餐状态");
    }


    //删除菜品要删除对应菜品分类的缓存
    @Transactional
    @DeleteMapping
    @ApiOperation(value = "删除菜品分类接口")
    public R<String> remove(@RequestParam List<Long> ids){
        log.info("删除选中菜品");
        ids.stream().forEach((id)->{
            Dish dish = dishService.getById(id);
            //如果keys=*,那么就是当菜品更新时就删除所有缓存//
            // Set Keys = redisTemplate.keys("dish_*");
            Set keys = redisTemplate.keys(dish.getCategoryId() + "_1" );
            redisTemplate.delete(keys);
            dishService.remove(id);
        });
        return R.success("删除成功");
    }



}
