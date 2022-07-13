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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("dish")
public class DishController{
    @Resource
 private DishService dishService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private DishFlavorService dishFlavorService;



    @PostMapping
   public R<String> save(@RequestBody DishDto dishDto){
       log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
       return R.success("新增菜品成功");

   }


   @GetMapping("/page")
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

   @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info("修改菜品");
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
   }

   @GetMapping("/list")
    public R<List<DishDto>> getById(long categoryId, int status){
        log.info("根据菜品分类获取所有的菜品");
       LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
       queryWrapper.eq(Dish::getCategoryId,categoryId);
       queryWrapper.eq(Dish::getStatus,1);
       queryWrapper.orderByDesc(Dish::getUpdateTime);
       queryWrapper.eq((Integer)status!= null,Dish::getStatus,status);
       List<Dish> dishes = dishService.list(queryWrapper);
        List<DishDto> dishDtos = dishes.stream().map((item)->{
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
       return R.success(dishDtos);
   }




   @Transactional
    @PostMapping("/status/{status}")
    public R<String> updateSetmealTobeiginSelling(@PathVariable("status") int status ,@RequestParam List<Long> ids){
        log.info("修改菜品状态为启售或停售");
        ids.stream().forEach((id)->{
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        });

        return R.success("成功修改套餐状态");
    }


    @Transactional
    @DeleteMapping
    public R<String> remove(@RequestParam List<Long> ids){
        log.info("删除选中菜品");
        ids.stream().forEach((id)->{

            dishService.remove(id);
        });
        return R.success("删除成功");
    }


}
