package com.changgou.test;

import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.test
 * @date 2019-11-13
 */
public class WxSDKTest {

    @Test
    public void testWXSDK() throws Exception{
        //生成随机字符
        System.out.println(WXPayUtil.generateNonceStr());

        //生成带签名的Map转xml
        Map map = new HashMap();
        map.put("id", "001");
        map.put("name", "风清扬");
        String xmlNo = WXPayUtil.mapToXml(map);
        System.out.println(xmlNo);
        String signedXml = WXPayUtil.generateSignedXml(map, "JKLDSJFjdfsjskfskjsjflajsl");
        System.out.println(signedXml);
        Map<String, String> toMap = WXPayUtil.xmlToMap(signedXml);
        System.out.println(toMap);
    }
}
