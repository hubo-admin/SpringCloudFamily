package com.thoughtmechanix.organization.events.models;

/**
 *
 * 这是一个 POJO（简单的Java对象），算是消息的模板，消息内容的封装载体
 * @author Administrator
 * */
public class OrganizationChangeModel {

    private String type;
    /**
     * 触发事件的劢作，让消息的消费者获得跟多的上下文，知道该做什么操作
     */
    private String action;

    private String organizationId;
    /**
     * 关联 ID，便于跟踪
     */
    private String correlationId;

    public OrganizationChangeModel(String type, String action, String organizationId, String correlationId) {
        super();
        this.type = type;
        this.action = action;
        this.organizationId = organizationId;
        this.correlationId = correlationId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
