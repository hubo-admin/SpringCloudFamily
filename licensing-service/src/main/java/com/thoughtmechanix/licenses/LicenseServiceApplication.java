package com.thoughtmechanix.licenses;

import com.thoughtmechanix.licenses.config.ServiceConfig;
import com.thoughtmechanix.licenses.events.models.OrganizationChangeModel;
import com.thoughtmechanix.licenses.utils.UserContextInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.client.ClientHttpRequestInterceptor;
//import org.springframework.security.oauth2.client.OAuth2ClientContext;
//import org.springframework.security.oauth2.client.OAuth2RestTemplate;
//import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * @Program: simple-service
 * @ClassName LicenseServiceApplication
 * @Description: license-service 服务启动类
 * @Author: b.hu@neusoft.com
 * @Create: 2021-05-15 21:02
 * @Version 1.0
 **/
/**
 * 启动类
 * @SpringBootApplication
 * @RefreshScope 配置刷新（经测试，不是很好用）
 * @EnableDiscoveryClient 使用DiscoveryClient方式查找服务时使用
 * @EnableFeignClients 使用FeignClients方式查找服务时使用
 *      EnableDiscoveryClient 和 EnableFeignClients 还有后面的 @LoadBalanced 三种方式用一种即可
 * @EnableCircuitBreaker 开启 Hystrix，如果不开启，Hystrix断路器不好处于活跃状态
 */
@SpringBootApplication
@EnableEurekaClient
@EnableBinding(Sink.class)
//@RefreshScope
@EnableFeignClients
@EnableCircuitBreaker
@EnableResourceServer
public class LicenseServiceApplication {
    @Autowired
    private ServiceConfig serviceConfig;

    private static final Logger logger = LoggerFactory.getLogger(LicenseServiceApplication.class);

    /**
     * 支持OAuth2调用的REST模板类实例
     *      在需要远程调用其他受保护的服务时使用，它会自动将头部Authorization信息向下传递
     * @param oAuth2ClientContext
     * @param protectedResourceDetails
     * @return
     */
//    @LoadBalanced
//    @Bean
//    public OAuth2RestTemplate oAuth2RestTemplate(OAuth2ClientContext oAuth2ClientContext,
//            OAuth2ProtectedResourceDetails protectedResourceDetails){
//        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(protectedResourceDetails, oAuth2ClientContext);
//
//        //获取现有的请求拦截器
//        List<ClientHttpRequestInterceptor> interceptors =
//                restTemplate.getInterceptors();
//        //添加自定义拦截器 UserContextInterceptor
//        if (interceptors == null){
//            restTemplate.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
//        } else {
//            interceptors.add(new UserContextInterceptor());
//            restTemplate.setInterceptors(interceptors);
//        }
//
//        return restTemplate;
//    }

    /**
     * 使用带有Ribbon功能的SpringRestTemplate调用服务
     * 通过 @LoadBalanced注解表明这个restTemplate开启负载均衡的功能
     * 通过 @Bean注解 将这个RestTemplate实例交给容器管理,其他需要使用的地方直接注入即可
     * @return
     */
    @LoadBalanced
    @Bean
    public RestTemplate getRestTemplate(){
        RestTemplate template = new RestTemplate();
        //获取现有的请求拦截器
        List<ClientHttpRequestInterceptor> interceptors =
                template.getInterceptors();
        //添加自定义拦截器 UserContextInterceptor
        if (interceptors == null){
            template.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
        } else {
            interceptors.add(new UserContextInterceptor());
            template.setInterceptors(interceptors);
        }

        return template;
    }

    /**
     * 构造一个 JedisConnectionFactory 的 Bean 实例
     *      设置连接Redis服务器的信息（IP 端口）
     * @return
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory(){
        JedisConnectionFactory jedisConnectionFactory =
                new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(serviceConfig.getRedisServer());
        jedisConnectionFactory.setPort(serviceConfig.getRedisPort());
        return jedisConnectionFactory;
    }

    /**
     * 利用 JedisConnectionFactory 创建的连接创建一个 RedisTemplate 实例
     *      可以用RedisTemplate的实例来完成对Redis的操作
     * @return
     */
    @Bean
    public RedisTemplate<String,Object> redisTemplate(){
        RedisTemplate<String,Object> template =
                new RedisTemplate<String,Object>();
        template.setConnectionFactory(jedisConnectionFactory());
        return  template;
    }

    /**
     * 这种写法官方不建议了
     * @param orgChange
     */
//    @StreamListener(Sink.INPUT)
//    public void loggerSink(OrganizationChangeModel orgChange) {
//        logger.debug("Received an event for organization id {}", orgChange.getOrganizationId());
//        System.out.println(orgChange.toString());
//    }

    public static void main(String[] args) {
        SpringApplication.run(LicenseServiceApplication.class, args);
    }
}
