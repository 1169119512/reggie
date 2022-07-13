package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

import javax.servlet.http.HttpSession;

public interface OrdersService extends IService<Orders> {

    public void sumbit(Orders orders);
}
