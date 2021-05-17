package com.thoughtmechanix.organization;

import com.thoughtmechanix.organization.utils.UserContextFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;

/**
 * @Program: SpringMicroservicesInAction
 * @ClassName OrganizationApplication
 * @Description:
 * @Author: b.hu@neusoft.com
 * @Create: 2021-04-26 19:44
 * @Version 1.0
 **/
@SpringBootApplication
@EnableEurekaClient
public class OrganizationApplication {

    @Bean
    public Filter userContextFilter() {
        UserContextFilter userContextFilter = new UserContextFilter();
        return userContextFilter;
    }

    public static void main(String[] args) {
        SpringApplication.run(OrganizationApplication.class,args);
    }
}
