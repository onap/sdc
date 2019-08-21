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
package org.openecomp.sdcrests.errors;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.map.JsonMappingException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ErrorCode.ErrorCodeBuilder;

@RunWith(MockitoJUnitRunner.class)
public class DefaultExceptionMapperTest {

    private static final String TEST_MESSAGE = "Test message";

    @Mock
    private ConstraintViolation<String> constraintViolation;
    private PathImpl path = PathImpl.createRootPath();

    @Test
    public void shouldMapCoreExceptionToResponse() {
        DefaultExceptionMapper defaultExceptionMapper = new DefaultExceptionMapper();
        ErrorCode errorCode = new ErrorCodeBuilder().withId("VSP_NOT_FOUND").withCategory(ErrorCategory.APPLICATION).build();
        CoreException exception = new CoreException(errorCode);
        Response response = defaultExceptionMapper.toResponse(exception);
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void shouldMapConstraintViolationExceptionToResponse() {
        Mockito.when(constraintViolation.getPropertyPath()).thenReturn(path);
        DefaultExceptionMapper defaultExceptionMapper = new DefaultExceptionMapper();
        Set<ConstraintViolation<String>> violations = new HashSet<>();
        violations.add(constraintViolation);
        ConstraintViolationException exception = new ConstraintViolationException(TEST_MESSAGE, violations);
        Response response = defaultExceptionMapper.toResponse(exception);
        assertEquals(response.getStatus(), 417);
    }

    @Test
    public void shouldMapJsonMappingExceptionToResponse() {
        DefaultExceptionMapper defaultExceptionMapper = new DefaultExceptionMapper();
        JsonMappingException exception = new JsonMappingException(TEST_MESSAGE);
        Response response = defaultExceptionMapper.toResponse(exception);
        assertEquals(response.getStatus(), 417);
    }

    @Test
    public void shouldMapOtherExceptionToResponse() {
        DefaultExceptionMapper defaultExceptionMapper = new DefaultExceptionMapper();
        Exception exception = new Exception(TEST_MESSAGE);
        Response response = defaultExceptionMapper.toResponse(exception);
        assertEquals(response.getStatus(), 500);
    }
}