package com.changgou.search.controller;

import com.changgou.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 导入数据
     * @return
     */
    @GetMapping("import")
    public Result search(){
        skuService.importSku();
        return new Result(true, StatusCode.OK,"导入数据到索引库中成功！");
    }

    @GetMapping
    public Result<Map> search(@RequestParam(required = false) Map<String, String> searchMap){
        Map resultMap = skuService.search(searchMap);
        return new Result<Map>(true, StatusCode.OK, "查询商品列表成功", resultMap);
    }
}
