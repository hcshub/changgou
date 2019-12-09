package com.changgou.search.feign;

import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.search.feign
 * @date 2019-11-6
 */
@FeignClient(name = "search")
@RequestMapping(value = "search")
public interface SkuFeign {

    @GetMapping
    public Result<Map> search(@RequestParam(required = false) Map<String, String> searchMap);
}
