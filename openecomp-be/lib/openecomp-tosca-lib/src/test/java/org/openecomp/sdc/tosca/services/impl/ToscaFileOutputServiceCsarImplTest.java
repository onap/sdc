/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.tosca.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.APPLICATION_NAME;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.APPLICATION_PROVIDER;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ENTRY_DEFINITION_TYPE;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryAsd.CREATED_BY_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryAsd.CSAR_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryAsd.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryAsd.ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryAsd.TOSCA_META_FILE_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.tosca.csar.AsdPackageHelper;
import org.openecomp.sdc.tosca.csar.ManifestTokenType;
import org.openecomp.sdc.tosca.csar.ManifestUtils;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;

public class ToscaFileOutputServiceCsarImplTest {

    private static ToscaFileOutputServiceCsarImpl toscaFileOutputServiceCsarImpl;

    @BeforeAll
    public static void setupBeforeClass() {
        toscaFileOutputServiceCsarImpl = new ToscaFileOutputServiceCsarImpl(new AsdPackageHelper(new ManifestUtils()));
    }

    @Test
    public void testCreationMetaFile() {
        String createdMeta = toscaFileOutputServiceCsarImpl.createMetaFile("entryFile.yaml", false);
        String expectedMeta =
            "TOSCA-Meta-File-Version: 1.0\n" +
                "CSAR-Version: 1.1\n" +
                "Created-By: ASDC Onboarding portal\n" +
                "Entry-Definitions: Definitions" + File.separator + "entryFile.yaml";
        assertEquals(createdMeta.replaceAll("\\s+", ""), expectedMeta.replaceAll("\\s+", ""));
    }

    @Test
    public void testCSARFileCreationWithExternalArtifacts() throws IOException {
        ToscaFileOutputServiceCsarImpl toscaFileOutputServiceCSARImpl =
            new ToscaFileOutputServiceCsarImpl(new AsdPackageHelper(new ManifestUtils()));
        ServiceTemplate mainServiceTemplate = new ServiceTemplate();
        Map<String, String> metadata1 = new HashMap<>();
        metadata1.put("Template_author", "OPENECOMP");
        metadata1.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, "ST1");
        metadata1.put("Template_version", "1.0.0");
        mainServiceTemplate.setMetadata(metadata1);
        mainServiceTemplate.setTosca_definitions_version("tosca_simple_yaml_1_0_0");
        mainServiceTemplate.setDescription("testing desc tosca service template");

