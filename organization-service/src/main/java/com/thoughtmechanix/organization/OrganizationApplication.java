package com.thoughtmechanix.organization;

import com.thoughtmechanix.organization.utils.UserContextFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import javax.servlet.Filter;

/**
 * @ClassName OrganizationApplication
 * @Description:
 * @Author: b.hu@neusoft.com
 * @Create: 2021-04-26 19:44
 * @Version 1.0
 * @EnableResourceServer 启用资源保护,在网上看到新版本中这个注解启用了,暂时现有这个学习
 *      增加这个注解后,程序会强制执行一个过滤器，拦戔向服务发出的所有调用，
 *      检查在传入调用的 HTTP 头中是否存在 OAuth2 访问令牌
 * @EnableBinding 注解告诉 Spring Cloud Stream 将应用程序绑定到消息代理
 *      Source中定义了一组通道与消息代理通信
 **/
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
@EnableResourceServer
@EnableBinding(Source.class)
public class OrganizationApplication {

    @Bean
    public Filter userContextFilter() {
        UserContextFilter userContextFilter = new UserContextFilter();
        return userContextFilter;
    }

    /**
     * 配置将服务的所有跟踪日志发送到zipkin服务
     *      spring.sleuth.sampler.percentage=1 来事项
     * @return
     */
    @Bean
    public Sampler defaultSampler() {
        return new AlwaysSampler();
    }

    public static void main(String[] args) {
        SpringApplication.run(OrganizationApplication.class,args);
    }
}
