package com.changgou.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.order.listener
 * @date 2019-11-13
 */
@Component
public class SeckillPayMessageListener {
    @Autowired
    private SeckillOrderService seckillOrderService;

    @RabbitListener(queues = "${mq.pay.queue.seckillorder}")
    public void payListener(String json){
        //把json转成Map
        Map<String,String> resultMap = JSON.parseObject(json, Map.class);

        System.out.println("收到消息，参数为：" + resultMap);

        if("SUCCESS".equals(resultMap.get("return_code"))){
            //业务结果：SUCCESS/FAIL
            String resultCode = resultMap.get("result_code");
            //out_trade_no-商户订单号
            String out_trade_no = resultMap.get("out_trade_no");
            //交易流水号
            String transaction_id = resultMap.get("transaction_id");
            //附加参数
            Map<String, String> attachMap = JSON.parseObject(resultMap.get("attach"), Map.class);
            //用户名
            String username = attachMap.get("username");

            //支付成功
            if("SUCCESS".equals(resultCode)){
                //修改订单状态
                seckillOrderService.updatePayStatus(out_trade_no,transaction_id,username);
            }else{
                //支付失败-redis订单删除-库存回滚，积分回滚-作业
                seckillOrderService.closeOrder(username);
            }
        }
    }
}
