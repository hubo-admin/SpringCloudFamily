package com.thoughtmechanix.organization.events.source;

import com.thoughtmechanix.organization.events.models.OrganizationChangeModel;
import com.thoughtmechanix.organization.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Administrator
 * */
@Component
public class SimpleSourceBean {
    private Source source;
    private static final Logger logger = LoggerFactory.getLogger(SimpleSourceBean.class);

    /**
     * 注入一个 Source接口的实现类实例，供服务使用
     * @param source
     */
    @Autowired
    public SimpleSourceBean(Source source){
        this.source = source;
    }

    /**
     * 发布消息的方法
     * @param action 行为
     * @param orgId 组织机构的ID
     */
    public void publishOrgChange(String action, String orgId){
        logger.debug("Sending Kafka message {} for Organization Id: {}", action, orgId);
        String correlationId = UserContextHolder.getContext().getCorrelationId();
        OrganizationChangeModel changeMessageModel = new OrganizationChangeModel(
                OrganizationChangeModel.class.getTypeName(),
                action,
                orgId,
                correlationId
        );
        logger.debug("Sending Kafka message >>>>>>>>> Correlation Id: {}", correlationId);
        /**
         * Source 类定义的通道的 send()方法发送消息
         *      source.output()返回一个 MessageChannel 类的对象
         */
        source.output().send(MessageBuilder.withPayload(changeMessageModel).build());
    }

}
