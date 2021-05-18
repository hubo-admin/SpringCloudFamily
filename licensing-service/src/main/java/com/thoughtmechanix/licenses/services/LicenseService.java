package com.thoughtmechanix.licenses.services;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.thoughtmechanix.licenses.clients.OrganizationDiscoveryClient;
import com.thoughtmechanix.licenses.clients.OrganizationFeignClient;
import com.thoughtmechanix.licenses.clients.OrganizationRestTemplateClient;
import com.thoughtmechanix.licenses.config.ServiceConfig;
import com.thoughtmechanix.licenses.model.License;
import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.repository.LicenseRepository;
import com.thoughtmechanix.licenses.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class LicenseService {

    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    ServiceConfig config;

    @Autowired
    OrganizationFeignClient organizationFeignClient;

    @Autowired
    OrganizationRestTemplateClient organizationRestClient;

    @Autowired
    OrganizationDiscoveryClient organizationDiscoveryClient;

    /**
     * 调用不同的远程方法
     * @param organizationId
     * @param clientType
     * @return
     */
    private Organization retrieveOrgInfo(String organizationId, String clientType){
        Organization organization = null;

        switch (clientType) {
            case "feign":
                System.out.println("I am using the feign client");
                organization = organizationFeignClient.getOrganization(organizationId);
                break;
            case "rest":
                System.out.println("I am using the rest client");
                organization = organizationRestClient.getOrganization(organizationId);
                break;
            case "discovery":
                System.out.println("I am using the discovery client");
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            default:
                organization = organizationRestClient.getOrganization(organizationId);
        }

        return organization;
    }

    /**
     * 利用不同的方式远程调用organization-service获取组织信息
     * @param organizationId
     * @param licenseId
     * @param clientType
     * @return
     */
    public License getLicense(String organizationId,String licenseId, String clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);

        Organization org = retrieveOrgInfo(organizationId, clientType);

        return license
                .withOrganizationName( org.getName())
                .withContactName( org.getContactName())
                .withContactEmail( org.getContactEmail() )
                .withContactPhone( org.getContactPhone() )
                .withComment(config.getExampleProperty());
    }

    /**
     * 获取证书信息
     * @param organizationId
     * @param licenseId
     * @return
     */
    public License getLicense(String organizationId,String licenseId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        //获取机构信息：远程调用其他微服务
        Organization org = getOrganization(organizationId);

        return license
                .withOrganizationName( org.getName() )
                .withContactName( org.getContactName() )
                .withContactEmail( org.getContactEmail() )
                .withContactPhone( org.getContactPhone() )
                .withComment(config.getExampleProperty());
    }

    /**
     * 调用远程组织机构微服务，获取组织机构的详细信息
     * @HystrixCommand 调用其他微服务的方法被断路器包裹
     *      commandProperties 中可以配置 Hystrix 的行为
     *      这里延长了断路器的超时时间，但是不建议在 Hystrix 上增加默认的超时时间，尽量优化程序的执行速度
     * @param organizationId
     * @return
     */
    @HystrixCommand(
        commandProperties = {
            @HystrixProperty(
                    name = "execution.isolation.thread.timeoutInMilliseconds",
                    value = "12000"
            )
        }
    )
    private Organization getOrganization(String organizationId) {
        return organizationRestClient.getOrganization(organizationId);
    }

    /**
     * 增加远程调用执行缓慢的概率
     *     有1/3的概率使得线程睡眠 11秒，这样远程调用的时间变长
     *     Hystrix断路器的默认行为是 1 秒后调用。（即远程调用超过1秒就会快速断路）
     */
    private void randomlyRunLong(){
        Random random = new Random();
        int randonNum = random.nextInt((3-1)+1) + 1;
        if (randonNum == 3){
            try {
                Thread.sleep(11000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Hystrix（一） 断路器包装改方法调用：
     *      这里包装的是 证书服务 ---> 数据库 的远程调用
     *      如果查询时间过长，许可服务将中断对数据库的调用,并抛出异常，返回 Internal Server Error错误，返回状态码 500
     * @param organizationId
     * @return
     */
    @HystrixCommand(
        //Hystrix(二) 后备模式
        fallbackMethod = "buildFallbackLicenseList",
        //Hystrix(三) 舱壁模式，断路器执行线程池隔离
        threadPoolKey = "licensesByOrgThreadPool",//定义线程池唯一名称
        threadPoolProperties = {
            @HystrixProperty(name = "coreSize",value = "30"),//线程池中线程最大数量
            @HystrixProperty(name = "maxQueueSize",value = "10")//请求队列的最大值，超过的请求直接失败
        },
        //配置断路器的行为
        commandProperties = {
            //断路器跳闸之前检查指定时间内调用次数（默认20次）
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "10"),
            //断路器在指定时间内达到指定调用次数，检查错误次数是否达到指定百分比（默认50%）
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "75"),
            //在断路器断路后，允许一个调用通过的睡眠时间，以检查远程服务是否修复
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds",value = "70000"),
            //监视服务调用问题的滑动时间窗口大小（默认10秒）
            @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds",value = "15000"),
            //在滑动窗口中统计信息的次数，也就时将监视窗口分段统计，分段越多监控故障的时间越短（默认10次）
            @HystrixProperty(name = "metrics.rollingStats.numBuckets",value = "5")
        }
    )
    public List<License> getLicensesByOrg(String organizationId){
        //打印请求的关联id
        logger.info("LicenseService.getLicensesByOrg  Correlation id: {}"
                , UserContextHolder.getContext().getCorrelationId());
        randomlyRunLong();
        return licenseRepository.findByOrganizationId( organizationId );
    }

    private List<License> buildFallbackLicenseList(String organizationId){
        List<License> licenseList = new ArrayList<>();
        License license = new License().withId("fallback-001")
                .withOrganizationId(organizationId)
                .withProductName("Sorry no License,this is a demo license!");
        licenseList.add(license);
        return licenseList;
    }

    public void saveLicense(License license){
        license.withId( UUID.randomUUID().toString());

        licenseRepository.save(license);

    }

    public void updateLicense(License license){
      licenseRepository.save(license);
    }

    public void deleteLicense(License license){
        licenseRepository.delete( license.getLicenseId());
    }

}
