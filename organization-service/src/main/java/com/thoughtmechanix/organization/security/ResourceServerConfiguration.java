package com.thoughtmechanix.organization.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * @Program: simple-service
 * @ClassName ResourceServerConfiguration
 * @Description: 保护服务资源的配置类
 * @Author: b.hu@neusoft.com
 * @Create: 2021-05-21 16:39
 * @Version 1.0
 **/
@Configuration
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.DELETE,"/v1/organizations/**")
                .hasAnyRole("ADMIN")
                .anyRequest()
                .authenticated();
    }
}
