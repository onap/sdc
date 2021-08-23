/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
 * Modification Copyright (C) 2021 Nokia.
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
package org.onap.sdc.backend.ci.tests.validation.pmdictionary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.exception.OnboardPackageException;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.ValidationResult;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.ValidatorFactory;

class CsarValidationTest {
    
    private final ValidatorFactory validatorFactory = new ValidatorFactory();

    @Test
    void shouldNotReturnErrors_whenPnfCsarIsValid() throws OnboardPackageException, IOException {
        //given
        FileContentHandler pnfFileContent = CsarLoader.load("validPnfCompliantWithSOL004.csar", "/Files/PNFs/validation/pmdictionary/validPnfCompliantWithSOL004.csar");

        //when
        final ValidationResult validationResult = validatorFactory.getValidator(pnfFileContent).validate(pnfFileContent);

        //then
        assertThat(validationResult.getErrors(), is(empty()));
    }

    @Test
    void shouldReturnError_whenPMDictionaryContentIsNotCompliantWithSchema() throws OnboardPackageException, IOException {
        //given
        String expectedErrorMessage = "Document number: 1, Path: /pmMetaData/, Message: Key not found: pmHeader";
        FileContentHandler pnfFileContent = CsarLoader.load("invalidPnfCompliantWithSOL004.csar", "/Files/PNFs/validation/pmdictionary/invalidPnfCompliantWithSOL004.csar");

        //when
        final ValidationResult validationResult = validatorFactory.getValidator(pnfFileContent).validate(pnfFileContent);
        List<ErrorMessage> errorList = validationResult.getErrors();

        //then
        assertThat(errorList, is(not(empty())));
        assertThat(getActualErrorMessages(errorList).get(0), is(equalTo(expectedErrorMessage)));
        assertThat(getActualErrorLevel(errorList), is(ErrorLevel.ERROR));
    }

    @Test
    void shouldNotReturnErrors_whenPnfCsarContainsIndividualSignatureInManifest() throws OnboardPackageException, IOException {
        //given
        FileContentHandler pnfFileContent = CsarLoader.load(
            "validPnfWithIndividualSignatureCompliantWithSOL004.csar",
            "/Files/PNFs/validation/individualSignature/validPnfWithIndividualSignatureCompliantWithSOL004.csar"
        );

        //when
        final ValidationResult validationResult = validatorFactory.getValidator(pnfFileContent).validate(pnfFileContent);

        //then
        assertThat(validationResult.isValid(), is(true));
        assertThat(validationResult.getErrors(), is(empty()));
    }

    @Test
    void shouldReturnErrors_whenPnfCsarContainsIndividualCertificateWithNoSignatureInManifest() throws OnboardPackageException, IOException {
        //given
        List<String> expectedErrorMessage = List.of("Expected 'Signature' entry before 'Certificate' entry;\nAt line 9: 'Certificate: Definitions/pnf_main_descriptor.cert'.");
        FileContentHandler pnfFileContent = CsarLoader.load(
            "invalidPnfWithIndividualSignatureCompliantWithSOL004.csar",
            "/Files/PNFs/validation/individualSignature/invalidPnfWithIndividualSignatureCompliantWithSOL004.csar"
        );

        //when
        final ValidationResult validationResult = validatorFactory.getValidator(pnfFileContent).validate(pnfFileContent);
        List<ErrorMessage> errorList = validationResult.getErrors();

        //then
        assertThat(getActualErrorMessages(errorList), containsInAnyOrder(expectedErrorMessage.toArray()));
        assertThat(getActualErrorLevel(errorList), is(ErrorLevel.ERROR));
    }


    private List<String> getActualErrorMessages(List<ErrorMessage> errorList) {
        return errorList.stream()
            .map((ErrorMessage::getMessage))
            .collect(Collectors.toUnmodifiableList());
    }

    private ErrorLevel getActualErrorLevel(List<ErrorMessage> errorList) {
        return errorList.get(0).getLevel();
    }
}
