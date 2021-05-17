package com.thoughtmechanix.licenses.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DiscoveryService
 * @Description: 服务发现测四服务类：DiscoveryClient
 * @Author: b.hu@neusoft.com
 * @Create: 2021-05-17 16:30
 * @Version 1.0
 **/
@Service
public class DiscoveryService {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    public List getEurekaServices(){
        List<String> services = new ArrayList<String>();

        discoveryClient.getServices().forEach(serviceName -> {
            discoveryClient.getInstances(serviceName).forEach(instance->{
                services.add( String.format("%s:%s",serviceName,instance.getUri()));
            });
        });

        return services;
    }
}
