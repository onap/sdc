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

import org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus;

/**
 * Builder to populate <i>metrics</i> data. This includes only data known to an application, and not otherwise available
 * to the logging framework.
 *
 * @author evitaliy
 * @since 26 Mar 2018
 */
public class MetricsData {

    // don't inherit from AuditData because it has a very different meaning

    private final long startTime;
    private final long endTime;
    private final ResponseStatus statusCode;
    private final String responseCode;
    private final String responseDescription;
    private final String clientIpAddress;
    private final String targetVirtualEntity;
    private final String targetEntity;

    private MetricsData(final MetricsDataBuilder builder) {
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.statusCode = builder.statusCode;
        this.responseCode = builder.responseCode;
        this.responseDescription = builder.responseDescription;
        this.clientIpAddress = builder.clientIpAddress;
        this.targetEntity = builder.targetEntity;
        this.targetVirtualEntity = builder.targetVirtualEntity;
    }

    /**
     * Begin timestamp of an API invocation.
     *
     * @return timestamp
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * End timestamp of an API invocation.
     *
     * @return timestamp
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Result status of an API invocation.
     *
     * @return protocol and application agnostic status code
     */
    public ResponseStatus getStatusCode() {
        return statusCode;
    }

    /**
     * Application/protocol specific response status of an API invocation.
     *
     * @return response code
     */
    public String getResponseCode() {
        return responseCode;
    }

    /**
     * Application/protocol specific response in a human-friendly way.
     *
     * @return human-friendly response description
     */
    public String getResponseDescription() {
        return responseDescription;
    }

    /**
     * IP address of the invoking client when available.
     *
     * @return IP address
     */
    public String getClientIpAddress() {
        return clientIpAddress;
    }

    /**
     * External entity invoked by the local system.
     *
     * @return identifier of an external entity (system, component, sub-component)
     */
    public String getTargetEntity() {
        return targetEntity;
    }

    /**
     * External API invoked by the local system.
     *
     * @return name of an external API
     */
    public String getTargetVirtualEntity() {
        return targetVirtualEntity;
    }

    @Override
    public String toString() {
        return "AuditData{startTime=" + startTime + ", endTime=" + endTime + ", statusCode=" + statusCode
                + ", responseCode=" + responseCode + ", responseDescription=" + responseDescription
                + ", clientIpAddress=" + clientIpAddress + '}';
    }

    public static MetricsDataBuilder builder() {
        return new MetricsDataBuilder();
    }

    /**
     * Fluent API for building metrics data.
     */
    public static class MetricsDataBuilder {

        private long startTime;
        private long endTime;
        private ResponseStatus statusCode;
        private String responseCode;
        private String responseDescription;
        private String clientIpAddress;
        private String targetEntity;
        private String targetVirtualEntity;

        MetricsDataBuilder() { /* package-private default constructor to hide the public one */ }

        /**
         * Begin timestamp of an activity being audited.
         *
         * @param startTime local timestamp, usually received from {@link System#currentTimeMillis()}
         * @return this builder for fluent API
         */
        public MetricsDataBuilder startTime(final long startTime) {
            this.startTime = startTime;
            return this;
        }

        /**
         * End timestamp of an activity being audited.
         *
         * @param endTime local timestamp, usually received from {@link System#currentTimeMillis()}
         * @return this builder for fluent API
         */
        public MetricsDataBuilder endTime(final long endTime) {
            this.endTime = endTime;
            return this;
        }

        /**
         * Indicate whether an invocation was successful. It is up the the application to decide if a particular result
         * must be treated as a success or a failure.
         *
         * @param statusCode invocation status success/failure
         * @return this builder for fluent API
         */
        public MetricsDataBuilder statusCode(final ResponseStatus statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        /**
         * Application/protocol specific response code. For a Web API, it is likely a standard HTTP response code.
         *
         * @param responseCode response code that depends on application and invocation protocol
         * @return this builder for fluent API
         */
        public MetricsDataBuilder responseCode(final String responseCode) {
            this.responseCode = responseCode;
            return this;
        }

        /**
         * Response description that explains {@link #responseCode(String)} in a human-friendly way. For a Web API, it
         * is likely to be a standard HTTP response phrase.
         *
         * @param responseDescription human-friendly response description
         * @return this builder for fluent API
         */
        public MetricsDataBuilder responseDescription(final String responseDescription) {
            this.responseDescription = responseDescription;
            return this;
        }

        /**
         * IP address of an invoking client.
         *
         * @param clientIpAddress IP address
         * @return this builder for fluent API
         */
        public MetricsDataBuilder clientIpAddress(final String clientIpAddress) {
            this.clientIpAddress = clientIpAddress;
            return this;
        }

        /**
         * External entity at which the operation is invoked.
         *
         * @param targetEntity external entity identifier
         * @return this builder for fluent API
         */
        public MetricsDataBuilder targetEntity(String targetEntity) {
            this.targetEntity = targetEntity;
            return this;
        }

        /**
         * Name of the API or operation activities invoked at the external entity.
         *
         * @param targetVirtualEntity invoked external API
         * @return this builder for fluent API
         */
        public MetricsDataBuilder targetVirtualEntity(String targetVirtualEntity) {
            this.targetVirtualEntity = targetVirtualEntity;
            return this;
        }

        /**
         * Create an instance of {@link MetricsData}.
         *
         * @return a populated instance of audit data
         */
        public MetricsData build() {
            return new MetricsData(this);
        }
    }
}
