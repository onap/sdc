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
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.GeneralErrorBuilder;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Stream;

@RestControllerAdvice
public class ZusammenExceptionMapper {

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

    @ExceptionHandler(SdcRuntimeException.class)
    public ResponseEntity handleSdcRuntimeException(SdcRuntimeException exception) {
        if (Stream.of(VLM_VSP_ITEM_ID_DOES_NOT_EXISTS, VLM_VSP_VERSION_ID_DOES_NOT_EXISTS)
                .anyMatch(exception.getMessage()::contains)) {
            return generateSdcErrorResponse(Messages.ENTITY_NOT_FOUND, HttpStatus.NOT_FOUND, exception);
        } else if (exception.getMessage().contains(SUB_ENTITY_ID_DOES_NOT_EXISTS)) {
            return generateSdcErrorResponse(Messages.SUB_ENTITY_NOT_FOUND, HttpStatus.NOT_FOUND, exception);
        } else if (exception.getMessage().contains(FAILED_TO_SYNC)) {
            return generateSdcErrorResponse(Messages.FAILED_TO_SYNC, HttpStatus.EXPECTATION_FAILED, exception);
        } else if (exception.getMessage().contains(FAILED_TO_PUBLISH_OUT_OF_SYNC)) {
            return generateSdcErrorResponse(Messages.FAILED_TO_PUBLISH_OUT_OF_SYNC, HttpStatus.EXPECTATION_FAILED, exception);
        }
        return genericError(exception);
    }

    private ResponseEntity generateSdcErrorResponse(Messages messages, HttpStatus status, Exception exception) {
        ErrorCode errorCode = new ErrorCode.ErrorCodeBuilder().withId(messages.name()).withMessage(exception.getMessage()).build();
        LOGGER.error(errorCode.message(), exception);
        return ResponseEntity.status(status).body(new ErrorCodeAndMessage(status, errorCode));
    }

    private ResponseEntity genericError(Exception exception) {
        ErrorCode errorCode = new GeneralErrorBuilder().build();
        LOGGER.error(errorCode.message(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorCodeAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, errorCode));
    }
}
