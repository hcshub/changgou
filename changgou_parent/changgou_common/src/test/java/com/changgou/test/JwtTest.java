package com.changgou.test;

import io.jsonwebtoken.*;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.test
 * @date 2019-11-7
 */
public class JwtTest {

    @Test
    public void testCreateJWT(){
        //1、创建Jwt构建器-jwtBuilder = Jwts.builder()
        JwtBuilder jwtBuilder = Jwts.builder();
        //2、设置唯一编号-setId
        jwtBuilder.setId("007");
        //3、设置主题，可以是JSON数据-setSubject()
        jwtBuilder.setSubject("Hello JWT");
        //4、设置签发日期-setIssuedAt
        jwtBuilder.setIssuedAt(new Date());
        //5、设置签发人-setIssuer
        jwtBuilder.setIssuer("传智播客");

        //指定有效时间
        //Date exp = new Date(System.currentTimeMillis() + 60000);
        //jwtBuilder.setExpiration(exp);

        //自定信息-Claims
        Map<String, Object> user = new HashMap<>();
        user.put("name", "steven");
        user.put("age", "18");
        user.put("address", "深圳市.黑马程序员");
        //设置自定内容
        jwtBuilder.addClaims(user);

        //6、设置签证
        jwtBuilder.signWith(SignatureAlgorithm.HS256, "sz.itheima");
        //7、生成令牌-compact()
        String token = jwtBuilder.compact();
        //8、输出结果
        System.out.println(token);
    }

    @Test
    public void testParseJwt(){
        //令牌
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwMDciLCJzdWIiOiJIZWxsbyBKV1QiLCJpYXQiOjE1NzMxMjExMjEsImlzcyI6IuS8oOaZuuaSreWuoiIsImFkZHJlc3MiOiLmt7HlnLPluIIu6buR6ams56iL5bqP5ZGYIiwibmFtZSI6InN0ZXZlbiIsImFnZSI6IjE4In0.7vG41HdZKQlaZoq50WFAynQ78sxAmA7Dk2r7hjF0TtI";
        //创建解析器
        JwtParser jwtParser = Jwts.parser();

        //设置签名密钥
        jwtParser.setSigningKey("sz.itheima");

        //解密
        Claims claims = jwtParser.parseClaimsJws(token).getBody();

        System.out.println(claims);
    }
}
