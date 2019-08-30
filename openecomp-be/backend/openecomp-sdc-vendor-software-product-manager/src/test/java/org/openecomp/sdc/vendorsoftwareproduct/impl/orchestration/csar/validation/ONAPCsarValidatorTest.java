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

import org.junit.Before;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ONAPCsarValidatorTest {

    ONAPCsarValidator onapCsarValidator;
    private FileContentHandler contentHandler;
    private List<String> folderList;

    @Before
    public void setUp() throws IOException{
        onapCsarValidator = new ONAPCsarValidator();
        contentHandler = new FileContentHandler();
        folderList = new ArrayList<>();

        contentHandler.addFile("TOSCA-Metadata/TOSCA.meta", ValidatorUtil.getFileResource("/validation.files/metafile/nonSOL004WithMetaDirectoryCompliantMetaFile.meta"));
        contentHandler.addFile("MainServiceTemplate.mf", ValidatorUtil.getFileResource("/validation.files/manifest/sampleManifest.mf"));
        contentHandler.addFile(TestConstants.TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(TestConstants.SAMPLE_DEFINITION_FILE_PATH));
    }

    @Test
    public void testGivenCSARPackage_withValidContent_thenNoErrorsReturned() {
        assertExpectedErrors("Valid CSAR Package should have 0 errors",
                onapCsarValidator.validateContent(contentHandler, folderList), 0);
    }

    @Test
    public void testGivenCSARPackage_withInvalidManifestFile_thenErrorsReturned() throws IOException{
        contentHandler = new FileContentHandler();
        contentHandler.addFile("TOSCA-Metadata/TOSCA.meta", ValidatorUtil.getFileResource("/validation.files/metafile/nonSOL004WithMetaDirectoryCompliantMetaFile.meta"));
        contentHandler.addFile("MainServiceTemplate.mf", ValidatorUtil.getFileResource("/validation.files/manifest/invalidManifest.mf"));
        contentHandler.addFile(TestConstants.TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(TestConstants.SAMPLE_DEFINITION_FILE_PATH));

        assertExpectedErrors("CSAR package with invalid manifest file should have errors", onapCsarValidator.validateContent(contentHandler, folderList), 1);

    }

    @Test
    public void testGivenCSARPackage_withUnwantedFolders_thenErrorsReturned(){

        folderList.add("Files/");
        assertExpectedErrors("CSAR package with unwanted folders should fail with errors", onapCsarValidator.validateContent(contentHandler, folderList), 1);
    }

    @Test
    public void testGivenCSARPackage_withUnwantedFiles_thenErrorsReturned(){

        contentHandler.addFile("ExtraFile.text", "".getBytes());
        assertExpectedErrors("CSAR package with unwanted files should fail with errors",
                onapCsarValidator.validateContent(contentHandler, folderList), 1);
    }

    private void assertExpectedErrors( String testCase, Map<String, List<ErrorMessage>> errors, int expectedErrors){
        if(expectedErrors > 0){
            List<ErrorMessage> errorMessages = errors.get(SdcCommon.UPLOAD_FILE);
            assertEquals(testCase, expectedErrors, errorMessages.size());
        }else{
            assertEquals(testCase, expectedErrors, errors.size());
        }
    }
}
