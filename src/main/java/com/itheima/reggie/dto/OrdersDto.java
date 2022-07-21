package com.itheima.reggie.dto;

import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("订单Dto")
public class OrdersDto extends Orders {

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("收货人")
    private String consignee;

    @ApiModelProperty("详情订单list")
    private List<OrderDetail> orderDetails;
	
}
