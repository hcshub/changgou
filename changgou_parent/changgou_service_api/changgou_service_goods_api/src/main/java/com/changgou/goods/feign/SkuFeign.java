package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.goods.feign
 * @date 2019-11-3
 */
//开启feign
@FeignClient(name = "goods")
@RequestMapping("sku")
public interface SkuFeign {
    @GetMapping("/status/{status}")
    public Result<List<Sku>> findByStatus(@PathVariable String status);

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Sku> findById(@PathVariable Long id);

    @PostMapping(value = "/decr/count/{username}")
    public Result decrCount(@PathVariable String username);
}
