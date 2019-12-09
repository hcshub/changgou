package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.seckill.task
 * @date 2019-11-15
 */
@Component
public class SeckillGoodsPushTask {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/5 * * * * *")
    public void loadGoodsPushRedis(){
        System.out.println("定时任务被调用了....");

        //获取时间菜单
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date date : dateMenus) {
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            //1)计算秒杀时间段
            //stratTime<=当前时间<endTime
            criteria.andLessThanOrEqualTo("startTime", date);
            //2)状态必须为审核通过 status=1
            criteria.andEqualTo("status", "1");
            //3)商品库存个数>0
            criteria.andGreaterThan("stockCount", 0);
            //4)活动没有结束  endTime>=now()
            criteria.andGreaterThan("endTime", date);

            //获取当前循环的时间串
            String extName = DateUtil.data2str(date, DateUtil.PATTERN_YYYYMMDDHH);
            //5)在Redis中没有该商品的缓存
            //获取到当前时间内所有在缓存中的商品id
            Set keys = redisTemplate.boundHashOps("SeckillGoods_" + extName).keys();
            if(keys != null && keys.size() > 0){
                //排除已存在的商品
                criteria.andNotIn("id", keys);
            }

            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            System.out.println(extName + "时段导入商品个数为：" + seckillGoodsList.size());

            for (SeckillGoods seckillGoods : seckillGoodsList) {
                //SeckillGoods_2019111516：{商品id:商品对象}
                //SeckillGoods_2019111518：{商品id:商品对象}
                redisTemplate.boundHashOps("SeckillGoods_" + extName).put(seckillGoods.getId(), seckillGoods);


                //方式一：记录商品信息库存队列
                /*for (int i = 0; i < seckillGoods.getStockCount(); i++) {
                    redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillGoods.getId()).leftPush(1);
                }*/
                Long[] ids = pushIds(seckillGoods.getStockCount(), seckillGoods.getId());
                redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillGoods.getId()).leftPushAll(ids);


                //方式二：redis的decr完成-开始时记录所有库存
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGoods.getId(), seckillGoods.getStockCount());
            }
        }
    }

    /***
     * 将商品ID存入到数组中
     * @param len:长度-库存
     * @param id :值
     */
    public Long[] pushIds(int len,Long id){
        Long[] ids = new Long[len];
        for (int i = 0; i <ids.length ; i++) {
            ids[i]=id;
        }
        return ids;
    }

}
