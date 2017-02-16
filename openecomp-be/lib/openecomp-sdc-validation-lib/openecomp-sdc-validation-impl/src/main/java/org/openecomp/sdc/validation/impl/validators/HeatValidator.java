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
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.yaml.YamlUtil;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.interfaces.Validator;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.utils.AsdcCommon;
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
import org.openecomp.sdc.validation.impl.util.HeatValidationService;
import org.openecomp.sdc.validation.impl.util.ResourceValidationHeatValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class HeatValidator implements Validator {

  protected static Logger logger = LoggerFactory.getLogger(HeatValidator.class);

  /* validation 9*/
  private static void validateAllRequiredArtifactsExist(String fileName,
                                     HeatOrchestrationTemplate heatOrchestrationTemplate,
                                     Set<String> artifacts,
                                     GlobalValidationContext globalContext) {
    Collection<Resource> resourcesValues = heatOrchestrationTemplate.getResources() == null ? null
        : heatOrchestrationTemplate.getResources().values();

    if (CollectionUtils.isNotEmpty(resourcesValues)) {
      for (Resource resource : resourcesValues) {
        Collection<Object> properties =
            resource.getProperties() == null ? null : resource.getProperties().values();
        if (CollectionUtils.isNotEmpty(properties)) {
          for (Object property : properties) {
            if (property instanceof Map) {
              Set<String> artifactNames = HeatStructureUtil
                  .getReferencedValuesByFunctionName(fileName,
                      ResourceReferenceFunctions.GET_FILE.getFunction(), property, globalContext);
              artifacts.addAll(artifactNames);
              HeatValidationService.checkArtifactsExistence(fileName, artifactNames, globalContext);
            }
          }
        }
      }
    }


  }

  /* validation 14 */
  private static void validateAllResourceReferencesExist(String fileName,
                                     HeatOrchestrationTemplate heatOrchestrationTemplate,
                                     GlobalValidationContext globalContext) {

    Set<String> resourcesNames = heatOrchestrationTemplate.getResources() == null ? null
        : heatOrchestrationTemplate.getResources().keySet();
    Collection<Resource> resourcesValues = heatOrchestrationTemplate.getResources() == null ? null
        : heatOrchestrationTemplate.getResources().values();
    Collection<Output> outputsValues = heatOrchestrationTemplate.getOutputs() == null ? null
        : heatOrchestrationTemplate.getOutputs().values();

    HeatValidationService
        .checkResourceExistenceFromResourcesMap(fileName, resourcesNames, resourcesValues,
            globalContext);
    HeatValidationService
        .checkResourceExistenceFromResourcesMap(fileName, resourcesNames, outputsValues,
            globalContext);

  }

  /* validation 16 */
  private static void validateGetParamPointToParameter(String fileName,
                                    HeatOrchestrationTemplate heatOrchestrationTemplate,
                                    GlobalValidationContext globalContext) {
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
              //Set<String> referencedParameterNames = HeatValidationService
              // .getParameterNameFromGetParamMap(propertyObject);
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
  }

  private static void validateReferenceParams(String fileName, String resourceName,
                                              Set<String> parametersNamesFromFile,
                                              Set<String> referencedParametersNames,
                                              GlobalValidationContext globalContext) {

    for (String parameterName : referencedParametersNames) {
      if (!isHeatPseudoParameter(parameterName)
          && !parametersNamesFromFile.contains(parameterName)) {
        globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.REFERENCED_PARAMETER_NOT_FOUND.getErrorMessage(),
                parameterName, resourceName));
      }
    }
  }

  private static boolean isHeatPseudoParameter(String parameterName) {
    return HeatPseudoParameters.getPseudoParameterNames().contains(parameterName);
  }

  /* validation 18*/
  private static void validateGetAttr(String fileName,
                                      HeatOrchestrationTemplate heatOrchestrationTemplate,
                                      GlobalValidationContext globalContext) {
    Map<String, Output> outputMap;
    outputMap = heatOrchestrationTemplate.getOutputs();

    if (MapUtils.isNotEmpty(outputMap)) {
      HeatValidationService.loopOverOutputMapAndValidateGetAttrFromNested(fileName, outputMap,
          heatOrchestrationTemplate, globalContext);
    }
  }

  /* validation 17 + */
  private static void validateEnvFile(String fileName, String envFileName,
                                      HeatOrchestrationTemplate heatOrchestrationTemplate,
                                      GlobalValidationContext globalContext) {

    Environment envContent;

    if (!envFileName.contains(".env")) {
      globalContext.addMessage(envFileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.WRONG_ENV_FILE_EXTENSION.getErrorMessage(),
              envFileName));
    }

    envContent = HeatValidationService.validateEnvContent(fileName, envFileName, globalContext);
    if (envContent != null) {
      validateEnvContentIsSubSetOfHeatParameters(envFileName, envContent, globalContext,
          heatOrchestrationTemplate);
      validateEnvParametersMatchDefinedHeatParameterTypes(envFileName, envContent, globalContext,
          heatOrchestrationTemplate);
    }

  }

  private static void validateEnvContentIsSubSetOfHeatParameters(String envFile,
                                            Environment envContent,
                                            GlobalValidationContext globalContext,
                                            HeatOrchestrationTemplate heatOrchestrationTemplate) {
    Set<String> parametersNames = heatOrchestrationTemplate.getParameters() == null ? null
        : heatOrchestrationTemplate.getParameters().keySet();

    if (MapUtils.isNotEmpty(envContent.getParameters())) {
      if (CollectionUtils.isNotEmpty(parametersNames)) {
        for (Map.Entry<String, Object> envEntry : envContent.getParameters().entrySet()) {
          String envParameter = envEntry.getKey();
          if (!parametersNames.contains(envParameter)) {
            globalContext.addMessage(envFile, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(
                    Messages.ENV_INCLUDES_PARAMETER_NOT_IN_HEAT.getErrorMessage(), envFile,
                    envParameter));
          }
        }
      } else {
        for (Map.Entry<String, Object> envEntry : envContent.getParameters().entrySet()) {
          globalContext.addMessage(envFile, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.ENV_INCLUDES_PARAMETER_NOT_IN_HEAT.getErrorMessage(),
                  envFile, envEntry.getKey()));
        }
      }
    }
  }

  private static void validateParameterDefaultTypeAlignWithType(String fileName,
                                       HeatOrchestrationTemplate heatOrchestrationTemplate,
                                       GlobalValidationContext globalContext) {
    Map<String, Parameter> parametersMap = heatOrchestrationTemplate.getParameters() == null ? null
        : heatOrchestrationTemplate.getParameters();

    if (MapUtils.isNotEmpty(parametersMap)) {
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
                    Messages.PARAMETER_DEFAULT_VALUE_NOT_ALIGN_WITH_TYPE.getErrorMessage(),
                    parameterEntry.getKey(), parameterType));
          }
        }
      }
    }
  }

  private static void validateEnvParametersMatchDefinedHeatParameterTypes(String envFile,
                                         Environment envContent,
                                         GlobalValidationContext globalContext,
                                         HeatOrchestrationTemplate heatOrchestrationTemplate) {
    Map<String, Parameter> heatParameters = heatOrchestrationTemplate.getParameters();

    if (MapUtils.isNotEmpty(heatParameters) && MapUtils.isNotEmpty(envContent.getParameters())) {
      for (Map.Entry<String, Object> envEntry : envContent.getParameters().entrySet()) {
        String parameterName = envEntry.getKey();
        Object parameterEnvValue = envEntry.getValue();
        Parameter parameterFromHeatFile = heatParameters.get(parameterName);
        if (parameterFromHeatFile != null) {
          String parameterType = parameterFromHeatFile.getType();
          if (!DefinedHeatParameterTypes.isEmptyValueInEnv(parameterEnvValue)
              && !DefinedHeatParameterTypes.isValueIsFromGivenType(parameterEnvValue,
              parameterType)) {
            globalContext.addMessage(envFile, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(
                    Messages.PARAMETER_ENV_VALUE_NOT_ALIGN_WITH_TYPE.getErrorMessage(),
                    parameterName));
          }
        }
      }
    }
  }

  @Override
  public void validate(GlobalValidationContext globalContext) {

    ManifestContent manifestContent;
    try {
      manifestContent = checkValidationPreCondition(globalContext);
    } catch (Exception e0) {
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
          fileName -> validate(fileName,
            fileEnvMap.get(fileName) == null ? null : fileEnvMap.get(fileName).getFile(),
            baseFileName == null ? null : baseFileName, artifacts,
            securityGroupsNamesFromBaseFileOutputs, globalContext));


    Set<String> manifestArtifacts = ManifestUtil.getArtifacts(manifestContent);

    globalContext.getFiles().stream()
        .filter(fileName -> manifestArtifacts.contains(fileName) && !artifacts.contains(fileName))
        .forEach(fileName -> globalContext.addMessage(fileName, ErrorLevel.WARNING,
            Messages.ARTIFACT_FILE_NOT_REFERENCED.getErrorMessage()));

    ResourceValidationHeatValidator
        .handleNotEmptyResourceNamesList(baseFileName, securityGroupsNamesFromBaseFileOutputs,
            "SecurityGroup", globalContext);

  }

  private void validate(String fileName, String envFileName, String baseFileName,
                        Set<String> artifacts, Set<String> securityGroupsNamesFromBaseFileOutputs,
                        GlobalValidationContext globalContext) {
    HeatOrchestrationTemplate heatOrchestrationTemplate =
        checkHeatOrchestrationPreCondition(fileName, globalContext);


    if (heatOrchestrationTemplate != null) {
      if (!(fileName.contains(".yaml") || fileName.contains(".yml"))) {
        globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.WRONG_HEAT_FILE_EXTENSION.getErrorMessage(),
                fileName));
      }

      validateHeatBaseStructure(fileName, heatOrchestrationTemplate, globalContext);

      ResourceValidationHeatValidator
          .validateResourceType(fileName, baseFileName, securityGroupsNamesFromBaseFileOutputs,
              heatOrchestrationTemplate, globalContext);
      validateParameterDefaultTypeAlignWithType(fileName, heatOrchestrationTemplate, globalContext);
      validateAllResourceReferencesExist(fileName, heatOrchestrationTemplate, globalContext);
      validateGetParamPointToParameter(fileName, heatOrchestrationTemplate, globalContext);
      validateGetAttr(fileName, heatOrchestrationTemplate, globalContext);
      validateAllRequiredArtifactsExist(fileName, heatOrchestrationTemplate, artifacts,
          globalContext);

      if (envFileName != null) {
        validateEnvFile(fileName, envFileName, heatOrchestrationTemplate, globalContext);
      }
    }
  }

  private void validateHeatBaseStructure(String fileName,
                                         HeatOrchestrationTemplate heatOrchestrationTemplate,
                                         GlobalValidationContext globalContext) {
    if (heatOrchestrationTemplate.getHeat_template_version() == null) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.INVALID_HEAT_FORMAT_REASON.getErrorMessage(),
              "missing template version"));
    }
    if (heatOrchestrationTemplate.getResources() == null
        || heatOrchestrationTemplate.getResources().size() == 0) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.INVALID_HEAT_FORMAT_REASON.getErrorMessage(),
              "heat file must have minimum one resource"));
    }
  }

  protected ManifestContent checkValidationPreCondition(GlobalValidationContext globalContext) {
    InputStream manifest = globalContext.getFileContent(AsdcCommon.MANIFEST_NAME);
    if (manifest == null) {
      throw new RuntimeException("Can't load manifest file for Heat Validator");
    }
    ManifestContent manifestContent;
    try {
      manifestContent = JsonUtil.json2Object(manifest, ManifestContent.class);
    } catch (Exception e0) {
      throw new RuntimeException("Can't load manifest file for Heat Validator");
    }

    return manifestContent;
  }


  private HeatOrchestrationTemplate checkHeatOrchestrationPreCondition(String fileName,
                                            GlobalValidationContext globalContext) {
    HeatOrchestrationTemplate heatOrchestrationTemplate;
    try {
      heatOrchestrationTemplate = new YamlUtil()
          .yamlToObject(globalContext.getFileContent(fileName), HeatOrchestrationTemplate.class);
    } catch (Exception e0) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.INVALID_HEAT_FORMAT_REASON.getErrorMessage(),
              getParserExceptionReason(e0)));
      return null;
    }

    return heatOrchestrationTemplate;
  }


  private Set<String> checkForBaseFilePortsExistenceAndReturnSecurityGroupNamesFromOutputsIfNot(
      String baseFileName, GlobalValidationContext globalContext) {
    Set<String> securityGroupsNamesFromOutputsMap = new HashSet<>();
    HeatOrchestrationTemplate heatOrchestrationTemplate =
        checkHeatOrchestrationPreCondition(baseFileName, globalContext);

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


  private String getParserExceptionReason(Exception e0) {
    String reason;

    if (e0.getCause() != null && e0.getCause().getCause() != null) {
      reason = e0.getCause().getCause().getMessage();
    } else if (e0.getCause() != null) {
      reason = e0.getCause().getMessage();
    } else {
      reason = Messages.GENERAL_HEAT_PARSER_ERROR.getErrorMessage();
    }
    return reason;
  }


}
