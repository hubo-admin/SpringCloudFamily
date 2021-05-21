package com.thoughtmechanix.organization.controllers;


import com.thoughtmechanix.organization.model.Organization;
import com.thoughtmechanix.organization.services.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping(value="v1/organizations")
public class OrganizationServiceController {

    @Autowired
    private OrganizationService orgService;

    /**
     * 获取组织信息
     * @param organizationId
     * @return
     */
    @RequestMapping(value="/{organizationId}",method = RequestMethod.GET)
    public Organization getOrganization( @PathVariable("organizationId") String organizationId) {
        Organization org = orgService.getOrg(organizationId);
        org.setContactName("OLD::" + org.getContactName());
        return org;
    }

    /**
     * 更新组织信息
     * @param orgId
     * @param org
     */
    @RequestMapping(value="/{organizationId}",method = RequestMethod.PUT)
    public void updateOrganization( @PathVariable("organizationId") String orgId, @RequestBody Organization org) {
        orgService.updateOrg( org );
    }

    /**
     * 保存组织信息
     * @param org
     */
    @RequestMapping(value="/{organizationId}",method = RequestMethod.POST)
    public void saveOrganization(@RequestBody Organization org) {
       orgService.saveOrg( org );
    }

    /**
     * 删除组织信息
     * @param orgId
     * @param org
     */
    @RequestMapping(value="/{organizationId}",method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT,reason = "204")
    public void deleteOrganization( @PathVariable("organizationId") String orgId,  @RequestBody Organization org) {
        orgService.deleteOrg( org );
    }
}
