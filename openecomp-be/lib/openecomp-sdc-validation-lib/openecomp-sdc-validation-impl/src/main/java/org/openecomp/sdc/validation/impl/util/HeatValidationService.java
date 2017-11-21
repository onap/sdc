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
import org.openecomp.sdc.tosca.services.YamlUtil;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.DefinedHeatParameterTypes;
import org.openecomp.sdc.heat.datatypes.model.Environment;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Parameter;
import org.openecomp.sdc.heat.datatypes.model.Resource;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


public class HeatValidationService {

  private static final Logger logger = (Logger) LoggerFactory.getLogger(HeatValidator.class);
  private static final String NESTED_FILE = "nested file";
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  /**
   * Check artifacts existence.
   *
   * @param fileName       the file name
   * @param artifactsNames the artifacts names
   * @param globalContext  the global context
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
                  .getErrorWithParameters(globalContext.getMessageCode(),Messages.MISSING_ARTIFACT.getErrorMessage()
                      ,artifactName),
                  LoggerTragetServiceName.VALIDATE_ARTIFACTS_EXISTENCE,
              LoggerErrorDescription.MISSING_FILE);
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
   * @param parentFileName         the calling nested file name
   * @param nestedFileName         the nested file name
   * @param resourceName           the resource name
   * @param globalContext          the global context
   * @param resourceFileProperties the resource file properties
   */
  public static void checkNestedParameters(String parentFileName, String nestedFileName,
                                           String resourceName, Resource resource,
                                           Set<String> resourceFileProperties,
                                           Optional<String> indexVarValue,
                                           GlobalValidationContext globalContext) {


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
      logger.debug("",exception);
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
      logger.debug("",exception);
      mdcDataDebugMessage.debugExitMessage("file", parentFileName);
      return;
    }
    Map<String, Parameter> parentParameters = parentHeatOrchestrationTemplate.getParameters();
    Map<String, Parameter> nestedParameters = nestedHeatOrchestrationTemplate.getParameters();
    Set<String> nestedParametersNames =
        nestedParameters == null ? null : nestedHeatOrchestrationTemplate.getParameters().keySet();

    checkNoMissingParameterInNested(parentFileName, nestedFileName, resourceName,
        resourceFileProperties, nestedParametersNames, globalContext);
    checkNestedInputValuesAlignWithType(parentFileName, nestedFileName, parentParameters,
        nestedParameters, resourceName, resource, indexVarValue, globalContext);

    mdcDataDebugMessage.debugExitMessage("file", parentFileName);

  }

  private static void checkNoMissingParameterInNested(String parentFileName, String nestedFileName,
                                                      String resourceName,
                                                      Set<String> resourceFileProperties,
                                                      Set<String> nestedParametersNames,
                                                      GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage(NESTED_FILE, nestedFileName);

    if (CollectionUtils.isNotEmpty(nestedParametersNames)) {
      resourceFileProperties
          .stream()
          .filter(propertyName -> !nestedParametersNames.contains(propertyName))
          .forEach(propertyName -> globalContext
              .addMessage(parentFileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                      .getErrorWithParameters(Messages
                              .MISSING_PARAMETER_IN_NESTED.getErrorMessage(),
                          nestedFileName, resourceName, propertyName),
                  LoggerTragetServiceName.VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS,
                  LoggerErrorDescription.MISSING_PARAMETER_IN_NESTED));
    }

    mdcDataDebugMessage.debugExitMessage(NESTED_FILE, nestedFileName);
  }


  private static void checkNestedInputValuesAlignWithType(String parentFileName,
                                                          String nestedFileName,
                                                          Map<String, Parameter> parentParameters,
                                                          Map<String, Parameter> nestedParameters,
                                                          String resourceName, Resource resource,
                                                          Optional<String> indexVarValue,
                                                          GlobalValidationContext globalContext) {


    mdcDataDebugMessage.debugEntryMessage(NESTED_FILE, nestedFileName);

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

    mdcDataDebugMessage.debugExitMessage(NESTED_FILE, nestedFileName);
  }

  private static void validateStaticValueForNestedInputParameter(String parentFileName,
                                                                 String nestedFileName,
                                                                 String resourceName,
                                                                 String parameterName,
                                                                 Object staticValue,
                                                                 Parameter parameterInNested,
                                                                 GlobalValidationContext
                                                                     globalContext) {


    mdcDataDebugMessage.debugEntryMessage(NESTED_FILE, nestedFileName);

    if (parameterInNested == null) {
      return;
    }
    if (!DefinedHeatParameterTypes
        .isValueIsFromGivenType(staticValue, parameterInNested.getType())) {
      globalContext.addMessage(parentFileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages
                      .WRONG_VALUE_TYPE_ASSIGNED_NESTED_INPUT.getErrorMessage(),
                  resourceName, parameterName, nestedFileName),
          LoggerTragetServiceName.VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS,
          LoggerErrorDescription.WRONG_VALUE_ASSIGNED_NESTED_PARAMETER);
    }

    mdcDataDebugMessage.debugExitMessage(NESTED_FILE, nestedFileName);
  }


  /**
   * Is nested loop exist in file boolean.
   *
   * @param callingFileName the calling file name
   * @param nestedFileName  the nested file name
   * @param filesInLoop     the files in loop
   * @param globalContext   the global context
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
      logger.debug("",exception);
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

  public static boolean isNestedResource(String resourceType) {
    return resourceType.contains(".yaml") || resourceType.contains(".yml");
  }

  /**
   * Validate env content environment.
   *
   * @param fileName      the file name
   * @param envFileName   the env file name
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
      logger.debug("",exception);
      mdcDataDebugMessage.debugExitMessage("env file", envFileName);
      return null;
    }
    return envContent;
  }


  public static String getResourceGroupResourceName(String resourceCallingToResourceGroup) {
    return "OS::Heat::ResourceGroup in " + resourceCallingToResourceGroup;
  }

}
