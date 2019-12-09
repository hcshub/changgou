package com.changgou.goods.dao;
import com.changgou.goods.pojo.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:sz.itheima
 * @Description:Sku的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface SkuMapper extends Mapper<Sku> {

    /**
     * 递减库存
     * @param num 扣减数量
     * @param skuId 购买商品skuId
     * @return
     */
    @Update("update tb_sku k set k.num = num - #{num},k.`sale_num` = sale_num + #{num} where k.`id` = #{skuId} AND k.num > #{num}")
    int decrCount(@Param("num") int num,@Param("skuId")Long skuId);
}
