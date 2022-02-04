/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications copyright (c) 2020 Nokia
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_PM_DICTIONARY;
import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_SW_INFORMATION;
import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_VES_EVENTS;
import static org.openecomp.sdc.be.test.util.TestResourcesHandler.getResourceBytesOrFail;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.PNFD_ARCHIVE_VERSION;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.PNFD_NAME;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.PNFD_RELEASE_DATE_TIME;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.VNF_PRODUCT_NAME;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.VNF_PROVIDER_ID;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.VNF_RELEASE_DATE_TIME;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CREATED_BY_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CSAR_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_CERTIFICATE;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_CHANGE_LOG;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_LICENSES;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_TESTS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.TOSCA_META_FILE_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.EMPTY_YAML_FILE_PATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.INVALID_YAML_FILE_PATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.SAMPLE_DEFINITION_FILE_PATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.SAMPLE_DEFINITION_IMPORT_FILE_PATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.SAMPLE_SOURCE;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_CHANGELOG_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_DEFINITION_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_MANIFEST_FILEPATH;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.tosca.csar.ManifestBuilder;
import org.openecomp.sdc.tosca.csar.ManifestTokenType;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageContentHandler;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManagerException;

class SOL004MetaDirectoryValidatorTest {

    protected SOL004MetaDirectoryValidator sol004MetaDirectoryValidator;
    protected OnboardingPackageContentHandler handler;
    protected StringBuilder metaFileBuilder;

    @BeforeEach
    public void setUp() {
        sol004MetaDirectoryValidator = getSOL004MetaDirectoryValidator();
        handler = new OnboardingPackageContentHandler();
        metaFileBuilder = getMetaFileBuilder();
    }

    protected SOL004MetaDirectoryValidator getSOL004MetaDirectoryValidator() {
        return new SOL004MetaDirectoryValidator();
    }

    protected SOL004MetaDirectoryValidator getSol004WithSecurity(SecurityManager securityManagerMock) {
        return new SOL004MetaDirectoryValidator(securityManagerMock);
    }

