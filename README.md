## Spring 微服务实战练习

本项目在本人学习SpringCloud微服务时创建，主要内容包括：
- SpringBoot 及搭建微服务部署环境
    - maven管理项目，并通过插件构建docker镜像
    - docker compose 编排容器
- Spring Cloud Config 配置管理
    - 本地管理
    - Git管理
- Eureka 服务发现
    - Spring Discovery Client 查找服务
    - RestTemplate with Ribbon
    - Netflix Feign
- Hystrix 客户端弹性模式
    - 断路器模式
    - 后备模式
    - 舱壁模式
    - Hystrix 并发策略自定义
- Zuul 服务网关
    - 路由
    - 过滤器
- OAuth2 认证服务 （微服务安全）
- Spring Cloud Stream 事件驱动
- Spring Cloud Sleuth & Zipkin 分布式跟踪

以上功能都以git分支区分，每一个目录一个分支。

> 参考书籍《Spring Microservices IN ACTION》


### 准备
- java环境：jdk8  
- 本项目中的所有微服务都通过maven管理，并通过docker容器部署微服务
    - maven 环境：
        - apache-maven-3.6.3
    - docker 环境
        - 本地 Windows Docker 环境注意：
          需要暴露出本地docker 的守护进程，在setings - General 中勾选：Expose daemon on tcp://localhost:2375 without TLS 选项。 
        - 远程Docker环境
          - 添加系统环境变量：
            DOCKER_HOST=tcp://192.168.195.128:2375
          - 远程机器开放2375/tcp端口，或关闭防火墙
          - 开启远程docker守护进程远程调用：
            ```shell script
            # 修改配置
            vim /lib/systemd/system/docker.service
            # 修改ExecStart一行，其中添加：-H tcp://0.0.0.0:2375
            # ExecStart=/usr/bin/dockerd -H tcp://0.0.0.0:2375 -H fd:// --containerd=/run/containerd/containerd.sock
            
            # 重启Docker服务
            systemctl daemon-reload
            systemctl restart docker
            
            # 测试
            curl http://localhost:22375/version
            # 本地浏览器访问 http://{远程主机IP地址}:22375/version
            ```
        - docker国内镜像配置：
          因为需要从共有镜像库拉取相关镜像，最好配置一个国内镜像地址，提高下载速度。 
          > Linux中，在/etc/docker/daemon.json配置

### 编译项目
- 编译整个项目需要在根目录下执行：
    ```shell script
    mvn clean package docker:build
    ```
    > 如果只想编译单个服务的镜像，需要到指定model的根目录执行

### 启动服务
> 所有指令都要在docker环境中执行

- 通过docker run 启动simple-service
    ```shell script
    docker run -p8080:8080 -d adminhubo/cloud-simple-service:chapter1
    ```
  simple-service测试接口:/hello/{firstName}/{lastName}  
  在浏览器访问：http://{docker容器宿主机}:8080/hello/firstName/lastName

- 通过 Docker Compose 启动服务
    - 需要安装，[参考Docker官网](https://docs.docker.com/compose/install/#install-compose)
    - 上传启动文件到docker容器宿主机
        - Windows本地docker环境不需要上传,镜像构建完成后直接在项目根目录下执行命令即可。
        
        - 远程环境  
          将项目根目录中的docker文件夹上传到docker宿主机
    - 执行命令启动微服务：
        ```shell script
        docker-compose -f docker/common/docker-compose.yml up
        
        # 后台运行加-d
        # docker-compose -f docker/common/docker-compose.yml up -d
        
        # 产看单个服务的启动日志
        # docker logs {容器标识}
        ```

### For the branch of v2.0-spring-cloud-config
#### 新曾模块说明
- config server 服务端
    - configuration-service-git  
      以Git仓库作为存储配置文件的配置中心服务
    - configuration-service-localfile  
      以本地文件系统作为存储配置文件的配置中心服务
- config client 客户端
    - licensing-servic  
      一个证书管理服务，将配置文件交给 config server 管理

#### 注意事项
- 项目以docker容器编排启动
    1. 通过maven编译docker镜像  
       项目根目录下执行：
       ```shell script
       mvn clean package docker:build
       ```
    2. 在容器宿主机上传根目录下单docker文件夹，然后执行命令：
       ```shell script
       docker-compose -f docker/common/docker-compose-chapter2-localfile.yml up -d
       ```
       或者：
       ```shell script
       docker-compose -f docker/common/docker-compose-chapter2-git.yml up -d
       ```
- 在本地idea启动：
1. 下载并安装加密所需要的Oracle JCE jar  
    > jce_policy-8:项目根目录下z_other_jar中提供了相关的jar
    
    将z_other_jar\jce_policy-8\UnlimitedJCEPolicyJDK8中的jar包拷贝到自己%JAVA_HOME%/jre/lib/security下
2. 添加密钥环境变量：
    ENCRYPT_KEY=IMSYMMENTRIC
2. 需要修改配置文件中的数据库连接  
   - 直接修改 config server 服务中licensing-service的配置文件： 
     文件路径：src\main\resources\config\licensingservice\licensingservice.yml：
     修改内容：
       ```text
       spring.datasource.url: "jdbc:postgresql://database:5432/eagle_eye_local" 
       spring.datasource.username: "postgres"
       spring.datasource.password: "{cipher}e3c0ee309724744fd660f83c851bee545558749369d68d30ea91ce9aee5ccc07"
       改为
       spring.datasource.url: "jdbc:postgresql://{自己的postgresql数据库地址}:{端口}/{数据库名称}"
       spring.datasource.username: "{数据库用户名}"
       spring.datasource.password: "{cipher}加密后的密码"
       ```
   - 为系统添加域名主机名映射  
     添加 database 主机名 到 数据库实际地址的映射：  
     例如：在C:\Windows\System32\drivers\etc\host中添加
     ```text
      127.0.0.1 database
     ```
     > 前面时数据库的实际地址，后面时映射的主机名。这样一来，licensing-service从config server读取的配置文件中jdbc:postgresql://database:5432/eagle_eye_local就可以直接映射到指定的数据库服务
   