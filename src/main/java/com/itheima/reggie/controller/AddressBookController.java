package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
@Api(tags = "地址相关接口")
public class AddressBookController {
    @Resource
    private AddressBookService addressBookService;

    @GetMapping("list")
    @ApiOperation(value = "展示地址list接口")
    public R<List<AddressBook>> list(){
        log.info("展示地址信息");
        Long user = BaseContext.getCurrentId();
//        Long user = (long) session.getAttribute("user");
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,user);
        List<AddressBook> addressBooks = addressBookService.list(queryWrapper);
        return R.success(addressBooks);

    }

    @PostMapping
    @ApiOperation(value = "新增地址接口")
    public R<String> save(@RequestBody AddressBook addressBook){
        log.info("保存地址");
        Long user = BaseContext.getCurrentId();
//        Long user = (long) session.getAttribute("user");
        addressBook.setUserId(user);
        addressBookService.save(addressBook);
        return R.success("保存地址成功");

    }

    @Transactional
    @PutMapping("default")
    @ApiOperation(value = "设置默认地址接口")
    public R<String> defaultAddress(@RequestBody AddressBook addressBook){
        log.info("设置默认地址");
        Long user = BaseContext.getCurrentId();
//        Long user = (long) session.getAttribute("user");
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,user);
        queryWrapper.eq(AddressBook::getIsDefault,1);
        List<AddressBook> addressBooks = addressBookService.list(queryWrapper);
        addressBooks.stream().forEach((item)->{
            item.setIsDefault(0);
            addressBookService.updateById(item);
        });
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success("设置成功");
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询地址接口")
    public R<AddressBook> getById(@PathVariable long id){
        log.info("根据id获取地址信息");
        AddressBook addressBook = addressBookService.getById(id);
        return R.success(addressBook);
    }

    @PutMapping
    @ApiOperation(value = "更新地址接口")
    public R<String> update(@RequestBody AddressBook addressBook){
        log.info("修改地址");
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }

    @DeleteMapping
    @ApiOperation(value = "删除地址接口")
    @ApiImplicitParam(name = "ids", value = "所有要删除id", required = true)
    public R<String> deleteById(long ids){
        log.info("删除地址");
        addressBookService.removeById(ids);
        return R.success("地址删除成功");
    }

    @GetMapping("default")
    @ApiOperation(value = "显示默认地址接口")
    public R<AddressBook> defaultAddress(){
        log.info("显示默认地址");
        Long user = BaseContext.getCurrentId();
//        Long user = (long) session.getAttribute("user");
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getIsDefault,1);
        queryWrapper.eq(AddressBook::getUserId,user);
        AddressBook addressBooks = addressBookService.getOne(queryWrapper);
        LambdaQueryWrapper<AddressBook> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(AddressBook::getUserId,user);
        queryWrapper1.eq(AddressBook::getIsDefault, 0);
        queryWrapper.orderByDesc(AddressBook::getCreateTime);
        List<AddressBook> noDefaultAddressBooks = addressBookService.list(queryWrapper1);
        if(addressBooks != null){
            return R.success(addressBooks);
        }
        else {
            if(noDefaultAddressBooks.size() > 0) {
                return R.success(noDefaultAddressBooks.get(0));
            } else {
                return R.success(new AddressBook());
            }
        }
    }
}
