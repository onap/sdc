package org.openecomp.core.converter.impl;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.openecomp.core.converter.ToscaConverter;
import org.openecomp.core.impl.ToscaConverterImpl;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.openecomp.core.converter.datatypes.Constants.globalStName;
import static org.openecomp.core.converter.datatypes.Constants.mainStName;

public class ToscaConverterImplTest {

  private static ToscaConverter toscaConverter = new ToscaConverterImpl();
  private static String inputFilesPath;
  private static String outputFilesPath;
  private static Map<String, ServiceTemplate> expectedOutserviceTemplates;


  @Test
  public void testConvertMainSt() throws IOException {
    inputFilesPath = "/mock/toscaConverter/convertMainSt/in";
    outputFilesPath = "/mock/toscaConverter/convertMainSt/out";

    FileContentHandler fileContentHandler =
        createFileContentHandlerFromInput(inputFilesPath);

    expectedOutserviceTemplates = new HashMap<>();
    loadServiceTemplates(outputFilesPath, new ToscaExtensionYamlUtil(),
        expectedOutserviceTemplates);

    ToscaServiceModel toscaServiceModel = toscaConverter.convert(fileContentHandler);
    ServiceTemplate mainSt = toscaServiceModel.getServiceTemplates().get(mainStName);

    checkSTResults(expectedOutserviceTemplates, null, mainSt);
  }



  private FileContentHandler createFileContentHandlerFromInput(String inputFilesPath)
      throws IOException {
    URL inputFilesUrl = this.getClass().getResource(inputFilesPath);
    String path = inputFilesUrl.getPath();
    File directory = new File(path);
    File[] listFiles = directory.listFiles();

    FileContentHandler fileContentHandler = new FileContentHandler();
    insertFilesIntoFileContentHandler(listFiles, fileContentHandler);
    return fileContentHandler;
  }

  private void insertFilesIntoFileContentHandler(File[] listFiles,
                                                 FileContentHandler fileContentHandler)
      throws IOException {
    byte[] fileContent;
    if(CollectionUtils.isEmpty(fileContentHandler.getFileList())) {
      fileContentHandler.setFiles(new HashMap<>());
    }

    for (File file : listFiles) {
      if(!file.isDirectory()) {
        try (FileInputStream fis = new FileInputStream(file)) {
          fileContent = FileUtils.toByteArray(fis);
          fileContentHandler.addFile(file.getPath(), fileContent);
        }
      }else{
        File[] currFileList = file.listFiles();
        insertFilesIntoFileContentHandler(currFileList, fileContentHandler);
      }

    }
  }

  private void checkSTResults(
      Map<String, ServiceTemplate> expectedOutserviceTemplates,
      ServiceTemplate gloablSubstitutionServiceTemplate, ServiceTemplate mainServiceTemplate) {
    YamlUtil yamlUtil = new YamlUtil();
    if (Objects.nonNull(gloablSubstitutionServiceTemplate)) {
      assertEquals("difference global substitution service template: ",
          yamlUtil.objectToYaml(expectedOutserviceTemplates.get(globalStName)),
          yamlUtil.objectToYaml(gloablSubstitutionServiceTemplate));
    }
    if (Objects.nonNull(mainServiceTemplate)) {
      assertEquals("difference main service template: ",
          yamlUtil.objectToYaml(expectedOutserviceTemplates.get(mainStName)),
          yamlUtil.objectToYaml(mainServiceTemplate));
    }
  }

  public static void loadServiceTemplates(String serviceTemplatesPath,
                                          ToscaExtensionYamlUtil toscaExtensionYamlUtil,
                                          Map<String, ServiceTemplate> serviceTemplates)
      throws IOException {
    URL urlFile = ToscaConverterImplTest.class.getResource(serviceTemplatesPath);
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
        createConcreteRequirementObjectsInServiceTemplate(serviceTemplateFromYaml, toscaExtensionYamlUtil);
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

  private static void createConcreteRequirementObjectsInServiceTemplate(ServiceTemplate
                                                                            serviceTemplateFromYaml,
                                                                        ToscaExtensionYamlUtil
                                                                            toscaExtensionYamlUtil) {

    if (serviceTemplateFromYaml == null
        || serviceTemplateFromYaml.getTopology_template() == null
        || serviceTemplateFromYaml.getTopology_template().getNode_templates() == null) {
      return;
    }

    //Creating concrete objects
    Map<String, NodeTemplate> nodeTemplates =
        serviceTemplateFromYaml.getTopology_template().getNode_templates();
    for (Map.Entry<String, NodeTemplate> entry : nodeTemplates.entrySet()) {
      NodeTemplate nodeTemplate = entry.getValue();
      List<Map<String, RequirementAssignment>> requirements = nodeTemplate.getRequirements();
      List<Map<String, RequirementAssignment>> concreteRequirementList = new ArrayList<>();
      if (requirements != null) {
        ListIterator<Map<String, RequirementAssignment>> reqListIterator = requirements
            .listIterator();
        while (reqListIterator.hasNext()){
          Map<String, RequirementAssignment> requirement = reqListIterator.next();
          Map<String, RequirementAssignment> concreteRequirement = new HashMap<>();
          for (Map.Entry<String, RequirementAssignment> reqEntry : requirement.entrySet()) {
            RequirementAssignment requirementAssignment = (toscaExtensionYamlUtil
                .yamlToObject(toscaExtensionYamlUtil.objectToYaml(reqEntry.getValue()),
                    RequirementAssignment.class));
            concreteRequirement.put(reqEntry.getKey(), requirementAssignment);
            concreteRequirementList.add(concreteRequirement);
            reqListIterator.remove();
          }
        }
        requirements.clear();
        requirements.addAll(concreteRequirementList);
        nodeTemplate.setRequirements(requirements);
      }
      System.out.println();
      //toscaExtensionYamlUtil.yamlToObject(nodeTemplate, NodeTemplate.class);
    }
  }
}
