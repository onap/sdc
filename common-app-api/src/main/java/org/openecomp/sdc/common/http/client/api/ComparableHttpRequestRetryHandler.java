package org.openecomp.sdc.common.http.client.api;
import org.apache.http.client.HttpRequestRetryHandler;

public interface ComparableHttpRequestRetryHandler extends HttpRequestRetryHandler {
    public default <T extends HttpRequestRetryHandler> boolean compare(T handler) {
        return (handler != null && getClass() == handler.getClass());
    }
}
