package com.changgou.seckill.service.impl;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.seckill.task.MultiThreadingCreateOrder;
import com.changgou.seckill.utils.SeckillStatus;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/****
 * @Author:sz.itheima
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;


    /**
     * SeckillOrder条件+分页查询
     * @param seckillOrder 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder){
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder){
        Example example=new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if(seckillOrder!=null){
            // 主键
            if(!StringUtils.isEmpty(seckillOrder.getId())){
                    criteria.andEqualTo("id",seckillOrder.getId());
            }
            // 秒杀商品ID
            if(!StringUtils.isEmpty(seckillOrder.getSeckillId())){
                    criteria.andEqualTo("seckillId",seckillOrder.getSeckillId());
            }
            // 支付金额
            if(!StringUtils.isEmpty(seckillOrder.getMoney())){
                    criteria.andEqualTo("money",seckillOrder.getMoney());
            }
            // 用户
            if(!StringUtils.isEmpty(seckillOrder.getUserId())){
                    criteria.andEqualTo("userId",seckillOrder.getUserId());
            }
            // 创建时间
            if(!StringUtils.isEmpty(seckillOrder.getCreateTime())){
                    criteria.andEqualTo("createTime",seckillOrder.getCreateTime());
            }
            // 支付时间
            if(!StringUtils.isEmpty(seckillOrder.getPayTime())){
                    criteria.andEqualTo("payTime",seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if(!StringUtils.isEmpty(seckillOrder.getStatus())){
                    criteria.andEqualTo("status",seckillOrder.getStatus());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(seckillOrder.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if(!StringUtils.isEmpty(seckillOrder.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",seckillOrder.getReceiverMobile());
            }
            // 收货人
            if(!StringUtils.isEmpty(seckillOrder.getReceiver())){
                    criteria.andEqualTo("receiver",seckillOrder.getReceiver());
            }
            // 交易流水
            if(!StringUtils.isEmpty(seckillOrder.getTransactionId())){
                    criteria.andEqualTo("transactionId",seckillOrder.getTransactionId());
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
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder){
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder){
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id){
        return  seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;
    @Override
    public boolean add(Long id, String time, String username) {
        System.out.println("进入了抢购业务方法，开始异步调用...");
        //识别用户有下过单-increment(操作的key,+值(可以是负数))
        /*Long count = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        if(count > 1){
            throw new RuntimeException("你有未付款的订单，请先支付！");
        }*/

        //超卖解决方案一：分布式队列实现
        /*Object pop = redisTemplate.boundListOps("SeckillGoodsCountList_" + id).rightPop();
        //队列没有库存
        if (pop == null) {
            //清理排队标示
            redisTemplate.boundHashOps("UserQueueCount").delete(username);
            //清理抢单标示
            redisTemplate.boundHashOps("UserQueueStatus").delete(username);

            throw new RuntimeException("抱歉你来晚一步，当前商品已被抢购一空！");
        }*/

        //超卖解决方案二：decr实现
        Long count = redisTemplate.boundHashOps("SeckillGoodsCount").increment(id, -1);
        if(count <= 0){
            //调取没有库存方法-后续处理
            multiThreadingCreateOrder.noStockOrder(username,time,id,count);

            throw new RuntimeException("抱歉你来晚一步，当前商品已被抢购一空！");
        }
        //排队
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(), 1, id, time);
        seckillStatus.setCount(count.intValue());
        //排队记录排队信息
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);
        //登记排队信息
        redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
        //开启多线程下单
        multiThreadingCreateOrder.createOrder(count);
        System.out.println("进入了抢购业务方法，结束异步调用...");
        return true;
    }

    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
    }

    @Override
    public void updatePayStatus(String out_trade_no, String transaction_id, String username) {
        //1、从redis中查询用户的订单
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        if(seckillOrder != null){
            seckillOrder.setStatus("1");
            seckillOrder.setPayTime(new Date());
            seckillOrder.setTransactionId(transaction_id);
            //2.把订单保存到mysql
            seckillOrderMapper.insertSelective(seckillOrder);
            //3、清除排队所有信息
            //删除订单Redis缓存
            redisTemplate.boundHashOps("SeckillOrder").delete(username);
            //删除用户下单记录数据
            redisTemplate.boundHashOps("UserQueueCount").delete(username);
            //删除抢购状态信息
            redisTemplate.boundHashOps("UserQueueStatus").delete(username);

        }

    }

    @Override
    public void closeOrder(String username) {
        //1.读取排队状态-redis.UserQueueStatus
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        //2.获取Redis中订单信息-SeckillOrder
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        //3.如果Redis中有订单信息，说明用户未支付
        if(seckillOrder != null){
            //3.1删除订单-SeckillOrder.delete
            redisTemplate.boundHashOps("SeckillOrder").delete(username);
            //3.2回滚库存
            //3.2.1从Redis中获取该商品-SeckillGoods_time.get(id)
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).get(seckillStatus.getGoodsId());
            //3.2.2如果Redis中没有，则从数据库中加载
            if(seckillGoods == null){
                seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
            }
            //3.2.3数量+1  (递增数量+1，队列数量+1)
            redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillStatus.getGoodsId(), 1);

            //3.3.数据同步到Redis中-SeckillGoods_time,UserQueueCount,UserQueueStatus
            redisTemplate.boundHashOps("SeckillGoods_"+seckillStatus.getTime()).put(seckillStatus.getGoodsId(),seckillGoods);
            //清理抢单标识
            redisTemplate.boundHashOps("UserQueueCount").delete(seckillStatus.getUsername());
            //清理排队标识
            redisTemplate.boundHashOps("UserQueueStatus").delete(seckillStatus.getUsername());

        }


    }
}
