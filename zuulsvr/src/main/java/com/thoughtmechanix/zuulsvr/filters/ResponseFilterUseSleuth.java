package com.thoughtmechanix.zuulsvr.filters;


import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;

/**
 * @Description: 后置过滤器，接收关联 ID
 **/
@Component
public class ResponseFilterUseSleuth extends ZuulFilter{
    private static final int  FILTER_ORDER=2;
    private static final boolean  SHOULD_FILTER=true;
    private static final Logger logger = LoggerFactory.getLogger(ResponseFilterUseSleuth.class);
    
    @Autowired
    FilterUtils filterUtils;

    @Autowired
    Tracer tracer;

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        //向响应中添加 关联ID (添加sleuth中的跟踪id)
        ctx.getResponse().addHeader(
                FilterUtils.CORRELATION_ID,
                tracer.getCurrentSpan().traceIdString());

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
