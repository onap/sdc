package org.openecomp.sdc.common.log.interceptors;

import java.io.IOException;
import java.net.URI;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.onap.logging.filter.base.AbstractMetricLogFilter;

public class ApacheClientLogResponseInterceptor extends AbstractMetricLogFilter<HttpRequest, HttpResponse, HttpMessage> implements
    HttpResponseInterceptor {

    @Override
    protected void addHeader(HttpMessage httpMessage, String s, String s1) {
        httpMessage.addHeader(s, s1);
    }

    @Override
    protected String getTargetServiceName(HttpRequest httpRequest) {
        return httpRequest.getRequestLine().getUri();
    }

    @Override
    protected String getServiceName(HttpRequest httpRequest) {
        return URI.create(httpRequest.getRequestLine().getUri()).getPath();
    }

    @Override
    protected int getHttpStatusCode(HttpResponse httpResponse) {
        return httpResponse.getStatusLine().getStatusCode();
    }

    @Override
    protected String getResponseCode(HttpResponse httpResponse) {
        return String.valueOf(httpResponse.getStatusLine().getStatusCode());
    }

    @Override
    protected String getTargetEntity(HttpRequest httpRequest) {
        //fallback to default value that provided by AbstractMetricLogFilter
        return null;
    }

    @Override
    public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
        super.post(null, httpResponse);
    }
}
