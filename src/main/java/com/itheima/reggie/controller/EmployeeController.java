package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestController
@RequestMapping("/employee")
@Api(tags = "员工相关接口")
public class EmployeeController {
    @Resource
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录接口")
    public R<Employee> login(HttpServletRequest request,@RequestBody Employee employee){
//        1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
//        2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

//        3、如果没有查询到则返回登录失败结果
        if(emp == null){
            return R.error("登录失败");
        }
//        4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }
//        5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

//        6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    @ApiOperation(value = "员工退出登录接口")
    public R<String> logout(HttpServletRequest request){
//        1、清理Session中的用户id
        request.getSession().removeAttribute("employee");
//        2、返回结果
        return R.success("用户已退出");
    }

    /**
     * 新增员工
     *
     *
     * @param employee
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增员工接口")
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee);
//        1、页面发送ajax请求，将新增员工页面中输入的数据以json的形式提交到服务端
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        2、服务端Controller接收页面提交的数据并调用Service将数据进行保存
//        Long empId = (Long) request.getSession().getAttribute("employee");
        Long empId = BaseContext.getCurrentId();
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setCreateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
//        3、Service调用Mapper操作数据库，保存数据
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    @GetMapping("/page")
    @ApiOperation(value = "员工信息分页接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value = "页码",required = true),
            @ApiImplicitParam(name="pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name="name",value = "要搜索的员工名字",required = false),
    })
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={}，pagesize={},name={}",page,pageSize,name);
        //构建分页构造器
        Page<Employee> pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getCreateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    //修改员工
    @PutMapping
    @ApiOperation(value = "修改员工接口")
    public R<String> update(@RequestBody Employee employee){
        //修改员工id的json数据精度丢失：long->String
        log.info(employee.toString());
//        Long emp = BaseContext.getCurrentId();
//        Long emp = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(emp);
//        employee.setUpdateTime(LocalDateTime.now());
        employeeService.updateById(employee);
        return R.success("员工修改成功");
    }


    /**
     * 根据id查询员工信息
     *
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取员工信息接口")
    public R<Employee> getbyId(@PathVariable Long id){
        log.info("根据员工id查找");
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("该用户找不到");



    }
}
