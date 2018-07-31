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
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private final Supplier<String> serviceNameSupplier;

    public ContextTracker(HttpHeader partnerNameHeaders, HttpHeader requestIdHeaders,
            Supplier<String> serviceNameSupplier) {
        this.partnerNameHeaders = Objects.requireNonNull(partnerNameHeaders);
        this.requestIdHeaders = Objects.requireNonNull(requestIdHeaders);
        this.serviceNameSupplier = Objects.requireNonNull(serviceNameSupplier);
    }

    @Override
    public void preRequest(HttpServletRequest request) {

        LoggingContext.clear();

        String requestId = requestIdHeaders.getAny(request::getHeader).orElse(UUID.randomUUID().toString());
        ContextData.ContextDataBuilder contextBuilder = ContextData.builder()
                                                                .serviceName(serviceNameSupplier.get())
                                                                .requestId(requestId);

        partnerNameHeaders.getAny(request::getHeader).ifPresent(contextBuilder::partnerName);
        LoggingContext.put(contextBuilder.build());
    }

    @Override
    public void postRequest(HttpServletRequest request, HttpServletResponse response) {
        LoggingContext.clear();
    }
}
