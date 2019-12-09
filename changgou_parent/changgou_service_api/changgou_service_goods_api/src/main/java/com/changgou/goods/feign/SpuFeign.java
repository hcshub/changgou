package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("spu")
public interface SpuFeign {
    /***
     * 根据ID查询Spu数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable Long id);
}
