package com.thoughtmechanix.licenses.clients;

import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.repository.OrganizationRedisRepository;
import com.thoughtmechanix.licenses.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrganizationRestTemplateClient {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationRestTemplateClient.class);

    /**
     * 用来操作跟踪信息
     */
    @Autowired
    Tracer tracer;

    /**
     * 使用原始的RestTemplate
     */
    @Autowired
    RestTemplate restTemplate;

    /**
     * 注入支持OAuth2的REST
     */
//    @Autowired
//    OAuth2RestTemplate restTemplate;

    @Autowired
    OrganizationRedisRepository orgRedisRepo;

    private Organization checkRedisCache(String organizationId) {
        //创建一个新的自定义跨度，传入参数是span的名称
        Span newSpan = tracer.createSpan("readLicensingDataFromRedis");
        try {
            return orgRedisRepo.findOrganization(organizationId);
        }
        catch (Exception ex){
            logger.error("Error encountered while trying to retrieve organization {} check Redis Cache.  Exception {}", organizationId, ex);
            return null;
        }finally {
            //添加标签信息
            newSpan.tag("peer.service","redis");
            //记录一个事件，告诉sleuth捕获调用完成的时间
            newSpan.logEvent(org.springframework.cloud.sleuth.Span.CLIENT_RECV);
            //关闭新建的跨度
            tracer.close(newSpan);
        }
    }

    private void cacheOrganizationObject(Organization org) {
        try {
            orgRedisRepo.saveOrganization(org);
        }catch (Exception ex){
            logger.error("Unable to cache organization {} in Redis. Exception {}", org.getId(), ex);
        }
    }

    public Organization getOrganization(String organizationId){
//        ResponseEntity<Organization> restExchange =
//                restTemplate.exchange(
//                        "http://organizationservice/v1/organizations/{organizationId}",
//                        HttpMethod.GET,
//                        null, Organization.class, organizationId);

        logger.debug(">>> In Licensing Service.getOrganization: {}. Thread Id: {}", UserContextHolder.getContext().getCorrelationId(), Thread.currentThread().getId());

        //从缓存中获取
        Organization org = checkRedisCache(organizationId);
        if (org!=null){
            logger.debug("I have successfully retrieved an organization {} from the redis cache: {}", organizationId, org);
            return org;
        }

        //通过zuul网关代理 （缓存未命中时，通过网关到组织服务查询）
        ResponseEntity<Organization> restExchange =
                restTemplate.exchange(
                        "http://zuulservice/api/organization/v1/organizations/{organizationId}",
                        HttpMethod.GET,
                        null, Organization.class, organizationId);

        /*Save the record from cache*/
        org = restExchange.getBody();
        //从组织服务查询到数据，缓存到Redis
        if (org!=null) {
            cacheOrganizationObject(org);
            logger.debug("save to redis {} : {}", organizationId, org);
        }

        return org;
    }
}
