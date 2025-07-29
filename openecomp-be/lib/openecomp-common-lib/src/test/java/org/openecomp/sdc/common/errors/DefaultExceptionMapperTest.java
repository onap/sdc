/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

package org.openecomp.sdc.common.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonMappingException;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.common.errors.ErrorCode.ErrorCodeBuilder;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class DefaultExceptionMapperTest {

    private static final String TEST_MESSAGE = "Test message";

    @Mock
    private ConstraintViolation<String> constraintViolation;
    private final PathImpl path = PathImpl.createRootPath();

    @Test
    void shouldMapCoreExceptionToResponse() {
        DefaultExceptionMapper defaultExceptionMapper = new DefaultExceptionMapper();
        ErrorCode errorCode = new ErrorCodeBuilder().withId("VSP_NOT_FOUND").withCategory(ErrorCategory.APPLICATION).build();
        CoreException exception = new CoreException(errorCode);
        ResponseEntity<ErrorCode> response = defaultExceptionMapper.handleCoreException(exception);
            assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void shouldMapConstraintViolationExceptionToResponse() {
        Mockito.when(constraintViolation.getPropertyPath()).thenReturn(path);
        DefaultExceptionMapper defaultExceptionMapper = new DefaultExceptionMapper();
        Set<ConstraintViolation<String>> violations = new HashSet<>();
        violations.add(constraintViolation);
        ConstraintViolationException exception = new ConstraintViolationException(TEST_MESSAGE, violations);
        ResponseEntity<ErrorCode> response = defaultExceptionMapper.handleConstraintViolationException(exception);
            assertEquals(417, response.getStatusCodeValue());
    }

    @Test
    void shouldMapJsonMappingExceptionToResponse() {
        DefaultExceptionMapper defaultExceptionMapper = new DefaultExceptionMapper();
        JsonMappingException exception = new JsonMappingException(TEST_MESSAGE);
        ResponseEntity response = defaultExceptionMapper.handleJsonMappingException(exception);
            assertEquals(417, response.getStatusCodeValue());
    }

    @Test
    void shouldMapOtherExceptionToResponse() {
        DefaultExceptionMapper defaultExceptionMapper = new DefaultExceptionMapper();
        Exception exception = new Exception(TEST_MESSAGE);
        ResponseEntity<ErrorCode> response = defaultExceptionMapper.handleException(exception);
            assertEquals(500, response.getStatusCodeValue());

    }
}
