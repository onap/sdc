package org.openecomp.sdc.common.http.client.api;

import org.apache.commons.lang3.StringUtils;

public class HttpResponse<T> {
    private final T response;
    private final int statusCode;
    private final String description;

    public HttpResponse(T response, int statusCode) {
        this.response = response;
        this.statusCode = statusCode;
        this.description = StringUtils.EMPTY;
    }
    
    public HttpResponse(T response, int statusCode, String description) {
        this.response = response;
        this.statusCode = statusCode;
        this.description = description;
    }

    public T getResponse() {
        return response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HttpResponse [response=");
        builder.append(response);
        builder.append(", statusCode=");
        builder.append(statusCode);
        builder.append(", description=");
        builder.append(description);
        builder.append("]");
        return builder.toString();
    }
    
    
}
