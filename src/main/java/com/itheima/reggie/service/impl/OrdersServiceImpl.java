package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Transactional
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Resource
    private UserService userService;

    @Resource
    private AddressBookService addressBookService;

    @Resource
    private ShoppingCartService shoppingCartService;

    @Resource
    private OrdersService ordersService;

    @Resource
    private OrdersDetailService ordersDetailService;

    @Override
    public void sumbit(Orders orders) {
        Long userId = BaseContext.getCurrentId();
//        Long userId = (Long) session.getAttribute("user");
        User user = userService.getById(userId);

        //雪花算法生成订单号
        long orderId = IdWorker.getId();
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        AtomicInteger count = new AtomicInteger(0);
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            if (item.getNumber() != null)
                count.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(item.getName());
            orderDetail.setOrderId(orderId);
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setAmount(item.getAmount());
            orderDetail.setImage(item.getImage());
            return orderDetail;
        }).collect(Collectors.toList());
        String address = (addressBook.getProvinceName() == null? "":addressBook.getProvinceName())+
                (addressBook.getCityName() == null? "":addressBook.getCityName())+
               (addressBook.getDistrictName() == null? "":addressBook.getDistrictName())+
                (addressBook.getDetail() == null? "":addressBook.getDetail());

        orders.setNumber(new String(String.valueOf(orderId)));
        //设置为已付款待派送
        orders.setStatus(2);
        orders.setId(orderId);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAmount(new BigDecimal(count.get()));
        orders.setUserName(user.getName());
        orders.setPhone(user.getPhone());
        orders.setAddress(address);
        orders.setConsignee(addressBook.getConsignee());

        if(orders.getAmount().compareTo(new BigDecimal(0)) == 0){
            throw new CustomException("金额为0，请返回购物车添加商品");
        }

        ordersService.save(orders);
        ordersDetailService.saveBatch(orderDetails);

        shoppingCartService.remove(queryWrapper);

    }
}
