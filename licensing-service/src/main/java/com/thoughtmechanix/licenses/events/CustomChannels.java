package com.thoughtmechanix.licenses.events;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface CustomChannels {
    /**
     *
     * 使用@Input注解标记一个返回SubscribableChannel类对象的方法：公开一个自定义input通道
     *      nboundOrgChanges是将要公开的自定义通道
     *
     * 使用@OutputChannel注解标记一个返回MessageChannel类对象的方法：公开一个output通道
     *
     */
    @Input("inboundOrgChanges") //定义通道名称,对应配置文件中绑定的通道名
    SubscribableChannel orgs();

}
