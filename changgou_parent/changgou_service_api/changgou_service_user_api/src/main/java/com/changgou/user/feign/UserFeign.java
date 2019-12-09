package com.changgou.user.feign;

import com.changgou.user.pojo.Address;
import com.changgou.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user")
@RequestMapping("user")
public interface UserFeign {
    /***
     * 根据ID查询User数据
     */
    @GetMapping("load/{id}")
    Result<User> findById(@PathVariable String id);

    /***
     * 获取当前用户的收件人地址列表
     * @return
     */
    @GetMapping(value = "/user/list")
    public Result<List<Address>> findListByUser();

    /***
     * 增加用户积分
     * @param points:要添加的积分
     */
    @GetMapping(value = "/points/add")
    public Result addPoints(@RequestParam(value = "points") Integer points);
}