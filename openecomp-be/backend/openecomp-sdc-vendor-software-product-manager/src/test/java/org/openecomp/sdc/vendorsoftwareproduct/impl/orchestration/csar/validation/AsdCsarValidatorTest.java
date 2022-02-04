/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 *
 *
 */

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.tosca.csar.ManifestTokenType;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageContentHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openecomp.sdc.be.test.util.TestResourcesHandler.getResourceBytesOrFail;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.APPLICATION_NAME;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.APPLICATION_PROVIDER;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ENTRY_DEFINITION_TYPE;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.RELEASE_DATE_TIME;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CREATED_BY_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CSAR_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_CHANGE_LOG;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_LICENSES;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_TESTS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.TOSCA_META_FILE_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.SAMPLE_DEFINITION_FILE_PATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.SAMPLE_DEFINITION_IMPORT_FILE_PATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.SAMPLE_SOURCE;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_CHANGELOG_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_DEFINITION_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_MANIFEST_FILEPATH;


class AsdCsarValidatorTest {

    private AsdValidator asdValidator;
    protected OnboardingPackageContentHandler handler;
    protected StringBuilder metaFileBuilder;
    private ValidatorFactory validatorFactory;

    @BeforeEach
    void setUp() {
        validatorFactory = new ValidatorFactory();
        asdValidator = new AsdValidator();
        handler = new OnboardingPackageContentHandler();
        metaFileBuilder = getMetaFileBuilder();
    }

    protected StringBuilder getMetaFileBuilder() {
        return new StringBuilder()
                .append(ENTRY_DEFINITION_TYPE.getToken())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" asd").append("\n")
                .append(APPLICATION_PROVIDER.getToken())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" Ericsson").append("\n")
                .append(APPLICATION_NAME.getToken())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" vCU").append("\n")
                .append(RELEASE_DATE_TIME.getToken())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" 2022-02-07T11:30:00+05:00").append("\n")
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

    @Test
    void testGivenASDTOSCAMeta_withAllSupportedEntries_thenNoErrorsReturned() {

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

        final ManifestBuilder manifestBuilder = getAsdManifestSampleBuilder()
                .withSource(TOSCA_META_PATH_FILE_NAME)
                .withSource(TOSCA_DEFINITION_FILEPATH)
                .withSource(TOSCA_CHANGELOG_FILEPATH)
                .withSource(TOSCA_MANIFEST_FILEPATH).withSource(SAMPLE_SOURCE)
                .withSource(SAMPLE_DEFINITION_IMPORT_FILE_PATH)
                .withSource(entryTestFilePath)
                .withSource(entryLicenseFilePath);

        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = asdValidator.validate(handler);

        assertTrue(validationResult.getErrors().isEmpty());
    }

    @Test
    void testGivenASDTOSCAMetaFile_withInvalidOranEntryDefinitionType_thenErrorIsReturned() {
        final StringBuilder metaFileBuilder = new StringBuilder()
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
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_CHANGELOG_FILEPATH);
        final ManifestBuilder manifestBuilder = getWrongAsdManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final ValidationResult validationResult = asdValidator.validate(handler);
        assertExpectedErrors("Invalid value invalid in TOSCA.meta file", validationResult.getErrors(), 2);
    }

    @Test
    void testGivenASDTOSCAMetaFile_withNoReleaseDateTime_thenNoAsdValidatorIsReturned()  throws IOException {
        final StringBuilder metaFileBuilder = new StringBuilder()
                .append(ENTRY_DEFINITION_TYPE.getToken())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" asd").append("\n")
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

        assertNotEquals(AsdValidator.class, validatorFactory.getValidator(handler).getClass());
    }

    protected ManifestBuilder getAsdManifestSampleBuilder() {
        return new ManifestBuilder()
                .withMetaData(APPLICATION_NAME.getToken(), "RadioNode")
                .withMetaData(APPLICATION_PROVIDER.getToken(), "Ericsson")
                .withMetaData(ENTRY_DEFINITION_TYPE.getToken(), "asd")
                .withMetaData(RELEASE_DATE_TIME.getToken(), "2022-02-01T11:25:00+00:00");
    }

    protected ManifestBuilder getWrongAsdManifestSampleBuilder() {
        return new ManifestBuilder()
                .withMetaData(APPLICATION_NAME.getToken(), "RadioNode")
                .withMetaData(APPLICATION_PROVIDER.getToken(), "Ericsson")
                .withMetaData(ENTRY_DEFINITION_TYPE.getToken(), " Invalid")
                .withMetaData(RELEASE_DATE_TIME.getToken(), "2022-02-01T11:25:00+00:00");
    }

    protected ManifestBuilder getVnfManifestSampleBuilder() {
        return new ManifestBuilder()
                .withMetaData(ManifestTokenType.VNF_PRODUCT_NAME.getToken(), "RadioNode")
                .withMetaData(ManifestTokenType.VNF_PROVIDER_ID.getToken(), "ACME")
                .withMetaData(ManifestTokenType.VNF_PACKAGE_VERSION.getToken(), "1.0")
                .withMetaData(ManifestTokenType.VNF_RELEASE_DATE_TIME.getToken(), "2019-03-11T11:25:00+00:00");
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
}
