/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright © 2026 Deutsche Telekom. All rights reserved.
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

package org.openecomp.sdc.be.servlets.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

    @Override
    public Response toResponse(final WebApplicationException exception) {
        final Response response = exception.getResponse();
        if (Family.familyOf(response.getStatus()) == Family.SERVER_ERROR) {
            LOGGER.error("The request failed with status {}", response.getStatus(), exception);
        } else {
            LOGGER.debug("The request failed with status {}: {}", response.getStatus(), exception.getMessage());
        }
        return response;
    }
}