        ServiceTemplate additionalServiceTemplate = new ServiceTemplate();
        Map<String, String> metadata2 = new HashMap<>();
        metadata2.put("Template_author", "OPENECOMP");
        metadata2.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, "ST2");
        metadata2.put("Template_version", "1.0.0");
        additionalServiceTemplate.setTosca_definitions_version("tosca_simple_yaml_1_0_0");
        additionalServiceTemplate.setDescription("testing desc tosca service template");
        additionalServiceTemplate.setMetadata(metadata2);

        Map<String, ServiceTemplate> definitionsInput = new HashMap<>();
        definitionsInput
            .put(ToscaUtil.getServiceTemplateFileName(mainServiceTemplate), mainServiceTemplate);
        definitionsInput.put(ToscaUtil.getServiceTemplateFileName(additionalServiceTemplate),
            additionalServiceTemplate);

        Map<String, byte[]> dummyHeatArtifacts = new HashMap<>();
        String file1Content = "this is file number 1";
        String file2Content = "this is file number 2";
        String file1 = "file1.xml";
        dummyHeatArtifacts.put(file1, file1Content.getBytes());
        String file2 = "file2.yml";
        dummyHeatArtifacts.put(file2, file2Content.getBytes());

        FileContentHandler heatFiles = new FileContentHandler();
        heatFiles.setFiles(dummyHeatArtifacts);
        Map<String, byte[]> licenseArtifacts = new HashMap<>();

        FileContentHandler licenseArtifactsFiles = new FileContentHandler();

        licenseArtifacts.put(
            ToscaFileOutputServiceCsarImpl.EXTERNAL_ARTIFACTS_FOLDER_NAME + File.separator +
                "license-file-1.xml", file1Content.getBytes());
        licenseArtifacts.put(
            ToscaFileOutputServiceCsarImpl.EXTERNAL_ARTIFACTS_FOLDER_NAME + File.separator +
                "license-file-2.xml", file1Content.getBytes());

        licenseArtifactsFiles.setFiles(licenseArtifacts);

        byte[] csarFile = toscaFileOutputServiceCSARImpl.createOutputFile(
            new ToscaServiceModel(heatFiles, definitionsInput,
                ToscaUtil.getServiceTemplateFileName(mainServiceTemplate)), licenseArtifactsFiles);

        File file = File.createTempFile("resultFile", "zip");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(csarFile);
        }

        try (ZipFile zipFile = new ZipFile(file)) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                entries.nextElement();
            }
        }

        Files.delete(Paths.get(file.getAbsolutePath()));
    }

    @Test
    public void testAsdCSARFileCreationWithExternalArtifacts() throws IOException {

        ServiceTemplate mainServiceTemplate = new ServiceTemplate();
        Map<String, String> metadata1 = new HashMap<>();
        metadata1.put("Template_author", "OPENECOMP");
        metadata1.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, "ST1");
        metadata1.put("Template_version", "1.0.0");
        metadata1.put("filename", "asd.yaml");
        mainServiceTemplate.setMetadata(metadata1);
        mainServiceTemplate.setTosca_definitions_version("tosca_simple_yaml_1_0_0");
        mainServiceTemplate.setDescription("testing desc tosca service template");

        ServiceTemplate additionalServiceTemplate = new ServiceTemplate();
        Map<String, String> metadata2 = new HashMap<>();
        metadata2.put("Template_author", "OPENECOMP");
        metadata2.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, "ST2");
        metadata2.put("Template_version", "1.0.0");
        additionalServiceTemplate.setTosca_definitions_version("tosca_simple_yaml_1_0_0");
        additionalServiceTemplate.setDescription("testing desc tosca service template");
        additionalServiceTemplate.setMetadata(metadata2);

        FileContentHandler handler = new FileContentHandler();
        String metaFile = new StringBuilder()
            .append(TOSCA_META_FILE_VERSION_ENTRY.getName())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" 1.0").append("\n")
            .append(CSAR_VERSION_ENTRY.getName())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" 1.1").append("\n")
            .append(CREATED_BY_ENTRY.getName())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" Vendor").append("\n")
            .append(ENTRY_DEFINITIONS.getName())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append("Definitions/asd.yaml").append("\n")
            .append(ETSI_ENTRY_MANIFEST.getName() + ATTRIBUTE_VALUE_SEPARATOR.getToken() + "asd.mf").append("\n").toString();
        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFile.getBytes(StandardCharsets.UTF_8));

        Map<String, byte[]> manifestMap = new HashMap<>();
        String manifestContent = new StringBuilder().append("metadata")
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append("\n")
            .append(ENTRY_DEFINITION_TYPE.getToken())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append("asd").append("\n")
            .append(ManifestTokenType.RELEASE_DATE_TIME.getToken())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append("2021-10-21T11:30:00+05:00").append("\n")
            .append(APPLICATION_NAME.getToken())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append("SampleApp").append("\n")
            .append(APPLICATION_PROVIDER.getToken())
            .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append("MyCompany")
            .toString();
        String manifestFile = "asd.mf";
        manifestMap.put(manifestFile, manifestContent.getBytes());
        handler.setFiles(manifestMap);

        Map<String, ServiceTemplate> definitionsInput = new HashMap<>();
        definitionsInput
            .put(ToscaUtil.getServiceTemplateFileName(mainServiceTemplate), mainServiceTemplate);
        definitionsInput.put(ToscaUtil.getServiceTemplateFileName(additionalServiceTemplate),
            additionalServiceTemplate);

        Map<String, byte[]> dummyHeatArtifacts = new HashMap<>();
        String file1Content = "this is file number 1";
        String file2Content = "this is file number 2";
        String file1 = "file1.xml";
        dummyHeatArtifacts.put(file1, file1Content.getBytes());
        String file2 = "file2.yml";
        dummyHeatArtifacts.put(file2, file2Content.getBytes());
        handler.setFiles(dummyHeatArtifacts);

        FileContentHandler heatFiles = new FileContentHandler();
        heatFiles.setFiles(dummyHeatArtifacts);
        heatFiles.addAll(handler);
        Map<String, byte[]> licenseArtifacts = new HashMap<>();

        FileContentHandler licenseArtifactsFiles = new FileContentHandler();

        licenseArtifacts.put(
            ToscaFileOutputServiceCsarImpl.EXTERNAL_ARTIFACTS_FOLDER_NAME + File.separator +
                "license-file-1.xml", file1Content.getBytes());
        licenseArtifacts.put(
            ToscaFileOutputServiceCsarImpl.EXTERNAL_ARTIFACTS_FOLDER_NAME + File.separator +
                "license-file-2.xml", file1Content.getBytes());

        licenseArtifactsFiles.setFiles(licenseArtifacts);

        byte[] csarFile = toscaFileOutputServiceCsarImpl.createOutputFile(
            new ToscaServiceModel(heatFiles, definitionsInput,
                ToscaUtil.getServiceTemplateFileName(mainServiceTemplate)), licenseArtifactsFiles);

        File file = File.createTempFile("resultFile", "zip");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(csarFile);
        }

        try (ZipFile zipFile = new ZipFile(file)) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int count = 0;
            while (entries.hasMoreElements()) {
                count++;
                entries.nextElement();
            }
            assertEquals(9, count);
        }

        Files.delete(Paths.get(file.getAbsolutePath()));
    }

    @Test
    public void testCSARFileCreation_noArtifacts() throws IOException {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Template_author", "OPENECOMP");
        metadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, "Test");
        metadata.put("Template_version", "1.0.0");
        serviceTemplate.setTosca_definitions_version("tosca_simple_yaml_1_0_0");
        serviceTemplate.setDescription("testing desc tosca service template");
        serviceTemplate.setMetadata(metadata);
        Map<String, ServiceTemplate> definitionsInput = new HashMap<>();
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        definitionsInput.put(serviceTemplateFileName, serviceTemplate);
        byte[] csarFile = toscaFileOutputServiceCsarImpl
            .createOutputFile(new ToscaServiceModel(null, definitionsInput, serviceTemplateFileName),
                null);

        File file = File.createTempFile("resultFile", "zip");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(csarFile);
        }

        try (ZipFile zipFile = new ZipFile(file)) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            int count = 0;
            while (entries.hasMoreElements()) {
                count++;
                entries.nextElement();
            }
            assertEquals(2, count);
        }

        Files.delete(Paths.get(file.getAbsolutePath()));
    }

    @Test
    public void testCreateOutputFileEntryDefinitionServiceTemplateIsNull() {
        ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
        toscaServiceModel.setServiceTemplates(Collections.emptyMap());
        assertThrows(CoreException.class, () -> toscaFileOutputServiceCsarImpl.createOutputFile(toscaServiceModel, null));
    }

    @Test
    public void testGetArtifactsFolderName() {
        assertEquals("Artifacts", toscaFileOutputServiceCsarImpl.getArtifactsFolderName());
    }
}
