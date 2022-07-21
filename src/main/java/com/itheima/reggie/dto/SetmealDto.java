package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("套餐Dto")
public class SetmealDto extends Setmeal {

    @ApiModelProperty("套餐菜品list")
    private List<SetmealDish> setmealDishes;

    @ApiModelProperty("菜品名")
    private String categoryName;
}
