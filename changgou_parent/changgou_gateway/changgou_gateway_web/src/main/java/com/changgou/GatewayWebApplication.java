package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableEurekaClient
public class GatewayWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayWebApplication.class,args);
    }

    @Bean(name = "ipKeyResolver")
    public KeyResolver getKeyResolver(){
        return new KeyResolver() {
            /**
             * 配置限流策略,以什么方式限流
             * @param exchange
             * @return
             */
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                //请求用户的ip地址
                String ip = exchange.getRequest().getRemoteAddress().getHostString();
                //以ip作为限流标准
                return Mono.just(ip);
            }
        };
    }
}
