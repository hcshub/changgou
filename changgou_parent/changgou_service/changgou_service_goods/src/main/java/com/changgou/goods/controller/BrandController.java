package com.changgou.goods.controller;

import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:shenkunlin
 * @Description:
 * @Date 2019/6/14 0:18
 *****/

@RestController
@RequestMapping("/brand")
@CrossOrigin
public class BrandController {

    @Autowired
    private BrandService brandService;

    /***
     * Brand分页条件搜索实现
     * @param brand
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@RequestBody(required = false)  Brand brand, @PathVariable  int page, @PathVariable  int size){
        //调用BrandService实现分页条件查询Brand
        PageInfo<Brand> pageInfo = brandService.findPage(brand, page, size);
        return new Result(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * Brand分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@PathVariable  int page, @PathVariable  int size){
        //调用BrandService实现分页查询Brand
        PageInfo<Brand> pageInfo = brandService.findPage(page, size);
        return new Result<PageInfo>(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     * @param brand
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<Brand>> findList(@RequestBody(required = false)  Brand brand){
        //调用BrandService实现条件查询Brand
        List<Brand> list = brandService.findList(brand);
        return new Result<List<Brand>>(true,StatusCode.OK,"查询成功",list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable Integer id){
        //调用BrandService实现根据主键删除
        brandService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 修改Brand数据
     * @param brand
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody  Brand brand,@PathVariable Integer id){
        //设置主键值
        brand.setId(id);
        //调用BrandService实现修改Brand
        brandService.update(brand);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /***
     * 新增Brand数据
     * @param brand
     * @return
     */
    @PostMapping
    public Result add(@RequestBody   Brand brand){
        //调用BrandService实现添加Brand
        brandService.add(brand);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    /***
     * 根据ID查询Brand数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Brand> findById(@PathVariable Integer id){
        //调用BrandService实现根据主键查询Brand
        Brand brand = brandService.findById(id);
        return new Result<Brand>(true,StatusCode.OK,"查询成功",brand);
    }

    /***
     * 查询Brand全部数据
     * @return
     */
    @GetMapping
    public Result<List<Brand>> findAll(){
        try {
            System.out.println("模拟当前业务处理需要的时间....开始");
            Thread.sleep(10000);
            System.out.println("模拟当前业务处理需要的时间....结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //调用BrandService实现查询所有Brand
        List<Brand> list = brandService.findAll();
        return new Result<List<Brand>>(true, StatusCode.OK,"查询成功",list) ;
    }

    @GetMapping("category/{cid}")
    public Result<List<Brand>> findByCategory(@PathVariable Integer cid){
        List<Brand> brandList = brandService.findByCategory(cid);
        return new Result<List<Brand>>(true, StatusCode.OK,"查询成功",brandList) ;
    }
}
