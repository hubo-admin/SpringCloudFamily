package com.thoughtmechanix.licenses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @Program: simple-service
 * @ClassName LicenseServiceApplication
 * @Description: license-service 服务启动类
 * @Author: b.hu@neusoft.com
 * @Create: 2021-05-15 21:02
 * @Version 1.0
 **/
@SpringBootApplication
@RefreshScope
public class LicenseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LicenseServiceApplication.class, args);
    }
}
