package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public  static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request =(HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
//        1、获取本次请求的URI
        String requestURI = request.getRequestURI();

        log.info("拦截到请求{}",requestURI);
        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",        //移动端发送短信
                "/user/login",           //移动端登录
                "favicon.ico"
        };


//        2、判断本次请求是否需要处理

        if(check(urls,requestURI)){
            log.info("本次请求不需要被处理");
            filterChain.doFilter(request,response);
            return;
        }
        else {
            //        4-1 网页端登录验证
            Object employee =request.getSession().getAttribute("employee");
            if(employee != null){
                log.info("用户已登录，用户id{}",(long)employee);
                BaseContext.setCurrentId((Long) employee);
                filterChain.doFilter(request,response);
                return;
            }

        //4-2 移动端登录验证
            Object user =request.getSession().getAttribute("user");
            if(user != null){
                log.info("用户已登录，用户id{}",(long)user);
                BaseContext.setCurrentId((Long) user);
                filterChain.doFilter(request,response);
                return;
            }

                log.info("用户未登录，本次请求需要被处理：返回登录界面");
                response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
                return;
        }
//        3、如果不需要处理，则直接放行
//        4、判断登录状态，如果已登录，则直接放行
//        5、如果未登录则返回未登录结果
    }

    public boolean check(String[] urls,String requestURL){
        for (int i = 0; i < urls.length; i++) {
            if(PATH_MATCHER.match(urls[i],requestURL)){
                return true;
            }
        }
        return false;
    }

}
