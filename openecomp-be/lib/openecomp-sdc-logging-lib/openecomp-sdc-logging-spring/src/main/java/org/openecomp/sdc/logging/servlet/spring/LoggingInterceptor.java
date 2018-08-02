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

import static org.openecomp.sdc.logging.api.StatusCode.COMPLETE;
import static org.openecomp.sdc.logging.api.StatusCode.ERROR;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.StatusCode;
import org.openecomp.sdc.logging.servlet.CombinedTracker;
import org.openecomp.sdc.logging.servlet.HttpHeader;
import org.openecomp.sdc.logging.servlet.RequestProcessingResult;
import org.openecomp.sdc.logging.servlet.Tracker;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * This interceptor must be either registered in Spring configuration XML as a bean, or programmatically, as described
 * in <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-config-interceptors">
 * Spring MVC Config: Interceptors</a>.
 *
 * @author evitaliy
 * @since 02 Aug 2018
 */
public class LoggingInterceptor extends HandlerInterceptorAdapter {

    private static final String LOGGING_TRACKER_KEY = "onap.logging.tracker";
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
        Tracker tracker = new CombinedTracker(resourceClass, partnerNameHeader, requestIdHeader);
        request.setAttribute(LOGGING_TRACKER_KEY, tracker);
        tracker.preRequest(request);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) {
        afterCompletion(request, response, handler, null);
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

    private static class ServletResponseResult implements RequestProcessingResult {

        private final int status;

        private ServletResponseResult(int status) {
            this.status = status;
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public StatusCode getStatusCode() {
            return status > 199 && status < 400 ? COMPLETE : ERROR;
        }

        @Override
        public String getStatusPhrase() {
            return HttpStatus.valueOf(status).getReasonPhrase();
        }
    }
}