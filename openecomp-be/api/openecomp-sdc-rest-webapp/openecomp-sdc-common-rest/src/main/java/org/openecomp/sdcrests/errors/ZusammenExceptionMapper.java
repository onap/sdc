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
package org.openecomp.sdcrests.errors;

import com.amdocs.zusammen.datatypes.response.Module;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.openecomp.sdc.common.errors.ErrorCodeAndMessage;
import org.openecomp.sdc.common.errors.GeneralErrorBuilder;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class ZusammenExceptionMapper implements ExceptionMapper<SdcRuntimeException> {

    private static final String ZUSAMMEN_DB_PREFIX = Module.ZDB + "-";
    static final String VLM_VSP_VERSION_ID_DOES_NOT_EXISTS =
        ZUSAMMEN_DB_PREFIX + com.amdocs.zusammen.datatypes.response.ErrorCode.ZU_ITEM_VERSION_NOT_EXIST;
    static final String VLM_VSP_ITEM_ID_DOES_NOT_EXISTS =
        ZUSAMMEN_DB_PREFIX + com.amdocs.zusammen.datatypes.response.ErrorCode.ZU_ITEM_DOES_NOT_EXIST;
    static final String SUB_ENTITY_ID_DOES_NOT_EXISTS = ZUSAMMEN_DB_PREFIX + com.amdocs.zusammen.datatypes.response.ErrorCode.ZU_ELEMENT_GET_INFO;
    static final String FAILED_TO_SYNC = ZUSAMMEN_DB_PREFIX + com.amdocs.zusammen.datatypes.response.ErrorCode.ZU_ITEM_VERSION_SYNC;
    static final String FAILED_TO_PUBLISH_OUT_OF_SYNC =
        ZUSAMMEN_DB_PREFIX + com.amdocs.zusammen.datatypes.response.ErrorCode.ZU_ITEM_VERSION_PUBLISH_NOT_ALLOWED;
    private static final Logger LOGGER = LoggerFactory.getLogger(ZusammenExceptionMapper.class);

    @Override
    public Response toResponse(SdcRuntimeException exception) {
        return transform(exception);
    }

    private Response transform(SdcRuntimeException exception) {
        if (Stream.of(VLM_VSP_ITEM_ID_DOES_NOT_EXISTS, VLM_VSP_VERSION_ID_DOES_NOT_EXISTS).anyMatch(exception.getMessage()::contains)) {
            return generateSdcErrorResponse(Messages.ENTITY_NOT_FOUND, Response.Status.NOT_FOUND,
                new SdcRuntimeException(Messages.ENTITY_NOT_FOUND.getErrorMessage(), exception));
        } else if (exception.getMessage().contains(SUB_ENTITY_ID_DOES_NOT_EXISTS)) {
            return generateSdcErrorResponse(Messages.SUB_ENTITY_NOT_FOUND, Response.Status.NOT_FOUND,
                new SdcRuntimeException(Messages.SUB_ENTITY_NOT_FOUND.getErrorMessage(), exception));
        } else if (exception.getMessage().contains(FAILED_TO_SYNC)) {
            return generateSdcErrorResponse(Messages.FAILED_TO_SYNC, Response.Status.EXPECTATION_FAILED,
                new SdcRuntimeException(Messages.FAILED_TO_SYNC.getErrorMessage(), exception));
        } else if (exception.getMessage().contains(FAILED_TO_PUBLISH_OUT_OF_SYNC)) {
            return generateSdcErrorResponse(Messages.FAILED_TO_PUBLISH_OUT_OF_SYNC, Response.Status.EXPECTATION_FAILED,
                new SdcRuntimeException(Messages.FAILED_TO_PUBLISH_OUT_OF_SYNC.getErrorMessage(), exception));
        }
        return genericError(exception);
    }

    private Response generateSdcErrorResponse(Messages messages, Response.Status status, Exception exception) {
        ErrorCode errorCode = new ErrorCode.ErrorCodeBuilder().withId(messages.name()).withMessage(exception.getMessage()).build();
        LOGGER.error(errorCode.message(), exception);
        return Response.status(status).entity(new ErrorCodeAndMessage(status, errorCode)).build();
    }

    private Response genericError(Exception exception) {
        ErrorCode errorCode = new GeneralErrorBuilder().build();
        LOGGER.error(errorCode.message(), exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new ErrorCodeAndMessage(Response.Status.INTERNAL_SERVER_ERROR, errorCode)).build();
    }
}
