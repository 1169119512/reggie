package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类
 */
@Data
@ApiModel("菜品分类")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;


    //类型 1 菜品分类 2 套餐分类
    @ApiModelProperty("菜品类型 1 菜品分类 2 套餐分类")
    private Integer type;


    //分类名称
    @ApiModelProperty("分类名称")
    private String name;


    //顺序
    @ApiModelProperty("顺序")
    private Integer sort;


    //创建时间
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;


    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;


    //创建人
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建人")
    private Long createUser;


    //修改人
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("修改人")
    private Long updateUser;


    //是否删除
//    @TableLogic
//    private Integer isDeleted;

}
