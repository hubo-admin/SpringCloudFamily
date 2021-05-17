package com.thoughtmechanix.eurekasvr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @Program: simple-service
 * @ClassName EurekaServerApplication
 * @Description:
 * @Author: b.hu@neusoft.com
 * @Create: 2021-05-17 10:33
 * @Version 1.0
 **/
@SpringBootApplication
@EnableEurekaServer // 启用eureka服务端
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class,args);
    }
}
