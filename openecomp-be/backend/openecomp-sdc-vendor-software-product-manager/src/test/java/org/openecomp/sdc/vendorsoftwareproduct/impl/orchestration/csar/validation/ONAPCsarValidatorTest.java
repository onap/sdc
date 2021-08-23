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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openecomp.sdc.be.test.util.TestResourcesHandler.getResourceBytesOrFail;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.datatypes.error.ErrorMessage;


class ONAPCsarValidatorTest {

    private ONAPCsarValidator onapCsarValidator;
    private FileContentHandler contentHandler;

    @BeforeEach
    void setUp() {
        onapCsarValidator = new ONAPCsarValidator();
        contentHandler = new FileContentHandler();
        contentHandler.addFile("TOSCA-Metadata/TOSCA.meta",
            getResourceBytesOrFail("validation.files/metafile/nonSOL004WithMetaDirectoryCompliantMetaFile.meta"));
        contentHandler
            .addFile("MainServiceTemplate.mf", getResourceBytesOrFail("validation.files/manifest/sampleManifest.mf"));
        contentHandler.addFile(TestConstants.TOSCA_DEFINITION_FILEPATH,
            getResourceBytesOrFail(TestConstants.SAMPLE_DEFINITION_FILE_PATH));
    }

    @Test
    void testGivenCSARPackage_withValidContent_thenNoErrorsReturned() {
        assertExpectedErrors("Valid CSAR Package should have 0 errors",
                onapCsarValidator.validate(contentHandler).getErrors(), 0);
    }

    @Test
    void testGivenCSARPackage_withInvalidManifestFile_thenErrorsReturned() {
        contentHandler = new FileContentHandler();
        contentHandler.addFile("TOSCA-Metadata/TOSCA.meta",
            getResourceBytesOrFail("validation.files/metafile/nonSOL004WithMetaDirectoryCompliantMetaFile.meta"));
        contentHandler
            .addFile("MainServiceTemplate.mf", getResourceBytesOrFail("validation.files/manifest/invalidManifest.mf"));
        contentHandler.addFile(TestConstants.TOSCA_DEFINITION_FILEPATH,
            getResourceBytesOrFail(TestConstants.SAMPLE_DEFINITION_FILE_PATH));

        assertExpectedErrors("CSAR package with invalid manifest file should have errors",
            onapCsarValidator.validate(contentHandler).getErrors(), 1);

    }

    @Test
    void testGivenCSARPackage_withUnwantedFolders_thenErrorsReturned() {
        contentHandler.addFolder("Files/");
        assertExpectedErrors("CSAR package with unwanted folders should fail with errors",
            onapCsarValidator.validate(contentHandler).getErrors(), 1);
    }

    @Test
    void testGivenCSARPackage_withUnwantedFiles_thenErrorsReturned() {
        contentHandler.addFile("ExtraFile.text", "".getBytes());
        assertExpectedErrors("CSAR package with unwanted files should fail with errors",
                onapCsarValidator.validate(contentHandler).getErrors(), 1);
    }

    private void assertExpectedErrors(String testCase, List<ErrorMessage> errorMessages, int expectedErrors) {
        if (expectedErrors > 0) {
            assertEquals(expectedErrors, errorMessages.size(), testCase);
        } else {
            assertTrue(errorMessages.isEmpty(), testCase);
        }
    }
}
