/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
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
package org.onap.sdc.backend.ci.tests.validation.pmdictvalidation;

import org.junit.jupiter.api.Test;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.exception.OnboardPackageException;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.ValidatorFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PnfPackageValidationTest {

    @Test
    public void shouldNotReturnErrors_whenPnfCsarIsValid() throws OnboardPackageException, IOException {
        //given
        FileContentHandler pnfFileContent = CsarLoader.load("validPnfCompliantWithSOL004.csar","/Files/PNFs/validPnfCompliantWithSOL004.csar");

        //when
        Map<String, List<ErrorMessage>> errorsMap = ValidatorFactory.getValidator(pnfFileContent).validateContent(pnfFileContent);

        //then
        assertThat(errorsMap, is(anEmptyMap()));
    }

    @Test
    public void shouldReturnError_whenPMDictionaryContentIsNotCompliantWithSchema() throws OnboardPackageException, IOException {
        //given
        String expectedErrorMessage = "Document number: 1, Path: /pmMetaData/, Message: Key not found: pmHeader";
        FileContentHandler pnfFileContent = CsarLoader.load("invalidPnfCompliantWithSOL004.csar","/Files/PNFs/invalidPnfCompliantWithSOL004.csar");

        //when
        Map<String, List<ErrorMessage>> errorMap = ValidatorFactory.getValidator(pnfFileContent).validateContent(pnfFileContent);
        List<ErrorMessage> errorList = errorMap.get("uploadFile");

        //then
        assertThat(errorList, is(not(empty())));
        assertThat(getActualErrorMessage(errorList), is(equalTo(expectedErrorMessage)));
        assertThat(getActualErrorLevel(errorList), is(ErrorLevel.ERROR));
    }

    private String getActualErrorMessage(List<ErrorMessage> errorList) {
        return errorList.get(0).getMessage();
    }

    private ErrorLevel getActualErrorLevel(List<ErrorMessage> errorList) {
        return errorList.get(0).getLevel();
    }
}
