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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.servlets.BasicServlet;
import org.openecomp.sdc.fe.impl.MdcData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

public abstract class LoggingServlet extends BasicServlet {

    private static final int EXPIRE_DURATION = 10;
    private static final Cache<String, MdcData> mdcDataCache = CacheBuilder.newBuilder().expireAfterWrite(EXPIRE_DURATION, TimeUnit.SECONDS).build();

    /**
     * log incoming requests
     *
     * @param httpRequest the http request
     */
    protected void logFeRequest(HttpServletRequest httpRequest) {

        MDC.clear();

        Long transactionStartTime = System.currentTimeMillis();
        String uuid = httpRequest.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER);
        String serviceInstanceID = httpRequest.getHeader(Constants.X_ECOMP_SERVICE_ID_HEADER);

        if (uuid != null && uuid.length() > 0) {
            String userId = httpRequest.getHeader(Constants.USER_ID_HEADER);

            String remoteAddr = httpRequest.getRemoteAddr();
            String localAddr = httpRequest.getLocalAddr();

            mdcDataCache.put(uuid, new MdcData(serviceInstanceID, userId, remoteAddr, localAddr, transactionStartTime));

            updateMdc(uuid, serviceInstanceID, userId, remoteAddr, localAddr, null);
        }
        inHttpRequest(httpRequest);
    }

    /**
     * log response
     *
     * @param request  orig request
     * @param response returned response
     */
    protected void logFeResponse(HttpServletRequest request, Response response) {
        String uuid = request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER);
        String transactionRoundTime = null;

        if (uuid != null) {
            MdcData mdcData = mdcDataCache.getIfPresent(uuid);
            if (mdcData != null) {
                Long transactionStartTime = mdcData.getTransactionStartTime();
                if (transactionStartTime != null) { // should'n ever be null, but
                    // just to be defensive
                    transactionRoundTime = Long.toString(System.currentTimeMillis() - transactionStartTime);
                }
                updateMdc(uuid, mdcData.getServiceInstanceID(), mdcData.getUserId(), mdcData.getRemoteAddr(), mdcData.getLocalAddr(), transactionRoundTime);
            }
        }
        outHttpResponse(response);

        MDC.clear();
    }

    /**
     * Extracted for purpose of clear method name, for logback %M parameter
     *
     * @param httpRequest http request
     */
    protected abstract void inHttpRequest(HttpServletRequest httpRequest);


    /**
     * Extracted for purpose of clear method name, for logback %M parameter
     *
     * @param response http response
     */
    protected abstract void outHttpResponse(Response response);

    /**
     * update mdc with values from the request
     *
     * @param uuid                 service uuid
     * @param serviceInstanceID    serviceInstanceID
     * @param userId               userId
     * @param remoteAddr           remoteAddr
     * @param localAddr            localAddr
     * @param transactionStartTime transactionStartTime
     */
    private void updateMdc(String uuid, String serviceInstanceID, String userId, String remoteAddr, String localAddr, String transactionStartTime) {
        MDC.put("uuid", uuid);
        MDC.put("serviceInstanceID", serviceInstanceID);
        MDC.put("userId", userId);
        MDC.put("remoteAddr", remoteAddr);
        MDC.put("localAddr", localAddr);
        MDC.put("timer", transactionStartTime);
    }
}
