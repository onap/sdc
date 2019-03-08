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
            assertTrue(testCase, errorMessages.size() == expectedErrors);
        }else{
            assertTrue(testCase,errors.size() == expectedErrors);
        }
    }
}