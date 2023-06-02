package org.openecomp.sdc.common.log.interceptors;

import java.io.IOException;
import java.net.URI;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.onap.logging.filter.base.AbstractMetricLogFilter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.elements.LogFieldsMdcHandler;

public class ApacheClientLogRequestInterceptor extends AbstractMetricLogFilter<HttpRequest, HttpResponse, HttpMessage> implements
    HttpRequestInterceptor {

    private String previousInvocationId;

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
    protected void additionalPre(HttpRequest httpRequest, HttpMessage httpMessage) {
        String outgoingInvocationId = httpMessage.getFirstHeader(ONAPLogConstants.Headers.INVOCATION_ID).getValue();
        LogFieldsMdcHandler.getInstance().setOutgoingInvocationId(outgoingInvocationId);
        LogFieldsMdcHandler.getInstance().setKeyInvocationId(previousInvocationId);
    }

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        previousInvocationId = LogFieldsMdcHandler.getInstance().getKeyInvocationId();
        super.pre(httpRequest, httpRequest);
    }
}
