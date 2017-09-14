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

package org.openecomp.sdc.tosca;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.Map;

public class TestUtil {

  private static final Logger logger = LoggerFactory.getLogger(TestUtil.class);

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
      try (InputStream yamlFile = new FileInputStream(file)) {
        ServiceTemplate serviceTemplateFromYaml =
            toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
        serviceTemplates.put(file.getName(), serviceTemplateFromYaml);
        try {
          yamlFile.close();
        } catch (IOException ignore) {
            logger.debug(ignore.getMessage(), ignore);
        }
      } catch (FileNotFoundException e) {
        throw e;
      } catch (IOException e) {
        throw e;
      }
    }
  }
}
