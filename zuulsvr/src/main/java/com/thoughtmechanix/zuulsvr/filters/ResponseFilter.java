package com.thoughtmechanix.zuulsvr.filters;


import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 后置过滤器，接收关联 ID
 **/
@Component
public class ResponseFilter extends ZuulFilter{
    private static final int  FILTER_ORDER=1;
    private static final boolean  SHOULD_FILTER=false;
    private static final Logger logger = LoggerFactory.getLogger(ResponseFilter.class);
    
    @Autowired
    FilterUtils filterUtils;

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        //向响应中添加 关联ID
        logger.debug("Adding the correlation id to the outbound headers. {}", filterUtils.getCorrelationId());
        ctx.getResponse().addHeader(
                FilterUtils.CORRELATION_ID,
                filterUtils.getCorrelationId());

        logger.debug("Completing outgoing request for {}.", ctx.getRequest().getRequestURL() + ctx.getRequest().getRequestURI());

        return null;
    }
    @Override
    public String filterType() {
        return FilterUtils.POST_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }
}
