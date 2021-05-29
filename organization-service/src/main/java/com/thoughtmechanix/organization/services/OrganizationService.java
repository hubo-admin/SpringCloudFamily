package com.thoughtmechanix.organization.services;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.thoughtmechanix.organization.events.source.SimpleSourceBean;
import com.thoughtmechanix.organization.model.Organization;
import com.thoughtmechanix.organization.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrganizationService {

    /**
     * 用来操作跟踪信息
     */
    @Autowired
    Tracer tracer;

    @Autowired
    private OrganizationRepository orgRepository;

    @HystrixCommand
    public Organization getOrg(String organizationId) {
        Span newSpan = tracer.createSpan("getOrgDBCall");
        logger.debug("In the organizationService.getOrg() call");
        try {
            return orgRepository.findById(organizationId);
        }finally {
            newSpan.tag("peer.service", "postgres");
            newSpan.logEvent(Span.CLIENT_RECV);
            tracer.close(newSpan);
        }
    }

    /**
     * 注入 SimpleSourceBean
     */
    @Autowired
    SimpleSourceBean simpleSourceBean;

    private static final Logger logger = LoggerFactory.getLogger(Organization.class);

    public void saveOrg(Organization org){
        org.setId( UUID.randomUUID().toString());

        orgRepository.save(org);

        //发送消息
        logger.debug("OrganizationService Sending SaveNewOrg id {}",org.getId());
        simpleSourceBean.publishOrgChange("SAVE",org.getId());

    }

    public void updateOrg(Organization org){
        orgRepository.save(org);
        //发送消息
        logger.debug("OrganizationService Sending updateOrg id {}",org.getId());
        simpleSourceBean.publishOrgChange("UPDATE",org.getId());
    }

    public void deleteOrg(Organization org){
        orgRepository.delete( org.getId());
    }
}
