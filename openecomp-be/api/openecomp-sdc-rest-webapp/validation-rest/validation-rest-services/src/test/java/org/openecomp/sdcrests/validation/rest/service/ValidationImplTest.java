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

package org.openecomp.sdcrests.validation.rest.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openecomp.sdc.validation.UploadValidationManager;
import org.openecomp.sdc.validation.types.ValidationFileResponse;
import org.openecomp.sdcrests.validation.rest.services.ValidationImpl;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class ValidationImplTest {

    @Mock
    private UploadValidationManager uploadValidationManager;
    @InjectMocks
    private ValidationImpl validation;

    @Before
    public void setUp(){
        openMocks(this);
    }

    @Test
    public void validateFileTest() throws IOException {
        when(uploadValidationManager.validateFile(any(), any())).thenReturn(new ValidationFileResponse());
        Response response = validation.validateFile("", new ByteArrayInputStream("".getBytes()));
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(expected = RuntimeException.class)
    public void validateFileExceptionTest() throws IOException {
        when(uploadValidationManager.validateFile(any(), any())).thenThrow(new IOException());
        Response response = validation.validateFile("", new ByteArrayInputStream("".getBytes()));
   }
}
