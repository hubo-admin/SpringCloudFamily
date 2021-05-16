package com.thoughtmechanix.confsvr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @Program: simple-service
 * @ClassName ConfigServerApplicationByGit
 * @Description: 配置服务启动类
 * @Author: b.hu@neusoft.com
 * @Create: 2021-05-15 22:37
 * @Version 1.0
 **/
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplicationByGit {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplicationByGit.class,args);
    }
}
