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
