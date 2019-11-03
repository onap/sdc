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

package org.openecomp.sdc.logging.servlet.spring;

import static org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus.*;
import static org.springframework.http.HttpStatus.Series.REDIRECTION;
import static org.springframework.http.HttpStatus.Series.SUCCESSFUL;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.servlet.AuditTracker;
import org.openecomp.sdc.logging.servlet.CombinedTracker;
import org.openecomp.sdc.logging.servlet.ContextTracker;
import org.openecomp.sdc.logging.servlet.HttpHeader;
import org.openecomp.sdc.logging.servlet.RequestProcessingResult;
import org.openecomp.sdc.logging.servlet.Tracker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * <p><b>IMPORTANT</b>: For this interceptor to work, all exceptions must be properly handled before being returned to a
 * client. Any unexpected, automatically handled exception bypasses the interceptor and will not be logged.</p>
 * <p>The interceptor must be either registered in Spring configuration XML as a bean, or programmatically as described
 * in <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-config-interceptors">
 * Spring MVC Config: Interceptors</a>.</p>
 *
 * @author evitaliy
 * @since 02 Aug 2018
 */
@Component
public class LoggingInterceptor extends HandlerInterceptorAdapter {

    static final String LOGGING_TRACKER_KEY = "onap.logging.tracker";

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

    private final HttpHeader partnerNameHeader;
    private final HttpHeader requestIdHeader;

    public LoggingInterceptor(HttpHeader partnerNameHeader, HttpHeader requestIdHeader) {
        this.partnerNameHeader = Objects.requireNonNull(partnerNameHeader);
        this.requestIdHeader = Objects.requireNonNull(requestIdHeader);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Class<?> resourceClass = getResourceType(handler);
        Tracker tracker = new CombinedTracker(
                new ContextTracker(partnerNameHeader, requestIdHeader),
                new AuditTracker(resourceClass));
        request.setAttribute(LOGGING_TRACKER_KEY, tracker);
        tracker.preRequest(request);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {

        Tracker tracker = (Tracker) request.getAttribute(LOGGING_TRACKER_KEY);

        if (tracker == null) {
            LOGGER.debug("No logging tracker received");
            return;
        }

        tracker.postRequest(new ServletResponseResult(response.getStatus()));
    }

    private Class<?> getResourceType(Object handler) {

        if (handler instanceof HandlerMethod) {
            return ((HandlerMethod) handler).getMethod().getDeclaringClass();
        }

        return LoggingInterceptor.class;
    }

    static class ServletResponseResult implements RequestProcessingResult {

        private final StatusInfo statusInfo;

        ServletResponseResult(int status) {
            this.statusInfo = init(status);
        }

        private StatusInfo init(int status) {

            try {
                return new StatusInfo(HttpStatus.valueOf(status));
            } catch (IllegalArgumentException e) {
                return new StatusInfo(status, "Non-standard HTTP status", HttpStatus.Series.valueOf(status));
            }
        }

        @Override
        public int getStatus() {
            return statusInfo.getStatus();
        }

        @Override
        public ResponseStatus getStatusCode() {
            return statusInfo.getStatusCode();
        }

        @Override
        public String getStatusPhrase() {
            return statusInfo.getReasonPhrase();
        }
    }

    private static class StatusInfo {

        private final int status;
        private final String reasonPhrase;
        private final HttpStatus.Series series;

        private StatusInfo(HttpStatus httpStatus) {
            this(httpStatus.value(), httpStatus.getReasonPhrase(), httpStatus.series());
        }

        private StatusInfo(int status, String reasonPhrase, HttpStatus.Series series) {
            this.status = status;
            this.reasonPhrase = reasonPhrase;
            this.series = series;
        }

        private int getStatus() {
            return status;
        }

        private String getReasonPhrase() {
            return reasonPhrase;
        }

        private ResponseStatus getStatusCode() {
            return series.equals(SUCCESSFUL) || series.equals(REDIRECTION) ? COMPLETE : ERROR;
        }
    }
}
