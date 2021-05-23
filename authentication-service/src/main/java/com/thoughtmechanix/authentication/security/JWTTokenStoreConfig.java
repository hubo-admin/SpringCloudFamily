package com.thoughtmechanix.authentication.security;

import com.thoughtmechanix.authentication.config.ServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**
 * JWT令牌存储配置类
 *      定义如何管理 JWT 令牌的创建、签名、翻译
 **/
@Configuration
public class JWTTokenStoreConfig {
    @Autowired
    private ServiceConfig serviceConfig;

    @Bean
    public TokenStore tokenStore(){
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * @Primary 告诉Spring 如果又多个特定类型的Bean，那么使用被@Primary标注的Bean注入
     *
     * 描述： 返回一个能从令牌中读取数据的服务类实例
     */
    @Bean
    @Primary
    public DefaultTokenServices tokenServices(){
        DefaultTokenServices defaultTokenServices =
                new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }

    /**
     * 描述：定义了令牌如何翻译
     * @return 返回一个在 JWT 和 OAuth2服务器 之间充当翻译功能的翻译器
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(){
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        //设置用于签名的密钥
        converter.setSigningKey(serviceConfig.getJwtSigningKey());
        return converter;
    }

    /**
     * @return 构造出一个对JWT进行增强的Bean
     */
    @Bean
    public TokenEnhancer jwtTokenEnhancer() {
        return new JWTTokenEnhancer();
    }
}
