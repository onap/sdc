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

package org.openecomp.sdc.heat.services;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.PropertiesMapKeyTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The type Heat structure util.
 */
public class HeatStructureUtil {

  /**
   * Gets nested files.
   *
   * @param filename      the filename
   * @param hot           the hot
   * @param globalContext the global context
   * @return the nested files
   */
  public static Set<String> getNestedFiles(String filename, HeatOrchestrationTemplate hot,
                                           GlobalValidationContext globalContext) {

    Set<String> nestedFileList = new HashSet<>();
    Set<String> resourceDefNestedFiles;
    hot.getResources().values().stream().filter(
        resource -> (resource.getType().endsWith(".yaml") || resource.getType().endsWith(".yml")))
        .forEach(resource -> nestedFileList.add(resource.getType()));

    resourceDefNestedFiles = getResourceDefNestedFiles(filename, hot, globalContext);
    nestedFileList.addAll(resourceDefNestedFiles);

    return nestedFileList;
  }


  private static Set<String> getResourceDefNestedFiles(String filename,
                                                       HeatOrchestrationTemplate hot,
                                                       GlobalValidationContext globalContext) {
    Set<String> resourceDefNestedFiles = new HashSet<>();
    hot.getResources()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getType()
            .equals(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource()))
        .filter(entry ->
            getResourceDef(filename, entry.getKey(), entry.getValue(), globalContext) != null
                && isNestedResource(
                    getResourceDef(filename, entry.getKey(), entry.getValue(), globalContext)
                        .getType()))
        .forEach(entry -> resourceDefNestedFiles.add(
            getResourceDef(filename, entry.getKey(), entry.getValue(), globalContext).getType()));

