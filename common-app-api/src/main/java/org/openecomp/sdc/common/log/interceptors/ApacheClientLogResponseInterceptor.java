package org.openecomp.sdc.common.log.interceptors;

import org.apache.http.*;
import org.apache.http.protocol.HttpContext;
import org.onap.logging.filter.base.AbstractMetricLogFilter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.elements.LogFieldsMdcHandler;

import java.io.IOException;
import java.net.URI;

public class ApacheClientLogResponseInterceptor extends AbstractMetricLogFilter<HttpRequest, HttpResponse, HttpMessage> implements HttpResponseInterceptor {

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
