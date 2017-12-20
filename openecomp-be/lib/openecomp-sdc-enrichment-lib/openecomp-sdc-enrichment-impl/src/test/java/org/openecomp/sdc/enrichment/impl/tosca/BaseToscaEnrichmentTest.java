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

package org.openecomp.sdc.enrichment.impl.tosca;

import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;

public class BaseToscaEnrichmentTest {

    protected String outputFilesPath;

    private final static Logger log = (Logger) LoggerFactory.getLogger
        (BaseToscaEnrichmentTest.class.getName());

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
        URL urlFile = BaseToscaEnrichmentTest.class.getResource(serviceTemplatesPath);
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


    /*public static ToscaServiceModel loadToscaServiceModel(String serviceTemplatesPath,
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
        URL urlFile = BaseToscaEnrichmentTest.class.getResource(serviceTemplatesPath);
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
                }
            } catch (FileNotFoundException e) {
                throw e;
            } catch (IOException e) {
                throw e;
            }
        }
    }*/

    void compareActualAndExpectedModel(ToscaServiceModel toscaServiceModel) throws IOException {

        ToscaFileOutputService toscaFileOutputService = new ToscaFileOutputServiceCsarImpl();
        byte[] toscaActualFile = toscaFileOutputService.createOutputFile(toscaServiceModel, null);

        URL url = BaseToscaEnrichmentTest.class.getResource(outputFilesPath);
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

        try (InputStream fis = new ByteArrayInputStream(toscaActualFile);
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
