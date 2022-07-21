package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
菜品口味
 */
@Data
@ApiModel("菜品口味")
public class DishFlavor implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;


    //菜品id
    @ApiModelProperty("菜品id")
    private Long dishId;


    //口味名称
    @ApiModelProperty("口味名称")
    private String name;


    //口味数据list
    @ApiModelProperty("口味数据list")
    private String value;


    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;


    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建用户")
    private Long createUser;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("修改用户")
    private Long updateUser;


    //是否删除
    @ApiModelProperty("是否删除")
    private Integer isDeleted;

}
