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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validateComputeConnectivityIn;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validateComputeConnectivityOut;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validateDependsOnInConsolidationData;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validateGetAttr;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validateGroupsInConsolidationData;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validateNestedConsolidationData;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validatePortConnectivityIn;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validatePortConnectivityOut;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validatePortsInConsolidationData;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validateNestedConsolidationDataNodeTemplateIds;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validateVolumeInConsolidationData;

import org.apache.commons.collections4.MapUtils;
import org.junit.Assert;
import org.junit.Before;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestFile;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.TestUtils;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileComputeConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FilePortConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.TypeComputeConsolidationData;
import org.openecomp.sdc.translator.services.heattotosca.TranslationService;
import org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataValidationType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class BaseResourceTranslationTest {

  protected String inputFilesPath;
  protected String outputFilesPath;
  protected TranslationContext translationContext;

  private String zipFilename = "VSP.zip";
  private TranslationService translationService;
  private File translatedZipFile;

  private Map<String, byte[]> expectedResultMap = new HashMap<>();
  private Set<String> expectedResultFileNameSet = new HashSet<>();

  private final String MANIFEST_NAME = SdcCommon.MANIFEST_NAME;
  private String validationFilename = "validationOutput.json";

  @Before
  public void setUp() throws IOException {
    initTranslatorAndTranslate();
  }

  protected void initTranslatorAndTranslate() throws IOException {
    translationService = new TranslationService();
    translationContext = new TranslationContext();
    translatedZipFile = translateZipFile();
  }

  protected void testTranslation() throws IOException {

    URL url = BaseResourceTranslationTest.class.getResource(outputFilesPath);

    String path = url.getPath();
    File pathFile = new File(path);
    File[] files = pathFile.listFiles();
    Assert.assertNotNull("manifest files is empty", files);
    for (File expectedFile : files) {
      expectedResultFileNameSet.add(expectedFile.getName());
      try (FileInputStream input = new FileInputStream(expectedFile)) {
        expectedResultMap.put(expectedFile.getName(), FileUtils.toByteArray(input));
      }
    }

    try (FileInputStream fis = new FileInputStream(translatedZipFile);
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
          //assertEquals("difference in file: " + name, expected, actual);

          expectedResultFileNameSet.remove(name);
        }
      }
      if (expectedResultFileNameSet.isEmpty()) {
        expectedResultFileNameSet.forEach(System.out::println);
      }
    }
    assertEquals(0, expectedResultFileNameSet.size());
  }

  private File translateZipFile() throws IOException {
    URL inputFilesUrl = this.getClass().getResource(inputFilesPath);
    String path = inputFilesUrl.getPath();
    addFilesToTranslator(translationContext, path);
    TranslatorOutput translatorOutput = translationService.translateHeatFiles(translationContext);
    Assert.assertNotNull(translatorOutput);
    if (MapUtils.isNotEmpty(translatorOutput.getErrorMessages()) && MapUtils.isNotEmpty(
        MessageContainerUtil
            .getMessageByLevel(ErrorLevel.ERROR, translatorOutput.getErrorMessages()))) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.VALIDATE_HEAT_BEFORE_TRANSLATE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't translate HEAT file");
      throw new CoreException((new ErrorCode.ErrorCodeBuilder()).withMessage(
          "Error in validation " + getErrorAsString(translatorOutput.getErrorMessages()))
          .withId("Validation Error").withCategory(ErrorCategory.APPLICATION).build());
    }
    File file = new File(path + "/" + zipFilename);
    file.createNewFile();

    try (FileOutputStream fos = new FileOutputStream(file)) {
      ToscaFileOutputService toscaFileOutputService = new ToscaFileOutputServiceCsarImpl();
      fos.write(
          toscaFileOutputService.createOutputFile(translatorOutput.getToscaServiceModel(), null));
    }

    return file;
  }

  private String getErrorAsString(Map<String, List<ErrorMessage>> errorMessages) {
    StringBuilder sb = new StringBuilder();
    errorMessages.entrySet().forEach(
        entry -> sb.append("File:").append(entry.getKey()).append(System.lineSeparator())
            .append(getErrorList(entry.getValue())));

    return sb.toString();
  }

  private String getErrorList(List<ErrorMessage> errors) {
    StringBuilder sb = new StringBuilder();
    errors.forEach(
        error -> sb.append(error.getMessage()).append("[").append(error.getLevel()).append("]")
            .append(System.lineSeparator()));
    return sb.toString();
  }

  public void addFilesToTranslator(TranslationContext translationContext, String path)
      throws IOException {
    File manifestFile = new File(path);
    File[] files = manifestFile.listFiles();
    byte[] fileContent;

    Assert.assertNotNull("manifest files is empty", files);

    for (File file : files) {

      try (FileInputStream fis = new FileInputStream(file)) {

        fileContent = FileUtils.toByteArray(fis);

        if (file.getName().equals(MANIFEST_NAME)) {
          addManifest(translationContext, MANIFEST_NAME, fileContent);
        } else {
          if (!file.getName().equals(zipFilename) && (!file.getName().equals(validationFilename))) {
            addFile(translationContext, file.getName(), fileContent);
          }
        }
      }
    }
  }

  public static void addManifest(TranslationContext translationContext,
                                 String name, byte[] content) {
    ManifestContent manifestData = JsonUtil.json2Object(new String(content), ManifestContent.class);
    ManifestFile manifest = new ManifestFile();
    manifest.setName(name);
    manifest.setContent(manifestData);
    translationContext.setManifest(manifest);
    translationContext.addFile(name, content);
    addFilesFromManifestToTranslationContextManifestFilesMap(translationContext, manifestData
        .getData());
  }

  public static void addFile(TranslationContext translationContext,
                             String name, byte[] content) {
    translationContext.addFile(name, content);
  }

  private static void addFilesFromManifestToTranslationContextManifestFilesMap(TranslationContext
                                                                                   translationContext, List<FileData> fileDataListFromManifest) {
    for (FileData fileFromManfiest : fileDataListFromManifest) {
      translationContext.addManifestFile(fileFromManfiest.getFile(), fileFromManfiest.getType());
    }
  }

  public void validateNodeTemplateIdInNestedConsolidationData(){
    ConsolidationData consolidationData = translationContext.getConsolidationData();
    Map<String, ServiceTemplate> expectedServiceTemplateModels = TestUtils.getServiceTemplates
        (expectedResultMap);
    Assert.assertNotNull(consolidationData);
    validateNestedConsolidationDataNodeTemplateIds(consolidationData,expectedServiceTemplateModels);
  }

  public void validateComputeTemplateConsolidationData(ConsolidationDataValidationType
                                                           validationType,
                                                       String testName) {
    ConsolidationData consolidationData = translationContext.getConsolidationData();
    Map<String, ServiceTemplate> expectedServiceTemplateModels = TestUtils.getServiceTemplates
        (expectedResultMap);
    Assert.assertNotNull(consolidationData);
    Assert.assertNotNull(consolidationData.getComputeConsolidationData());
    Set<String> serviceTemplateFileNames = consolidationData.getComputeConsolidationData()
        .getAllServiceTemplateFileNames();
    Assert.assertNotNull(serviceTemplateFileNames);
    for(String serviceTemplateName : serviceTemplateFileNames){
      Assert.assertTrue(expectedServiceTemplateModels.containsKey(serviceTemplateName));
      ServiceTemplate expectedServiceTemplate = expectedServiceTemplateModels.get
          (serviceTemplateName);
      FileComputeConsolidationData fileComputeConsolidationData = consolidationData
          .getComputeConsolidationData().getFileComputeConsolidationData(serviceTemplateName);
      Assert.assertNotNull(fileComputeConsolidationData);
      Set<String> computeTypes = fileComputeConsolidationData.getAllComputeTypes();
      Assert.assertNotNull(computeTypes);
      for(String computeType : computeTypes) {
        TypeComputeConsolidationData typeComputeConsolidationData = fileComputeConsolidationData
            .getTypeComputeConsolidationData(computeType);
        Assert.assertNotNull(typeComputeConsolidationData);

        Set<String> computeNodeTemplateIds = typeComputeConsolidationData
            .getAllComputeNodeTemplateIds();
        Assert.assertNotNull(computeNodeTemplateIds);
        Assert.assertNotEquals(computeNodeTemplateIds.size(), 0);

        for(String computeNodeTemplateId : computeNodeTemplateIds) {
          ComputeTemplateConsolidationData computeTemplateConsolidationData =
              typeComputeConsolidationData.getComputeTemplateConsolidationData
                  (computeNodeTemplateId);
          switch(validationType){
            case VALIDATE_GROUP:
              validateGroupsInConsolidationData(computeNodeTemplateId,
                  computeTemplateConsolidationData, expectedServiceTemplate);
              break;
            case VALIDATE_PORT:
              validatePortsInConsolidationData(computeNodeTemplateId,
                  computeTemplateConsolidationData,
                  expectedServiceTemplate);
              break;
            case VALIDATE_VOLUME:
              validateVolumeInConsolidationData(computeNodeTemplateId,
                  computeTemplateConsolidationData, expectedServiceTemplate, testName);
              break;
            case VALIDATE_CONNECTIVITY:
              validateComputeConnectivityIn(computeTemplateConsolidationData,
                  expectedServiceTemplate);
              validateComputeConnectivityOut(computeNodeTemplateId,computeTemplateConsolidationData,
                  expectedServiceTemplate);
              break;
            case VALIDATE_DEPENDS_ON:
              validateDependsOnInConsolidationData(computeNodeTemplateId,
                  computeTemplateConsolidationData,
                  expectedServiceTemplate, testName);
              break;
          }
        }
      }
    }
  }

  public void validateGetAttribute(String testName){
    Map<String, ServiceTemplate> expectedServiceTemplateModels = TestUtils.getServiceTemplates
        (expectedResultMap);
    validateGetAttr(translationContext,expectedServiceTemplateModels,testName);
  }

  public void validateNestedTemplateConsolidationData(String testName){
    validateNestedConsolidationData(translationContext, testName);
  }

  public void validatePortTemplateConsolidationData(ConsolidationDataValidationType
                                                        validationType,
                                                    String testName) {
    ConsolidationData consolidationData = translationContext.getConsolidationData();
    Map<String, ServiceTemplate> expectedServiceTemplateModels = TestUtils.getServiceTemplates
        (expectedResultMap);
    Assert.assertNotNull(consolidationData);
    Assert.assertNotNull(consolidationData.getPortConsolidationData());
    Set<String> serviceTemplateFileNames = consolidationData.getPortConsolidationData()
        .getAllServiceTemplateFileNames();
    Assert.assertNotNull(serviceTemplateFileNames);
    for(String serviceTemplateName : serviceTemplateFileNames){
      Assert.assertTrue(expectedServiceTemplateModels.containsKey(serviceTemplateName));
      ServiceTemplate expectedServiceTemplate = expectedServiceTemplateModels.get
          (serviceTemplateName);
      FilePortConsolidationData filePortConsolidationData = consolidationData
          .getPortConsolidationData().getFilePortConsolidationData(serviceTemplateName);
      Assert.assertNotNull(filePortConsolidationData);

      Set<String> portNodeTemplateIds = filePortConsolidationData.getAllPortNodeTemplateIds();
      Assert.assertNotNull(portNodeTemplateIds);
      Assert.assertNotEquals(portNodeTemplateIds.size(), 0);

      for(String portNodeTemplateId : portNodeTemplateIds) {
        PortTemplateConsolidationData portTemplateConsolidationData =
            filePortConsolidationData.getPortTemplateConsolidationData(portNodeTemplateId);
        switch(validationType){
          case VALIDATE_CONNECTIVITY:
            validatePortConnectivityIn(portTemplateConsolidationData,expectedServiceTemplate);
            validatePortConnectivityOut(portNodeTemplateId, portTemplateConsolidationData,
                expectedServiceTemplate);
            break;
        }
      }
    }
  }
}
