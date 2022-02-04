/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_CNF_HELM;
import static org.openecomp.sdc.be.test.util.TestResourcesHandler.getResourceBytesOrFail;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.OTHER_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.SAMPLE_DEFINITION_FILE_PATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.SAMPLE_DEFINITION_IMPORT_FILE_PATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.SAMPLE_SOURCE;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_CHANGELOG_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_DEFINITION_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_MANIFEST_FILEPATH;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.tosca.csar.ManifestBuilder;
import org.openecomp.sdc.tosca.csar.ManifestTokenType;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;

class SOL004Version4MetaDirectoryValidatorTest extends SOL004MetaDirectoryValidatorTest {

    private final ValidatorFactory validatorFactory = new ValidatorFactory();

    @Override
    public SOL004MetaDirectoryValidator getSOL004MetaDirectoryValidator() {
        return new SOL004Version4MetaDirectoryValidator();
    }

    @Override
    public StringBuilder getMetaFileBuilder() {
        return super.getMetaFileBuilder().append(OTHER_DEFINITIONS.getName())
        .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_DEFINITION_FILEPATH).append("\n");
    }

    @Override
    protected SOL004MetaDirectoryValidator getSol004WithSecurity(SecurityManager securityManagerMock) {
        return new SOL004Version4MetaDirectoryValidator(securityManagerMock);
    }

    @Override
    protected ManifestBuilder getVnfManifestSampleBuilder() {
        return super.getVnfManifestSampleBuilder()
            .withMetaData(ManifestTokenType.VNF_SOFTWARE_VERSION.getToken(), "1.0.0")
            .withMetaData(ManifestTokenType.VNFD_ID.getToken(), "2116fd24-83f2-416b-bf3c-ca1964793aca")
            .withMetaData(ManifestTokenType.COMPATIBLE_SPECIFICATION_VERSIONS.getToken(), "2.6.1, 2.7.1, 3.3.1")
            .withMetaData(ManifestTokenType.VNF_PROVIDER_ID.getToken(), "ACME")
            .withMetaData(ManifestTokenType.VNF_RELEASE_DATE_TIME.getToken(), "2021-02-11T11:25:00+00:00")
            .withMetaData(ManifestTokenType.VNF_PACKAGE_VERSION.getToken(), "1.0")
            .withMetaData(ManifestTokenType.VNFM_INFO.getToken(), "etsivnfm:v2.3.1,0:myGreatVnfm-1")
            .withMetaData(ManifestTokenType.VNF_PRODUCT_NAME.getToken(), "RadioNode");
    }

    @Override
    protected ManifestBuilder getPnfManifestSampleBuilder() {
        return super.getPnfManifestSampleBuilder()
            .withMetaData(ManifestTokenType.COMPATIBLE_SPECIFICATION_VERSIONS.getToken(), "2.6.1, 2.7.1, 3.3.1");
    }

    @Override
    protected int getManifestDefinitionErrorCount() {
        return 2;
    }

    @Test
    void testGivenManifestFile_withValidSourceAndNonManoSources_thenNoErrorIsReturned() throws IOException {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

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

        final String nonManoSource = "Artifacts/Deployment/non-mano/onap-cnf-helm-valid.yaml";
        handler.addFile(nonManoSource, getResourceBytesOrFail("validation.files/empty.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_CNF_HELM.getType(), nonManoSource);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final Validator validator = validatorFactory.getValidator(handler);
        final ValidationResult validationResult = validator.validate(handler);
        assertTrue(validationResult.getErrors().isEmpty());
    }

    @Test
    void testGivenManifestFile_withNotReferencedNonManoSources_thenErrorIsReturned() throws IOException {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

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

        //non existent reference
        manifestBuilder.withNonManoArtifact(ONAP_CNF_HELM.getType(), "validation.files/notReferencedFile.yaml");

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final Validator validator = validatorFactory.getValidator(handler);
        final ValidationResult validationResult = validator.validate(handler);
        assertExpectedErrors("Non-MANO file does not exist", validationResult.getErrors(), 1);
    }

    @Test
    void testGivenManifestFile_withNonExistentSourceFile_thenErrorIsReturned() throws IOException {
        final ManifestBuilder manifestBuilder = getPnfManifestSampleBuilder();
        //non existent reference
        manifestBuilder.withSource("Artifacts/Deployment/non-mano/RadioNode.yaml");

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes());
        manifestBuilder.withSource(TOSCA_CHANGELOG_FILEPATH);

        handler.addFile(TOSCA_DEFINITION_FILEPATH, getResourceBytesOrFail(SAMPLE_DEFINITION_FILE_PATH));
        manifestBuilder.withSource(TOSCA_DEFINITION_FILEPATH);

        handler.addFile(SAMPLE_DEFINITION_IMPORT_FILE_PATH, "".getBytes());
        manifestBuilder.withSource(SAMPLE_DEFINITION_IMPORT_FILE_PATH);

        final String nonManoSource = "Artifacts/Deployment/non-mano/onap-cnf-helm-valid.yaml";
        handler.addFile(nonManoSource, getResourceBytesOrFail("validation.files/empty.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_CNF_HELM.getType(), nonManoSource);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        final Validator validator = validatorFactory.getValidator(handler);
        final ValidationResult validationResult = validator.validate(handler);
        assertExpectedErrors("Manifest with non existent source files", validationResult.getErrors(), 1);
    }

}
