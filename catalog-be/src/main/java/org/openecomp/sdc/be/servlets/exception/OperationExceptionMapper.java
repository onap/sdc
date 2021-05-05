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

package org.openecomp.sdc.be.servlets.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.servlets.builder.ServletResponseBuilder;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component
@Provider
public class OperationExceptionMapper implements ExceptionMapper<OperationException> {

    private final ServletResponseBuilder servletResponseBuilder;
    private final ResponseFormatManager responseFormatManager;

    private static final Logger log = Logger.getLogger(OperationExceptionMapper.class);

    public OperationExceptionMapper(final ServletResponseBuilder servletResponseBuilder) {
        this.servletResponseBuilder = servletResponseBuilder;
        this.responseFormatManager = ResponseFormatManager.getInstance();
    }

    @Override
    public Response toResponse(final OperationException exception) {
        log.debug("Handling OperationException response", exception);
        return servletResponseBuilder.buildErrorResponse(responseFormatManager.getResponseFormat(exception.getActionStatus(), exception.getParams()));
    }
}
