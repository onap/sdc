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

package org.openecomp.sdc.validation.impl.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.tosca.services.YamlUtil;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.DefinedHeatParameterTypes;
import org.openecomp.sdc.heat.datatypes.model.Environment;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Parameter;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.validation.impl.validators.HeatValidator;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.HashSet;


public class HeatValidationService {

  private static final Logger logger = (Logger) LoggerFactory.getLogger(HeatValidator.class);
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  /**
   * Check artifacts existence.
   *
   * @param fileName the file name
   * @param artifactsNames the artifacts names
   * @param globalContext the global context
   */
  public static void checkArtifactsExistence(String fileName, Set<String> artifactsNames,
                                             GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);
    artifactsNames
            .stream()
            .filter(artifactName -> !globalContext.getFileContextMap().containsKey(artifactName))
            .forEach(artifactName -> {
              globalContext.addMessage(fileName,
                      ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                              .getErrorWithParameters(Messages.MISSING_ARTIFACT.getErrorMessage(),
                                      artifactName), LoggerTragetServiceName.VALIDATE_ARTIFACTS_EXISTENCE,
                      LoggerErrorDescription.MISSING_FILE);
            });

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  /**
   * Check resource existence from resources map.
   *
   * @param fileName the file name
   * @param resourcesNames the resources names
   * @param valuesToSearchIn the values to search in
   * @param globalContext the global context
   */
  public static void checkResourceExistenceFromResourcesMap(String fileName,
                                                            Set<String> resourcesNames,
                                                            Collection<?> valuesToSearchIn,
                                                            GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    if (CollectionUtils.isNotEmpty(valuesToSearchIn)) {
      for (Object value : valuesToSearchIn) {
        if (value instanceof Resource) {
          Resource resource = (Resource) value;

          Collection<Object> resourcePropertiesValues =
                  resource.getProperties() == null ? null : resource.getProperties().values();
          if (CollectionUtils.isNotEmpty(resourcePropertiesValues)) {
            for (Object propertyValue : resourcePropertiesValues) {
              handleReferencedResources(fileName, propertyValue, resourcesNames, globalContext);
            }
          }
        } else if (value instanceof Output) {
          Output output = (Output) value;
          Object outputsValue = output.getValue();
          handleReferencedResources(fileName, outputsValue, resourcesNames, globalContext);
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private static void handleReferencedResources(String fileName, Object valueToSearchReferencesIn,
                                                Set<String> resourcesNames,
                                                GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Set<String> referencedResourcesNames = HeatStructureUtil
            .getReferencedValuesByFunctionName(fileName,
                    ResourceReferenceFunctions.GET_RESOURCE.getFunction(), valueToSearchReferencesIn,
                    globalContext);
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
                              .getErrorWithParameters(Messages.REFERENCED_RESOURCE_NOT_FOUND.getErrorMessage(),
                                      referencedResource),
                      LoggerTragetServiceName.VALIDATE_RESOURCE_REFERENCE_EXISTENCE,
                      LoggerErrorDescription.RESOURCE_NOT_FOUND);
            });

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  /**
   * Draw files loop string.
   *
   * @param filesInPath the files in path
   * @return the string
   */
  public static String drawFilesLoop(List<String> filesInPath) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("[");
    int pathSize = filesInPath.size();

    for (int i = 0; i < pathSize; i++) {
      stringBuilder.append(filesInPath.get(i));
      if (i != pathSize - 1) {
        stringBuilder.append(" -- ");
      }
    }
    if (!filesInPath.get(0).equals(filesInPath.get(pathSize - 1))) {
      stringBuilder.append(" -- ");
      stringBuilder.append(filesInPath.get(0));
    }
    stringBuilder.append("]");

    return stringBuilder.toString();
  }

  /**
   * Check nested parameters.
   *
   * @param parentFileName the calling nested file name
   * @param nestedFileName the nested file name
   * @param globalContext the global context
   * @param parentParameters parent parameters.
   * @param nestedParameters nested parameters.
   * @param nestedParametersNames nested parameter names.
   */
  public static void checkNestedParameters(String parentFileName, String nestedFileName,
                                           GlobalValidationContext globalContext,
                                           Map<String, Parameter> parentParameters,
                                           Map<String, Parameter> nestedParameters,
                                           Set<String> nestedParametersNames) {

    mdcDataDebugMessage.debugEntryMessage("file", parentFileName);

    HeatOrchestrationTemplate parentHeatOrchestrationTemplate;
    HeatOrchestrationTemplate nestedHeatOrchestrationTemplate;

    try {
      Optional<InputStream> fileContent = globalContext.getFileContent(nestedFileName);
      if (fileContent.isPresent()) {
        nestedHeatOrchestrationTemplate =
                new YamlUtil().yamlToObject(fileContent.get(), HeatOrchestrationTemplate.class);
      } else {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
                LoggerTragetServiceName.VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS,
                ErrorLevel.ERROR.name(), LoggerErrorCode.DATA_ERROR.getErrorCode(),
                LoggerErrorDescription.EMPTY_FILE);
        throw new Exception("The file '" + nestedFileName + "' has no content");
      }
    } catch (Exception exception) {
      logger.debug("", exception);
      mdcDataDebugMessage.debugExitMessage("file", parentFileName);
      return;
    }

    try {
      Optional<InputStream> fileContent = globalContext.getFileContent(parentFileName);
      if (fileContent.isPresent()) {
        parentHeatOrchestrationTemplate =
                new YamlUtil().yamlToObject(fileContent.get(), HeatOrchestrationTemplate.class);
      } else {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
                LoggerTragetServiceName.VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS,
                ErrorLevel.ERROR.name(), LoggerErrorCode.DATA_ERROR.getErrorCode(),
                LoggerErrorDescription.EMPTY_FILE);
        throw new Exception("The file '" + parentFileName + "' has no content");
      }
    } catch (Exception exception) {
      logger.debug("", exception);
      mdcDataDebugMessage.debugExitMessage("file", parentFileName);
      return;
    }
    parentParameters.putAll(parentHeatOrchestrationTemplate.getParameters());
    nestedParameters.putAll(nestedHeatOrchestrationTemplate.getParameters());
    if (nestedParameters != null && nestedParameters.isEmpty() == false) {
      nestedParametersNames.addAll(nestedHeatOrchestrationTemplate.getParameters().keySet());
    } else {
      nestedParametersNames = null;
    }

