package com.thoughtmechanix.authentication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 服务配置类，可以用来统一管理配置文件中的属性
 **/
@Component
@Configuration
public class ServiceConfig {
    /**
     * 从配置文件中读取
     */
    @Value("${signing.key}")
    private String jwtSigningKey="";

    public String getJwtSigningKey() {
        return jwtSigningKey;
    }
}
