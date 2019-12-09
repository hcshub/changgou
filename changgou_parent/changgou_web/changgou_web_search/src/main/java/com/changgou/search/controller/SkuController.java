package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import entity.Page;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.jws.WebParam;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.search.controller
 * @date 2019-11-6
 */
@Controller
@RequestMapping("search")
public class SkuController {
    @Autowired
    private SkuFeign skuFeign;

    /**
     * @param searchMap
     * @param model
     * 注意此处的@GetMapping()要添加list的url请求，不然会跟SkuFeign中的请求url冲突
     * @return
     */
    @GetMapping("list")
    public String search(@RequestParam(required = false) Map searchMap, Model model){
        Result result = skuFeign.search(searchMap);
        Map resultMap = (Map) result.getData();
        //返回真实数据
        model.addAttribute("result", resultMap);
        //把查询条件回显
        model.addAttribute("searchMap", searchMap);
        //获取新的url
        String url = this.getUrl(searchMap);
        model.addAttribute("url", url);
        //把分页参数返回
        Page page = new Page(
                new Long(resultMap.get("total").toString()),
                new Integer(resultMap.get("pageNum").toString()),
                new Integer(resultMap.get("pageSize").toString()));
        model.addAttribute("page", page);

        return "search";
    }

    /**
     * 拼接用户传入参数的url
     * @param searchMap  传入的参数
     * @return url
     */
    private String getUrl(Map<String,String> searchMap){
        ///search/list?category=笔记本&brand=华为&spec_网络=移动4G
        String url = "/search/list";
        if(searchMap != null){
            url += "?";
            for (String key : searchMap.keySet()) {
                //如果是排序的参数，不拼接到url上，便于下次换种方式排序
                if(key.indexOf("sort") > -1 || "pageNum".equals(key)){
                    continue;
                }
                url = url + key + "=" + searchMap.get(key) + "&";
            }
            //去掉最后一个&
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
