/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.servlets.builder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Service;

@Service
public class ServletResponseBuilder {

    private final Gson gson;

    public ServletResponseBuilder() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private static final Logger log = Logger.getLogger(ServletResponseBuilder.class);

    public Response buildErrorResponse(final ResponseFormat requestErrorWrapper) {
        return Response.status(requestErrorWrapper.getStatus()).entity(gson.toJson(requestErrorWrapper.getRequestError())).build();
    }

    public Response buildOkResponse(final ResponseFormat errorResponseWrapper, final Object entity) {
        return buildOkResponse(errorResponseWrapper, entity, null);
    }

    public Response buildOkResponse(final ResponseFormat errorResponseWrapper, final Object entity,
                                    final Map<String, String> additionalHeaders) {
        final int status = errorResponseWrapper.getStatus();
        var responseBuilder = Response.status(status);
        if (entity != null) {
            if (log.isTraceEnabled()) {
                log.trace("returned entity is {}", entity.toString());
            }
            responseBuilder = responseBuilder.entity(entity);
        }
        if (MapUtils.isNotEmpty(additionalHeaders)) {
            for (final Entry<String, String> additionalHeader : additionalHeaders.entrySet()) {
                final String headerName = additionalHeader.getKey();
                final String headerValue = additionalHeader.getValue();
                log.trace("Adding header {} with value {} to the response", headerName, headerValue);
                responseBuilder = responseBuilder.header(headerName, headerValue);
            }
        }
        return responseBuilder.build();
    }

}
