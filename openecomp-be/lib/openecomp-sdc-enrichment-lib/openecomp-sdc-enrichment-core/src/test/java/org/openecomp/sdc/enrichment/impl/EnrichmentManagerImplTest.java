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

package org.openecomp.sdc.enrichment.impl;

import org.openecomp.core.enrichment.factory.EnrichmentManagerFactory;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;


public class EnrichmentManagerImplTest {

  private final static Logger log = (Logger) LoggerFactory.getLogger
      (EnrichmentManagerImplTest.class.getName());


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
            log.debug("",ignore);
          }
        } catch (FileNotFoundException e) {
          throw e;
        } catch (IOException e) {
          throw e;
        }
      }
    }
  }

  @Test
  public void testEnrichmentManagerImpl() throws Exception {
    Assert.assertTrue(
        EnrichmentManagerFactory.getInstance().createInterface() instanceof EnrichmentManagerImpl);
  }

  private File getToscaModelAsFile(ToscaServiceModel toscaServiceModel) throws IOException {

    URL inputFilesUrl = EnrichmentManagerImplTest.class.getResource("/mock/enrich/input");
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

  protected void compareActualAndExpected(File actualFile, String expectedOutputPath)
      throws IOException {

    URL url = EnrichmentManagerImplTest.class.getResource(expectedOutputPath);
    Set<String> expectedResultFileNameSet = new HashSet<>();
    Map<String, byte[]> expectedResultMap = new HashMap<>();
    String path = url.getPath();
    File pathFile = new File(path);
    File[] files = pathFile.listFiles();
    org.junit.Assert.assertNotNull("model is empty", files);
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
