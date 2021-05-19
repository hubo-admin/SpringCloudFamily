package com.thoughtmechanix.licenses;

import com.thoughtmechanix.licenses.utils.UserContextInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
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
@RefreshScope
@EnableFeignClients
@EnableCircuitBreaker
public class LicenseServiceApplication {

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

    public static void main(String[] args) {
        SpringApplication.run(LicenseServiceApplication.class, args);
    }
}
