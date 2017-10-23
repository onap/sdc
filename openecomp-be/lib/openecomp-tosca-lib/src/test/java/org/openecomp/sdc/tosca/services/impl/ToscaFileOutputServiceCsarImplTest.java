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

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ToscaFileOutputServiceCsarImplTest {

  private final ToscaFileOutputServiceCsarImpl toscaFileOutputServiceCSARImpl =
      new ToscaFileOutputServiceCsarImpl();

  @Test
  public void testCreationMetaFile() {
    String createdMeta = toscaFileOutputServiceCSARImpl.createMetaFile("entryFile.yaml");
    String expectedMeta =
        "TOSCA-Meta-File-Version: 1.0\n" +
            "CSAR-Version: 1.1\n" +
            "Created-By: ASDC Onboarding portal\n" +
            "Entry-Definitions: Definitions" + File.separator + "entryFile.yaml";
    Assert.assertEquals(createdMeta.replaceAll("\\s+", ""), expectedMeta.replaceAll("\\s+", ""));
  }

  @Test
  public void testCSARFileCreationWithExternalArtifacts() throws IOException {
    ServiceTemplate mainServiceTemplate = new ServiceTemplate();
    Map<String, String> metadata1 = new HashMap<>();
    metadata1.put("Template_author", "OPENECOMP");
    metadata1.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME,"ST1");
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
    heatFiles.putAll(dummyHeatArtifacts);
    Map<String, byte[]> licenseArtifacts = new HashMap<>();

    FileContentHandler licenseArtifactsFiles = new FileContentHandler();

    licenseArtifacts.put(
        ToscaFileOutputServiceCsarImpl.EXTERNAL_ARTIFACTS_FOLDER_NAME + File.separator +
            "license-file-1.xml", file1Content.getBytes());
    licenseArtifacts.put(
        ToscaFileOutputServiceCsarImpl.EXTERNAL_ARTIFACTS_FOLDER_NAME + File.separator +
            "license-file-2.xml", file1Content.getBytes());

    licenseArtifactsFiles.putAll(licenseArtifacts);

    byte[] csarFile = toscaFileOutputServiceCSARImpl.createOutputFile(
        new ToscaServiceModel(heatFiles, definitionsInput,
            ToscaUtil.getServiceTemplateFileName(mainServiceTemplate)), licenseArtifactsFiles);

    String resultFileName = "resultFile.zip";
    File file = new File(resultFileName);
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(csarFile);
    }

    try (ZipFile zipFile = new ZipFile(resultFileName)) {

      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      int count = 0;
      while (entries.hasMoreElements()) {
        count++;
        entries.nextElement();
      }
      Assert.assertEquals(7, count);
    }

    Files.delete(Paths.get(file.getPath()));
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
    byte[] csarFile = toscaFileOutputServiceCSARImpl
        .createOutputFile(new ToscaServiceModel(null, definitionsInput, serviceTemplateFileName),
            null);


    String resultFileName = "resultFile.zip";
    File file = new File(resultFileName);
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(csarFile);
    }

    try (ZipFile zipFile = new ZipFile(resultFileName)) {

      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      int count = 0;
      while (entries.hasMoreElements()) {
        count++;
        entries.nextElement();
      }
      Assert.assertEquals(2, count);
    }

    Files.delete(Paths.get(file.getPath()));
  }
}
