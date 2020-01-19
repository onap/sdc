/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.fe.servlets;

import org.openecomp.sdc.common.servlets.BasicServlet;
import org.openecomp.sdc.fe.impl.LogHandler;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

public abstract class LoggingServlet extends BasicServlet {

    public static final String UUID = "uuid";
    public static final String TRANSACTION_START_TIME = "transactionStartTime";

    /**
     * log incoming requests
     * @param httpRequest the http request
     */
    void logFeRequest(HttpServletRequest httpRequest){
        LogHandler.logFeRequest(httpRequest);
        inHttpRequest(httpRequest);
    }

    /**
     * log response
     * @param request orig request
     * @param response returned response
     */
    void logFeResponse(HttpServletRequest request, Response response) {
        LogHandler.logFeResponse(request);
        outHttpResponse(response);
    }

    /**
     * Extracted for purpose of clear method name, for logback %M parameter
     * @param httpRequest http request
     */
    protected abstract void inHttpRequest(HttpServletRequest httpRequest) ;


    /**
     * Extracted for purpose of clear method name, for logback %M parameter
     * @param response http response
     */
    protected abstract void outHttpResponse(Response response);
}
