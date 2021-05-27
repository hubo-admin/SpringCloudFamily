package com.thoughtmechanix.zuulsvr;

import com.thoughtmechanix.zuulsvr.utils.UserContextInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * @EnableZuulServer 这个注解将创建一个Zuul服务
 * 但是不会加载任何Zuul的反向代理过滤器
 * 也不会使用Netflix Eureka服务发现功能
 * 这个注解用于开发人员构建自己特殊的路由服务，比如继承其他服务发现组件
 *
 * @EnableZuulProxy 这个注解会使用Zuul的一些预置功能
 **/
@EnableZuulProxy
@SpringBootApplication
public class ZuulServerApplication {

    @LoadBalanced
    @Bean
    public RestTemplate getRestTemplate(){
        RestTemplate template = new RestTemplate();
        List interceptors = template.getInterceptors();
        if (interceptors == null) {
            template.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
        } else {
            interceptors.add(new UserContextInterceptor());
            template.setInterceptors(interceptors);
        }

        return template;
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
        SpringApplication.run(ZuulServerApplication.class,args);
    }
}
