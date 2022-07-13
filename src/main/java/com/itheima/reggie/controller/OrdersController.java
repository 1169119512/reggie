package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    @Resource
    private OrdersService ordersService;

    @Resource
    private UserService userService;

    @Resource
    private AddressBookService addressBookService;

    @Resource
    private OrdersDetailService ordersDetailService;

    @Resource
    private ShoppingCartService shoppingCartService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        ordersService.sumbit(orders);
        return R.success("订单支付完成");
    }


    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize){
//        Long userId = (long) session.getAttribute("user");
        Long userId = BaseContext.getCurrentId();
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,userId);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo,queryWrapper);
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        List<Orders> orders = pageInfo.getRecords();
        List<OrdersDto> ordersDtos = orders.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            User user = userService.getById(item.getUserId());
            AddressBook addressBook = addressBookService.getById(item.getAddressBookId());
            ordersDto.setUserName(user.getName());
            ordersDto.setPhone(user.getPhone());
            String address = (addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName()) +
                    (addressBook.getCityName() == null ? "" : addressBook.getCityName()) +
                    (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName()) +
                    (addressBook.getDetail() == null ? "" : addressBook.getDetail());
            ordersDto.setAddress(address);
            ordersDto.setConsignee(addressBook.getConsignee());
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> orderDetails = ordersDetailService.list(queryWrapper1);
            ordersDto.setOrderDetails(orderDetails);
            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtos);
        return R.success(ordersDtoPage);
    }

    @GetMapping("page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime){
        //LocalDateTime = 2022-07-11 00:00:00
        LocalDateTime begin =beginTime == null? null:
                LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime end =endTime == null?null:
                LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Page<Orders> pageInfo = new Page<>(page,pageSize);
//        Page<OrdersDto> ordersDtoPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(number != null,Orders::getNumber,number);
        queryWrapper.ge(beginTime != null,Orders::getCheckoutTime,beginTime);
        queryWrapper.le(endTime!= null,Orders::getCheckoutTime,endTime);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo,queryWrapper);
//        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");


//        List<Orders> orders = pageInfo.getRecords();
//        List<OrdersDto> ordersDtos = orders.stream().map((item) -> {
//            OrdersDto ordersDto = new OrdersDto();
//            BeanUtils.copyProperties(item, ordersDto);
//            User user = userService.getById(item.getUserId());
//            AddressBook addressBook = addressBookService.getById(item.getAddressBookId());
//            ordersDto.setUserName(user.getName());
//            ordersDto.setPhone(user.getPhone());
//            String address = (addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName()) +
//                    (addressBook.getCityName() == null ? "" : addressBook.getCityName()) +
//                    (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName()) +
//                    (addressBook.getDetail() == null ? "" : addressBook.getDetail());
//            ordersDto.setAddress(address);
//            ordersDto.setConsignee(addressBook.getConsignee());
//            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
//            queryWrapper1.eq(OrderDetail::getOrderId, item.getId());
//            List<OrderDetail> orderDetails = ordersDetailService.list(queryWrapper1);
//            ordersDto.setOrderDetails(orderDetails);
//            return ordersDto;
//        }).collect(Collectors.toList());
//
//        ordersDtoPage.setRecords(ordersDtos);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> statusTopost(@RequestBody Orders order){
        ordersService.updateById(order);
        return R.success("修改状态成功");
    }


@Transactional
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        log.info("再来一单：orders:{}",orders.toString());
        Long user = BaseContext.getCurrentId();
        orders = ordersService.getById(orders.getId());
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,orders.getId());
        List<OrderDetail> orderDetails = ordersDetailService.list(queryWrapper);
        List<ShoppingCart> shoppingCarts = orderDetails.stream().map((item) -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setName(item.getName());
            shoppingCart.setImage(item.getImage());
            shoppingCart.setUserId(user);
            shoppingCart.setDishId(item.getDishId());
            shoppingCart.setSetmealId(item.getDishId());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        //先清空购物车在添加数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(ShoppingCart::getUserId,user);
        shoppingCartService.remove(queryWrapper1);
        shoppingCartService.saveBatch(shoppingCarts);
        return R.success("再来一单！");
    }
}
