package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/****
 * @Author:sz.itheima
 * @Description:Spu业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SkuMapper skuMapper;


    /**
     * Spu条件+分页查询
     * @param spu 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(spu);
        //执行搜索
        return new PageInfo<Spu>(spuMapper.selectByExample(example));
    }

    /**
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Spu>(spuMapper.selectAll());
    }

    /**
     * Spu条件查询
     * @param spu
     * @return
     */
    @Override
    public List<Spu> findList(Spu spu){
        //构建查询条件
        Example example = createExample(spu);
        //根据构建的条件查询数据
        return spuMapper.selectByExample(example);
    }


    /**
     * Spu构建查询对象
     * @param spu
     * @return
     */
    public Example createExample(Spu spu){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(spu!=null){
            // 主键
            if(!StringUtils.isEmpty(spu.getId())){
                    criteria.andEqualTo("id",spu.getId());
            }
            // 货号
            if(!StringUtils.isEmpty(spu.getSn())){
                    criteria.andEqualTo("sn",spu.getSn());
            }
            // SPU名
            if(!StringUtils.isEmpty(spu.getName())){
                    criteria.andLike("name","%"+spu.getName()+"%");
            }
            // 副标题
            if(!StringUtils.isEmpty(spu.getCaption())){
                    criteria.andEqualTo("caption",spu.getCaption());
            }
            // 品牌ID
            if(!StringUtils.isEmpty(spu.getBrandId())){
                    criteria.andEqualTo("brandId",spu.getBrandId());
            }
            // 一级分类
            if(!StringUtils.isEmpty(spu.getCategory1Id())){
                    criteria.andEqualTo("category1Id",spu.getCategory1Id());
            }
            // 二级分类
            if(!StringUtils.isEmpty(spu.getCategory2Id())){
                    criteria.andEqualTo("category2Id",spu.getCategory2Id());
            }
            // 三级分类
            if(!StringUtils.isEmpty(spu.getCategory3Id())){
                    criteria.andEqualTo("category3Id",spu.getCategory3Id());
            }
            // 模板ID
            if(!StringUtils.isEmpty(spu.getTemplateId())){
                    criteria.andEqualTo("templateId",spu.getTemplateId());
            }
            // 运费模板id
            if(!StringUtils.isEmpty(spu.getFreightId())){
                    criteria.andEqualTo("freightId",spu.getFreightId());
            }
            // 图片
            if(!StringUtils.isEmpty(spu.getImage())){
                    criteria.andEqualTo("image",spu.getImage());
            }
            // 图片列表
            if(!StringUtils.isEmpty(spu.getImages())){
                    criteria.andEqualTo("images",spu.getImages());
            }
            // 售后服务
            if(!StringUtils.isEmpty(spu.getSaleService())){
                    criteria.andEqualTo("saleService",spu.getSaleService());
            }
            // 介绍
            if(!StringUtils.isEmpty(spu.getIntroduction())){
                    criteria.andEqualTo("introduction",spu.getIntroduction());
            }
            // 规格列表
            if(!StringUtils.isEmpty(spu.getSpecItems())){
                    criteria.andEqualTo("specItems",spu.getSpecItems());
            }
            // 参数列表
            if(!StringUtils.isEmpty(spu.getParaItems())){
                    criteria.andEqualTo("paraItems",spu.getParaItems());
            }
            // 销量
            if(!StringUtils.isEmpty(spu.getSaleNum())){
                    criteria.andEqualTo("saleNum",spu.getSaleNum());
            }
            // 评论数
            if(!StringUtils.isEmpty(spu.getCommentNum())){
                    criteria.andEqualTo("commentNum",spu.getCommentNum());
            }
            // 是否上架,0已下架，1已上架
            if(!StringUtils.isEmpty(spu.getIsMarketable())){
                    criteria.andEqualTo("isMarketable",spu.getIsMarketable());
            }
            // 是否启用规格
            if(!StringUtils.isEmpty(spu.getIsEnableSpec())){
                    criteria.andEqualTo("isEnableSpec",spu.getIsEnableSpec());
            }
            // 是否删除,0:未删除，1：已删除
            if(!StringUtils.isEmpty(spu.getIsDelete())){
                    criteria.andEqualTo("isDelete",spu.getIsDelete());
            }
            // 审核状态，0：未审核，1：已审核，2：审核不通过
            if(!StringUtils.isEmpty(spu.getStatus())){
                    criteria.andEqualTo("status",spu.getStatus());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //检查是否被逻辑删除  ,必须先逻辑删除后才能物理删除
        if(!"1".equals(spu.getIsDelete())){
            throw new RuntimeException("此商品不能物理删除！");
        }
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Spu
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 增加Spu
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
    @Override
    public Spu findById(Long id){
        return  spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spu全部数据
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    @Autowired
    private IdWorker idWorker;

    @Override
    public void saveGoods(Goods goods) {
        //新增
         if(goods.getSpu().getId() == null){
             //保存spu信息
             long spuId = idWorker.nextId();
             goods.getSpu().setId(spuId);
             spuMapper.insertSelective(goods.getSpu());
         }else{  //修改
             spuMapper.updateByPrimaryKeySelective(goods.getSpu());

             //修改sku列表,先删除，再新增
             Sku where = new Sku();
             where.setSpuId(goods.getSpu().getId());
             skuMapper.delete(where);
         }

        //当前时间
        Date now = new Date();
        //查询分类与品牌
        Category category = categoryMapper.selectByPrimaryKey(goods.getSpu().getCategory3Id());
        Brand brand = brandMapper.selectByPrimaryKey(goods.getSpu().getBrandId());
        //保存sku列表
        for (Sku sku : goods.getSkuList()) {
            sku.setId(idWorker.nextId());  //skuId

            //计算sku名称
            String name = goods.getSpu().getName();
            if(StringUtils.isEmpty(sku.getSpec())){
                sku.setSpec("{}");
            }
            Map<String,String> specMap = JSON.parseObject(sku.getSpec(), Map.class);
            for (String value : specMap.values()) {
                name += value + " ";
            }
            sku.setName(name);
            if(goods.getSpu().getId() == null){
                sku.setCreateTime(now);  //创建时间
            }
            sku.setUpdateTime(now);  //更新时间
            sku.setSpuId(goods.getSpu().getId());
            sku.setCategoryId(goods.getSpu().getCategory3Id());

            sku.setCategoryName(category.getName());
            //品牌名字
            sku.setBrandName(brand.getName());
            //保存sku
            skuMapper.insertSelective(sku);
        }
    }

    @Override
    public Goods findGoodsById(Long spuId) {
        Goods goods = new Goods();
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        Sku where = new Sku();
        where.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(where);

        goods.setSpu(spu);
        goods.setSkuList(skuList);

        return goods;
    }

    @Override
    public void audit(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu.getIsDelete() == "1") {
            throw new RuntimeException("已删除的商品，不允许审核！");
        }
        spu.setStatus("1");  //审核通过
        spu.setIsMarketable("1");  //已上架
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void pull(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu.getIsDelete() == "1") {
            throw new RuntimeException("已删除的商品，不允许下架！");
        }
        spu.setIsMarketable("0");  //已下架
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void put(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu.getIsDelete() == "1") {
            throw new RuntimeException("已删除的商品，不允许上架！");
        }
        spu.setIsMarketable("1");  //已上架
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public int putMany(Long[] ids) {
        //修改的结果
        Spu spu = new Spu();
        spu.setIsMarketable("1");
        //修改范围
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        criteria.andEqualTo("status", "1"); //审核通过的
        criteria.andEqualTo("isDelete", "0"); //非删除的

        //updateByExampleSelective(修改的结果，修改的范围)
        int count = spuMapper.updateByExampleSelective(spu, example);

        return count;
    }

    @Override
    public void logicDelete(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu.getIsMarketable().equals("1")) {
            throw new RuntimeException("已上架的商品，不允许删除！");
        }
        spu.setIsDelete("1");  //已删除状态
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void restore(Long spuId) {
        Spu spu = new Spu();
        spu.setId(spuId);
        spu.setIsDelete("0");  //未删除
        spuMapper.updateByPrimaryKeySelective(spu);
    }
}
