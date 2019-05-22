/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.servlets.exception;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class ComponentExceptionMapper implements ExceptionMapper<ComponentException> {

    private static final Logger log = Logger.getLogger(ComponentExceptionMapper.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ComponentsUtils componentsUtils;

    public ComponentExceptionMapper(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public Response toResponse(ComponentException exception) {
        // TODO log this? BeEcompErrorManager.getInstance().logBeRestApiGeneralError(requestURI);
        log.debug("#toResponse - An error occurred: ", exception);
        ResponseFormat responseFormat = null;
        if (exception instanceof ByActionStatusComponentException) {
            ByActionStatusComponentException byActionStatusComponentException = (ByActionStatusComponentException) exception;
            responseFormat = componentsUtils.getResponseFormat(byActionStatusComponentException.getActionStatus(),
                byActionStatusComponentException.getParams());
        }else if (exception instanceof ByResponseFormatComponentException){
            ByResponseFormatComponentException byResponseFormatComponentException = (ByResponseFormatComponentException) exception;
            responseFormat = byResponseFormatComponentException.getResponseFormat();
        }
        return Response.status(responseFormat.getStatus())
                .entity(gson.toJson(responseFormat.getRequestError()))
                .build();
    }

}