    mdcDataDebugMessage.debugExitMessage("file", parentFileName);
  }

  /**
   * Check nested parameters.
   *
   * @param parentFileName the calling nested file name
   * @param nestedFileName the nested file name
   * @param resourceName the resource name
   * @param globalContext the global context
   * @param resourceFileProperties the resource file properties
   */
  public static void checkNestedParametersNoMissingParameterInNested(String parentFileName,
                                                                     String nestedFileName,
                                                                     String resourceName, Resource resource,
                                                                     Set<String> resourceFileProperties,
                                                                     Optional<String> indexVarValue,
                                                                     GlobalValidationContext globalContext) {
    mdcDataDebugMessage.debugEntryMessage("file", parentFileName);

    Map<String, Parameter> parentParameters = new HashMap<String, Parameter>();
    Map<String, Parameter> nestedParameters = new HashMap<String, Parameter>();
    Set<String> nestedParametersNames = new HashSet<String>();
    checkNestedParameters(parentFileName, nestedFileName, globalContext, parentParameters,
            nestedParameters, nestedParametersNames);

    checkNoMissingParameterInNested(parentFileName, nestedFileName, resourceName,
            resourceFileProperties, nestedParametersNames, globalContext);

    mdcDataDebugMessage.debugExitMessage("file", parentFileName);
  }

  /**
   * Check nested parameters.
   *
   * @param parentFileName the calling nested file name
   * @param nestedFileName the nested file name
   * @param resourceName the resource name
   * @param globalContext the global context
   * @param resourceFileProperties the resource file properties
   */
  public static void checkNestedInputValuesAlignWithType(String parentFileName,
                                                         String nestedFileName,
                                                         String resourceName, Resource resource,
                                                         Set<String> resourceFileProperties,
                                                         Optional<String> indexVarValue,
                                                         GlobalValidationContext globalContext) {
    mdcDataDebugMessage.debugEntryMessage("file", parentFileName);

    Map<String, Parameter> parentParameters = new HashMap<String, Parameter>();
    Map<String, Parameter> nestedParameters = new HashMap<String, Parameter>();
    Set<String> nestedParametersNames = new HashSet<String>();
    checkNestedParameters(parentFileName, nestedFileName, globalContext, parentParameters,
            nestedParameters, nestedParametersNames);

    checkNestedInputValuesAlignWithType(parentFileName, nestedFileName, parentParameters,
            nestedParameters, resourceName, resource, indexVarValue, globalContext);

    mdcDataDebugMessage.debugExitMessage("file", parentFileName);
  }

  private static void checkNoMissingParameterInNested(String parentFileName, String nestedFileName,
                                                      String resourceName,
                                                      Set<String> resourceFileProperties,
                                                      Set<String> nestedParametersNames,
                                                      GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("nested file", nestedFileName);

    if (CollectionUtils.isNotEmpty(nestedParametersNames)) {
      resourceFileProperties
              .stream()
              .filter(propertyName -> !nestedParametersNames.contains(propertyName))
              .forEach(propertyName -> globalContext
                      .addMessage(parentFileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                                      .getErrorWithParameters(
                                              globalContext.getMessageCode(),
                                              Messages.MISSING_PARAMETER_IN_NESTED.getErrorMessage(),
                                              nestedFileName, resourceName, propertyName),
                              LoggerTragetServiceName.VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS,
                              LoggerErrorDescription.MISSING_PARAMETER_IN_NESTED));
    }

    mdcDataDebugMessage.debugExitMessage("nested file", nestedFileName);
  }

  private static void checkNestedInputValuesAlignWithType(String parentFileName,
                                                          String nestedFileName,
                                                          Map<String, Parameter> parentParameters,
                                                          Map<String, Parameter> nestedParameters,
                                                          String resourceName, Resource resource,
                                                          Optional<String> indexVarValue,
                                                          GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("nested file", nestedFileName);

    Map<String, Object> properties = resource.getProperties();
    for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
      String parameterName = propertyEntry.getKey();
      Object parameterInputValue = propertyEntry.getValue();

      if (Objects.nonNull(parameterInputValue)) {
        if (parameterInputValue instanceof String) {
          if (indexVarValue.isPresent() && indexVarValue.get().equals(parameterInputValue)) {
            parameterInputValue = 3; //indexVarValue is actually number value in runtime
          }
          validateStaticValueForNestedInputParameter(parentFileName, nestedFileName, resourceName,
                  parameterName, parameterInputValue, nestedParameters.get(parameterName),
                  globalContext);
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage("nested file", nestedFileName);
  }

  private static void validateStaticValueForNestedInputParameter(String parentFileName,
                                                                 String nestedFileName,
                                                                 String resourceName,
                                                                 String parameterName,
                                                                 Object staticValue,
                                                                 Parameter parameterInNested,
                                                                 GlobalValidationContext
                                                                         globalContext) {

    mdcDataDebugMessage.debugEntryMessage("nested file", nestedFileName);

    if (parameterInNested == null) {
      return;
    }
    if (!DefinedHeatParameterTypes
            .isValueIsFromGivenType(staticValue, parameterInNested.getType())) {
      globalContext.addMessage(parentFileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                      .getErrorWithParameters(globalContext.getMessageCode(),
                              Messages.WRONG_VALUE_TYPE_ASSIGNED_NESTED_INPUT.getErrorMessage(),
                              resourceName, parameterName, nestedFileName),
              LoggerTragetServiceName.VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS,
              LoggerErrorDescription.WRONG_VALUE_ASSIGNED_NESTED_PARAMETER);
    }

    mdcDataDebugMessage.debugExitMessage("nested file", nestedFileName);
  }


  /**
   * Is nested loop exist in file boolean.
   *
   * @param callingFileName the calling file name
   * @param nestedFileName the nested file name
   * @param filesInLoop the files in loop
   * @param globalContext the global context
   * @return the boolean
   */
  public static boolean isNestedLoopExistInFile(String callingFileName, String nestedFileName,
                                                List<String> filesInLoop,
                                                GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", callingFileName);

    HeatOrchestrationTemplate nestedHeatOrchestrationTemplate;
    try {
      Optional<InputStream> fileContent = globalContext.getFileContent(nestedFileName);
      if (fileContent.isPresent()) {
        nestedHeatOrchestrationTemplate =
                new YamlUtil().yamlToObject(fileContent.get(), HeatOrchestrationTemplate.class);
      } else {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
                LoggerTragetServiceName.VALIDATE_NESTING_LOOPS, ErrorLevel.ERROR.name(),
                LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.EMPTY_FILE);
        throw new Exception("The file '" + nestedFileName + "' has no content");
      }

    } catch (Exception exception) {
      logger.debug("", exception);
      logger.warn("HEAT Validator will not be executed on file " + nestedFileName
              + " due to illegal HEAT format");

      mdcDataDebugMessage.debugExitMessage("file", callingFileName);
      return false;
    }
    filesInLoop.add(nestedFileName);
    Collection<Resource> nestedResources =
            nestedHeatOrchestrationTemplate.getResources() == null ? null
                    : nestedHeatOrchestrationTemplate.getResources().values();
    if (CollectionUtils.isNotEmpty(nestedResources)) {
      for (Resource resource : nestedResources) {
        String resourceType = resource.getType();

        if (Objects.nonNull(resourceType) && isNestedResource(resourceType)) {
          mdcDataDebugMessage.debugExitMessage("file", callingFileName);
          return resourceType.equals(callingFileName) || !filesInLoop.contains(resourceType)
                  && isNestedLoopExistInFile(callingFileName, resourceType, filesInLoop, globalContext);
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", callingFileName);
    return false;
  }


  /**
   * Loop over output map and validate get attr from nested.
   *
   * @param fileName the file name
   * @param outputMap the output map
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param globalContext the global context
   */
  @SuppressWarnings("unchecked")
  public static void loopOverOutputMapAndValidateGetAttrFromNested(String fileName,
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
                (List<String>) outputValueMap.get(ResourceReferenceFunctions.GET_ATTR.getFunction());
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
                  && isNestedResource(resourceType)) {
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
      logger.debug("", exception);
      return;
    }
    nestedOutputMap = nestedHeatOrchestrationTemplate.getOutputs();

    if (MapUtils.isEmpty(nestedOutputMap) || !nestedOutputMap.containsKey(attName)) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                      .getErrorWithParameters(Messages.GET_ATTR_NOT_FOUND.getErrorMessage(),
                              attName, resourceName),
              LoggerTragetServiceName.VALIDATE_GET_ATTR_FROM_NESTED,
              LoggerErrorDescription.GET_ATTR_NOT_FOUND);
    }
  }

  public static boolean isNestedResource(String resourceType) {
    return resourceType.contains(".yaml") || resourceType.contains(".yml");
  }

  private static String getResourceTypeFromResourcesMap(String resourceName,
                                                        HeatOrchestrationTemplate
                                                                heatOrchestrationTemplate) {
    return heatOrchestrationTemplate.getResources().get(resourceName).getType();
  }

  /**
   * Validate env content environment.
   *
   * @param fileName the file name
   * @param envFileName the env file name
   * @param globalContext the global context
   * @return the environment
   */
  public static Environment validateEnvContent(String fileName, String envFileName,
                                               GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("env file", envFileName);

    Environment envContent = null;
    try {
      Optional<InputStream> fileContent = globalContext.getFileContent(envFileName);
      if (fileContent.isPresent()) {
        envContent = new YamlUtil().yamlToObject(fileContent.get(), Environment.class);
      } else {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
                LoggerTragetServiceName.VALIDATE_ENV_FILE, ErrorLevel.ERROR.name(),
                LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.EMPTY_FILE);
        throw new Exception("The file '" + envFileName + "' has no content");
      }
    } catch (Exception exception) {
      logger.debug("", exception);
      mdcDataDebugMessage.debugExitMessage("env file", envFileName);
      return null;
    }
    return envContent;
  }


  public static String getResourceGroupResourceName(String resourceCallingToResourceGroup) {
    return "OS::Heat::ResourceGroup in " + resourceCallingToResourceGroup;
  }

}
