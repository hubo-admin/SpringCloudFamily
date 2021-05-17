package com.thoughtmechanix.licenses.controllers;

import com.thoughtmechanix.licenses.services.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName ToolsController
 * @Description: 服务发现测试
 * @Author: b.hu@neusoft.com
 * @Create: 2021-05-17 16:28
 * @Version 1.0
 **/
@RestController
@RequestMapping(value="v1/tools")
public class ToolsController {
    @Autowired
    private DiscoveryService discoveryService;

    @RequestMapping(value="/eureka/services",method = RequestMethod.GET)
    public List<String> getEurekaServices() {

        return discoveryService.getEurekaServices();
    }
}
