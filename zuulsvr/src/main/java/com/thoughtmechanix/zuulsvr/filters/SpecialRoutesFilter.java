package com.thoughtmechanix.zuulsvr.filters;


import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.thoughtmechanix.zuulsvr.model.AbTestingRoute;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @Description: 路由过滤器
 **/
@Component
public class SpecialRoutesFilter extends ZuulFilter {
    private static final int FILTER_ORDER =  1;
    private static final boolean SHOULD_FILTER =true;

    @Autowired
    FilterUtils filterUtils;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public String filterType() {
        return FilterUtils.ROUTE_FILTER_TYPE;
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
     * 代理服务请求的辅助工具
     */
    private ProxyRequestHelper helper = new ProxyRequestHelper();


    /**
     * 获取请求方式
     * @param request
     * @return
     */
    private String getVerb(HttpServletRequest request) {
        String sMethod = request.getMethod();
        return sMethod.toUpperCase();
    }

    private HttpHost getHttpHost(URL host) {
        HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(),
                host.getProtocol());
        return httpHost;
    }

    private Header[] convertHeaders(MultiValueMap<String, String> headers) {
        List<Header> list = new ArrayList<>();
        for (String name : headers.keySet()) {
            for (String value : headers.get(name)) {
                list.add(new BasicHeader(name, value));
            }
        }
        return list.toArray(new BasicHeader[0]);
    }

