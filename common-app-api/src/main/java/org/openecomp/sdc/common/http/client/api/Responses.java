package org.openecomp.sdc.common.http.client.api;

import org.apache.http.HttpStatus;

public final class Responses {
    public static final HttpResponse<String> INTERNAL_SERVER_ERROR = new HttpResponse<>("Internal server error", HttpStatus.SC_INTERNAL_SERVER_ERROR);
    
    private Responses() {
    }
}
