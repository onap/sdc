package org.openecomp.sdc.healing.healers.util;

import org.junit.Assert;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TestUtil {
  private final static Logger log = (Logger) LoggerFactory.getLogger
      (TestUtil.class.getName());

  public static ToscaServiceModel loadToscaServiceModel(String serviceTemplatesPath,
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
    URL urlFile = TestUtil.class.getResource(serviceTemplatesPath);
    if (urlFile != null) {
      File pathFile = new File(urlFile.getFile());
      Collection<File> files = org.apache.commons.io.FileUtils.listFiles(pathFile, null, true);
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
                                              Collection<File> files,
                                              ToscaExtensionYamlUtil toscaExtensionYamlUtil)
      throws IOException {
    for (File file : files) {
      try (InputStream yamlFile = new FileInputStream(file)) {
        ServiceTemplate serviceTemplateFromYaml =
            toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
        serviceTemplates.put(ToscaUtil.getServiceTemplateFileName(serviceTemplateFromYaml), serviceTemplateFromYaml);
        try {
          yamlFile.close();
        } catch (IOException ignore) {
          log.debug("",ignore);
        }
      } catch (FileNotFoundException exception) {
        throw exception;
      } catch (IOException exception) {
        throw exception;
      }
    }
  }

  public static void compareToscaServiceModels(String expectedServiceModelPath,
                                                ToscaServiceModel actualServiceModel)
      throws IOException {
    ToscaServiceModel expectedServiceModel =
        loadToscaServiceModel(expectedServiceModelPath, null, null);

    Map<String, ServiceTemplate> expectedServiceTemplates =
        new HashMap<>(expectedServiceModel.getServiceTemplates());
    Map<String, ServiceTemplate> actualServiceTemplates =
        new HashMap<>(actualServiceModel.getServiceTemplates());

    for (Map.Entry<String, ServiceTemplate> expectedServiceTemplateEntry : expectedServiceTemplates.entrySet()) {
      String serviceTemplateName = expectedServiceTemplateEntry.getKey();
      ServiceTemplate actualServiceTemplate =
          actualServiceTemplates.get(serviceTemplateName);

      Assert.assertNotNull("Missing service template in service model : " + serviceTemplateName,actualServiceTemplate);
      org.junit.Assert.assertEquals("Difference in file " + serviceTemplateName,
          JsonUtil.object2Json(expectedServiceTemplateEntry.getValue()),
          JsonUtil.object2Json(actualServiceTemplate));
    }
  }
}
