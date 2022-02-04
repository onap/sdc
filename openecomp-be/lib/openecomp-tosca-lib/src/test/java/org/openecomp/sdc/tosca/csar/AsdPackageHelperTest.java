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

package org.openecomp.sdc.tosca.csar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.core.utilities.file.FileContentHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.APPLICATION_NAME;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.APPLICATION_PROVIDER;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ENTRY_DEFINITION_TYPE;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.RELEASE_DATE_TIME;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CREATED_BY_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CSAR_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_CHANGE_LOG;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.TOSCA_META_FILE_VERSION_ENTRY;

class AsdPackageHelperTest {

    public static final String TOSCA_DEFINITION_FILEPATH = "Definitions/MainServiceTemplate.yaml";
    public static final String TOSCA_MANIFEST_FILEPATH = "Definitions/MainServiceTemplate.mf";
    public static final String TOSCA_CHANGELOG_FILEPATH = "Artifacts/changeLog.text";
    public static final String TOSCA_META_PATH_FILE_NAME = "TOSCA-Metadata/TOSCA.meta";

    private FileContentHandler handler;
    private StringBuilder metaFileBuilder;

    @BeforeEach
    void setUp() {
        handler = new FileContentHandler();
        metaFileBuilder = getMetaFileBuilder();
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

    @Test
    public void givenRightAsdHandlerItReturnsTrueAsAsdPackage() throws IOException {
        final ManifestBuilder manifestBuilder = getAsdManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        assertTrue(AsdPackageHelper.isAsdPackage(handler));

    }

    @Test
    public void givenWrongAsdHandlerItReturnsFalseAsAsdPackage() throws IOException {
        final ManifestBuilder manifestBuilder = getWrongAsdManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        assertFalse(AsdPackageHelper.isAsdPackage(handler));

    }

    @Test
    public void givenNonAsdHandlerItReturnsFalseAsAsdPackage() throws IOException {
        final ManifestBuilder manifestBuilder = getVnfManifestSampleBuilder();

        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
        manifestBuilder.withSource(TOSCA_META_PATH_FILE_NAME);

        manifestBuilder.withSource(TOSCA_MANIFEST_FILEPATH);
        handler.addFile(TOSCA_MANIFEST_FILEPATH, manifestBuilder.build().getBytes(StandardCharsets.UTF_8));

        assertFalse(AsdPackageHelper.isAsdPackage(handler));

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

}