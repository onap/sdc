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

package org.openecomp.sdc.logging.api;

/**
 * Builder to populate logging <i>context</i> data, i.e. data that should be available to any log writing event
 * throughout an application. This includes only data known at some point to the application (e.g. at an API call),
 * and not otherwise available to the logging framework (e.g. information about local runtime, machine, etc.).
 *
 * @author evitaliy
 * @since Mar 22, 2018
 */
public class ContextData {

    private final String requestId;
    private final String serviceName;
    private final String partnerName;

    private ContextData(final ContextDataBuilder builder) {
        this.requestId = builder.requestId;
        this.serviceName = builder.serviceName;
        this.partnerName = builder.partnerName;
    }

    /**
     * Uniques request ID received from a calling peer, or created.
     *
     * @return unique identifier of a request
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Service, in the context of which logs will be written.
     *
     * @return a string that identifies an exposed service
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Identifies a peer (if any).
     *
     * @return identification of a calling partner
     */
    public String getPartnerName() {
        return partnerName;
    }

    @Override
    public String toString() {
        return "ContextData{responseCode=" + requestId + ", responseDescription=" + serviceName
                + ", clientIpAddress=" + partnerName + '}';
    }

    public static ContextDataBuilder builder() {
        return new ContextDataBuilder();
    }

    /**
     * Fluent API for building context data.
     */
    public static class ContextDataBuilder {

        private String requestId;
        private String serviceName;
        private String partnerName;

        ContextDataBuilder() { /* package-private default constructor to hide the public one */ }

        /**
         * Unique request ID, most likely propagated via an HTTP header.
         *
         * @param requestId generated or propagated request ID.
         * @return this builder for fluent API
         */
        public ContextDataBuilder requestId(final String requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * Name of a invoked API, by which it can be identified in the application.
         *
         * @param serviceName human-friendly service identifier
         * @return this builder for fluent API
         */
        public ContextDataBuilder serviceName(final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        /**
         * Identifier of a peer calling a service {@link #serviceName(String)}).
         *
         * @param partnerName an string that is received from a calling peer and can identify it
         * @return this builder for fluent API
         */
        public ContextDataBuilder partnerName(final String partnerName) {
            this.partnerName = partnerName;
            return this;
        }

        /**
         * Create an instance of {@link ContextData}.
         *
         * @return a populated instance of audit data
         */
        public ContextData build() {
            return new ContextData(this);
        }
    }
}
