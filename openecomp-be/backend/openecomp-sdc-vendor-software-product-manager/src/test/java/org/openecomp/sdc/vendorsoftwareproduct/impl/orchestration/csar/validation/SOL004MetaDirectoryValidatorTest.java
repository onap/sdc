package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.*;

public class SOL004MetaDirectoryValidatorTest {

    private SOL004MetaDirectoryValidator sol004MetaDirectoryValidator;
    private FileContentHandler handler;
    private String metaFile;

    @Before
    public void setUp(){
        sol004MetaDirectoryValidator = new SOL004MetaDirectoryValidator();
        handler = new FileContentHandler();
        metaFile =
                "TOSCA-Meta-File-Version: 1.0\n"+
                "CSAR-Version: 1.1\n"+
                "Created-by: Vendor\n"+
                "Entry-Definitions: Definitions/MainServiceTemplate.yaml\n"+
                "Entry-Manifest: Definitions/MainServiceTemplate.mf\n"+
                "Entry-Change-Log: Artifacts/changeLog.text\n";
    }

    @Test
    public void testGivenTOSCAMetaFile_whenEntryHasNoValue_thenErrorIsReturned() throws IOException{

        String  metaFileWithInvalidEntry = "TOSCA-Meta-File-Version: \n" +
                "Entry-Definitions: Definitions/MainServiceTemplate.yaml";

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFileWithInvalidEntry.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(TestConstants.SAMPLE_DEFINITION_FILE_PATH));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        List<ErrorMessage> errorMessages = errors.get(SdcCommon.UPLOAD_FILE);
        assertTrue(errors.size() == 1 && errorMessages.size() == 1);
    }

    @Test
    public void testGivenTOSCAMeta_withAllSupportedEntries_thenNoErrorsReturned() throws IOException{

        String entryTestFilePath = "Files/Tests";
        String entryLicenseFilePath = "Files/Licenses";

        List<String> folderList = new ArrayList<>();
        folderList.add("Files/Tests/");
        folderList.add("Files/Licenses/");

        metaFile = metaFile +
                "Entry-Tests: "+ entryTestFilePath + "\n" +
                "Entry-Licenses: "+ entryLicenseFilePath +"\n";

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_MANIFEST_FILE_PATH));
        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());
        handler.addFile(entryTestFilePath, "".getBytes());
        handler.addFile(entryLicenseFilePath, "".getBytes());

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, folderList);
        assertTrue(errors.size() == 0);
    }

    @Test
    public void testGivenTOSCAMeta_withUnsupportedEntry_thenWarningIsReturned(){

        metaFile = "Entry-Events: Definitions/events.log";

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        List<ErrorMessage> errorMessages = errors.get(SdcCommon.UPLOAD_FILE);
        assertTrue(errors.size() == 1 && errorMessages.size() == 1);
        assertTrue(errorMessages.get(0).getLevel() == ErrorLevel.ERROR);

    }

    @Test
    public void testGivenTOSCAMetaFile_withInvalidTOSCAMetaFileVersionAndCSARVersion_thenErrorIsReturned() throws IOException{

        String metaFile =
                "TOSCA-Meta-File-Version: " + Integer.MAX_VALUE +
                "\nCSAR-Version: " + Integer.MAX_VALUE  +
                "\nCreated-by: Bilal Iqbal" +
                "\nEntry-Definitions: Definitions/MainServiceTemplate.yaml" +
                "\nEntry-Manifest: Definitions/MainServiceTemplate.mf"+
                "\nEntry-Change-Log: Artifacts/changeLog.text";

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(TestConstants.SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_MANIFEST_FILE_PATH));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        List<ErrorMessage> errorMessages = errors.get(SdcCommon.UPLOAD_FILE);
        assertTrue(errors.size() == 1 && errorMessages.size() == 2);
    }

    @Test
    public void testGivenTOSCAMetaFile_withNonExistentFileReferenced_thenErrorsReturned(){

        metaFile = metaFile +
            "Entry-Tests: Files/Tests\n" +
            "Entry-License: Files/Licenses\n";

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        List<ErrorMessage> errorMessages = errors.get(SdcCommon.UPLOAD_FILE);
        assertTrue(errors.size() == 1 && errorMessages.size() == 5);
    }


    @Test
    public void testGivenDefinitionFile_whenValidImportStatementExist_thenNoErrorsReturned() throws IOException{

        String definitionFileWithValidImports = "/validation.files/definition/definitionFileWithValidImports.yaml";


        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_MANIFEST_FILE_PATH));
        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        handler.addFile("Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml", ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(definitionFileWithValidImports));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertTrue(errors.size() == 0);
    }

    @Test
    public void testGivenDefinitionFile_whenMultipleDefinitionsImportStatementExist_thenNoErrorsReturned() throws IOException{

        byte [] sampleDefinitionFile1 = ValidatorUtil.getFileResource("/validation.files/definition/sampleDefinitionFile1.yaml");
        byte [] sampleDefinitionFile2 = ValidatorUtil.getFileResource("/validation.files/definition/sampleDefinitionFile2.yaml");
        byte [] sampleDefinitionFile3 = ValidatorUtil.getFileResource("/validation.files/definition/sampleDefinitionFile3.yaml");

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_MANIFEST_FILE_PATH));
        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, sampleDefinitionFile1);
        handler.addFile("Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml", sampleDefinitionFile2);
        handler.addFile("Definitions/etsi_nfv_sol001_pnfd_2_5_2_types.yaml", sampleDefinitionFile3);

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertTrue(errors.size() == 0);
    }

    @Test
    public void testGivenDefinitionFile_whenInvalidImportStatementExist_thenErrorIsReturned() throws IOException{

        String definitionFileWithInvalidImports = "/validation.files/definition/definitionFileWithInvalidImport.yaml";

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_MANIFEST_FILE_PATH));
        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(definitionFileWithInvalidImports));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        List<ErrorMessage> errorMessages = errors.get(SdcCommon.UPLOAD_FILE);
        assertTrue(errors.size() == 1 && errorMessages.size() == 1);
    }

    @Test
    public void testGivenDefinitionFile_whenReferencedImportDoesNotExist_thenErrorIsReturned() throws IOException{

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_MANIFEST_FILE_PATH));
        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        handler.addFile("Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml", "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource("/validation.files/definition/sampleDefinitionFile2.yaml"));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        List<ErrorMessage> errorMessages = errors.get(SdcCommon.UPLOAD_FILE);
        assertTrue(errors.size() == 1 && errorMessages.size() == 1);

    }

    @Test
    public void testGivenDefinitionFile_withInvalidYAML_thenErrorIsReturned() throws IOException{

        String definitionFileWithInvalidYAML = "/validation.files/definition/invalidDefinitionFile.yaml";

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_MANIFEST_FILE_PATH));
        handler.addFile(SAMPLE_SOURCE, "".getBytes());

        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(definitionFileWithInvalidYAML));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        List<ErrorMessage> errorMessages = errors.get(SdcCommon.UPLOAD_FILE);
        assertTrue(errors.size() == 1 && errorMessages.size() == 1);
    }

    @Test
    public void testGivenManifestFile_withValidSourceAndNonManoSources_thenNoErrorIsReturned() throws IOException{

        String nonManoSource = "Artifacts/Deployment/Measurements/PM_Dictionary.yaml";

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource("/validation.files/manifest/validManifest.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(nonManoSource, "".getBytes());

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertTrue(errors.size() == 0);
    }

    @Test
    public void testGivenManifestFile_withNonExistentSourceFile_thenErrorIsReturned() throws IOException{
        String nonManoSource = "Artifacts/Deployment/Measurements/PM_Dictionary.yaml";

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource("/validation.files/manifest/validManifest.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());
        handler.addFile(nonManoSource, "".getBytes());

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        List<ErrorMessage> errorMessages = errors.get(SdcCommon.UPLOAD_FILE);
        assertTrue(errors.size() == 1 && errorMessages.size() == 1);
    }

    @Test
    public void testGivenManifestFile_withInvalidData_thenErrorIsReturned() throws IOException{

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource("/validation.files/manifest/invalidManifest.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        List<ErrorMessage> errorMessages = errors.get(SdcCommon.UPLOAD_FILE);
        assertTrue(errors.size() == 1 && errorMessages.size() == 1);
    }

    @Test
    public void testGivenManifestAndDefinitionFile_withSameNames_thenNoErrorReturned() throws IOException {

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource("/validation.files/manifest/sampleManifest.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertTrue(errors.size() == 0);
    }

    @Test
    public void testGivenManifestAndMainDefinitionFile_withDifferentNames_thenErrorIsReturned() throws IOException {
        metaFile =
                "TOSCA-Meta-File-Version: 1.0\n"+
                "CSAR-Version: 1.1\n"+
                "Created-by: Vendor\n"+
                "Entry-Definitions: Definitions/MainServiceTemplate.yaml\n"+
                "Entry-Manifest: Definitions/MainServiceTemplate2.mf\n"+
                "Entry-Change-Log: Artifacts/changeLog.text\n";

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile("Definitions/MainServiceTemplate2.mf", ValidatorUtil.getFileResource("/validation.files/manifest/sampleManifest.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertExpectedErrors("Main TOSCA definitions file and Manifest file with different name should return error",
               errors, 1);
    }

    @Test
    public void testGivenManifestFile_withDifferentExtension_thenErrorIsReturned() throws IOException {
        metaFile =
                "TOSCA-Meta-File-Version: 1.0\n"+
                "CSAR-Version: 1.1\n"+
                "Created-by: Vendor\n"+
                "Entry-Definitions: Definitions/MainServiceTemplate.yaml\n"+
                "Entry-Manifest: Definitions/MainServiceTemplate.txt\n"+
                "Entry-Change-Log: Artifacts/changeLog.text\n";

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile("Definitions/MainServiceTemplate.txt", ValidatorUtil.getFileResource("/validation.files/manifest/sampleManifest.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertExpectedErrors("Manifest file with different extension than .mf should return error",
                errors, 1);
    }

    @Test
    public void testGivenManifestFile_withValidVnfMetadata_thenNoErrorsReturned() throws IOException{
        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource("/validation.files/manifest/sampleManifest.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertExpectedErrors("Manifest with valid vnf mandatory values should not return any errors", errors, 0);
    }

    @Test
    public void testGivenManifestFile_withValidPnfMetadata_thenNoErrorsReturned() throws IOException {
        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource("/validation.files/manifest/sampleManifest2.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertExpectedErrors("Manifest with valid pnf mandatory values should not return any errors", errors, 0);
    }

    @Test
    public void testGivenManifestFile_withMetadataContainingMixedPnfVnfMetadata_thenErrorIsReturned() throws IOException {

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource("/validation.files/manifest/manifestInvalidMetadata.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertExpectedErrors("Manifest with mixed metadata should return error", errors, 1);
    }


    @Test
    public void testGivenManifestFile_withMetadataMissingPnfOrVnfMandatoryEntries_thenErrorIsReturned() throws IOException{

        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource("/validation.files/manifest/manifestInvalidMetadata2.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertExpectedErrors("Manifest with missing vnf or pnf mandatory entries should return error", errors, 1);
    }

    @Test
    public void testGivenManifestFile_withMetadataMissingMandatoryPnfEntries_thenErrorIsReturned() throws IOException{
        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource("/validation.files/manifest/manifestInvalidMetadata4.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertExpectedErrors("Manifest with metadata missing pnf mandatory entries should return error", errors, 3);

    }

    @Test
    public void testGivenManifestFile_withMetadataMissingMandatoryVnfEntries_thenErrorIsReturned() throws IOException{
        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource("/validation.files/manifest/manifestInvalidMetadata5.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertExpectedErrors("Manifest with metadata missing vnf mandatory entries should return error", errors, 4);

    }

    @Test
    public void testGivenManifestFile_withMetadataEntriesExceedingTheLimit_thenErrorIsReturned() throws IOException{
        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, ValidatorUtil.getFileResource("/validation.files/manifest/manifestInvalidMetadata3.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, ValidatorUtil.getFileResource(SAMPLE_DEFINITION_FILE_PATH));

        Map<String, List<ErrorMessage>> errors = sol004MetaDirectoryValidator.validateContent(handler, Collections.emptyList());
        assertExpectedErrors("Manifest with more than 4 metadata entries should return error", errors, 2);
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