package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.LoginService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.oauth.service.impl
 * @date 2019-11-9
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //1.选中认证服务的地址-serviceInstance = loadBalancerClient.choose("user-auth")
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        //2.如果服务器实例为空，返回找不到服务异常信息
        if(serviceInstance == null){
            throw new RuntimeException("找不到认证服务!");
        }
        //3.拼接令牌的请求地址-url = serviceInstance.getUri().toString() + "/oauth/token"
        //http://localhost:9001/
        String url = serviceInstance.getUri().toString() + "/oauth/token";
        //4.定义body = new LinkedMultiValueMap<String, String>()
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //4.1 设置授权方式-body.add(名字，"password")
        body.add("grant_type","password");
        //4.2 设置账号
        body.add("username",username);
        //4.3 设置密码
        body.add("password",password);
        //5.定义响应头信息-headers = new LinkedMultiValueMap<String, String>()
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        //5.1 使用base64编码拼接 [Basic 客户端id:客户端密钥]-Base64Utils.encode
        //转成64位
       /* String client = clientId + ":" + clientSecret;
        String authorization = "Basic " + Base64Utils.encode(client.getBytes());
        //5.2 设置头信息-Authorization
        headers.add("Authorization",authorization);*/

        //5.1 使用base64编码拼接 [Basic 客户端id:客户端密钥]-Base64Utils.encode
        String AuthorizationStr = clientId + ":" + clientSecret;
        //转64编码
        byte[] encode = Base64Utils.encode(AuthorizationStr.getBytes());
        AuthorizationStr = "Basic " + new String(encode);
        //5.2 设置头信息-Authorization
        headers.add("Authorization", AuthorizationStr);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });

        //6.使用restTemplate请求spring security的申请令牌接口-
        // restTemplate.exchange(url,请求方式,参数,返回类型)
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        //7.获取响应数据-responseEntity.getBody()
        Map map = responseEntity.getBody();
        //8.将响应数据封装成AuthToken对象
        //8.将响应数据封装成AuthToken对象
        AuthToken authToken = new AuthToken();
        //访问令牌(jwt)
        String accessToken = (String) map.get("access_token");
        //刷新令牌(jwt)
        String refreshToken = (String) map.get("refresh_token");
        //jti，作为用户的身份标识
        String jwtToken= (String) map.get("jti");
        authToken.setJti(jwtToken);
        authToken.setAccessToken(accessToken);
        authToken.setRefreshToken(refreshToken);

        return authToken;
    }
}
