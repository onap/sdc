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

package org.openecomp.sdc.validation.impl.validators;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.DefinedHeatParameterTypes;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.Environment;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatPseudoParameters;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Parameter;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.heat.services.manifest.ManifestUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.impl.util.HeatValidationService;
import org.openecomp.sdc.validation.util.ValidationUtil;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class HeatValidator implements Validator {
  public static final MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  protected static Logger logger = (Logger) LoggerFactory.getLogger(HeatValidator.class);
  private static final ErrorMessageCode ERROR_CODE_HOT_1 = new ErrorMessageCode("HOT1");
  private static final ErrorMessageCode ERROR_CODE_HOT_2 = new ErrorMessageCode("HOT2");
  private static final ErrorMessageCode ERROR_CODE_HOT_3 = new ErrorMessageCode("HOT3");
  private static final ErrorMessageCode ERROR_CODE_HOT_4 = new ErrorMessageCode("HOT4");
  private static final ErrorMessageCode ERROR_CODE_HOT_5 = new ErrorMessageCode("HOT5");
  private static final ErrorMessageCode ERROR_CODE_HOT_6 = new ErrorMessageCode("HOT6");
  private static final ErrorMessageCode ERROR_CODE_HOT_7 = new ErrorMessageCode("HOT7");
  private static final ErrorMessageCode ERROR_CODE_HOT_8 = new ErrorMessageCode("HOT8");
  private static final ErrorMessageCode ERROR_CODE_HOT_9 = new ErrorMessageCode("HOT9");
  private static final ErrorMessageCode ERROR_CODE_HOT_10 = new ErrorMessageCode("HOT10");
  private static final ErrorMessageCode ERROR_CODE_HOT_11 = new ErrorMessageCode("HOT11");
  private static final ErrorMessageCode ERROR_CODE_HOT_12 = new ErrorMessageCode("HOT12");
  private static final ErrorMessageCode ERROR_CODE_HOT_13 = new ErrorMessageCode("HOT13");
  private static final ErrorMessageCode ERROR_CODE_HOT_14 = new ErrorMessageCode("HOT14");
  private static final ErrorMessageCode ERROR_CODE_HOT_15 = new ErrorMessageCode("HOT15");
  private static final ErrorMessageCode ERROR_CODE_HOT_16 = new ErrorMessageCode("HOT16");
  private static final ErrorMessageCode ERROR_CODE_HOT_17 = new ErrorMessageCode("HOT17");

  private static void validateAllRequiredArtifactsExist(String fileName,
                                                        HeatOrchestrationTemplate
                                                            heatOrchestrationTemplate,
                                                        Set<String> artifacts,
                                                        GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Collection<Resource> resourcesValues = heatOrchestrationTemplate.getResources() == null ? null
        : heatOrchestrationTemplate.getResources().values();

    if (CollectionUtils.isNotEmpty(resourcesValues)) {
      for (Resource resource : resourcesValues) {
        Collection<Object> properties =
            resource.getProperties() == null ? null : resource.getProperties().values();
        if (CollectionUtils.isNotEmpty(properties)) {
          for (Object property : properties) {
            if (property instanceof Map) {
              globalContext.setMessageCode(ERROR_CODE_HOT_14);
              Set<String> artifactNames = HeatStructureUtil
                  .getReferencedValuesByFunctionName(fileName,
                      ResourceReferenceFunctions.GET_FILE.getFunction(), property, globalContext);
              artifacts.addAll(artifactNames);
              globalContext.setMessageCode(ERROR_CODE_HOT_15);
              HeatValidationService.checkArtifactsExistence(fileName, artifactNames, globalContext);
            }
          }
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  /* validation 14 */

  private static void validateAllResourceReferencesExist(String fileName,
                                                         HeatOrchestrationTemplate
                                                             heatOrchestrationTemplate,
                                                         GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Set<String> resourcesNames = heatOrchestrationTemplate.getResources() == null ? null
        : heatOrchestrationTemplate.getResources().keySet();
    Collection<Resource> resourcesValues = heatOrchestrationTemplate.getResources() == null ? null
        : heatOrchestrationTemplate.getResources().values();
    Collection<Output> outputsValues = heatOrchestrationTemplate.getOutputs() == null ? null
        : heatOrchestrationTemplate.getOutputs().values();
    checkResourceExistenceFromResourcesMap(fileName, resourcesNames, resourcesValues,
        globalContext);
    checkResourceExistenceFromResourcesMap(fileName, resourcesNames, outputsValues,
        globalContext);

    mdcDataDebugMessage.debugExitMessage("file", fileName);

  }

  private static void checkResourceExistenceFromResourcesMap(String fileName,
                                                             Set<String> resourcesNames,
                                                             Collection<?> valuesToSearchIn,
                                                             GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    if (CollectionUtils.isNotEmpty(valuesToSearchIn)) {
      for (Object value : valuesToSearchIn) {
        if (value instanceof Resource) {
          extractResourceProperty(fileName, resourcesNames, globalContext, (Resource) value);
        } else if (value instanceof Output) {
          Output output = (Output) value;
          Object outputsValue = output.getValue();
          handleReferencedResources(fileName, outputsValue, resourcesNames,
              globalContext);
        }
      }
    }
  }

  private static void extractResourceProperty(String fileName, Set<String> resourcesNames,
                                              GlobalValidationContext globalContext,
                                              Resource value) {
    Resource resource = value;
    Collection<Object> resourcePropertiesValues =
        resource.getProperties() == null ? null : resource.getProperties()
            .values();
    if (CollectionUtils.isNotEmpty(resourcePropertiesValues)) {
      for (Object propertyValue : resourcePropertiesValues) {
        handleReferencedResources(fileName, propertyValue, resourcesNames,
            globalContext);
      }
    }
  }

  private static void handleReferencedResources(String fileName, Object valueToSearchReferencesIn,
                                                Set<String> resourcesNames,
                                                GlobalValidationContext globalContext) {


    mdcDataDebugMessage.debugEntryMessage("file", fileName);
    globalContext.setMessageCode(ERROR_CODE_HOT_13);
    Set<String> referencedResourcesNames = HeatStructureUtil
        .getReferencedValuesByFunctionName(fileName,
            ResourceReferenceFunctions.GET_RESOURCE.getFunction(),
            valueToSearchReferencesIn, globalContext);
    if (CollectionUtils.isNotEmpty(referencedResourcesNames)) {
      checkIfResourceReferenceExist(fileName, resourcesNames, referencedResourcesNames,
          globalContext);
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private static void checkIfResourceReferenceExist(String fileName,
                                                    Set<String> referencedResourcesNames,
                                                    Set<String> referencedResources,
                                                    GlobalValidationContext globalContext) {


    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    referencedResources.stream()
        .filter(referencedResource -> !referencedResourcesNames.contains(referencedResource))
        .forEach(referencedResource -> {
          globalContext.addMessage(fileName,
              ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                  .getErrorWithParameters(ERROR_CODE_HOT_16,Messages
                      .REFERENCED_RESOURCE_NOT_FOUND.getErrorMessage(), referencedResource),
              LoggerTragetServiceName.VALIDATE_RESOURCE_REFERENCE_EXISTENCE,
              LoggerErrorDescription.RESOURCE_NOT_FOUND);
        });

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  /* validation 16 */

  private static void validateGetParamPointToParameter(String fileName,
                                                       HeatOrchestrationTemplate
                                                           heatOrchestrationTemplate,
                                                       GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Set<String> parametersNames = heatOrchestrationTemplate.getParameters() == null ? null
        : heatOrchestrationTemplate.getParameters().keySet();
    Map<String, Resource> resourcesMap = heatOrchestrationTemplate.getResources();

    if (CollectionUtils.isNotEmpty(parametersNames) && MapUtils.isNotEmpty(resourcesMap)) {
      for (Map.Entry<String, Resource> resourceEntry : resourcesMap.entrySet()) {
        Resource resource = resourceEntry.getValue();
        Map<String, Object> properties = resource.getProperties();
        if (MapUtils.isNotEmpty(properties)) {
          Collection<Object> propertiesValues = properties.values();
          if (CollectionUtils.isNotEmpty(propertiesValues)) {
            for (Object propertyObject : propertiesValues) {
              Set<String> referencedParameterNames = HeatStructureUtil
                  .getReferencedValuesByFunctionName(fileName, "get_param", propertyObject,
                      globalContext);

              validateReferenceParams(fileName, resourceEntry.getKey(), parametersNames,
                  referencedParameterNames, globalContext);
            }
          }
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private static void validateReferenceParams(String fileName, String resourceName,
                                              Set<String> parametersNamesFromFile,
                                              Set<String> referencedParametersNames,
                                              GlobalValidationContext globalContext) {


    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    for (String parameterName : referencedParametersNames) {
      if (!isHeatPseudoParameter(parameterName)
          && !parametersNamesFromFile.contains(parameterName)) {
        globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_CODE_HOT_1,Messages.
                        REFERENCED_PARAMETER_NOT_FOUND.getErrorMessage(),
                    parameterName, resourceName),
            LoggerTragetServiceName.VALIDATE_PARAMETER_REFERENCE_EXITENCE,
            LoggerErrorDescription.PARAMETER_NOT_FOUND);
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private static boolean isHeatPseudoParameter(String parameterName) {
    return HeatPseudoParameters.getPseudoParameterNames().contains(parameterName);
  }

  /* validation 18*/

  private static void validateGetAttr(String fileName,
                                      HeatOrchestrationTemplate heatOrchestrationTemplate,
                                      GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Map<String, Output> outputMap;
    outputMap = heatOrchestrationTemplate.getOutputs();

    if (MapUtils.isNotEmpty(outputMap)) {
      loopOverOutputMapAndValidateGetAttrFromNested(fileName, outputMap,
          heatOrchestrationTemplate, globalContext);
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private static void loopOverOutputMapAndValidateGetAttrFromNested(String fileName,
                                                                    Map<String, Output> outputMap,
                                                                    HeatOrchestrationTemplate
                                                                        heatOrchestrationTemplate,
                                                                    GlobalValidationContext
                                                                        globalContext) {
    for (Output output : outputMap.values()) {
      Object outputValue = output.getValue();
      if (outputValue != null && outputValue instanceof Map) {
        Map<String, Object> outputValueMap = (Map<String, Object>) outputValue;
        List<String> getAttrValue =
            (List<String>) outputValueMap.get(
                ResourceReferenceFunctions.GET_ATTR.getFunction());
        if (!CollectionUtils.isEmpty(getAttrValue)) {
          String resourceName = getAttrValue.get(0);
          Object attNameObject = getAttrValue.get(1);
          if (!(attNameObject instanceof String)) {
            return;
          }
          String attName = getAttrValue.get(1);
          String resourceType =
              getResourceTypeFromResourcesMap(resourceName, heatOrchestrationTemplate);

          if (Objects.nonNull(resourceType)
              && HeatValidationService.isNestedResource(resourceType)) {
            handleGetAttrNestedResource(fileName, globalContext, resourceName, attName,
                resourceType);
          }
        }
      }
    }
  }

  private static void handleGetAttrNestedResource(String fileName,
                                                  GlobalValidationContext globalContext,
                                                  String resourceName, String attName,
                                                  String resourceType) {
    Map<String, Output> nestedOutputMap;
    HeatOrchestrationTemplate nestedHeatOrchestrationTemplate;
    try {
      Optional<InputStream> fileContent = globalContext.getFileContent(resourceType);
      if (fileContent.isPresent()) {
        nestedHeatOrchestrationTemplate =
            new YamlUtil().yamlToObject(fileContent.get(), HeatOrchestrationTemplate.class);
      } else {
        MdcDataErrorMessage
            .createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
                LoggerTragetServiceName.VALIDATE_GET_ATTR_FROM_NESTED,
                ErrorLevel.ERROR.name(), LoggerErrorCode.DATA_ERROR.getErrorCode(),
                LoggerErrorDescription.EMPTY_FILE);
        throw new Exception("The file '" + resourceType + "' has no content");
      }
    } catch (Exception exception) {
      logger.debug("",exception);
      return;
    }
    nestedOutputMap = nestedHeatOrchestrationTemplate.getOutputs();

    if (MapUtils.isEmpty(nestedOutputMap) || !nestedOutputMap.containsKey(attName)) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(ERROR_CODE_HOT_17,Messages.
                      GET_ATTR_NOT_FOUND.getErrorMessage(),
                  attName, resourceName),
          LoggerTragetServiceName.VALIDATE_GET_ATTR_FROM_NESTED,
          LoggerErrorDescription.GET_ATTR_NOT_FOUND);
    }
  }

  private static String getResourceTypeFromResourcesMap(String resourceName,
                                                        HeatOrchestrationTemplate
                                                            heatOrchestrationTemplate) {
    return heatOrchestrationTemplate.getResources().get(resourceName).getType();
  }

  /* validation 17 + */
  private static void validateEnvFile(String fileName, String envFileName,
                                      HeatOrchestrationTemplate heatOrchestrationTemplate,
                                      GlobalValidationContext globalContext) {


    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Environment envContent;

    if (!envFileName.contains(".env")) {
      globalContext.addMessage(envFileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(ERROR_CODE_HOT_2,Messages
                  .WRONG_ENV_FILE_EXTENSION.getErrorMessage(), envFileName),
          LoggerTragetServiceName.VALIDATE_ENV_FILE, LoggerErrorDescription.WRONG_FILE_EXTENSION);
    }

    envContent = HeatValidationService.validateEnvContent(fileName, envFileName, globalContext);
    if (envContent != null) {
      validateEnvContentIsSubSetOfHeatParameters(envFileName, envContent, globalContext,
          heatOrchestrationTemplate);
      validateEnvParametersMatchDefinedHeatParameterTypes(envFileName, envContent, globalContext,
          heatOrchestrationTemplate);
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);

  }

  private static void validateEnvContentIsSubSetOfHeatParameters(String envFile,
                                                                 Environment envContent,
                                                                 GlobalValidationContext
                                                                     globalContext,
                                                                 HeatOrchestrationTemplate
                                                                     heatOrchestrationTemplate) {

    mdcDataDebugMessage.debugEntryMessage("file", envFile);

    Set<String> parametersNames = heatOrchestrationTemplate.getParameters() == null ? null
        : heatOrchestrationTemplate.getParameters().keySet();

    if (MapUtils.isNotEmpty(envContent.getParameters())) {
      if (CollectionUtils.isNotEmpty(parametersNames)) {
        for (Map.Entry<String, Object> envEntry : envContent.getParameters().entrySet()) {
          String envParameter = envEntry.getKey();
          if (parametersNames != null && !parametersNames.contains(envParameter)) {
            globalContext.addMessage(envFile, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                    .getErrorWithParameters(
                        ERROR_CODE_HOT_3,Messages.
                            ENV_INCLUDES_PARAMETER_NOT_IN_HEAT.getErrorMessage(), envFile,
                        envParameter), LoggerTragetServiceName.VALIDATE_ENV_FILE,
                LoggerErrorDescription.ENV_PARAMETER_NOT_IN_HEAT);
          }
        }
      } else {
        for (Map.Entry<String, Object> envEntry : envContent.getParameters().entrySet()) {
          globalContext.addMessage(envFile, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                  .getErrorWithParameters(ERROR_CODE_HOT_3,Messages
                          .ENV_INCLUDES_PARAMETER_NOT_IN_HEAT.getErrorMessage(),
                      envFile, envEntry.getKey()), LoggerTragetServiceName.VALIDATE_ENV_FILE,
              LoggerErrorDescription.ENV_PARAMETER_NOT_IN_HEAT);
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", envFile);
  }


  private static void validateParameterDefaultTypeAlignWithType(String fileName,
                                                                HeatOrchestrationTemplate
                                                                    heatOrchestrationTemplate,
                                                                GlobalValidationContext
                                                                    globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Map<String, Parameter> parametersMap = heatOrchestrationTemplate.getParameters() == null ? null
        : heatOrchestrationTemplate.getParameters();

    if (parametersMap != null && MapUtils.isNotEmpty(parametersMap)) {
      for (Map.Entry<String, Parameter> parameterEntry : parametersMap.entrySet()) {
        Parameter parameter = parameterEntry.getValue();
        String parameterType = parameter.getType();
        Object parameterDefault = parameter.get_default();
        if (parameterDefault != null && parameterType != null) {
          boolean isValueMatchDefault =
              DefinedHeatParameterTypes.isValueIsFromGivenType(parameterDefault, parameterType);
          if (!isValueMatchDefault) {
            globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                    .getErrorWithParameters(
                        ERROR_CODE_HOT_4,Messages.
                            PARAMETER_DEFAULT_VALUE_NOT_ALIGN_WITH_TYPE.getErrorMessage(),
                        parameterEntry.getKey(), parameterType),
                LoggerTragetServiceName.VALIDATE_PARAMTER_DEFAULT_MATCH_TYPE,
                LoggerErrorDescription.PARAMETER_DEFAULT_VALUE_NOT_ALIGNED_WITH_TYPE);
          }
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }


  private static void validateEnvParametersMatchDefinedHeatParameterTypes(String envFile,
                                                                          Environment envContent,
                                                                          GlobalValidationContext globalContext,
                                                                          HeatOrchestrationTemplate heatOrchestrationTemplate) {


    mdcDataDebugMessage.debugEntryMessage("file", envFile);

    Map<String, Parameter> heatParameters = heatOrchestrationTemplate.getParameters();

    if (MapUtils.isNotEmpty(heatParameters) && MapUtils.isNotEmpty(envContent.getParameters())) {
      for (Map.Entry<String, Object> envEntry : envContent.getParameters().entrySet()) {
        String parameterName = envEntry.getKey();
        Object parameterEnvValue = envEntry.getValue();
        Parameter parameterFromHeatFile = heatParameters.get(parameterName);
        if (parameterFromHeatFile != null) {
          String parameterType = parameterFromHeatFile.getType();
          if (!DefinedHeatParameterTypes.isEmptyValueInEnv(parameterEnvValue)
              && !DefinedHeatParameterTypes
              .isValueIsFromGivenType(parameterEnvValue, parameterType)) {
            globalContext.addMessage(envFile, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                    .getErrorWithParameters(
                        ERROR_CODE_HOT_5,Messages.
                            PARAMETER_ENV_VALUE_NOT_ALIGN_WITH_TYPE.getErrorMessage(),
                        parameterName), LoggerTragetServiceName.VALIDATE_ENV_PARAMETER_MATCH_TYPE,
                LoggerErrorDescription.PARAMETER_DEFAULT_VALUE_NOT_ALIGNED_WITH_TYPE);
          }
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", envFile);
  }

  @Override

  public void validate(GlobalValidationContext globalContext) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    ManifestContent manifestContent;
    try {
      manifestContent = ValidationUtil.validateManifest(globalContext);
    } catch (Exception exception) {
      logger.debug("",exception);
      return;
    }
    String baseFileName;
    Map<String, FileData.Type> fileTypeMap = ManifestUtil.getFileTypeMap(manifestContent);
    Map<String, FileData> fileEnvMap = ManifestUtil.getFileAndItsEnv(manifestContent);
    Set<String> baseFiles = ManifestUtil.getBaseFiles(manifestContent);
    Set<String> securityGroupsNamesFromBaseFileOutputs;
    Set<String> artifacts = new HashSet<>();


    baseFileName = CollectionUtils.isEmpty(baseFiles) ? null : baseFiles.iterator().next();
    securityGroupsNamesFromBaseFileOutputs = baseFileName == null ? null
        : checkForBaseFilePortsExistenceAndReturnSecurityGroupNamesFromOutputsIfNot(baseFileName,
            globalContext);


    globalContext.getFiles().stream()
        .filter(fileName -> FileData.isHeatFile(fileTypeMap.get(fileName))).forEach(
        fileName -> validate(fileName, fileEnvMap.get(fileName) == null ? null : fileEnvMap.get(
            fileName).getFile(),
            baseFileName == null ? null : baseFileName, artifacts,
            securityGroupsNamesFromBaseFileOutputs, globalContext));


    Set<String> manifestArtifacts = ManifestUtil.getArtifacts(manifestContent);

    globalContext.getFiles().stream()
        .filter(fileName -> isManifestArtifact(manifestArtifacts, fileName) &&
            isNotArtifact(artifacts, fileName))
        .forEach(fileName -> globalContext.addMessage(fileName, ErrorLevel.WARNING,
            ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_CODE_HOT_11,
                    Messages.ARTIFACT_FILE_NOT_REFERENCED.getErrorMessage()),
            LoggerTragetServiceName.CHECK_FOR_ORPHAN_ARTIFACTS,
            LoggerErrorDescription.ARTIFACT_NOT_REFERENCED));

    mdcDataDebugMessage.debugExitMessage(null, null);

  }

  private boolean isManifestArtifact(Set<String> manifestArtifacts, String fileName) {
    return manifestArtifacts.contains(fileName);
  }

  private boolean isNotArtifact(Set<String> artifacts, String fileName) {
    return !artifacts.contains(fileName);
  }

  private void validate(String fileName, String envFileName, String baseFileName,
                        Set<String> artifacts, Set<String> securityGroupsNamesFromBaseFileOutputs,
                        GlobalValidationContext globalContext) {
    globalContext.setMessageCode(ERROR_CODE_HOT_12);
    HeatOrchestrationTemplate
        heatOrchestrationTemplate = ValidationUtil.checkHeatOrchestrationPreCondition(
        fileName, globalContext);


    if (heatOrchestrationTemplate != null) {
      if (!(fileName.contains(".yaml") || fileName.contains(".yml"))) {
        globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_CODE_HOT_6,Messages
                    .WRONG_HEAT_FILE_EXTENSION.getErrorMessage(), fileName),
            LoggerTragetServiceName.CHECK_FOR_VALID_FILE_EXTENTION,
            LoggerErrorDescription.WRONG_FILE_EXTENSION);
      }

      validateHeatBaseStructure(fileName, heatOrchestrationTemplate, globalContext);
      validateParameterDefaultTypeAlignWithType(fileName, heatOrchestrationTemplate, globalContext);
      validateAllResourceReferencesExist(fileName, heatOrchestrationTemplate, globalContext);
      validateResourceDependsOn(fileName, heatOrchestrationTemplate, globalContext);
      validateGetParamPointToParameter(fileName, heatOrchestrationTemplate, globalContext);
      validateGetAttr(fileName, heatOrchestrationTemplate, globalContext);
      validateAllRequiredArtifactsExist(fileName, heatOrchestrationTemplate, artifacts,
          globalContext);

      if (envFileName != null) {
        validateEnvFile(fileName, envFileName, heatOrchestrationTemplate, globalContext);
      }
    }
  }

  private void validateResourceDependsOn(String fileName,
                                         HeatOrchestrationTemplate heatOrchestrationTemplate,
                                         GlobalValidationContext globalContext) {
    Map<String, Resource> resourcesMap = heatOrchestrationTemplate.getResources();
    if(MapUtils.isEmpty(resourcesMap)){
      return;
    }

    Set<String> resourcesNames = resourcesMap.keySet();

    resourcesMap.entrySet().stream()
        .forEach(entry -> checkResourceDependsOn(fileName, entry.getValue(),
            resourcesNames, globalContext));
  }

  @SuppressWarnings("unchecked")
  private static void checkResourceDependsOn(String fileName, Resource resource,
                                             Set<String> resourcesNames,
                                             GlobalValidationContext globalContext) {


    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Object dependencies = resource.getDepends_on();
    if (dependencies instanceof Collection) {
      ((Collection<String>) dependencies)
          .stream()
          .filter(resource_id -> !resourcesNames.contains(resource_id))
          .forEach(resource_id -> globalContext.addMessage(fileName, ErrorLevel.ERROR,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(ERROR_CODE_HOT_7,Messages.
                          MISSING_RESOURCE_IN_DEPENDS_ON.getErrorMessage(),
                      (String) resource_id), LoggerTragetServiceName.CHECK_RESOURCE_DEPENDS_ON,
              LoggerErrorDescription.MISSING_RESOURCE_DEPENDS_ON));
    } else if (dependencies instanceof String) {
      if (!resourcesNames.contains(dependencies)) {
        globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_CODE_HOT_8,Messages.
                        MISSING_RESOURCE_IN_DEPENDS_ON.getErrorMessage(),
                    (String) dependencies), LoggerTragetServiceName.CHECK_RESOURCE_DEPENDS_ON,
            LoggerErrorDescription.MISSING_RESOURCE_DEPENDS_ON);
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }


  private void validateHeatBaseStructure(String fileName,
                                         HeatOrchestrationTemplate heatOrchestrationTemplate,
                                         GlobalValidationContext globalContext) {


    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    if (heatOrchestrationTemplate.getHeat_template_version() == null) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(ERROR_CODE_HOT_9,Messages.
                      INVALID_HEAT_FORMAT_REASON.getErrorMessage(),
                  "missing template version"), LoggerTragetServiceName.VALIDATE_HEAT_FORMAT,
          LoggerErrorDescription.INVALID_HEAT_FORMAT);
    }
    if (heatOrchestrationTemplate.getResources() == null
        || heatOrchestrationTemplate.getResources().size() == 0) {
      globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
              .getErrorWithParameters(ERROR_CODE_HOT_10,Messages.
                      INVALID_HEAT_FORMAT_REASON.getErrorMessage(),
                  "The heat file does not contain any resources"),
          LoggerTragetServiceName.VALIDATE_HEAT_FORMAT, LoggerErrorDescription.INVALID_HEAT_FORMAT);
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private Set<String> checkForBaseFilePortsExistenceAndReturnSecurityGroupNamesFromOutputsIfNot(
      String baseFileName, GlobalValidationContext globalContext) {
    Set<String> securityGroupsNamesFromOutputsMap = new HashSet<>();
    HeatOrchestrationTemplate heatOrchestrationTemplate =
        ValidationUtil.checkHeatOrchestrationPreCondition(baseFileName, globalContext);

    if (heatOrchestrationTemplate != null) {
      Map<String, Resource> resourceMap = heatOrchestrationTemplate.getResources();
      if (!isPortResourceExistInBaseFile(resourceMap)) {
        getSecurityGroupsReferencedResourcesFromOutputs(securityGroupsNamesFromOutputsMap,
            heatOrchestrationTemplate.getOutputs(), resourceMap);
      }
    }
    return securityGroupsNamesFromOutputsMap;
  }


  @SuppressWarnings("unchecked")
  private void getSecurityGroupsReferencedResourcesFromOutputs(
      Set<String> securityGroupsNamesFromOutputsMap, Map<String, Output> outputMap,
      Map<String, Resource> resourceMap) {
    if (MapUtils.isNotEmpty(outputMap)) {
      for (Map.Entry<String, Output> outputEntry : outputMap.entrySet()) {
        Object outputValue = outputEntry.getValue().getValue();
        if (Objects.nonNull(outputValue) && outputValue instanceof Map) {
          String resourceName = (String) ((Map) outputValue)
              .get(ResourceReferenceFunctions.GET_RESOURCE.getFunction());
          if (Objects.nonNull(resourceName)) {
            Resource resource = resourceMap.get(resourceName);
            if (Objects.nonNull(resource) && resource.getType().equals(
                HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource())) {
              securityGroupsNamesFromOutputsMap.add(outputEntry.getKey());
            }
          }
        }
      }
    }
  }


  private boolean isPortResourceExistInBaseFile(Map<String, Resource> resourceMap) {
    for (Map.Entry<String, Resource> resourceEntry : resourceMap.entrySet()) {
      if (resourceEntry.getValue().getType()
          .equals(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource())) {
        return true;
      }
    }

    return false;
  }
}
