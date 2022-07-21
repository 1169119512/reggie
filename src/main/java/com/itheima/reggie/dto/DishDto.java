package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("菜品Dto")
public class DishDto extends Dish {

    @ApiModelProperty("菜品口味信息")
    private List<DishFlavor> flavors = new ArrayList<>();

    @ApiModelProperty("菜品分类归属")
    private String categoryName;

    @ApiModelProperty("份数")
    Integer copies;
}
