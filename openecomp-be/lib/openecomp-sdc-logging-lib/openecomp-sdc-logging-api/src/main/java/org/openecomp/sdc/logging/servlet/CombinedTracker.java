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

import javax.servlet.http.HttpServletRequest;
import org.openecomp.sdc.logging.api.Logger;

/**
 * Tracker for all the elements of ONAP logging and tracing at an entry point to an application - context and audit.
 * The order of invocations is important, assuming the context must be kept as long as audit hasn't been finished.
 *
 * @author evitaliy
 * @since 01 Aug 2018
 */
public class CombinedTracker implements Tracker {

    private final ContextTracker context;
    private final AuditTracker audit;

    public CombinedTracker(Logger logger, HttpHeader partnerNameHeader, HttpHeader requestIdHeader) {
        this.context = new ContextTracker(partnerNameHeader, requestIdHeader);
        this.audit = new AuditTracker(logger);
    }

    public CombinedTracker(Class<?> resourceType, HttpHeader partnerNameHeader, HttpHeader requestIdHeader) {
        this.context = new ContextTracker(partnerNameHeader, requestIdHeader);
        this.audit = new AuditTracker(resourceType);
    }

    @Override
    public void preRequest(HttpServletRequest request) {
        this.context.preRequest(request);
        this.audit.preRequest(request);
    }

    @Override
    public void postRequest(RequestProcessingResult result) {
        this.audit.postRequest(result);
        this.context.postRequest(result);
    }
}
