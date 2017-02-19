package org.openecomp.sdc.enrichment.impl;

import org.openecomp.sdc.enrichment.impl.tosca.ComponentInfo;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.tosca.services.yamlutil.ToscaExtensionYamlUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.core.enrichment.api.EnrichmentManager;
import org.openecomp.core.enrichment.factory.EnrichmentManagerFactory;

import org.openecomp.core.enrichment.types.CeilometerInfo;
import org.openecomp.core.enrichment.types.ComponentCeilometerInfo;
import org.openecomp.core.utilities.file.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;


public class EnrichmentManagerImplTest {


  private static ToscaServiceModel loadToscaServiceModel(String serviceTemplatesPath,
                                                         String globalServiceTemplatesPath,
                                                         String entryDefinitionServiceTemplate)
      throws IOException {
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    Map<String, ServiceTemplate> serviceTemplates = new HashMap<>();
    if (entryDefinitionServiceTemplate == null) {
      entryDefinitionServiceTemplate = "MainServiceTemplate.yaml";
    }

    loadServiceTemplates(serviceTemplatesPath, toscaExtensionYamlUtil, serviceTemplates);
    if (globalServiceTemplatesPath != null) {
      loadServiceTemplates(globalServiceTemplatesPath, toscaExtensionYamlUtil, serviceTemplates);
    }

    return new ToscaServiceModel(null, serviceTemplates, entryDefinitionServiceTemplate);
  }

  private static void loadServiceTemplates(String serviceTemplatesPath,
                                           ToscaExtensionYamlUtil toscaExtensionYamlUtil,
                                           Map<String, ServiceTemplate> serviceTemplates)
      throws IOException {
    URL urlFile = EnrichmentManagerImplTest.class.getResource(serviceTemplatesPath);
    if (urlFile != null) {
      File pathFile = new File(urlFile.getFile());
      File[] files = pathFile.listFiles();
      if (files != null) {
        addServiceTemplateFiles(serviceTemplates, files, toscaExtensionYamlUtil);
      } else {
        throw new NotDirectoryException(serviceTemplatesPath);
      }
    } else {
      throw new NotDirectoryException(serviceTemplatesPath);
    }
  }

  private static void addServiceTemplateFiles(Map<String, ServiceTemplate> serviceTemplates,
                                              File[] files,
                                              ToscaExtensionYamlUtil toscaExtensionYamlUtil)
      throws IOException {
    for (File file : files) {
      if (!file.getName().equals("CSR.zip") && !file.isDirectory()) {
        try (InputStream yamlFile = new FileInputStream(file)) {
          ServiceTemplate serviceTemplateFromYaml =
              toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
          serviceTemplates.put(file.getName(), serviceTemplateFromYaml);
          try {
            yamlFile.close();
          } catch (IOException ignore) {
          }
        } catch (FileNotFoundException e) {
          throw e;
        } catch (IOException e) {
          throw e;
        }
      }
    }
  }

//  @Test
  public void testEnrichmentManagerImpl() throws Exception {
    Assert.assertTrue(
        EnrichmentManagerFactory.getInstance().createInterface() instanceof EnrichmentManagerImpl);
  }

//  @Test
  public void testEnrichModel() throws Exception {
    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/extractServiceComposition/onlyComponents/",
            "/extractServiceComposition/toscaGlobalServiceTemplates/", "OnlyComponentsST.yaml");
    EnrichmentManager enrichmentManager = EnrichmentManagerFactory.getInstance().createInterface();
    enrichmentManager.initInput("vsp_enrich", new Version(0, 1));
    enrichmentManager.addModel(toscaServiceModel);

    String[] componentNames = new String[]{"org.openecomp.resource.vfc.nodes.heat.pcrf_psm",
        "org.openecomp.resource.vfc.nodes.heat.pcm"};
    ComponentInfo componentInfo = new ComponentInfo();

    CeilometerInfo ceilometerInfo;
    ComponentCeilometerInfo componentCeilometerInfo = new ComponentCeilometerInfo();
    componentCeilometerInfo.setCeilometerInfoList(new ArrayList<>());
    componentInfo.setCeilometerInfo(componentCeilometerInfo);

    for (String componentName : componentNames) {
      ceilometerInfo =
          getCeilometerInfo("instance", "Gauge", "instance", "compute", "Existence of instance");
      componentInfo.getCeilometerInfo().getCeilometerInfoList().add(ceilometerInfo);
      ceilometerInfo = getCeilometerInfo("memory", "Gauge", "MB", "compute",
          "Volume of RAM allocated to the instance");
      componentInfo.getCeilometerInfo().getCeilometerInfoList().add(ceilometerInfo);
      ceilometerInfo = getCeilometerInfo("cpu", "Cumulative", "ns", "compute", "CPU time used");
      componentInfo.getCeilometerInfo().getCeilometerInfoList().add(ceilometerInfo);
      enrichmentManager.addEntityInput(componentName, componentInfo);
    }