    return resourceDefNestedFiles;
  }


  /**
   * Gets resource def.
   *
   * @param filename      the filename
   * @param resourceName  the resource name
   * @param resource      the resource
   * @param globalContext the global context
   * @return the resource def
   */
  @SuppressWarnings("unchecked")
  public static Resource getResourceDef(String filename, String resourceName, Resource resource,
                                        GlobalValidationContext globalContext) {
    Resource resourceDef = null;
    Map<String, Object> resourceDefValueMap = resource.getProperties() == null ? null
        : (Map<String, Object>) resource.getProperties()
            .get(PropertiesMapKeyTypes.RESOURCE_DEF.getKeyMap());
    if (MapUtils.isNotEmpty(resourceDefValueMap)) {
      Object resourceDefType = resourceDefValueMap.get("type");
      if (Objects.nonNull(resourceDefType)) {
        if (resourceDefType instanceof String) {
          boolean isNested =
              checkIfResourceGroupTypeIsNested(filename, resourceName, (String) resourceDefType,
                  globalContext);
          if (isNested) {
            resourceDef = new Resource();
            resourceDef.setType((String) resourceDefType);
            //noinspection unchecked
            resourceDef.setProperties((Map<String, Object>) resourceDefValueMap.get("properties"));
          }
        } else {
          globalContext.addMessage(filename, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.INVALID_RESOURCE_GROUP_TYPE.getErrorMessage(),
                  resourceName, resourceDefType.toString()));
        }
      } else {
        globalContext.addMessage(filename, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.INVALID_RESOURCE_TYPE.getErrorMessage(), "null",
                resourceName));
      }

    }
    return resourceDef;
  }


  /**
   * Check if resource group type is nested boolean.
   *
   * @param filename        the filename
   * @param resourceName    the resource name
   * @param resourceDefType the resource def type
   * @param globalContext   the global context
   * @return the boolean
   */
  public static boolean checkIfResourceGroupTypeIsNested(String filename, String resourceName,
                                                         String resourceDefType,
                                                         GlobalValidationContext globalContext) {
    if (!HeatStructureUtil.isNestedResource(resourceDefType)) {
      globalContext.addMessage(filename, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.INVALID_RESOURCE_GROUP_TYPE.getErrorMessage(),
              resourceName, resourceDefType));
      return false;
    }
    return true;
  }

  /**
   * Gets artifact files.
   *
   * @param filename      the filename
   * @param hot           the hot
   * @param globalContext the global context
   * @return the artifact files
   */
  public static Set<String> getArtifactFiles(String filename, HeatOrchestrationTemplate hot,
                                             GlobalValidationContext globalContext) {
    Set<String> artifactSet = new HashSet<>();
    Collection<Resource> resourcesValue =
        hot.getResources() == null ? null : hot.getResources().values();
    if (CollectionUtils.isNotEmpty(resourcesValue)) {
      for (Resource resource : resourcesValue) {
        Collection<Object> properties =
            resource.getProperties() == null ? null : resource.getProperties().values();
        if (CollectionUtils.isNotEmpty(properties)) {
          for (Object property : properties) {
            Set<String> artifactNames =
                getReferencedValuesByFunctionName(filename, "get_file", property, globalContext);
            artifactSet.addAll(artifactNames);
          }
        }
      }
    }
    return artifactSet;
  }

  /**
   * Gets referenced values by function name.
   *
   * @param filename      the filename
   * @param functionName  the function name
   * @param propertyValue the property value
   * @param globalContext the global context
   * @return the referenced values by function name
   */
  public static Set<String> getReferencedValuesByFunctionName(String filename, String functionName,
                                               Object propertyValue,
                                               GlobalValidationContext globalContext) {
    Set<String> valuesNames = new HashSet<>();
    if (propertyValue instanceof Map) {
      Map<String, Object> currPropertyMap = (Map<String, Object>) propertyValue;
      if (currPropertyMap.containsKey(functionName)) {
        Object getFunctionValue = currPropertyMap.get(functionName);
        if (!(getFunctionValue instanceof String)
            && functionName.equals(ResourceReferenceFunctions.GET_RESOURCE.getFunction())) {
          globalContext.addMessage(filename, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.INVALID_GET_RESOURCE_SYNTAX.getErrorMessage(),
                  getFunctionValue == null ? "null" : getFunctionValue.toString()));
          return valuesNames;
        }
        if (getFunctionValue instanceof String) {

          if (functionName.equals(ResourceReferenceFunctions.GET_FILE.getFunction())) {
            getFunctionValue = ((String) getFunctionValue).replace("file:///", "");
          }

          valuesNames.add((String) getFunctionValue);
        } else if (getFunctionValue instanceof List) {
          if (CollectionUtils.isNotEmpty((List) getFunctionValue)) {
            if (((List) getFunctionValue).get(0) instanceof String) {
              valuesNames.add(((String) ((List) getFunctionValue).get(0)).replace("file:///", ""));
            } else {
              valuesNames.addAll(getReferencedValuesByFunctionName(filename, functionName,
                  ((List) getFunctionValue).get(0), globalContext));
            }

          }
        } else {
          valuesNames.addAll(
              getReferencedValuesByFunctionName(filename, functionName, getFunctionValue,
                  globalContext));
        }
      } else {
        for (Map.Entry<String, Object> nestedPropertyMap : currPropertyMap.entrySet()) {
          valuesNames.addAll(getReferencedValuesByFunctionName(filename, functionName,
              nestedPropertyMap.getValue(), globalContext));
        }
      }
    } else if (propertyValue instanceof List) {
      List propertyValueArray = (List) propertyValue;
      for (Object propertyValueArrayInstance : propertyValueArray) {
        valuesNames.addAll(
            getReferencedValuesByFunctionName(filename, functionName, propertyValueArrayInstance,
                globalContext));
      }
    }

    return valuesNames;
  }


  /**
   * Is nested resource boolean.
   *
   * @param resourceType the resource type
   * @return the boolean
   */
  public static boolean isNestedResource(String resourceType) {
    return resourceType.endsWith(".yaml") || resourceType.endsWith(".yml");
  }
}
