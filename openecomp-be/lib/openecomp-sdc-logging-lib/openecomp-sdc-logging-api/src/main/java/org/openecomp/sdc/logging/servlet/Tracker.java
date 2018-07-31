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

/**
 * Tracks logging and tracing information through the processing of an HTTP request.
 *
 * @author evitaliy
 * @since 31 Jul 2018
 */
public interface Tracker {

    /**
     * Will be executed before request processing has started.
     *
     * @param request provided by every Servlet container
     */
    void preRequest(HttpServletRequest request);

    /**
     * Will be executed after a request has been processed.  Results may be treated differently depending on a container
     * and application. For instance, JAX-RS applications may take into account exception mappers before generating a
     * response; some applications may Swagger annotations to map a response status to a human-friendly message, etc.
     *
     * @param result application- and container-specific request results
     */
    void postRequest(RequestProcessingResult result);
}
