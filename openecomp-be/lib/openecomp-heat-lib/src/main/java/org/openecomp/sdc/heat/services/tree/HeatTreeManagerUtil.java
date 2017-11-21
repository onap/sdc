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

package org.openecomp.sdc.heat.services.tree;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.PropertiesMapKeyTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class HeatTreeManagerUtil {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  /**
   * Init heat tree manager heat tree manager.
   *
   * @param fileContentMap the file content map
   * @return the heat tree manager
   */
  public static HeatTreeManager initHeatTreeManager(FileContentHandler fileContentMap) {

    HeatTreeManager heatTreeManager = new HeatTreeManager();
    fileContentMap.getFileList().stream().forEach(
            fileName -> heatTreeManager.addFile(fileName, fileContentMap.getFileContent(fileName)));

    return heatTreeManager;
  }

  /**
   * Gets nested files.
   *
   * @param filename the filename
   * @param hot the hot
   * @param globalContext the global context
   * @return the nested files
   */
  public static Set<String> getNestedFiles(String filename, HeatOrchestrationTemplate hot,
                                           GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    Set<String> nestedFileList = new HashSet<>();
    Set<String> resourceDefNestedFiles;
    hot.getResources().values().stream().filter(
            resource -> (resource.getType().endsWith(".yaml") || resource.getType().endsWith(".yml")))
            .forEach(resource -> nestedFileList.add(resource.getType()));

    resourceDefNestedFiles = getResourceDefNestedFiles(filename, hot, globalContext);
    nestedFileList.addAll(resourceDefNestedFiles);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return nestedFileList;
  }

  /**
   * Gets artifact files.
   *
   * @param filename the filename
   * @param hot the hot
   * @param globalContext the global context
   * @return the artifact files
   */
  public static Set<String> getArtifactFiles(String filename, HeatOrchestrationTemplate hot,
                                             GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

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
                    HeatStructureUtil.getReferencedValuesByFunctionName(filename, "get_file", property,
                            globalContext);
            artifactSet.addAll(artifactNames);
          }
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return artifactSet;
  }

  private static Set<String> getResourceDefNestedFiles(String filename,
                                                       HeatOrchestrationTemplate hot,
                                                       GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    Set<String> resourceDefNestedFiles = new HashSet<>();
    hot.getResources()
            .entrySet().stream().filter(entry -> entry.getValue().getType()
            .equals(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource()))
            .filter(entry ->
                    getResourceDef(filename, entry.getKey(), entry.getValue(), globalContext) != null
                            && HeatStructureUtil.isNestedResource(
                            getResourceDef(filename, entry.getKey(), entry.getValue(), globalContext)
                                    .getType()))
            .forEach(entry -> resourceDefNestedFiles.add(
                    getResourceDef(filename, entry.getKey(), entry.getValue(), globalContext).getType()));

    mdcDataDebugMessage.debugExitMessage(null, null);
    return resourceDefNestedFiles;
  }

  /**
   * Gets resource def.
   *
   * @param filename the filename
   * @param resourceName the resource name
   * @param resource the resource
   * @param globalContext the global context
   * @return the resource def
   */
  @SuppressWarnings("unchecked")
  public static Resource getResourceDef(String filename, String resourceName, Resource resource,
                                        GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    Resource resourceDef = null;
    Map<String, Object> resourceDefValueMap = resource.getProperties() == null ? null
            : (Map<String, Object>) resource.getProperties().get(
            PropertiesMapKeyTypes.RESOURCE_DEF.getKeyMap());
    if (MapUtils.isNotEmpty(resourceDefValueMap) && resourceDefValueMap != null) {
      Object resourceDefType = resourceDefValueMap.get("type");
      if (Objects.nonNull(resourceDefType)) {
        if (resourceDefType instanceof String) {
          boolean isNested =
                  isResourceGroupTypeNested(filename, resourceName, (String) resourceDefType,
                          globalContext);
          if (isNested) {
            resourceDef = new Resource();
            resourceDef.setType((String) resourceDefType);
            //noinspection unchecked
            resourceDef.setProperties((Map<String, Object>) resourceDefValueMap.get("properties"));
          }
        }
      }

    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return resourceDef;
  }

  /**
   * Check for valid resource group type.
   *
   * @param filename the filename
   * @param resourceName the resource name
   * @param resource the resource
   * @param globalContext the global context
   */
  @SuppressWarnings("unchecked")
  public static void checkResourceGroupTypeValid(String filename, String resourceName,
                                                 Resource resource,
                                                 GlobalValidationContext globalContext) {
    Map<String, Object> resourceDefValueMap = resource.getProperties() == null ? null
            : (Map<String, Object>) resource.getProperties().get(
            PropertiesMapKeyTypes.RESOURCE_DEF.getKeyMap());
    if (MapUtils.isNotEmpty(resourceDefValueMap) && resourceDefValueMap != null) {
      Object resourceDefType = resourceDefValueMap.get("type");
      if (Objects.nonNull(resourceDefType)) {
        if ((resourceDefType instanceof String) == false) {
          globalContext.addMessage(filename, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                          .getErrorWithParameters(
                                  globalContext.getMessageCode(),
                                  Messages.INVALID_RESOURCE_GROUP_TYPE.getErrorMessage(),
                                  resourceName, resourceDefType.toString()),
                  LoggerTragetServiceName.VALIDATE_RESOURCE_GROUP_TYPE, "Invalid resource group type");
        }
      }
    }
  }

  /**
   * Check for valid resource type.
   *
   * @param filename the filename
   * @param resourceName the resource name
   * @param resource the resource
   * @param globalContext the global context
   */
  @SuppressWarnings("unchecked")
  public static void checkResourceTypeValid(String filename, String resourceName,
                                            Resource resource,
                                            GlobalValidationContext globalContext) {
    Map<String, Object> resourceDefValueMap = resource.getProperties() == null ? null
            : (Map<String, Object>) resource.getProperties().get(
            PropertiesMapKeyTypes.RESOURCE_DEF.getKeyMap());
    if (MapUtils.isNotEmpty(resourceDefValueMap) && resourceDefValueMap != null) {
      Object resourceDefType = resourceDefValueMap.get("type");
      if (Objects.isNull(resourceDefType)) {
        globalContext.addMessage(filename, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                        .getErrorWithParameters(
                                globalContext.getMessageCode(), Messages.INVALID_RESOURCE_TYPE.getErrorMessage(),
                                "null", resourceName), LoggerTragetServiceName.VALIDATE_RESOURCE_GROUP_TYPE,
                "Invalid resource type");
      }
    }
  }

  /**
   * Is resource group type is nested boolean.
   *
   * @param filename the filename
   * @param resourceName the resource name
   * @param resourceDefType the resource def type
   * @param globalContext the global context
   * @return the boolean
   */
  public static boolean isResourceGroupTypeNested(String filename, String resourceName,
                                                  String resourceDefType,
                                                  GlobalValidationContext globalContext) {
    if (!HeatStructureUtil.isNestedResource(resourceDefType)) {
      return false;
    }
    return true;
  }

  /**
   * Check for valid resource type.
   *
   * @param filename the filename
   * @param resourceName the resource name
   * @param resource the resource
   * @param globalContext the global context
   */
  public static boolean checkIfResourceGroupTypeIsNested(String filename, String resourceName,
                                                         Resource resource,
                                                         GlobalValidationContext globalContext) {
    Map<String, Object> resourceDefValueMap = resource.getProperties() == null ? null
            : (Map<String, Object>) resource.getProperties().get(
            PropertiesMapKeyTypes.RESOURCE_DEF.getKeyMap());
    if (MapUtils.isNotEmpty(resourceDefValueMap) && resourceDefValueMap != null) {
      Object resourceDefType = resourceDefValueMap.get("type");
      if (Objects.nonNull(resourceDefType)) {
        if (resourceDefType instanceof String) {
          boolean isNested =
                  isResourceGroupTypeNested(filename, resourceName, (String) resourceDefType,
                          globalContext);
          if (isNested) {
            globalContext.addMessage(filename, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                            .getErrorWithParameters(
                                    globalContext.getMessageCode(),
                                    Messages.INVALID_RESOURCE_GROUP_TYPE.getErrorMessage(),
                                    resourceName, resourceDefType.toString()),
                    LoggerTragetServiceName.VALIDATE_RESOURCE_GROUP_TYPE,
                    "Invalid resource group type");
            return isNested;
          }
        }
      }
    }
    return false;
  }
}