    protected StringBuilder getMetaFileBuilder() {
        return new StringBuilder()
        .append(TOSCA_META_FILE_VERSION_ENTRY.getName())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" 1.0").append("\n")
        .append(CSAR_VERSION_ENTRY.getName())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" 1.1").append("\n")
        .append(CREATED_BY_ENTRY.getName())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" Vendor").append("\n")
        .append(ENTRY_DEFINITIONS.getName())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_DEFINITION_FILEPATH).append("\n")
        .append(ETSI_ENTRY_MANIFEST.getName())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_MANIFEST_FILEPATH).append("\n")
        .append(ETSI_ENTRY_CHANGE_LOG.getName())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_CHANGELOG_FILEPATH).append("\n");
    }

    protected ManifestBuilder getPnfManifestSampleBuilder() {
        return new ManifestBuilder()
            .withMetaData(PNFD_NAME.getToken(), "myPnf")
            .withMetaData(ManifestTokenType.PNFD_PROVIDER.getToken(), "ACME")
            .withMetaData(PNFD_ARCHIVE_VERSION.getToken(), "1.0")
            .withMetaData(PNFD_RELEASE_DATE_TIME.getToken(), "2019-03-11T11:25:00+00:00");
    }

    protected ManifestBuilder getVnfManifestSampleBuilder() {
        return new ManifestBuilder()
            .withMetaData(ManifestTokenType.VNF_PRODUCT_NAME.getToken(), "RadioNode")
            .withMetaData(ManifestTokenType.VNF_PROVIDER_ID.getToken(), "ACME")
            .withMetaData(ManifestTokenType.VNF_PACKAGE_VERSION.getToken(), "1.0")
            .withMetaData(ManifestTokenType.VNF_RELEASE_DATE_TIME.getToken(), "2019-03-11T11:25:00+00:00");
    }

    /**
     * ETSI Version 2.7.1 below contains one Definition File reference
     * ETSI Version 2.7.1 onwards contains two possible Definition File reference
     */
    protected int getManifestDefinitionErrorCount() {
        return 1;
    }

    @Test
    void testGivenTOSCAMetaFile_whenEntryHasNoValue_thenErrorIsReturned() {
        final String metaFileWithInvalidEntry = "TOSCA-Meta-File-Version: \n" +
                "Entry-Definitions: " + TOSCA_DEFINITION_FILEPATH;

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileWithInvalidEntry.getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("TOSCA Meta file with no entries", validationResult.getErrors(), 1);
    }

    @Test
    void testGivenTOSCAMeta_withAllSupportedEntries_thenNoErrorsReturned() {

        final String entryTestFilePath = "Files/Tests";
        final String entryLicenseFilePath = "Files/Licenses";

        handler.addFolder("Files/Tests/");
        handler.addFolder("Files/Licenses/");
        metaFileBuilder
                .append(ETSI_ENTRY_TESTS.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(entryTestFilePath).append("\n")
                .append(ETSI_ENTRY_LICENSES.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(entryLicenseFilePath).append("\n");

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));

        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());
        handler.addFile(entryTestFilePath, "".getBytes());
        handler.addFile(entryLicenseFilePath, "".getBytes());

        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder()
                .withSource(TOSCA_META_PATH_FILE_NAME)
                .withSource(TOSCA_DEFINITION_FILEPATH)
                .withSource(TOSCA_CHANGELOG_FILEPATH)
                .withSource(TOSCA_MANIFEST_FILEPATH).withSource(SAMPLE_SOURCE)
                .withSource(SAMPLE_DEFINITION_IMPORT_FILE_PATH)
                .withSource(entryTestFilePath)
                .withSource(entryLicenseFilePath);

        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);

        assertTrue(validationResult.getErrors().isEmpty());
    }

    @Test
    void testGivenTOSCAMeta_withUnsupportedEntry_thenNoErrorIsReturned() {
        metaFileBuilder
                .append("a-unknown-entry")
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ")
                .append("Definitions/events.log");

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder()
                .withSource(TOSCA_META_PATH_FILE_NAME)
                .withSource(TOSCA_DEFINITION_FILEPATH)
                .withSource(TOSCA_CHANGELOG_FILEPATH)
                .withSource(TOSCA_MANIFEST_FILEPATH);

        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));
        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertThat("Validation should produce no errors", validationResult.getErrors(), is(empty()));
    }

    /**
     * Tests if the meta file contains invalid versions in TOSCA-Meta-File-Version and CSAR-Version attributes.
     */
    @Test
    void testGivenTOSCAMetaFile_withInvalidTOSCAMetaFileVersionAndCSARVersion_thenErrorIsReturned() {
        final StringBuilder metaFileBuilder = new StringBuilder()
                .append(TOSCA_META_FILE_VERSION_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(Integer.MAX_VALUE).append("\n")
                .append(CSAR_VERSION_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(Integer.MAX_VALUE).append("\n")
                .append(CREATED_BY_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" Vendor").append("\n")
                .append(ENTRY_DEFINITIONS.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_DEFINITION_FILEPATH).append("\n")
                .append(ETSI_ENTRY_MANIFEST.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_MANIFEST_FILEPATH).append("\n")
                .append(ETSI_ENTRY_CHANGE_LOG.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_CHANGELOG_FILEPATH);
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Invalid TOSCA-Meta-File-Version and CSAR-Version attributes", validationResult.getErrors(), 2);
    }

    @Test
    void testGivenTOSCAMetaFile_withNonExistentFileReferenced_thenErrorsReturned() {
        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        final List<ErrorMessage> errors = validationResult.getErrors();
        assertThat("Total of errors messages should be as expected", errors.size(), is((2 + getManifestDefinitionErrorCount())));
    }


    @Test
    void testGivenDefinitionFile_whenValidImportStatementExist_thenNoErrorsReturned() {
        final ManifestBuilder manifestBuilder = getPnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        manifestBuilder.withSource(SAMPLE_SOURCE);

        handler.addFile("Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
                getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource("Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml");

        final String definitionFileWithValidImports = "validation.files/definition/definitionFileWithValidImports.yaml";
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(definitionFileWithValidImports));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertEquals(0, validationResult.getErrors().size());
    }

    @Test
    void testGivenDefinitionFile_whenMultipleDefinitionsImportStatementExist_thenNoErrorsReturned() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        manifestBuilder.withSource(SAMPLE_SOURCE);

        final byte[] sampleDefinitionFile1 =
                getResourceBytesOrFail("validation.files/definition/sampleDefinitionFile1.yaml");
        handler.addFile(TOSCA_DEFINITION_FILEPATH, sampleDefinitionFile1);
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        final byte[] sampleDefinitionFile2 =
                getResourceBytesOrFail("validation.files/definition/sampleDefinitionFile2.yaml");
        handler.addFile("Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml", sampleDefinitionFile2);
        manifestBuilder.withSource("Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml");

        final byte[] sampleDefinitionFile3 =
                getResourceBytesOrFail("validation.files/definition/sampleDefinitionFile1.yaml");
        handler.addFile("Definitions/etsi_nfv_sol001_pnfd_2_5_2_types.yaml", sampleDefinitionFile3);
        manifestBuilder.withSource("Definitions/etsi_nfv_sol001_pnfd_2_5_2_types.yaml");

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getErrors().isEmpty());
    }

    @Test
    void testGivenDefinitionFile_whenInvalidImportStatementExist_thenErrorIsReturned() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        manifestBuilder.withSource(SAMPLE_SOURCE);

        final String definitionFileWithInvalidImports =
                "validation.files/definition/definitionFileWithInvalidImport.yaml";
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(definitionFileWithInvalidImports));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        String manifest = manifestBuilder.build();
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifest.getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("", validationResult.getErrors(), getManifestDefinitionErrorCount());
    }

    /**
     * Manifest referenced import file missing
     */
    @Test
    void testGivenDefinitionFile_whenReferencedImportDoesNotExist_thenErrorIsReturned() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        manifestBuilder.withSource(SAMPLE_SOURCE);

        handler.addFile("Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml", "".getBytes());
        manifestBuilder.withSource("Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml");

        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);
        handler.addFile(TOSCA_DEFINITION_FILEPATH,
                getResourceBytesOrFail("validation.files/definition/sampleDefinitionFile2.yaml"));

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Manifest referenced import file missing", validationResult.getErrors(), getManifestDefinitionErrorCount());
    }

    @Test
    void testGivenDefinitionFile_whenFileInPackageNotInManifest_thenErrorIsReturned() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(SAMPLE_SOURCE, "".getBytes());

        final byte[] sampleDefinitionFile =
                getResourceBytesOrFail("validation.files/definition/sampleDefinitionFile2.yaml");
        handler.addFile("Definitions/etsi_nfv_sol001_pnfd_2_5_2_types.yaml", sampleDefinitionFile);
        manifestBuilder.withSource("Definitions/etsi_nfv_sol001_pnfd_2_5_2_types.yaml");

        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);
        handler.addFile(TOSCA_DEFINITION_FILEPATH,
                getResourceBytesOrFail("validation.files/definition/sampleDefinitionFile2.yaml"));

        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Artifact is not being referenced in manifest file", validationResult.getErrors(), 1);
    }

    @Test
    void testGivenDefinitionFile_whenManifestNotreferencedInManifest_thenNoErrorIsReturned() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        manifestBuilder.withSource(SAMPLE_SOURCE);

        final byte[] sampleDefinitionFile =
                getResourceBytesOrFail("validation.files/definition/sampleDefinitionFile2.yaml");
        handler.addFile("Definitions/etsi_nfv_sol001_pnfd_2_5_2_types.yaml", sampleDefinitionFile);
        manifestBuilder.withSource("Definitions/etsi_nfv_sol001_pnfd_2_5_2_types.yaml");

        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);
        handler.addFile(TOSCA_DEFINITION_FILEPATH,
                getResourceBytesOrFail("validation.files/definition/sampleDefinitionFile2.yaml"));

        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getErrors().isEmpty());
    }

    /**
     * Reference with invalid YAML format.
     */
    @Test
    void testGivenDefinitionFile_withInvalidYAML_thenErrorIsReturned() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        manifestBuilder.withSource(SAMPLE_SOURCE);

        final String definitionFileWithInvalidYAML = "validation.files/definition/invalidDefinitionFile.yaml";
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(definitionFileWithInvalidYAML));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Reference with invalid YAML format", validationResult.getErrors(), getManifestDefinitionErrorCount());
    }

    @Test
    void testGivenManifestFile_withValidSourceAndNonManoSources_thenNoErrorIsReturned() throws IOException {
        final ManifestBuilder manifestBuilder = getPnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        manifestBuilder.withSource(SAMPLE_SOURCE);

        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(SAMPLE_DEFINITION_IMPORT_FILE_PATH);

        final String nonManoSource = "Artifacts/Deployment/Measurements/PM_Dictionary.yaml";
        handler.addFile(nonManoSource, getResourceBytesOrFail("validation.files/measurements/pmEvents-valid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoSource);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getErrors().isEmpty());
    }

    /**
     * Manifest with non existent source files should return error.
     */
    @Test
    void testGivenManifestFile_withNonExistentSourceFile_thenErrorIsReturned() throws IOException {
        final ManifestBuilder manifestBuilder = getPnfManifestSampleBuilder();
        //non existent reference
        manifestBuilder.withSource("Artifacts/Deployment/Events/RadioNode_pnf_v1.yaml");

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());
        manifestBuilder.withSource(SAMPLE_DEFINITION_IMPORT_FILE_PATH);

        String nonManoSource = "Artifacts/Deployment/Measurements/PM_Dictionary.yaml";
        handler.addFile(nonManoSource, getResourceBytesOrFail("validation.files/measurements/pmEvents-valid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoSource);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Manifest with non existent source files", validationResult.getErrors(), 1);
    }

    /**
     * Tests the validation for a TOSCA Manifest with invalid data.
     */
    @Test
    void testGivenManifestFile_withInvalidData_thenErrorIsReturned() {
        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, getResourceBytesOrFail("validation.files/manifest/invalidManifest.mf"));
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("TOSCA manifest with invalid data", validationResult.getErrors(), 1);
    }

    @Test
    void testGivenManifestAndDefinitionFile_withSameNames_thenNoErrorReturned()  {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());
        manifestBuilder.withSource(SAMPLE_DEFINITION_IMPORT_FILE_PATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getErrors().isEmpty());
    }

    /**
     * Main TOSCA definitions file and Manifest file with different name should return error.
     */
    @Test
    void testGivenManifestAndMainDefinitionFile_withDifferentNames_thenErrorIsReturned() {
        metaFileBuilder = new StringBuilder()
                .append(TOSCA_META_FILE_VERSION_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" 1.0").append("\n")
                .append(CSAR_VERSION_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" 1.1").append("\n")
                .append(CREATED_BY_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" Vendor").append("\n")
                .append(ENTRY_DEFINITIONS.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_DEFINITION_FILEPATH).append("\n")
                .append(ETSI_ENTRY_MANIFEST.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" Definitions/MainServiceTemplate2.mf\n")
                .append(ETSI_ENTRY_CHANGE_LOG.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_CHANGELOG_FILEPATH).append("\n");

        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());
        manifestBuilder.withSource(SAMPLE_DEFINITION_IMPORT_FILE_PATH);

        manifestBuilder.withSource("Definitions/MainServiceTemplate2.mf");
        handler.addFile("Definitions/MainServiceTemplate2.mf", manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Main TOSCA definitions file and Manifest file with different name should return error",
            validationResult.getErrors(), 1);
    }

    @Test
    void testGivenManifestFile_withDifferentExtension_thenErrorIsReturned() {
        metaFileBuilder = new StringBuilder()
                .append(TOSCA_META_FILE_VERSION_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" 1.0").append("\n")
                .append(CSAR_VERSION_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" 1.1").append("\n")
                .append(CREATED_BY_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" Vendor").append("\n")
                .append(ENTRY_DEFINITIONS.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_DEFINITION_FILEPATH).append("\n")
                .append(ETSI_ENTRY_MANIFEST.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" Definitions/MainServiceTemplate.txt\n")
                .append(ETSI_ENTRY_CHANGE_LOG.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_CHANGELOG_FILEPATH).append("\n");

        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());
        manifestBuilder.withSource(SAMPLE_DEFINITION_IMPORT_FILE_PATH);

        manifestBuilder.withSource("Definitions/MainServiceTemplate.txt");
        handler.addFile("Definitions/MainServiceTemplate.txt", manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Manifest file with different extension than .mf should return error",
            validationResult.getErrors(), 1);
    }

    @Test
    void testGivenManifestFile_withValidVnfMetadata_thenNoErrorsReturned() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertTrue(validationResult.isValid(), "Manifest with valid vnf mandatory values should be valid");
        assertExpectedErrors("Manifest with valid vnf mandatory values should not return any errors", validationResult.getErrors(), 0);
    }

    @Test
    void testGivenManifestFile_withValidPnfMetadata_thenNoErrorsReturned() {
        final ManifestBuilder manifestBuilder = getPnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        manifestBuilder.withSignedSource(TOSCA_DEFINITION_FILEPATH
                , "SHA-abc", "09e5a788acb180162c51679ae4c998039fa6644505db2415e35107d1ee213943");
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Manifest with valid pnf mandatory values should not return any errors", validationResult.getErrors(), 0);
    }

    /**
     * Manifest with mixed metadata should return error.
     */
    @Test
    void testGivenManifestFile_withMetadataContainingMixedPnfVnfMetadata_thenErrorIsReturned() {
        final ManifestBuilder manifestBuilder = new ManifestBuilder()
                .withMetaData(PNFD_NAME.getToken(), "RadioNode")
                .withMetaData(VNF_PROVIDER_ID.getToken(), "Bilal Iqbal")
                .withMetaData(PNFD_ARCHIVE_VERSION.getToken(), "1.0")
                .withMetaData(VNF_RELEASE_DATE_TIME.getToken(), "2019-12-14T11:25:00+00:00");

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Manifest with mixed metadata should return error", validationResult.getErrors(), 1);
    }


    @Test
    void testGivenManifestFile_withMetadataMissingPnfOrVnfMandatoryEntries_thenErrorIsReturned() {
        final ManifestBuilder manifestBuilder = new ManifestBuilder()
                .withMetaData("invalid_product_name", "RadioNode")
                .withMetaData("invalid_provider_id", "Bilal Iqbal")
                .withMetaData("invalid_package_version", "1.0")
                .withMetaData("invalid_release_date_time", "2019-12-14T11:25:00+00:00");

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Manifest with missing vnf or pnf mandatory entries should return error", validationResult.getErrors(), 1);
    }

    @Test
    void testGivenManifestFile_withMetadataMissingMandatoryPnfEntries_thenErrorIsReturned() {
        final ManifestBuilder manifestBuilder = new ManifestBuilder();

        manifestBuilder.withMetaData(PNFD_NAME.getToken(), "RadioNode");
        manifestBuilder.withMetaData(PNFD_RELEASE_DATE_TIME.getToken(), "2019-12-14T11:25:00+00:00");

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);

        assertExpectedErrors("Manifest with metadata missing pnf mandatory entries should return error", validationResult.getErrors(), 1);
    }

    @Test
    void testGivenManifestFile_withMetadataMissingMandatoryVnfEntries_thenErrorIsReturned() {
        final ManifestBuilder manifestBuilder = new ManifestBuilder();

        manifestBuilder.withMetaData(VNF_PRODUCT_NAME.getToken(), "RadioNode");

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);

        assertExpectedErrors("Manifest with metadata missing vnf mandatory entries should return error", validationResult.getErrors(), 1);

    }

    /**
     * Manifest with more than 4 metadata entries should return error.
     */
    @Test
    void testGivenManifestFile_withMetadataEntriesExceedingTheLimit_thenErrorIsReturned() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder()
                .withMetaData(PNFD_NAME.getToken(), "RadioNode")
                .withMetaData(ManifestTokenType.PNFD_PROVIDER.getToken(), "Bilal Iqbal")
                .withMetaData(PNFD_ARCHIVE_VERSION.getToken(), "1.0")
                .withMetaData(PNFD_RELEASE_DATE_TIME.getToken(), "2019-03-11T11:25:00+00:00");

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Manifest with more than 4 metadata entries should return error", validationResult.getErrors(), 1);
    }

    @Test
    void testGivenManifestFile_withPnfMetadataAndVfEntries_thenErrorIsReturned() {
        final ManifestBuilder manifestBuilder = getPnfManifestSampleBuilder();
        metaFileBuilder
                .append(ETSI_ENTRY_TESTS.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" Files/Tests").append("\n")
                .append(ETSI_ENTRY_LICENSES.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" Files/Licenses");

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors("Tosca.meta should not have entries applicable only to VF", validationResult.getErrors(), 2);

    }

    /**
     * Tests an imported descriptor with a missing imported file.
     */
    @Test
    void testGivenDefinitionFileWithImportedDescriptor_whenImportedDescriptorImportsMissingFile_thenMissingImportErrorOccur() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        manifestBuilder.withSource(SAMPLE_SOURCE);

        final String definitionImportOne = "Definitions/importOne.yaml";
        handler.addFile(definitionImportOne,
                getResourceBytesOrFail("validation.files/definition/sampleDefinitionFile2.yaml"));
        manifestBuilder.withSource(definitionImportOne);

        final String definitionFileWithValidImports = "validation.files/definition/definitionFileWithOneImport.yaml";
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(definitionFileWithValidImports));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);

        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        for (int i =0;i < getManifestDefinitionErrorCount();i++)
            expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                    , Messages.MISSING_IMPORT_FILE.formatMessage("Definitions/etsi_nfv_sol001_pnfd_2_5_2_types.yaml"))
                    );

        assertExpectedErrors(validationResult.getErrors(), expectedErrorList);
    }

    /**
     * Tests an imported descriptor with invalid import statement.
     */
    @Test
    void testGivenDefinitionFileWithImportedDescriptor_whenInvalidImportStatementExistInImportedDescriptor_thenInvalidImportErrorOccur() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(SAMPLE_SOURCE, "".getBytes());
        manifestBuilder.withSource(SAMPLE_SOURCE);

        final String definitionImportOne = "Definitions/importOne.yaml";
        handler.addFile(definitionImportOne,
                getResourceBytesOrFail("validation.files/definition/definitionFileWithInvalidImport.yaml"));
        manifestBuilder.withSource(definitionImportOne);

        final String definitionFileWithValidImports = "validation.files/definition/definitionFileWithOneImport.yaml";
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(definitionFileWithValidImports));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);

        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        for (int i =0;i < getManifestDefinitionErrorCount();i++)
            expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , Messages.INVALID_IMPORT_STATEMENT.formatMessage(definitionImportOne, "null"))
         );

        assertExpectedErrors(validationResult.getErrors(), expectedErrorList);
    }

    @Test
    void givenManifestWithNonManoPmAndVesArtifacts_whenNonManoArtifactsAreValid_thenNoErrorsOccur() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        final String nonManoPmEventsSource = "Artifacts/Deployment/Measurements/PM_Dictionary.yaml";
        handler.addFile(nonManoPmEventsSource,
                getResourceBytesOrFail("validation.files/measurements/pmEvents-valid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoPmEventsSource);

        final String nonManoVesEventsSource = "Artifacts/Deployment/Events/ves_events.yaml";
        handler.addFile(nonManoVesEventsSource,
                getResourceBytesOrFail("validation.files/events/vesEvents-valid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_VES_EVENTS.getType(), nonManoVesEventsSource);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);

        assertExpectedErrors(validationResult.getErrors(), Collections.emptyList());
    }

    @Test
    void givenManifestWithNonManoPmOrVesArtifacts_whenNonManoArtifactsYamlAreInvalid_thenInvalidYamlErrorOccur() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        final String nonManoPmEventsSource = "Artifacts/Deployment/Measurements/PM_Dictionary.yaml";
        handler.addFile(nonManoPmEventsSource, getResourceBytesOrFail(INVALID_YAML_FILE_PATH));
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoPmEventsSource);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , Messages.INVALID_YAML_FORMAT_1.formatMessage(nonManoPmEventsSource, "while scanning a simple key in 'reader', line 2, column 1: key {} ^could not find expected ':' in 'reader', line 2, column 7: key {} ^"))
        );
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR, "while scanning a simple key in 'reader', line 2, column 1: key {} ^could not find expected ':' in 'reader', line 2, column 7: key {} ^")
            );

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors(validationResult.getErrors(), expectedErrorList);
    }

    @Test
    void givenManifestWithNonManoPmOrVesArtifacts_whenNonManoArtifactsYamlAreEmpty_thenEmptyYamlErrorOccur() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        final String nonManoPmEventsSource = "Artifacts/Deployment/Measurements/PM_Dictionary.yaml";
        handler.addFile(nonManoPmEventsSource, getResourceBytesOrFail(EMPTY_YAML_FILE_PATH));
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoPmEventsSource);

        final String nonManoVesEventsSource = "Artifacts/Deployment/Events/ves_events.yaml";
        handler.addFile(nonManoVesEventsSource, getResourceBytesOrFail(EMPTY_YAML_FILE_PATH));
        manifestBuilder.withNonManoArtifact(ONAP_VES_EVENTS.getType(), nonManoVesEventsSource);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , Messages.EMPTY_YAML_FILE_1.formatMessage(nonManoPmEventsSource))
        );
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , Messages.EMPTY_YAML_FILE_1.formatMessage(nonManoVesEventsSource))
        );
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , "PM_Dictionary YAML file is empty")
        );

        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        assertExpectedErrors(validationResult.getErrors(), expectedErrorList);
    }

    @Test
    void givenManifestWithNonManoPmOrVesArtifacts_whenNonManoArtifactsHaveNotYamlExtension_thenInvalidYamlExtensionErrorOccur() {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        final String nonManoPmEventsSource = "Artifacts/Deployment/Measurements/PM_Dictionary.y1";
        handler.addFile(nonManoPmEventsSource, "".getBytes());
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoPmEventsSource);

        final String nonManoVesEventsSource = "Artifacts/Deployment/Events/ves_events.y2";
        handler.addFile(nonManoVesEventsSource, "".getBytes());
        manifestBuilder.withNonManoArtifact(ONAP_VES_EVENTS.getType(), nonManoVesEventsSource);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , Messages.INVALID_YAML_EXTENSION.formatMessage(nonManoPmEventsSource))
        );
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , Messages.INVALID_YAML_EXTENSION.formatMessage(nonManoVesEventsSource))
        );
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , "PM_Dictionary YAML file is empty")
        );


        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);

        assertExpectedErrors(validationResult.getErrors(), expectedErrorList);
    }

    @Test
    void givenPackageWithValidSoftwareInformationNonMano_whenThePackageIsValidated_thenNoErrorsAreReturned() {
        //given a package with software information non-mano artifact
        final ManifestBuilder manifestBuilder = getPnfManifestSampleBuilder();
        final String nonManoSoftwareInformationPath = "Artifacts/software-information/pnf-sw-information-valid.yaml";
        handler.addFile(nonManoSoftwareInformationPath,
                getResourceBytesOrFail("validation.files/non-mano/pnf-sw-information-valid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_SW_INFORMATION.getType(), nonManoSoftwareInformationPath);
        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString()
                .getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));
        //when package is validated
        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        //then no errors
        assertExpectedErrors(validationResult.getErrors(), Collections.emptyList());
    }

    @Test
    void givenPackageWithUnparsableSwInformationNonMano_whenThePackageIsValidated_thenInvalidErrorIsReturned() {
        //given a package with unparsable software information non-mano artifact
        final ManifestBuilder manifestBuilder = getPnfManifestSampleBuilder();
        final String nonManoSoftwareInformationPath = "Artifacts/software-information/pnf-sw-information-valid.yaml";
        handler.addFile(nonManoSoftwareInformationPath,
                getResourceBytesOrFail("validation.files/invalid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_SW_INFORMATION.getType(), nonManoSoftwareInformationPath);
        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString()
                .getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));
        //when package is validated
        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        //then invalid error returned
        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , Messages.INVALID_SW_INFORMATION_NON_MANO_ERROR.formatMessage(nonManoSoftwareInformationPath))
        );
        assertExpectedErrors(validationResult.getErrors(), expectedErrorList);
    }

    @Test
    void givenPackageWithIncorrectSwInformationNonMano_whenThePackageIsValidated_thenInvalidErrorIsReturned() {
        //given a package with incorrect software information non-mano artifact
        final ManifestBuilder manifestBuilder = getPnfManifestSampleBuilder();
        final String nonManoSoftwareInformationPath = "Artifacts/software-information/pnf-sw-information-invalid.yaml";
        handler.addFile(nonManoSoftwareInformationPath,
                getResourceBytesOrFail("validation.files/non-mano/pnf-sw-information-invalid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_SW_INFORMATION.getType(), nonManoSoftwareInformationPath);
        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString()
                .getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));
        //when package is validated
        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        //then incorrect error returned
        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , Messages.INCORRECT_SW_INFORMATION_NON_MANO_ERROR.formatMessage(nonManoSoftwareInformationPath))
        );
        assertExpectedErrors(validationResult.getErrors(), expectedErrorList);
    }

    @Test
    void givenPackageWithTwoSoftwareInformationNonMano_whenThePackageIsValidated_thenUniqueErrorIsReturned() {
        //given a package with two software information non-mano artifacts
        final ManifestBuilder manifestBuilder = getPnfManifestSampleBuilder();
        final String nonManoSoftwareInformation1Path = "Artifacts/software-information/pnf-sw-information-valid1.yaml";
        handler.addFile(nonManoSoftwareInformation1Path,
                getResourceBytesOrFail("validation.files/non-mano/pnf-sw-information-valid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_SW_INFORMATION.getType(), nonManoSoftwareInformation1Path);
        final String nonManoSoftwareInformation2Path = "Artifacts/software-information/pnf-sw-information-valid2.yaml";
        handler.addFile(nonManoSoftwareInformation2Path,
                getResourceBytesOrFail("validation.files/non-mano/pnf-sw-information-valid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_SW_INFORMATION.getType(), nonManoSoftwareInformation2Path);
        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString()
                .getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));
        //when package is validated
        final ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        //then unique error returned
        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        final String errorFiles = Stream.of(nonManoSoftwareInformation1Path, nonManoSoftwareInformation2Path)
                .map(s -> String.format("'%s'", s))
                .collect(Collectors.joining(", "));
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , Messages.UNIQUE_SW_INFORMATION_NON_MANO_ERROR.formatMessage(errorFiles))
        );
        assertExpectedErrors(validationResult.getErrors(), expectedErrorList);
    }

    @Test
    void signedPackage() throws SecurityManagerException {
        //given
        final ManifestBuilder manifestBuilder = getPnfManifestSampleBuilder();
        final String fakeArtifactPath = "Artifacts/aArtifact.yaml";
        final String fakeArtifactCmsPath = "Artifacts/aArtifact.cms";
        final String fakeCertificatePath = "certificate.cert";
        handler.addFile(fakeArtifactPath, new byte[0]);
        manifestBuilder.withSource(fakeArtifactPath);
        handler.addFile(fakeArtifactCmsPath, new byte[0]);
        manifestBuilder.withSource(fakeArtifactCmsPath);
        handler.addFile(fakeCertificatePath, new byte[0]);
        manifestBuilder.withSource(fakeCertificatePath);
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);
        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        metaFileBuilder.append(ETSI_ENTRY_CERTIFICATE.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(fakeCertificatePath).append("\n");
        handler.addFile(TOSCA_META_PATH_FILE_NAME,
                metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final SecurityManager securityManagerMock = mock(SecurityManager.class);
        when(securityManagerMock.verifySignedData(any(), any(), any())).thenReturn(true);
        sol004MetaDirectoryValidator = getSol004WithSecurity(securityManagerMock);

        //when
        ValidationResult validationResult = sol004MetaDirectoryValidator.validate(handler);
        //then
        assertExpectedErrors(validationResult.getErrors(), Collections.emptyList());

        //given
        sol004MetaDirectoryValidator = getSol004WithSecurity(securityManagerMock);
        when(securityManagerMock.verifySignedData(any(), any(), any())).thenReturn(false);

        //when
        validationResult = sol004MetaDirectoryValidator.validate(handler);

        //then
        List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR
                , Messages.ARTIFACT_INVALID_SIGNATURE.formatMessage(fakeArtifactCmsPath, fakeArtifactPath))
        );
        assertExpectedErrors(validationResult.getErrors(), expectedErrorList);

        //given
        sol004MetaDirectoryValidator = getSol004WithSecurity(securityManagerMock);
        when(securityManagerMock.verifySignedData(any(), any(), any()))
                .thenThrow(new SecurityManagerException("SecurityManagerException"));
        //when
        validationResult = sol004MetaDirectoryValidator.validate(handler);

        //then
        expectedErrorList = new ArrayList<>();
        expectedErrorList.add(
            new ErrorMessage(ErrorLevel.ERROR,
                Messages.ARTIFACT_SIGNATURE_VALIDATION_ERROR.formatMessage(fakeArtifactCmsPath,
                    fakeArtifactPath, fakeCertificatePath, "SecurityManagerException")
            )
        );
        assertExpectedErrors(validationResult.getErrors(), expectedErrorList);
    }

    protected void assertExpectedErrors(final String testCase, final Map<String, List<ErrorMessage>> errors, final int expectedErrors){
        assertExpectedErrors(testCase, errors.get(SdcCommon.UPLOAD_FILE), expectedErrors);
    }

    protected void assertExpectedErrors(final String testCase, final List<ErrorMessage> errorMessages, final int expectedErrors){
        printErrorMessages(errorMessages);
        if (expectedErrors > 0) {
            assertEquals(expectedErrors, errorMessages.size(), testCase);
        } else {
            assertTrue(errorMessages.isEmpty(), testCase);
        }
    }

    private void printErrorMessages(final List<ErrorMessage> errorMessages) {
        if (CollectionUtils.isNotEmpty(errorMessages)) {
            errorMessages.forEach(errorMessage ->
                    System.out.printf("%s: %s%n", errorMessage.getLevel(), errorMessage.getMessage())
            );
        }
    }

    private void assertExpectedErrors(List<ErrorMessage> actualErrorList, final List<ErrorMessage> expectedErrorList) {
        if (actualErrorList == null) {
            actualErrorList = new ArrayList<>();
        }

        printErrorMessages(actualErrorList);

        assertThat("The actual error list should have the same size as the expected error list"
                , actualErrorList, hasSize(expectedErrorList.size())
        );

        actualErrorList.forEach(error -> {
            Predicate<ErrorMessage> matching = e -> e.getLevel() == error.getLevel() && sanitize(e.getMessage()).equalsIgnoreCase(sanitize(error.getMessage()));
            assertTrue(expectedErrorList.stream().anyMatch(matching), "The actual error and expected error lists should be the same");
        });
    }

    private static String sanitize(String s) {
        return s.trim().replaceAll("\n", "").replaceAll("\r", "").replaceAll("\\s{2,}", " ").trim();
    }

}
