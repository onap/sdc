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
 *//*


package org.openecomp.sdcrests.validation.rest.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openecomp.sdc.validation.UploadValidationManager;
import org.openecomp.sdc.validation.types.ValidationFileResponse;
import org.openecomp.sdcrests.validation.rest.services.ValidationImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
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
        when(validation.validateInputFile(any(), any())).thenReturn(new ValidationFileResponse());
        // Create a MultipartFile (for example, from a byte array)
        MultipartFile multipartFile = new MockMultipartFile("file", "filename.txt", "text/plain", "".getBytes());
        ResponseEntity response = validation.validateFile("", multipartFile);
        assertEquals(response.getStatusCodeValue(), Response.Status.OK.getStatusCode());
    }

    @Test(expected = RuntimeException.class)
    public void validateFileExceptionTest() throws IOException {
        when(validation.validateInputFile(any(), any())).thenThrow(new IOException());
        // Create a MultipartFile (for example, from a byte array)
        MultipartFile multipartFile = new MockMultipartFile("file", "filename.txt", "text/plain", "".getBytes());
        ResponseEntity response = validation.validateFile("", multipartFile);
   }
}
*/
