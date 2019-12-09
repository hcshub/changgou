package com.changgou.oauth.interceptor;

import com.changgou.oauth.util.JwtToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 所在aouth系统中发起feign请求之前会触发当前拦截器的apply方法
 * @author Steven
 * @version 1.0
 * @description com.changgou.oauth.interceptor
 * @date 2019-11-10
 */
@Configuration
public class FeignOauth2RequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //创建管理员令牌
        String token = "bearer " + JwtToken.adminJwt();
        requestTemplate.header("Authorization", token);

        //使用RequestContextHolder工具获取request相关变量
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            //取出request
            HttpServletRequest request = attributes.getRequest();
            //获取所有头文件信息的key
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    //头文件的key
                    String name = headerNames.nextElement();
                    //头文件的value
                    String values = request.getHeader(name);
                    //将令牌数据添加到头文件中
                    requestTemplate.header(name, values);
                }
            }
        }

    }
}
