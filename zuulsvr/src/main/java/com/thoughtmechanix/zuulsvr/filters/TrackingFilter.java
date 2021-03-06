package com.thoughtmechanix.zuulsvr.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.thoughtmechanix.zuulsvr.config.ServiceConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 网关前置过滤器 负责生成和设置请求关联 ID 用于跟踪
 **/
@Component
public class TrackingFilter extends ZuulFilter{
    private static final int      FILTER_ORDER =  1;
    private static final boolean  SHOULD_FILTER=true;
    private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);

    @Autowired
    FilterUtils filterUtils;

    @Autowired
    private ServiceConfig serviceConfig;

    public Object run() {

        if (isCorrelationIdPresent()) {
            // 前置过滤器中发现关联ID
            logger.debug("tmx-correlation-id found in tracking filter: {}. ", filterUtils.getCorrelationId());
        }else{
            // 前置过滤器中没有发现关联ID，生成一个新的关联ID
            filterUtils.setCorrelationId(generateCorrelationId());
            logger.debug("tmx-correlation-id generated in tracking filter: {}.", filterUtils.getCorrelationId());
        }

        RequestContext ctx = RequestContext.getCurrentContext();

        //输出从JWT令牌中解析的数据 organizationID
        System.out.println(" ---->>> The organization id from the token is : " + getOrganizationId());
        filterUtils.setOrgId(getOrganizationId());

        //日志输出访问来源地址
        logger.debug("Processing incoming request for {}.",  ctx.getRequest().getRequestURL() + ctx.getRequest().getRequestURI());

        return null;
    }
    @Override
    public String filterType() {
        return FilterUtils.PRE_FILTER_TYPE;
    }
    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }
    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    /**
     * 判断是否有关联ID
     */
    private boolean isCorrelationIdPresent(){
      if (filterUtils.getCorrelationId() !=null){
          return true;
      }
      return false;
    }

    /**
     * 生成新的关联ID
     * @return
     */
    private String generateCorrelationId(){
        return java.util.UUID.randomUUID().toString();
    }

    private String getOrganizationId(){

        String result="";
        if (filterUtils.getAuthToken()!=null){
            // 从 Http获取首部 Authorization 并解析过户临牌部分
            String authToken = filterUtils.getAuthToken()
                    .replace("Bearer ","");
            // 利用密钥和JETS类解析数据
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(serviceConfig.getJwtSigningKey().getBytes("UTF-8"))
                        .parseClaimsJws(authToken).getBody();
                //从数据中提取数据
                result = (String) claims.get("organizationId");
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }
}