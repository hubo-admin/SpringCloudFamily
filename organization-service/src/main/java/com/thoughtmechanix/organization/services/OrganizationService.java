package com.thoughtmechanix.organization.services;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.thoughtmechanix.organization.events.source.SimpleSourceBean;
import com.thoughtmechanix.organization.model.Organization;
import com.thoughtmechanix.organization.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrganizationService {
    @Autowired
    private OrganizationRepository orgRepository;

    @HystrixCommand
    public Organization getOrg(String organizationId) {
        return orgRepository.findById(organizationId);
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
