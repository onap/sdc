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
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import org.openecomp.sdc.logging.api.ContextData;
import org.openecomp.sdc.logging.api.LoggingContext;

/**
 * Populates the context before a request is processed, and cleans it after the request has been processed.
 *
 * @author evitaliy
 * @since 31 Jul 2018
 */
public class ContextTracker implements Tracker {

    private final HttpHeader partnerNameHeaders;
    private final HttpHeader requestIdHeaders;
    private final Supplier<Void> loggingContextClear;
    private final Consumer<ContextData> loggingContextPut;

    /**
     * Constructs tracker to handle required logging context in Servlet-based applications. Refer to ONAP logging guidelines for fields required to be
     * put on logging context.
     *
     * @param partnerNameHeaders HTTP headers to check for a partner name, cannot be null
     * @param requestIdHeaders   HTTP headers to check for a request ID, cannot be null
     */
    public ContextTracker(HttpHeader partnerNameHeaders, HttpHeader requestIdHeaders) {
        this.partnerNameHeaders = Objects.requireNonNull(partnerNameHeaders);
        this.requestIdHeaders = Objects.requireNonNull(requestIdHeaders);
        this.loggingContextPut = LoggingContext::put;
        this.loggingContextClear = () -> {
            LoggingContext.clear();
            return null;
        };
    }

    /**
     * Package level constructor used for tests. Clean and Put are passed as functions in order to avoid static mock and service loader config -
     * LoggingServiceProvider in LoggingContext
     *
     * @param partnerNameHeaders
     * @param requestIdHeaders
     * @param loggingContextClear
     * @param loggingContextPut
     */
    ContextTracker(HttpHeader partnerNameHeaders, HttpHeader requestIdHeaders, Supplier<Void> loggingContextClear,
                   Consumer<ContextData> loggingContextPut) {
        this.partnerNameHeaders = Objects.requireNonNull(partnerNameHeaders);
        this.requestIdHeaders = Objects.requireNonNull(requestIdHeaders);
        this.loggingContextPut = loggingContextPut;
        this.loggingContextClear = loggingContextClear;
    }

    @Override
    public void preRequest(HttpServletRequest request) {
        loggingContextClear.get();
        String serviceName = ServiceNameFormatter.format(request);
        String requestId = requestIdHeaders.getAny(request::getHeader).orElse(UUID.randomUUID().toString());
        ContextData.ContextDataBuilder contextBuilder = ContextData.builder().serviceName(serviceName).requestId(requestId);
        String partnerName = partnerNameHeaders.getAny(request::getHeader).orElse("UNKNOWN");
        contextBuilder.partnerName(partnerName);
        loggingContextPut.accept(contextBuilder.build());
    }

    @Override
    public void postRequest(RequestProcessingResult result) {
        loggingContextClear.get();
    }
}
