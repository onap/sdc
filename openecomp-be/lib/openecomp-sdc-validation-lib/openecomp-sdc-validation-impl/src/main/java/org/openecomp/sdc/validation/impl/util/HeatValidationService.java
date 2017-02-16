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
import org.openecomp.core.utilities.yaml.YamlUtil;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.Environment;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.validation.impl.validators.HeatValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class HeatValidationService {

  private static final Logger logger = LoggerFactory.getLogger(HeatValidator.class);

  /**
   * Check artifacts existence.
   *
   * @param fileName       the file name
   * @param artifactsNames the artifacts names
   * @param globalContext  the global context
   */
  public static void checkArtifactsExistence(String fileName, Set<String> artifactsNames,
                                             GlobalValidationContext globalContext) {
    artifactsNames
        .stream()
        .filter(artifactName -> !globalContext.getFileContextMap().containsKey(artifactName))
        .forEach(artifactName -> {
          globalContext
              .addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.MISSING_ARTIFACT.getErrorMessage(),
                      artifactName));
        });
  }


  /**
   * Check resource existence from resources map.
   *
   * @param fileName         the file name
   * @param resourcesNames   the resources names
   * @param valuesToSearchIn the values to search in
   * @param globalContext    the global context
   */
  public static void checkResourceExistenceFromResourcesMap(String fileName,
                                                            Set<String> resourcesNames,
                                                            Collection<?> valuesToSearchIn,
                                                            GlobalValidationContext globalContext) {
    if (CollectionUtils.isNotEmpty(valuesToSearchIn)) {
      for (Object value : valuesToSearchIn) {
        if (value instanceof Resource) {
          Resource resource = (Resource) value;
          //checkResourceDependsOn(fileName,resource,resourcesNames,globalContext);

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
  }


  private static void handleReferencedResources(String fileName, Object valueToSearchReferencesIn,
                                                Set<String> resourcesNames,
                                                GlobalValidationContext globalContext) {
    Set<String> referencedResourcesNames = HeatStructureUtil
        .getReferencedValuesByFunctionName(fileName,
            ResourceReferenceFunctions.GET_RESOURCE.getFunction(), valueToSearchReferencesIn,
            globalContext);
    if (CollectionUtils.isNotEmpty(referencedResourcesNames)) {
      HeatValidationService
          .checkIfResourceReferenceExist(fileName, resourcesNames, referencedResourcesNames,
              globalContext);
    }
  }


  private static void checkIfResourceReferenceExist(String fileName,
                                                    Set<String> referencedResourcesNames,
                                                    Set<String> referencedResources,
                                                    GlobalValidationContext globalContext) {
    referencedResources.stream()
        .filter(referencedResource -> !referencedResourcesNames.contains(referencedResource))
        .forEach(referencedResource -> {
          globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.REFERENCED_RESOURCE_NOT_FOUND.getErrorMessage(),
                  referencedResource));
        });
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
   * @param callingNestedFileName  the calling nested file name
   * @param nestedFileName         the nested file name
   * @param resourceName           the resource name
   * @param globalContext          the global context
   * @param resourceFileProperties the resource file properties
   */
  public static void checkNestedParameters(String callingNestedFileName, String nestedFileName,
                                           String resourceName,
                                           GlobalValidationContext globalContext,
                                           Set<String> resourceFileProperties) {
    HeatOrchestrationTemplate heatOrchestrationTemplate;
    try {
      heatOrchestrationTemplate = new YamlUtil()
          .yamlToObject(globalContext.getFileContent(nestedFileName),
              HeatOrchestrationTemplate.class);
    } catch (Exception e0) {
      return;
    }
    Set<String> nestedParametersNames = heatOrchestrationTemplate.getParameters() == null ? null
        : heatOrchestrationTemplate.getParameters().keySet();

    if (CollectionUtils.isNotEmpty(nestedParametersNames)) {
      resourceFileProperties
          .stream()
          .filter(propertyName -> !nestedParametersNames.contains(propertyName))
          .forEach(propertyName -> globalContext
              .addMessage(callingNestedFileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.MISSING_PARAMETER_IN_NESTED.getErrorMessage(),
                      nestedFileName, resourceName, propertyName)));
    }
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
    HeatOrchestrationTemplate nestedHeatOrchestrationTemplate;
    try {
      nestedHeatOrchestrationTemplate = new YamlUtil()
          .yamlToObject(globalContext.getFileContent(nestedFileName),
              HeatOrchestrationTemplate.class);
    } catch (Exception e0) {
      logger.warn("HEAT Validator will not be executed on file " + nestedFileName
          + " due to illegal HEAT format");
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
          return resourceType.equals(callingFileName) || !filesInLoop.contains(resourceType)
              && isNestedLoopExistInFile(callingFileName, resourceType, filesInLoop, globalContext);
        }
      }
    }
    return false;
  }


  /**
   * Loop over output map and validate get attr from nested.
   *
   * @param fileName                  the file name
   * @param outputMap                 the output map
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param globalContext             the global context
   */
  @SuppressWarnings("unchecked")
  public static void loopOverOutputMapAndValidateGetAttrFromNested(String fileName,
                                       Map<String, Output> outputMap,
                                       HeatOrchestrationTemplate heatOrchestrationTemplate,
                                       GlobalValidationContext globalContext) {
    for (Output output : outputMap.values()) {
      Object outputValue = output.getValue();
      if (outputValue != null && outputValue instanceof Map) {
        Map<String, Object> outputValueMap = (Map<String, Object>) outputValue;
        List<String> getAttrValue =
            (List<String>) outputValueMap.get(ResourceReferenceFunctions.GET_ATTR.getFunction());
        if (!CollectionUtils.isEmpty(getAttrValue)) {
          String resourceName = getAttrValue.get(0);
          String propertyName = getAttrValue.get(1);
          String resourceType =
              getResourceTypeFromResourcesMap(resourceName, heatOrchestrationTemplate);

          if (Objects.nonNull(resourceType)
              && HeatValidationService.isNestedResource(resourceType)) {
            Map<String, Output> nestedOutputMap;
            HeatOrchestrationTemplate nestedHeatOrchestrationTemplate;
            try {
              nestedHeatOrchestrationTemplate = new YamlUtil()
                  .yamlToObject(globalContext.getFileContent(resourceType),
                      HeatOrchestrationTemplate.class);
            } catch (Exception e0) {
              return;
            }
            nestedOutputMap = nestedHeatOrchestrationTemplate.getOutputs();

            if (MapUtils.isEmpty(nestedOutputMap) || !nestedOutputMap.containsKey(propertyName)) {
              globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.GET_ATTR_NOT_FOUND.getErrorMessage(),
                      propertyName, resourceName));
            }
          }
        }
      }
    }
  }


  public static boolean isNestedResource(String resourceType) {
    return resourceType.contains(".yaml") || resourceType.contains(".yml");
  }


  private static String getResourceTypeFromResourcesMap(String resourceName,
                                    HeatOrchestrationTemplate heatOrchestrationTemplate) {
    return heatOrchestrationTemplate.getResources().get(resourceName).getType();
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
    Environment envContent = null;
    try {
      envContent =
          new YamlUtil().yamlToObject(globalContext.getFileContent(envFileName), Environment.class);
    } catch (Exception e0) {
      return null;
    }
    return envContent;
  }


  public static String getResourceGroupResourceName(String resourceCallingToResourceGroup) {
    return "OS::Heat::ResourceGroup in " + resourceCallingToResourceGroup;
  }

}
