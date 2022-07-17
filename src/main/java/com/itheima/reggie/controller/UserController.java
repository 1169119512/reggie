package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @PostMapping("sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //        获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
//               生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode4String(4).toString();
            log.info("code={}",code);
//              调用阿里云提供的短信服务API完成发送短信
//            SMSUtils.sendMessage("瑞吉外卖","",phone,code);
            //需要将生成的验证码保存到Session
//            session.setAttribute(phone,code);
            //改为用redis缓存，设置5分钟时限
            ValueOperations valueOperations = redisTemplate.opsForValue();
            valueOperations.set(phone,code,5, TimeUnit.MINUTES);
            return R.success("手机验证码发送成功");
        }
        return R.error("短信发送失败");


    }

    //带有验证码的登录
//    @PostMapping("login")
//    public R<User> login(@RequestBody Map map, HttpSession session){
//        String phone = null;
//        String code = null;
////                 获取手机号
//        if(map.containsKey("phone"))
//        phone = (String) map.get("phone");
////                 获取验证码
//        if(map.containsKey("code"))
//            code = (String) map.get("code");
//        if(phone == null || code == null){
//            return R.error("请填入手机号/验证码");
//        }
////                 从Session中获取保存的验证码
//        String sessionCode = (String) session.getAttribute(phone.toString());
                    //!!!!改为使用redis获取验证码
//    String  redisCode = (String) redisTemplate.opsForValue().get("code");
////                 进行验证码的比对(页面提交的验证码和Session中保存的验证码比对)
//        //                 如果能够比对成功，说明登录成功
//        if(sessionCode != null && redisCode.equals(code)){
//            //                 判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
//            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(User::getPhone,phone.toString());
//            User user = userService.getOne(queryWrapper);
//            if(user == null){
//                user = new User();
//                user.setPhone(phone.toString());
//                user.setStatus(1);
//                userService.save(user);
//            }
//            session.setAttribute("user",user);
//            return R.success(user);
//        }
//
                //用户登录成功，删除redis的验证码
//                        redisTemplate.delete("code");
//
//        return R.error("验证码/手机号有误，请重新输入");
//
//    }

    @PostMapping("login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        String phone = null;
//                 获取手机号
        if(map.containsKey("phone"))
            phone = (String) map.get("phone");
            // 判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone.toString());
            User user = userService.getOne(queryWrapper);
            if(user == null){
                user = new User();
                user.setPhone(phone.toString());
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
        return R.success(user);
    }

    @PostMapping("loginout")
    public R<String> loginout(HttpSession session){
        session.removeAttribute("user");
        return R.success("退出成功");
    }
}
