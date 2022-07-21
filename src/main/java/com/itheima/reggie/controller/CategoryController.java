package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
@Api(tags = "菜品分类相关接口")
public class CategoryController{
    @Resource
    private CategoryService categoryService;

    //添加菜品分类
    @PostMapping
    @ApiOperation(value = "添加菜品分类接口")
    public R<String> save(@RequestBody Category category){
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    //分页查询
    @GetMapping("/page")
    @ApiOperation(value = "菜品分类分页接口")
    @ApiImplicitParams ({
        @ApiImplicitParam(name="page",value = "页码",required = false),
        @ApiImplicitParam(name="pageSize",value = "每页的记录数",required = false),
    })
    public R<Page> page(int page, int pageSize){
        log.info("page={}，pagesize={}",page,pageSize);
        //构建分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    @DeleteMapping
    @ApiOperation(value = "删除菜品分类接口")
    public R<String> deleteById(long id){
        categoryService.remove(id);
        return R.success("分类信息删除成功");
    }

    @PutMapping
    @ApiOperation(value = "修改菜品分类接口")
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息category：{}",category);
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    @GetMapping("/list")
    @ApiOperation(value = "展示菜品分类list接口")
    public R<List<Category>> list(Category category){
        log.info("把菜品列表展示出来");
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

}
