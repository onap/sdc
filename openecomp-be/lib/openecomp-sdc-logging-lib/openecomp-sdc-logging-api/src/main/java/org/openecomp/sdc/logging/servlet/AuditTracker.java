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

package org.openecomp.sdc.logging.servlet;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import static org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus.COMPLETE;

/**
 * Tracks and logs audit information when a request is being processed. An instance of this class cannot be reused, and
 * the pre- and post-request methods must be called only once.
 *
 * @author evitaliy
 * @since 31 Jul 2018
 */
public class AuditTracker implements Tracker {

    private final Logger logger;
    private volatile long started;
    private volatile String clientIpAddress;

    /**
     * Allows passing a class that will be used to log audit.
     *
     * @param resourceType audit will be logged through the logger of this class
     */
    public AuditTracker(Class<?> resourceType) {
        this.logger = LoggerFactory.getLogger(resourceType);
    }

    /**
     * Allows passing a logger that will be used to log audit.
     *
     * @param logger audit will be logged through this logger, cannot be null
     */
    public AuditTracker(Logger logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    public synchronized void preRequest(HttpServletRequest request) {

        if (this.started > 0) {
            throw new IllegalStateException("Pre-request has been already called");
        }

        this.started = System.currentTimeMillis();
        this.clientIpAddress = request.getRemoteAddr();
        AuditData auditData = AuditData.builder().startTime(started).endTime(started).statusCode(COMPLETE)
                .clientIpAddress(clientIpAddress)
                .build();
        logger.auditEntry(auditData);
    }

    @Override
    public synchronized void postRequest(RequestProcessingResult result) {

        if (this.started == 0) {
            throw new IllegalStateException("Pre-request must be called first");
        }

        if (!logger.isAuditEnabled()) {
            return;
        }

        long end = System.currentTimeMillis();
        AuditData auditData = AuditData.builder().startTime(started).endTime(end).statusCode(result.getStatusCode())
                                      .responseCode(Integer.toString(result.getStatus()))
                                      .responseDescription(result.getStatusPhrase()).clientIpAddress(clientIpAddress)
                                      .build();
        logger.auditExit(auditData);
    }
}
