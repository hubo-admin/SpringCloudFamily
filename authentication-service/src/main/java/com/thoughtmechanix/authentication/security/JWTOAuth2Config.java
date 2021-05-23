package com.thoughtmechanix.authentication.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.Arrays;

/**
 * 定义OAuth2服务配置 (替代之前的配置类OAuth2Config)
 **/
@Configuration
public class JWTOAuth2Config extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private DefaultTokenServices tokenServices;

    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired
    private TokenEnhancer jwtTokenEnhancer;


    /**
     * 配置用于服务的验证管理器，并增加了JWT相关组件
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        /**
         * TokenEnhancerChain令牌增强链
         *      可以挂钩多个令牌增强器
         */
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(
                Arrays.asList(jwtTokenEnhancer,jwtAccessTokenConverter));

        endpoints
                //(JWT)
                .tokenStore(tokenStore)
                //(JWT)
                .accessTokenConverter(jwtAccessTokenConverter)
                //(JWT)将令牌增强器挂钩到configure()方法的endpoints参数
                .tokenEnhancer(tokenEnhancerChain)
                //添加验证管理器
                .authenticationManager(authenticationManager)
                //添加详细信息服务实例
                .userDetailsService(userDetailsService);
    }

    /**
     * 定义了哪些客户端将被你的服务注册，存在内存中，实际应用中应该是需要持久化且可动态配置的
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

        clients.inMemory()
                .withClient("eagleeye")
                .secret("thisissecret")
                .authorizedGrantTypes("refresh_token", "password", "client_credentials")
                .scopes("webclient", "mobileclient");
    }
}
