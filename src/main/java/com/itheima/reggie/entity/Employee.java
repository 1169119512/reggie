package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * 员工实体
 *
 */
@Data
@ApiModel("员工")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("电话号码")
    private String phone;

    @ApiModelProperty("性别")
    private String sex;

    @ApiModelProperty("身份证号")
    private String idNumber;//身份证号码

    @ApiModelProperty("账号状态")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;


    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建人")
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("修改人")
    private Long updateUser;

}
