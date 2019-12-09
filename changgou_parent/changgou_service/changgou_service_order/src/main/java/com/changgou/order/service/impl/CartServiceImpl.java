package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.order.service.impl
 * @date 2019-11-10
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SpuFeign spuFeign;
    @Override
    public void add(Integer num, Long skuId, String username) {
        //根据id查询商品信息
        Sku sku = skuFeign.findById(skuId).getData();
        if(sku == null){
            throw new RuntimeException("添加的商品不存在");
        }else{
            //如果传入数据少于1，说明是删除操作
            if(num < 1){
                redisTemplate.boundHashOps("Cart_" + username).delete(skuId);
                return;
            }
            //查询spu
            Spu spu = spuFeign.findById(sku.getSpuId()).getData();
            //购物车对象创建
            OrderItem orderItem = new OrderItem();
            orderItem.setSkuId(skuId);
            orderItem.setSpuId(sku.getSpuId());
            orderItem.setName(sku.getName());
            orderItem.setPrice(sku.getPrice());
            orderItem.setNum(num);
            orderItem.setMoney(num * orderItem.getPrice());       //单价*数量
            orderItem.setPayMoney(num * orderItem.getPrice());    //实付金额
            orderItem.setImage(sku.getImage());
            orderItem.setWeight(sku.getWeight() * num);           //重量=单个重量*数量

            //分类ID设置
            orderItem.setCategoryId1(spu.getCategory1Id());
            orderItem.setCategoryId2(spu.getCategory2Id());
            orderItem.setCategoryId3(spu.getCategory3Id());

            //把数据保存redis
            /******
             * 购物车数据存入到Redis
             * namespace = Cart_[username]
             * key=skuId
             * value=OrderItem
             */
            redisTemplate.boundHashOps("Cart_" + username).put(skuId, orderItem);

        }
    }

    @Override
    public List<OrderItem> list(String username) {
        List<OrderItem> values = redisTemplate.boundHashOps("Cart_" + username).values();
        return values;
    }
}
