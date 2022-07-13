package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Resource
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品到购物车，存进购物车是根据口味的来存，只能存一种口味
     *
     * 问题：下面做法问题：前端设置没有可以加其他口味的情况
     * 添加菜品到购物车：存进购物车是根据口味的来存，但是回显的时候是根据相同套餐/菜品数量来回显
     *
     *
     *
     *
     * @param shoppingCart
     * @param session
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart, HttpSession session){
//        Long user = (Long) session.getAttribute("user");
        Long user = BaseContext.getCurrentId();
        log.info("添加菜品/套餐到购物车：{}",shoppingCart.toString());
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getName,shoppingCart.getName());
        queryWrapper.eq(ShoppingCart::getUserId,user);
        queryWrapper.eq(shoppingCart.getDishId() != null,ShoppingCart::getDishId,shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId() != null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
//        queryWrapper.eq(ShoppingCart::getDishFlavor,shoppingCart.getDishFlavor());
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
        if(shoppingCart1 == null){
            shoppingCart.setUserId(user);
            //不能自动填充否则会报错，因为自动填充含有updateTime的改变，但这个entity没有updateTime字段会报错
            shoppingCart.setCreateTime(LocalDateTime.now());
            //要设置number为1，尽管数据库默认为1，但是你要把数据传到前端，因此需要把这个类设置好
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            return R.success(shoppingCart);
        }
        else {
            shoppingCart1.setNumber(shoppingCart1.getNumber()+1);
            shoppingCartService.updateById(shoppingCart1);
            return R.success(shoppingCart1);
        }
        //下面这种做法有问题，问题在方法描述上！
//        //------回显要把所有该菜品的数量标示-------------
//        LambdaQueryWrapper<ShoppingCart> queryWrapper1 = new LambdaQueryWrapper<>();
//        queryWrapper1.eq(ShoppingCart::getName,shoppingCart.getName());
//        queryWrapper1.eq(ShoppingCart::getUserId,user);
//        queryWrapper1.eq(shoppingCart.getDishId() != null,ShoppingCart::getDishId,shoppingCart.getDishId());
//        queryWrapper1.eq(shoppingCart.getSetmealId() != null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
//        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper1);
//        AtomicInteger count = new AtomicInteger(0);
//        shoppingCarts.stream().forEach((item->{
//            if(item.getNumber() != null){
//                //addAndGet == ++i getAndAdd == i++
//               count.addAndGet(item.getNumber().intValue());
//            }
//        }));
//        shoppingCart.setNumber(count.get());
//        return R.success(shoppingCart);

    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(HttpSession session){
//        Long user = (Long) session.getAttribute("user");
        Long user = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,user);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        return R.success(shoppingCarts);

    }


    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart,HttpSession session){
        Long user = BaseContext.getCurrentId();
//        Long user = (long) session.getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(shoppingCart.getDishId() != null,ShoppingCart::getDishId,shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId() != null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        queryWrapper.eq(ShoppingCart::getUserId,user);
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
        if(shoppingCart1 == null){
            return R.error("购物车没有此项");
        }
        else {
            if(shoppingCart1.getNumber() == 1){
                shoppingCartService.removeById(shoppingCart1.getId());
                shoppingCart1.setNumber(0);
            }
            else {
                shoppingCart1.setNumber(shoppingCart1.getNumber()-1);
                shoppingCartService.updateById(shoppingCart1);
            }
            return R.success(shoppingCart1);
        }

    }

    @Transactional
    @DeleteMapping("/clean")
    public R<String> clean(HttpSession session){
        Long user = BaseContext.getCurrentId();
//        Long user = (long) session.getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,user);
        shoppingCartService.remove(queryWrapper);
        return R.success("已清空购物车");
    }
}
