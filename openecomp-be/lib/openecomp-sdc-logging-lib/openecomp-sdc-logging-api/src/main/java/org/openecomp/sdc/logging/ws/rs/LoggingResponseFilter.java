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

import static org.openecomp.sdc.logging.ws.rs.AuditRequestFilter.START_TIME_KEY;
import static org.openecomp.sdc.logging.ws.rs.AuditRequestFilter.CLIENT_IP_ADDRESS;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * @author EVITALIY
 * @since 29 Oct 17
 */
@Provider
public class AuditResponseFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditResponseFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext containerRequestContext,
        ContainerResponseContext containerResponseContext) {

        if (!LOGGER.isAuditEnabled()) {
            return;
        }

        Long start = null;
        try {
            start = Long.class.cast(containerRequestContext.getProperty(START_TIME_KEY));
            if (start == null) {
                LOGGER.error("Cannot instantiate start time - object is null");
            }
        } catch (ClassCastException e) {
            LOGGER.error("Cannot instantiate start time - object is not of type Long.",e);
        }
        long end;
        if (start == null) {
            start = new Long(0);
            end = 0;
        } else {
            end = System.currentTimeMillis();
        }

        Response.StatusType statusInfo = containerResponseContext.getStatusInfo();
        int responseCode = statusInfo.getStatusCode();
        AuditData.StatusCode statusCode;
        if (responseCode == 0) {
            statusCode = AuditData.StatusCode.COMPLETE;
        } else {
            statusCode = AuditData.StatusCode.ERROR;
        }

//                "Endpoint " + containerRequestContext.getUriInfo().getAbsolutePath().getPath() +
//                    " was called with HTTP method " + containerRequestContext.getMethod() +
//                    ". Processed by " + resourceInfo.getResourceClass().getName() +
//                    "." + resourceInfo.getResourceMethod().getName() +
//                    "(). Result " + statusInfo.getReasonPhrase() + "(" + statusInfo.getStatusCode() +
//                    "). Start timestamp: " + start +
//                    ". Processing time: " + (System.currentTimeMillis() - (start == null ? 0 : start)));
        LOGGER.audit(new AuditDataBuilder()
            .withStartTime(start)
            .withEndTime(end)
            .withStatusCode(statusCode)
            .withResponseCode(new Integer(responseCode).toString())
            .withResponseDescription(statusInfo.getReasonPhrase())
            .withClientIpAddress((String) containerRequestContext.getProperty(CLIENT_IP_ADDRESS))
            .build());

        }

    private static class AuditDataBuilder implements AuditData {

        private long start;
        private long end;
        private long elapsed;
        private AuditData.StatusCode statusCode;
        private String responseCode;
        private String responseDescription;
        private String clientIpAddress;

        AuditDataBuilder withStartTime(long start) {
            this.start = start;
            return this;
        }

        AuditDataBuilder withEndTime(long end) {
            this.end = end;
            return this;
        }

        AuditDataBuilder withStatusCode(AuditData.StatusCode statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        AuditDataBuilder withResponseCode(String responseCode) {
            this.responseCode = responseCode;
            return this;
        }

        AuditDataBuilder withResponseDescription(String responseDescription) {
            this.responseDescription = responseDescription;
            return this;
        }

        AuditDataBuilder withClientIpAddress(String clientIpAddress) {
            this.clientIpAddress = clientIpAddress;
            return this;
        }

        public AuditData build() {
            this.elapsed = end - start;
            return this;
        }

        @Override
        public long getStartTime() {
            return start;
        }

        @Override
        public long getEndTime() {
            return end;
        }

        @Override
        public long getElapsedTime() {
            return elapsed;
        }

        @Override
        public AuditData.StatusCode getStatusCode() {
            return statusCode;
        }

        @Override
        public String getResponseCode() {
            return responseCode;
        }

        @Override
        public String getResponseDescription() {
            return responseDescription;
        }

        @Override
        public String getClientIpAddress() {
            return clientIpAddress;
        }

    }
}

