package com.thoughtmechanix.confsvr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * @Program: simple-service
 * @ClassName ConfigServerApplicationByLocalfile
 * @Description:
 * @Author: b.hu@neusoft.com
 * @Create: 2021-05-16 00:45
 * @Version 1.0
 **/
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplicationByLocalfile {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplicationByLocalfile.class,args);
    }
}
