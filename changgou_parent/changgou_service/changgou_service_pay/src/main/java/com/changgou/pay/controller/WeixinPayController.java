package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.pay.controller
 * @date 2019-11-13
 */
@RestController
@RequestMapping("/weixin/pay")
public class WeixinPayController {
    @Autowired
    private WeixinPayService weixinPayService;

    @RequestMapping("/create/native")
    public Result<Map> createNative(@RequestParam Map<String,String> paramMap) {
        paramMap.put("username", "zhangsan");
        Map map = weixinPayService.createNative(paramMap);
        return new Result<Map>(true, StatusCode.OK, "生成二维成功", map);
    }

    @GetMapping(value = "/status/query")
    public Result queryStatus(String out_trade_no) {
        Map map = weixinPayService.queryPayStatus(out_trade_no);
        return new Result<Map>(true, StatusCode.OK, "查询订单成功", map);
    }


    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${mq.pay.exchange.order}")
    private String exchange;
    @Value("${mq.pay.queue.order}")
    private String queue;
    @Value("${mq.pay.routing.key}")
    private String routing;

    /***
     * 支付回调
     * 支付完成后，微信会把相关支付结果及用户信息通过数据流的形式发送给商户，
     * 商户需要接收处理，并按文档规范返回应答
     * @param request
     * @return
     */
    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request){
        try {
            //获取微信转入数据流
            ServletInputStream inputStream = request.getInputStream();

            //使用apache.commons.io下的IOUtils，把输入流转成字符串
            String result = IOUtils.toString(inputStream, "utf-8");
            //把xml转成Map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);

            System.out.println("支付成功，微信回调的参数为：\n" + resultMap);


            String json = resultMap.get("attach");
            Map<String,String> attachMap = JSON.parseObject(json, Map.class);
            exchange = attachMap.get("exchange");
            routing = attachMap.get("routingKey");
           /* Map<String, String> attachMap = new HashMap<>();
            attachMap.put("exchange", paramMap.get("exchange"));
            attachMap.put("routingKey", paramMap.get("routingKey"));
            param.put("attach", JSON.toJSONString(attachMap));*/

            //发送MQ消息
            rabbitTemplate.convertAndSend(exchange,routing, JSON.toJSONString(resultMap));

            //包装响应结果
            Map responseMap = new HashMap();
            responseMap.put("return_code", "SUCCESS");
            responseMap.put("return_msg", "OK");
            String responseXml = WXPayUtil.mapToXml(responseMap);
            return responseXml;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("createSeckillQueue")
    public String createSeckillQueue(){
        //发送MQ消息-用于创建秒杀队列
        rabbitTemplate.convertAndSend("exchange.order","queue.seckillorder", "{'flag':'ok'}");
        return "ok";
    }



}