    private MultiValueMap<String, String> revertHeaders(Header[] headers) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        for (Header header : headers) {
            String name = header.getName();
            if (!map.containsKey(name)) {
                map.put(name, new ArrayList<String>());
            }
            map.get(name).add(header.getValue());
        }
        return map;
    }

    private InputStream getRequestBody(HttpServletRequest request) {
        InputStream requestEntity = null;
        try {
            requestEntity = request.getInputStream();
        }
        catch (IOException ex) {
            // no requestBody is ok.
        }
        return requestEntity;
    }

    private void setResponse(HttpResponse response) throws IOException {
        this.helper.setResponse(response.getStatusLine().getStatusCode(),
                response.getEntity() == null ? null : response.getEntity().getContent(),
                revertHeaders(response.getAllHeaders()));
    }

    /**
     * 特殊路由过滤器开始
     * @return
     */
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();

        //从 special routes service 服务根据服务名查询特殊路由信息
        AbTestingRoute abTestRoute = getAbRoutingInfo( filterUtils.getServiceId() );

        //如果存在可用的特殊路由选项
        if (abTestRoute!=null && useSpecialRoute(abTestRoute)) {
            //用自带的ribbon路由过滤器去转发
            ctx.set("serviceId","orgservice-new");

            //(这个方法不起作用,依然会走后面的请求)
            //构建路由地址
//            String route = buildRouteString(
//                    ctx.getRequest().getRequestURI(),
//                    abTestRoute.getEndpoint(),
//                    ctx.get("serviceId").toString());
            //转发到特定路由
//            forwardToSpecialRoute(route);
        }

        return null;
    }

    /**
     * 根据服务名称从 special routes service 获取路由信息
     * @param serviceName
     * @return
     */
    private AbTestingRoute getAbRoutingInfo(String serviceName){
        ResponseEntity<AbTestingRoute> restExchange = null;
        try {
            restExchange = restTemplate.exchange(
                    "http://specialroutesservice/v1/route/abtesting/{serviceName}",
                    HttpMethod.GET,
                    null, AbTestingRoute.class, serviceName);
        } catch(HttpClientErrorException ex){
            if (ex.getStatusCode()== HttpStatus.NOT_FOUND){
                return null;
            }
            throw ex;
        }
        return restExchange.getBody();
    }

    /**
     * 构建路由
     * @param oldEndpoint 原路由端点
     * @param newEndpoint 新路由端点，从 special routes service 服务获取
     * @param serviceName 请求服务名称
     * @return
     */
    private String buildRouteString(String oldEndpoint, String newEndpoint, String serviceName){
        int index = oldEndpoint.indexOf(serviceName);

        //获取请求后缀，即接口地址
        String strippedRoute = oldEndpoint.substring(index + serviceName.length());
        System.out.println("Target route: " + String.format("%s/%s", newEndpoint, strippedRoute));
        return String.format("%s/%s", newEndpoint, strippedRoute);
    }

    /**
     * 路由转发
     * @param route
     */
    private void forwardToSpecialRoute(String route) {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        MultiValueMap<String, String> headers = this.helper
                .buildZuulRequestHeaders(request);
        MultiValueMap<String, String> params = this.helper
                .buildZuulRequestQueryParams(request);
        String verb = getVerb(request);
        InputStream requestEntity = getRequestBody(request);
        if (request.getContentLength() < 0) {
            context.setChunkedRequestBody();
        }

        this.helper.addIgnoredHeaders();
        CloseableHttpClient httpClient = null;
        HttpResponse response = null;

        try {
            /**
             * 创建一个 httpClient 向指定服务转发请求，
             * 并获取返回结果 response，然后把结果存到当前响应中，最终响应到客户端
             */
            httpClient  = HttpClients.createDefault();
            response = forward(httpClient, verb, route, request, headers,
                    params, requestEntity);
            setResponse(response);
        } catch (Exception ex ) {
            ex.printStackTrace();

        }
        finally{
            try {
                httpClient.close();
            } catch(IOException ex){}
        }
    }

    /**
     * 转发逻辑
     * @param httpclient http请求客户端
     * @param verb 请求方式
     * @param uri 请求地址
     * @param request
     * @param headers 请求头
     * @param params 请求参数
     * @param requestEntity
     * @return
     * @throws Exception
     */
    private HttpResponse forward(HttpClient httpclient, String verb, String uri,
                                 HttpServletRequest request, MultiValueMap<String, String> headers,
                                 MultiValueMap<String, String> params, InputStream requestEntity)
            throws Exception {
        Map<String, Object> info = this.helper.debug(verb, uri, headers, params,
                requestEntity);
        URL host = new URL( uri );
        HttpHost httpHost = getHttpHost(host);

        HttpRequest httpRequest;
        int contentLength = request.getContentLength();
        InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength,
                request.getContentType() != null
                        ? ContentType.create(request.getContentType()) : null);
        switch (verb.toUpperCase()) {
            case "POST":
                HttpPost httpPost = new HttpPost(uri);
                httpRequest = httpPost;
                httpPost.setEntity(entity);
                break;
            case "PUT":
                HttpPut httpPut = new HttpPut(uri);
                httpRequest = httpPut;
                httpPut.setEntity(entity);
                break;
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uri );
                httpRequest = httpPatch;
                httpPatch.setEntity(entity);
                break;
            default:
                httpRequest = new BasicHttpRequest(verb, uri);

        }
        try {
            httpRequest.setHeaders(convertHeaders(headers));
            HttpResponse zuulResponse = forwardRequest(httpclient, httpHost, httpRequest);

            return zuulResponse;
        }
        finally {
        }
    }

    /**
     * 执行请求转发 返回响应数据 HttpResponse
     * @param httpclient
     * @param httpHost
     * @param httpRequest
     * @return
     * @throws IOException
     */
    private HttpResponse forwardRequest(HttpClient httpclient, HttpHost httpHost,
                                        HttpRequest httpRequest) throws IOException {
        HttpResponse response = httpclient.execute(httpHost, httpRequest);
        return response;
    }

    /**
     * 决定是否替代原服务路由
     *     1.活跃状态 active： Y 允许，N 不允许
     *     2.只有 1/10 的流量会通过新路由
     * @param testRoute
     * @return
     */
    public boolean useSpecialRoute(AbTestingRoute testRoute){
        Random random = new Random();
        //‘N’标识不开启特殊路由，‘Y’表示开启
        if (testRoute.getActive().equals("N")){
            return false;
        }
        //如果开启aptest特殊路由，则有 1/10 的概率将请求转发到特殊路由
        int value = random.nextInt((10 - 1) + 1) + 1;

        if (testRoute.getWeight()<value){
            return true;
        }

        return false;
    }
}
