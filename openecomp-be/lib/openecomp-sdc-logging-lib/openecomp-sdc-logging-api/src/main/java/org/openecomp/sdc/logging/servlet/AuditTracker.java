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
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.StatusCode;

/**
 * Tracks and logs audit information when a request is being processed. Although thread-safe, an instance of this class
 * must not be reused in order to get accurate results.
 *
 * @author evitaliy
 * @since 31 Jul 2018
 */
public class AuditTracker implements Tracker {

    private final Logger logger;
    private final Function<Integer, Result> statusCodeInterpreter;
    private long started;

    public AuditTracker(Class<?> resourceType, Function<Integer, Result> statusCodeInterpreter) {
        this(LoggerFactory.getLogger(resourceType), statusCodeInterpreter);
    }

    public AuditTracker(Logger logger, Function<Integer, Result> statusCodeInterpreter) {
        this.logger = Objects.requireNonNull(logger);
        this.statusCodeInterpreter = Objects.requireNonNull(statusCodeInterpreter);
    }

    @Override
    public synchronized void preRequest(HttpServletRequest request) {
        started = System.currentTimeMillis();
    }

    @Override
    public synchronized void postRequest(HttpServletRequest request, HttpServletResponse response) {

        if (!logger.isAuditEnabled()) {
            return;
        }

        long end = System.currentTimeMillis();
        int responseCode = response.getStatus();
        Result result = statusCodeInterpreter.apply(responseCode);
        AuditData auditData = AuditData.builder().startTime(started).endTime(end).statusCode(result.getStatus())
                                      .responseCode(Integer.toString(responseCode))
                                      .responseDescription(result.getMessage()).clientIpAddress(request.getRemoteAddr())
                                      .build();
        logger.audit(auditData);
    }

    interface Result {

        StatusCode getStatus();

        String getMessage();
    }
}
