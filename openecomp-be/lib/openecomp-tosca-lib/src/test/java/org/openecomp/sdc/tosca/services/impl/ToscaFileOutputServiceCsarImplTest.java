package org.openecomp.sdc.tosca.services.impl;

import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.Metadata;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.junit.Assert;
import org.junit.Test;

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

  private ToscaFileOutputServiceCsarImpl toscaFileOutputServiceCsarImpl =
      new ToscaFileOutputServiceCsarImpl();

  @Test
  public void testCreationMetaFile() {
    String createdMeta = toscaFileOutputServiceCsarImpl.createMetaFile("entryFile.yaml");
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
    Metadata metadata1 = new Metadata();
    metadata1.setTemplate_author("OPENECOMP");
    metadata1.setTemplate_name("ST1");
    metadata1.setTemplate_version("1.0.0");
    mainServiceTemplate.setTosca_definitions_version("tosca_simple_yaml_1_0_0");
    mainServiceTemplate.setDescription("testing desc tosca service template");
    mainServiceTemplate.setMetadata(metadata1);

    ServiceTemplate additionalServiceTemplate = new ServiceTemplate();
    Metadata metadata2 = new Metadata();
    metadata2.setTemplate_author("OPENECOMP");
    metadata2.setTemplate_name("ST2");
    metadata2.setTemplate_version("1.0.0");
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

    byte[] csarFile = toscaFileOutputServiceCsarImpl.createOutputFile(
        new ToscaServiceModel(heatFiles, definitionsInput,
            ToscaUtil.getServiceTemplateFileName(mainServiceTemplate)), licenseArtifactsFiles);

    String resultFileName = "resultFile.zip";
    File file = new File(resultFileName);
    FileOutputStream fos = new FileOutputStream(file);
    fos.write(csarFile);
    fos.close();

    ZipFile zipFile = new ZipFile(resultFileName);

    Enumeration<? extends ZipEntry> entries = zipFile.entries();

    int count = 0;
    while (entries.hasMoreElements()) {
      count++;
      entries.nextElement();
    }
    Assert.assertEquals(7, count);
    zipFile.close();
    Files.delete(Paths.get(file.getPath()));
  }

  @Test
  public void testCSARFileCreation_noArtifacts() throws IOException {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    Metadata metadata = new Metadata();
    metadata.setTemplate_author("OPENECOMP");
    metadata.setTemplate_name("Test");
    metadata.setTemplate_version("1.0.0");
    serviceTemplate.setTosca_definitions_version("tosca_simple_yaml_1_0_0");
    serviceTemplate.setDescription("testing desc tosca service template");
    serviceTemplate.setMetadata(metadata);
    Map<String, ServiceTemplate> definitionsInput = new HashMap<>();
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    definitionsInput.put(serviceTemplateFileName, serviceTemplate);
    byte[] csarFile = toscaFileOutputServiceCsarImpl
        .createOutputFile(new ToscaServiceModel(null, definitionsInput, serviceTemplateFileName),
            null);


    String resultFileName = "resultFile.zip";
    File file = new File(resultFileName);
    FileOutputStream fos = new FileOutputStream(file);
    fos.write(csarFile);
    fos.close();

    ZipFile zipFile = new ZipFile(resultFileName);

    Enumeration<? extends ZipEntry> entries = zipFile.entries();

    int count = 0;
    while (entries.hasMoreElements()) {
      count++;
      entries.nextElement();
    }
    Assert.assertEquals(2, count);
    zipFile.close();
    Files.delete(Paths.get(file.getPath()));
  }
}