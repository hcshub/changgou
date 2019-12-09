package com.changgou.oauth.controller;

import com.changgou.oauth.service.LoginService;
import com.changgou.oauth.util.AuthToken;
import com.changgou.oauth.util.CookieUtil;
import entity.Result;
import entity.StatusCode;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.oauth.controller
 * @date 2019-11-9
 */
@RestController
@RequestMapping("my")
public class LoginController {
    //客户端ID
    @Value("${auth.clientId}")
    private String clientId;

    //秘钥
    @Value("${auth.clientSecret}")
    private String clientSecret;

    //Cookie存储的域名
    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    //Cookie生命周期
    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;

    @Autowired
    private LoginService loginService;


    @PostMapping("login")
    public Result<AuthToken> login(String username,String password){
        if (StringUtils.isBlank(username)) {
            throw new RuntimeException("用户名不能为空");
        }
        if (StringUtils.isBlank(password)) {
            throw new RuntimeException("密码不能为空");
        }
        AuthToken authToken = loginService.login(username, password, clientId, clientSecret);
        //把生成的信息保存到cookie中
        saveCookie(authToken.getAccessToken());

        return new Result<AuthToken>(true, StatusCode.OK,"登录成功",authToken);
    }

    /***
     * 将令牌存储到cookie
     * @param token
     */
    private void saveCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/","Authorization",token,cookieMaxAge,false);
    }
}
