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
 package org.openecomp.sdc.validation.impl;

import org.junit.Test;
import org.openecomp.sdc.validation.types.ValidationFileResponse;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

public class UploadValidationManagerImplTest {

    @Test
    public void shouldValidateHeatFile() throws IOException {
        UploadValidationManagerImpl uploadValidationManager = new UploadValidationManagerImpl();

        try (InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream("vfw.zip")) {
            MockMultipartFile multipartFile =
                new MockMultipartFile("file", "vfw.zip", "application/zip", fileStream);

            ValidationFileResponse validationFileResponse = uploadValidationManager.validateFile("heat", multipartFile);
            assertNotNull(validationFileResponse.getValidationData());
        }
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotValidateNonHeatFile() throws IOException {
        UploadValidationManagerImpl uploadValidationManager = new UploadValidationManagerImpl();

        MockMultipartFile invalidFile =
            new MockMultipartFile("file", "test.txt", "text/plain", "test".getBytes());

        uploadValidationManager.validateFile("txt", invalidFile);
    }
}
