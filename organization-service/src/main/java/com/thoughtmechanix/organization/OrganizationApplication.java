package com.thoughtmechanix.organization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @ClassName OrganizationApplication
 * @Description:
 * @Author: b.hu@neusoft.com
 * @Create: 2021-04-26 19:44
 * @Version 1.0
 **/
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class OrganizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrganizationApplication.class,args);
    }
}
