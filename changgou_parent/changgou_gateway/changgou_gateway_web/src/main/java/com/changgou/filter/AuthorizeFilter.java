package com.changgou.filter;

import com.changgou.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.filter
 * @date 2019-11-7
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //令牌的key
    private static final String AUTHORIZE_TOKEN = "Authorization";
    private static final String USER_LOGIN_URL = "http://localhost:9001/oauth/login";

    /**
     * 实现拦截规则
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1、获取Request、Response对象-exchange.get...
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //2、获取请求的URI-request.getURI().getPath()
        String uri = request.getURI().getPath();
        //3、如果是登录请求-uri.startsWith，放行-chain.filter
        if(!URLFilter.hasAuthorize(uri)){
            chain.filter(exchange);
        }else{//4、如果是非登录请求
            //4.1 获取前端传入的令牌-从请求头中获取-request.getHeaders().getFirst
            String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
            //4.2 如果头信息中没有，从请求参数中获取-request.getQueryParams().getFirst
            if (StringUtils.isBlank(token)) {
                token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            }
            //4.3 如果请求参数中没有，从cookie中获取-request.getCookies()-取值前先判断不为空-getFirst
            if (StringUtils.isBlank(token)) {
                MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                if(cookies != null){
                    HttpCookie first = cookies.getFirst(AUTHORIZE_TOKEN);
                    if(first != null){
                        token = first.getValue();
                    }
                }
            }
            //4.4 如果以上方式都取不到令牌-返回405错误-response.setStatusCode(405)-return response.setComplete
            if (StringUtils.isBlank(token)) {
                //返回405表示没有权限访问
                //response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);

                //设置其它响应
                response.setStatusCode(HttpStatus.SEE_OTHER);
                //把原来请求的url记录丢到后面的微服务
                String url = USER_LOGIN_URL + "?FROM=" + request.getURI();
                //设置Location，相当重定向地址
                response.getHeaders().set("Location", url);  //把请求地址带回去


                return response.setComplete();
            }else{
                // 4.5 如果获取到了令牌，解析令牌-JwtUtil.parseJWT，放行-chain.filter(exchange)
                try {
                    /*Claims claims = JwtUtil.parseJWT(token);
                    //4.5.1解析成功-把令牌返回-request.mutate().header(key,value)
                    request.mutate().header(AUTHORIZE_TOKEN, claims.toString());*/

                    //把用户转过来的请求头信息，带到后续的系统中
                    request.mutate().header(AUTHORIZE_TOKEN, "bearer " + token);
                } catch (Exception e) {
                    e.printStackTrace();
                    //返回401，表示认证失败
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
            }
        }
        //放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
