/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.logging.ws.rs;

import org.openecomp.sdc.logging.api.LoggingContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.util.UUID;

/**
 * @author EVITALIY, KATYR
 * @since 29 Oct 17
 */
@Provider
public class AuditRequestFilter implements ContainerRequestFilter {

    static final String START_TIME_KEY = "audit.start.time";
    static final String CLIENT_IP_ADDRESS = "audit.clientIpAddress";

    @Context
    private HttpServletRequest httpRequest;

    @Context
    private ResourceInfo resourceInfo;

    private String requestIdHeader;
    private String partnerNameHeader;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {

        if (resourceInfo == null) {
            return; // there's no handler for this response
        }

        String requestId = containerRequestContext.getHeaderString(requestIdHeader);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        LoggingContext.putRequestId(requestId);

        String partnerName = containerRequestContext.getHeaderString(partnerNameHeader);
        if (partnerName != null) {
            LoggingContext.putPartnerName(partnerName);
        }

        String serviceName = resourceInfo.getResourceClass().getName() + "." + resourceInfo.getResourceMethod().getName();
        LoggingContext.putServiceName(serviceName);

        containerRequestContext.setProperty(START_TIME_KEY, System.currentTimeMillis());
        containerRequestContext.setProperty(CLIENT_IP_ADDRESS,httpRequest.getRemoteAddr());
    }

    /**
     * Configuration
     */
    public void setRequestIdHeader(String requestIdHeader) {
        this.requestIdHeader = requestIdHeader;
    }

    /**
     * Configuration
     */
    public void setPartnerNameHeader(String partnerNameHeader) {
        this.partnerNameHeader = partnerNameHeader;
    }
}
