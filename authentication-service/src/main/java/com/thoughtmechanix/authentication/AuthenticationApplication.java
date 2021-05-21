package com.thoughtmechanix.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @EnableAuthorizationServer 告诉SpringBoot作为一个 OAuth2 服务
 * @RestController 添加一些基亍 REST 的端点
 **/
@SpringBootApplication
@EnableEurekaClient
@RestController
@EnableResourceServer
@EnableAuthorizationServer
public class AuthenticationApplication {

    /**
     * 映射到 /auth/user 端点,服务配置文件配置了根目录 /auth
     * @param user
     * @return
     */
    @RequestMapping(value = { "/user" }, produces = "application/json")
    public Map<String, Object> user(OAuth2Authentication user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("user", user.getUserAuthentication().getPrincipal());
        userInfo.put("authorities", AuthorityUtils.authorityListToSet(user.getUserAuthentication().getAuthorities()));
        return userInfo;
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationApplication.class,args);
    }
}
