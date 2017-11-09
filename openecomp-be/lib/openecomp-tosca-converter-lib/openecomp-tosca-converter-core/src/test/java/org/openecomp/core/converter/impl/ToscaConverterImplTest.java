package org.openecomp.core.converter.impl;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.core.converter.ToscaConverter;
import org.openecomp.core.impl.ToscaConverterImpl;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.openecomp.core.converter.datatypes.Constants.globalStName;
import static org.openecomp.core.converter.datatypes.Constants.mainStName;

public class ToscaConverterImplTest {

  private static final ToscaConverter toscaConverter = new ToscaConverterImpl();
  private static final String VIRTUAL_LINK = "virtualLink";
  private static final String UNBOUNDED = "UNBOUNDED";
  private static final String BASE_DIR = "/mock/toscaConverter";


  @Test
  public void testConvertMainSt() throws IOException {
    String inputFilesPath = BASE_DIR + "/convertMainSt/in";
    String outputFilesPath = BASE_DIR + "/convertMainSt/out";

    convertAndValidate(inputFilesPath, outputFilesPath);
  }

  @Test
  public void testNodesConversion() throws IOException {
    String inputFilesPath = BASE_DIR + "/convertCsar/in";
    String outputFilesPath = BASE_DIR + "/convertCsar/out";

    convertAndValidate(inputFilesPath, outputFilesPath);
  }

  @Test
  public void testParameterConversion() throws IOException {
    String inputFilesPath = BASE_DIR + "/convertParameters/in";
    String outputFilesPath = BASE_DIR + "/convertParameters/out";

    convertAndValidate(inputFilesPath, outputFilesPath);
  }

  @Ignore
  public void testConversionWithInt() throws IOException {
    String inputFilesPath = BASE_DIR + "/conversionWithInt/in";
    String outputFilesPath = BASE_DIR + "/conversionWithInt/out";

    convertAndValidate(inputFilesPath, outputFilesPath);
  }

  @Test
  public void testOccurrencesUpperString() {
    Object[] occurrences = buildOccurrences("0", UNBOUNDED);
    Assert.assertEquals(occurrences[0], 0);
    Assert.assertEquals(occurrences[1], UNBOUNDED);
  }

  @Test
  public void testOccurrencesAsInts() {
    Object[] occurrences = buildOccurrences("0", "1");
    Assert.assertEquals(occurrences[0], 0);
    Assert.assertEquals(occurrences[1], 1);
  }

  @Test
  public void testOccurrencesAsStrings() {
    String test = "TEST_A";
    Object[] occurrences = buildOccurrences(UNBOUNDED, test);
    Assert.assertEquals(occurrences[0], UNBOUNDED);
    Assert.assertEquals(occurrences[1], test);
  }

  @Test
  public void testOccurrencesLowerString() {
    Object[] occurrences = buildOccurrences(UNBOUNDED, "100");
    Assert.assertEquals(occurrences[0], UNBOUNDED);
    Assert.assertEquals(occurrences[1], 100);
  }

  @Test
  public void testOccurrencesEmpty() {
    Object[] occurrences = buildOccurrences();
    Assert.assertEquals(occurrences.length, 0);
  }

  @Test
  public void testOccurrencesMany() {
    String test = "TEST_B";
    Object[] occurrences = buildOccurrences("1", "2", test);
    Assert.assertEquals(occurrences[0], 1);
    Assert.assertEquals(occurrences[1], 2);
    Assert.assertEquals(occurrences[2], test);
  }

  @Test
  public void testDefaultOccurrences() {
    Object[] occurrences = buildOccurrences((List<String>) null);
    Assert.assertEquals(1, occurrences[0]);
    Assert.assertEquals(1, occurrences[1]);
  }

  private Object[] buildOccurrences(String... bounds) {
    return buildOccurrences(Arrays.asList(bounds));
  }

  private void convertAndValidate(String inputFilesPath, String outputFilesPath)
      throws IOException {
    FileContentHandler fileContentHandler =
        createFileContentHandlerFromInput(inputFilesPath);

    ToscaServiceModel toscaServiceModel = toscaConverter.convert(fileContentHandler);
    validateConvertorOutput(outputFilesPath, toscaServiceModel);
  }

  private void validateConvertorOutput(String outputFilesPath, ToscaServiceModel toscaServiceModel)
      throws IOException {
    ServiceTemplate mainSt = toscaServiceModel.getServiceTemplates().get(mainStName);
    Map<String, ServiceTemplate> expectedOutserviceTemplates = new HashMap<>();
    loadServiceTemplates(outputFilesPath, new ToscaExtensionYamlUtil(),
        expectedOutserviceTemplates);

    checkSTResults(expectedOutserviceTemplates, null, mainSt);
  }

  private Object[] buildOccurrences(List<String> bounds) {
    NodeType nodeType = JsonUtil.json2Object("{derived_from=tosca.nodes.Root, description=MME_VFC, " +
            "properties={vendor={type=string, default=ERICSSON}, " +
            "csarVersion={type=string, default=v1.0}, csarProvider={type=string, default=ERICSSON}, " +
            "id={type=string, default=vMME}, version={type=string, default=v1.0}, csarType={type=string, default=NFAR}}, " +
            "requirements=[{virtualLink={" +
            (bounds == null ? "" : "occurrences=[" + String.join(", ", bounds) +   "], ") +
            "capability=tosca.capabilities.network.Linkable}}]}", NodeType.class);
    List<Map<String, RequirementDefinition>> requirements = nodeType.getRequirements();
    return requirements.get(0).get(VIRTUAL_LINK).getOccurrences();
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
                                              ToscaExtensionYamlUtil toscaExtensionYamlUtil) throws IOException {

    for (File file : files) {

      try (InputStream yamlFile = new FileInputStream(file)) {
        ServiceTemplate serviceTemplateFromYaml =
            toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
        createConcreteRequirementObjectsInServiceTemplate(serviceTemplateFromYaml, toscaExtensionYamlUtil);
        serviceTemplates.put(file.getName(), serviceTemplateFromYaml);
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
