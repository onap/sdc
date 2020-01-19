/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.fe.impl;

import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.openecomp.sdc.common.log.elements.LogFieldsMdcHandler;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;

public class LogHandler {
    public static final String UUID = "uuid";
    public static final String TRANSACTION_START_TIME = "transactionStartTime";

    public static void logFeRequest(HttpServletRequest httpRequest) {
        Long transactionStartTime = System.currentTimeMillis();
        String uuid = LogFieldsMdcHandler.getInstance().getKeyRequestId();
        String serviceInstanceID = httpRequest.getHeader(Constants.X_ECOMP_SERVICE_ID_HEADER);

        if (uuid != null && uuid.length() > 0) {
            String userId = httpRequest.getHeader(Constants.USER_ID_HEADER);
            String remoteAddr = httpRequest.getRemoteAddr();
            String localAddr = httpRequest.getLocalAddr();

            httpRequest.setAttribute(UUID,uuid);
            httpRequest.setAttribute(TRANSACTION_START_TIME,transactionStartTime);

            updateMdc(uuid, serviceInstanceID, userId, remoteAddr, localAddr, null);
        }
    }

    public static void logFeResponse(HttpServletRequest request) {
        String uuid = (String)request.getAttribute(UUID);
        String serviceInstanceID = request.getHeader(Constants.X_ECOMP_SERVICE_ID_HEADER);
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        String remoteAddr = request.getRemoteAddr();
        String localAddr = request.getLocalAddr();
        String transactionRoundTime = null;

        if (uuid != null) {
            Long transactionStartTime = (Long)request.getAttribute(TRANSACTION_START_TIME);
            if(transactionStartTime != null){
                transactionRoundTime = Long.toString(System.currentTimeMillis() - transactionStartTime);
            }
            updateMdc(uuid, serviceInstanceID, userId, remoteAddr, localAddr, transactionRoundTime);
        }
    }

    private static void updateMdc(String uuid, String serviceInstanceID, String userId, String remoteAddr, String localAddr, String transactionElapsedTime) {
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, uuid);
        MDC.put(ILogConfiguration.MDC_SERVICE_INSTANCE_ID, serviceInstanceID);
        MDC.put("userId", userId);
        MDC.put("remoteAddr", remoteAddr);
        MDC.put("localAddr", localAddr);
        MDC.put(ILogConfiguration.MDC_ELAPSED_TIME, transactionElapsedTime);
    }
}
