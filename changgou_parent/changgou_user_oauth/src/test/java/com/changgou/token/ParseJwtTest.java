package com.changgou.token;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

/*****
 * @Author: Steven
 * @Date: 2019/7/7 13:48
 * @Description: com.changgou.token
 *  使用公钥解密令牌数据
 ****/
public class ParseJwtTest {

    /***
     * 校验令牌
     */
    @Test
    public void testParseToken(){
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IlJPTEVfVklQLFJPTEVfVVNFUiIsIm5hbWUiOiJpdGhlaW1hIiwiaWQiOiIxIn0.YZL9TG-yStPRfXbC4DkLM-pPCwYeuODzE-bfYUWnv5Nw8QGO41XB-tsEyd5Ks21jxngZaEnthVt1flrEXXWi9SWvKBshNgvOTk9IrTVBmuJP3o3hQwE_Op8wNThcQcDM8NgWRa8lu9n3ec_K1VKt_HHudUE93Y_zrvgvXqeCEHF1NqPUdsylXALFLW8uQPaEMDuSd3Hx1M5CvUaiBxMPdboMQlyVjoUvH8nbMAAsOWSWKUUZGEAFN_yV1m-l8JNh_zUp6zAMJghhLyyzNvUpzqwJ5PmCVEI38M38tX-uQQ0wcFwQVeeEvvuhiERjKj3rFxUBq_Ay707-aalkKbfj8A";

        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj12UOYzjEm+54KGgJRQa7+f5M7o05UJQKMbvGJwiDlaFzUXMaJCq7jpoTSBAbCJOW6cfvVQ7yq8URgHMbiWmMN0uaUVAE9Z827kFJhPFH1Vydd823DMKk1HTJpE4P8eZ8drOz9grVObVdIY57mORXrnvyIKkf8coTSIh95Xj/AnJ5wQ0xOdUvDu7PlYCtzz9gZYbzL/nPglhqtzyiDMcQSIEVYXtuwBI28sqGDdLfuM0HxiH/q40UBdqRifdCsPMZ5dSreepAiDA3buU9cJRCJVPNb73JJeQeLEOvxw8G4PpHICoC7My7NAbXTEll1AawyXOS//QMDQxIkqujb8McQIDAQAB-----END PUBLIC KEY-----";

        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));

        //获取Jwt原始内容
        String claims = jwt.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }
}