    enrichmentManager.enrich();

    File csrFile = getToscaModelAsFile(toscaServiceModel);
    compareActualAndExpected(csrFile);

  }

//  @Test
  public void testAllEnrichModel() throws Exception {
    ToscaServiceModel toscaServiceModel = loadToscaServiceModel("/extractServiceComposition/all/",
        "/extractServiceComposition/toscaGlobalServiceTemplates/", "OnlyComponentsST.yaml");
    EnrichmentManager enrichmentManager = EnrichmentManagerFactory.getInstance().createInterface();
    enrichmentManager.initInput("vsp_enrich", new Version(0, 1));
    enrichmentManager.addModel(toscaServiceModel);

    String[] componentNames = new String[]{"org.openecomp.resource.vfc.nodes.heat.pcrf_psm",
        "org.openecomp.resource.vfc.nodes.heat.pcm"};
    ComponentInfo componentInfo = new ComponentInfo();

    CeilometerInfo ceilometerInfo;
    ComponentCeilometerInfo componentCeilometerInfo = new ComponentCeilometerInfo();
    componentCeilometerInfo.setCeilometerInfoList(new ArrayList<>());
    componentInfo.setCeilometerInfo(componentCeilometerInfo);

    for (String componentName : componentNames) {
      ceilometerInfo =
          getCeilometerInfo("instance", "Gauge", "instance", "compute", "Existence of instance");
      componentInfo.getCeilometerInfo().getCeilometerInfoList().add(ceilometerInfo);
      ceilometerInfo = getCeilometerInfo("memory", "Gauge", "MB", "compute",
          "Volume of RAM allocated to the instance");
      componentInfo.getCeilometerInfo().getCeilometerInfoList().add(ceilometerInfo);
      ceilometerInfo = getCeilometerInfo("cpu", "Cumulative", "ns", "compute", "CPU time used");
      componentInfo.getCeilometerInfo().getCeilometerInfoList().add(ceilometerInfo);
      enrichmentManager.addEntityInput(componentName, componentInfo);
    }

    enrichmentManager.enrich();

    File csrFile = getToscaModelAsFile(toscaServiceModel);
    compareActualAndExpected(csrFile);

  }

  private CeilometerInfo getCeilometerInfo(String name, String type, String unit, String category,
                                           String description) {
    CeilometerInfo ceilometerInfo = new CeilometerInfo();
    ceilometerInfo.setName(name);
    ceilometerInfo.setType(type);
    ceilometerInfo.setUnit(unit);
    ceilometerInfo.setCategory(category);
    ceilometerInfo.setDescription(description);
    return ceilometerInfo;
  }

  private File getToscaModelAsFile(ToscaServiceModel toscaServiceModel) throws IOException {

    URL inputFilesUrl =
        EnrichmentManagerImplTest.class.getResource("/extractServiceComposition/onlyComponents");
    String path = inputFilesUrl.getPath();


    File file = new File(path + "/" + "CSR.zip");
    file.createNewFile();

    try (FileOutputStream fos = new FileOutputStream(file))

    {
      ToscaFileOutputService toscaFileOutputService = new ToscaFileOutputServiceCsarImpl();
      fos.write(toscaFileOutputService.createOutputFile(toscaServiceModel, null));
    }

    return file;
  }

  protected void compareActualAndExpected(File actualFile) throws IOException {

    URL url = EnrichmentManagerImplTest.class
        .getResource("/extractServiceComposition/onlyComponents/expectedOutput");
    Set<String> expectedResultFileNameSet = new HashSet<>();
    Map<String, byte[]> expectedResultMap = new HashMap<>();
    String path = url.getPath();
    File pathFile = new File(path);
    File[] files = pathFile.listFiles();
    org.junit.Assert.assertNotNull("manifest files is empty", files);
    for (File expectedFile : files) {
      expectedResultFileNameSet.add(expectedFile.getName());
      try (FileInputStream input = new FileInputStream(expectedFile)) {
        expectedResultMap.put(expectedFile.getName(), FileUtils.toByteArray(input));
      }
    }

    try (FileInputStream fis = new FileInputStream(actualFile);
         ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis))) {
      ZipEntry entry;
      String name;
      String expected;
      String actual;

      while ((entry = zis.getNextEntry()) != null) {

        name = entry.getName()
            .substring(entry.getName().lastIndexOf(File.separator) + 1, entry.getName().length());
        if (expectedResultFileNameSet.contains(name)) {
          expected = new String(expectedResultMap.get(name)).trim().replace("\r", "");
          actual = new String(FileUtils.toByteArray(zis)).trim().replace("\r", "");
          assertEquals("difference in file: " + name, expected, actual);

          expectedResultFileNameSet.remove(name);
        }
      }
      if (expectedResultFileNameSet.isEmpty()) {
        expectedResultFileNameSet.forEach(System.out::println);
      }
    }
    assertEquals(0, expectedResultFileNameSet.size());
  }


}