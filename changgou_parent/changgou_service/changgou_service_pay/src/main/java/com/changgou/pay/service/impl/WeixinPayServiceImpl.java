package com.changgou.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.pay.service.impl
 * @date 2019-11-13
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${weixin.appid}")
    private String appid;
    @Value("${weixin.partner}")
    private String partner;
    @Value("${weixin.notifyurl}")
    private String notifyurl;
    @Value("${weixin.partnerkey}")
    private String partnerkey;


    @Override
    public Map createNative(Map<String,String> paramMap){
        Map<String,String> map = null;
        try {
            map = new HashMap();
            //1、组装请求参数
            Map<String,String> param = new HashMap();
            param.put("appid", appid);  //公众号id
            param.put("mch_id", partner);  //商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());  //随机字符串
            param.put("body", "畅购");  //商品描述
            param.put("out_trade_no", paramMap.get("out_trade_no"));  //商户订单，我们的订单号
            param.put("total_fee", paramMap.get("total_fee"));  //支付金额(分)
            param.put("spbill_create_ip", "127.0.0.1");  //终端ip
            param.put("notify_url", notifyurl);  //回调地址
            param.put("trade_type", "NATIVE");  //支付类型

            //附加参数
            //exchange 交换机,
            //routingKey 路由Key
            Map<String, String> attachMap = new HashMap<>();
            attachMap.put("exchange", paramMap.get("exchange"));
            attachMap.put("routingKey", paramMap.get("routingKey"));
            attachMap.put("username", paramMap.get("username"));
            param.put("attach", JSON.toJSONString(attachMap));
            //签名可以通过api直接生成---------------------
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("正在发起统一下单接口，参数为：" + xmlParam);
            //2、通过HttpClient发起统一下单请求
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);  //加密
            httpClient.setXmlParam(xmlParam);  //设置参数
            httpClient.post();
            String content = httpClient.getContent();
            System.out.println("发起统一下单接口成功，响应结果为：" + content);
            //3、解析结果，包装返回
            Map<String, String> result = WXPayUtil.xmlToMap(content);
            map.put("out_trade_no", paramMap.get("out_trade_no"));  //订单号
            map.put("total_fee", paramMap.get("total_fee"));  //支付金额
            map.put("code_url", result.get("code_url"));  //支付金额

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            //1、组装请求参数
            Map<String,String> paramMap = new HashMap();
            paramMap.put("appid", appid);  //公众号id
            paramMap.put("mch_id", partner);  //商户号
            paramMap.put("out_trade_no", out_trade_no);  //商户订单，我们的订单号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());  //随机字符串

            //签名可以通过api直接生成---------------------
            String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            System.out.println("正在发起查询订单接口，参数为：" + xmlParam);
            //2、通过HttpClient发起统一下单请求
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);  //加密
            httpClient.setXmlParam(xmlParam);  //设置参数
            httpClient.post();
            String content = httpClient.getContent();
            System.out.println("发起查询订单接口成功，响应结果为：" + content);
            //3、解析结果，包装返回
            Map<String, String> result = WXPayUtil.xmlToMap(content);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
