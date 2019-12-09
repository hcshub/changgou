package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.utils.SeckillStatus;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.seckill.task
 * @date 2019-11-15
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
public class MultiThreadingCreateOrder {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;

    @Async  //标识当前方法是多线程执行，异步
    public void noStockOrder(String username,String time,Long id,Long count){
        //清理排队标示
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //清理抢单标示
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);
        if(count.longValue() == 0) {
            //更新redis缓存
            seckillGoods.setStockCount(count.intValue());
            redisTemplate.boundHashOps("SeckillGoods_" + time).put(id, seckillGoods);
            //同步数据库
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
        }
    }

    @Async  //标识当前方法是多线程执行，异步
    public void createOrder(Long count){
        //左进右取
        SeckillStatus seckillStatus  = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();

        //如果有排队信息
        if (seckillStatus != null) {
            //便于测试我们这里的参数先写死
            //时间区间
            String time = seckillStatus.getTime();
            //用户登录名
            String username = seckillStatus.getUsername();
            //用户抢购商品
            Long id = seckillStatus.getGoodsId();

            //1、从Redis中查询商品信息
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);
            //2、判断库存是否有
            if(seckillGoods != null && seckillGoods.getStockCount() < 1){
                throw new RuntimeException("抱歉你来晚一步，当前商品已被抢购一空！");
            }

            //3、扣减库存-扣完库存后，还是否有货,没货同步mysql
            seckillGoods.setStockCount(count.intValue());

            //修改库存
            redisTemplate.boundHashOps("SeckillGoods_" + time).put(id, seckillGoods);
            //4、下单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setSeckillId(id);
            seckillOrder.setMoney(seckillGoods.getCostPrice());  //秒杀价就是支付金额
            seckillOrder.setUserId(username);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0");  //未支付
            //保存订单到redis中
            redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);

            //更新排队状态
            seckillStatus.setStatus(2);
            seckillStatus.setMoney(new Float(seckillOrder.getMoney()));  //支付金额
            seckillStatus.setOrderId(seckillOrder.getId());  //订单号
            //更新排队状态
            redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
        }

    }
}
