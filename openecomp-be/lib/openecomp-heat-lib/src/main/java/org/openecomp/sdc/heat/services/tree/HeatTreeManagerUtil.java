/*
* Copyright © 2016-2017 European Support Limited
*
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
  private HeatTreeManagerUtil(){

  }

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
            resource -> resource.getType().endsWith(".yaml") || resource.getType().endsWith(".yml"))
            .forEach(resource -> nestedFileList.add(resource.getType()));

    resourceDefNestedFiles = getResourceDefNestedFiles(hot);
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

        artifactSet=getArtifactsFromPropertiesAndAddInArtifactSet(properties,
                                      filename, globalContext);

      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return artifactSet;
  }
  private static Set<String> getArtifactsFromPropertiesAndAddInArtifactSet(Collection<Object> properties,
                                                    String filename,
                                                    GlobalValidationContext globalContext ){
    Set<String> artifactSet = new HashSet<>();
    if (CollectionUtils.isNotEmpty(properties)) {

      for (Object property : properties) {
        Set<String> artifactNames =
                HeatStructureUtil.getReferencedValuesByFunctionName(filename, "get_file", property,
                        globalContext);
        artifactSet.addAll(artifactNames);
      }
    }
    return artifactSet;
  }

  private static Set<String> getResourceDefNestedFiles(HeatOrchestrationTemplate hot) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    Set<String> resourceDefNestedFiles = new HashSet<>();
    hot.getResources()
            .entrySet().stream().filter(entry -> entry.getValue().getType()
            .equals(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource()))
            .filter(entry ->
                    getResourceDef(entry.getValue()) != null
                            && HeatStructureUtil.isNestedResource(
                            getResourceDef(entry.getValue())
                                    .getType()))
            .forEach(entry -> resourceDefNestedFiles.add(
                    getResourceDef( entry.getValue()).getType()));

    mdcDataDebugMessage.debugExitMessage(null, null);
    return resourceDefNestedFiles;
  }

  /**
   * Gets resource def.
   *
   * @param resource the resource
   * @return the resource def
   */
  @SuppressWarnings("unchecked")
  public static Resource getResourceDef( Resource resource) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    Resource resourceDef = null;
    Map<String, Object> resourceDefValueMap = resource.getProperties() == null ? null
            : (Map<String, Object>) resource.getProperties().get(
            PropertiesMapKeyTypes.RESOURCE_DEF.getKeyMap());
    if (resourceDefValueMap != null && MapUtils.isNotEmpty(resourceDefValueMap) ) {
      Object resourceDefType = resourceDefValueMap.get("type");
      if ( resourceDefType instanceof String && isResourceGroupTypeNested((String) resourceDefType)) {
        resourceDef = new Resource();
        resourceDef.setType((String) resourceDefType);
        //noinspection unchecked
        resourceDef.setProperties((Map<String, Object>) resourceDefValueMap.get("properties"));
      }

    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return resourceDef;
  }

  @SuppressWarnings("unchecked")
  public static void checkResourceGroupTypeValid(String filename, String resourceName,
                                                 Resource resource,
                                                 GlobalValidationContext globalContext) {
    Map<String, Object> resourceDefValueMap = resource.getProperties() == null ? null
            : (Map<String, Object>) resource.getProperties().get(
            PropertiesMapKeyTypes.RESOURCE_DEF.getKeyMap());
    if (resourceDefValueMap != null && MapUtils.isNotEmpty(resourceDefValueMap) ) {
      Object resourceDefType = resourceDefValueMap.get("type");
      if (Objects.nonNull(resourceDefType) && !(resourceDefType instanceof String) ) {
        globalContext.addMessage(filename, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                        .getErrorWithParameters(
                                globalContext.getMessageCode(),
                                Messages.INVALID_RESOURCE_GROUP_TYPE.getErrorMessage(),
                                resourceName, resourceDefType.toString()),
                LoggerTragetServiceName.VALIDATE_RESOURCE_GROUP_TYPE, "Invalid resource group type");
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkResourceTypeValid(String filename, String resourceName,
                                            Resource resource,
                                            GlobalValidationContext globalContext) {
    Map<String, Object> resourceDefValueMap = resource.getProperties() == null ? null
            : (Map<String, Object>) resource.getProperties().get(
            PropertiesMapKeyTypes.RESOURCE_DEF.getKeyMap());
    if (resourceDefValueMap != null && MapUtils.isNotEmpty(resourceDefValueMap) ) {
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

  public static boolean isResourceGroupTypeNested( String resourceDefType) {

    return HeatStructureUtil.isNestedResource(resourceDefType);
  }

  public static boolean checkIfResourceGroupTypeIsNested(String filename, String resourceName,
                                                         Resource resource,
                                                         GlobalValidationContext globalContext) {
    Map<String, Object> resourceDefValueMap = resource.getProperties() == null ? null
            : (Map<String, Object>) resource.getProperties().get(
            PropertiesMapKeyTypes.RESOURCE_DEF.getKeyMap());
    if (resourceDefValueMap != null && MapUtils.isNotEmpty(resourceDefValueMap) ) {
      Object resourceDefType = resourceDefValueMap.get("type");
      if (resourceDefType instanceof String && isResourceGroupTypeNested((String) resourceDefType)) {

        globalContext.addMessage(filename, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                        .getErrorWithParameters(
                                globalContext.getMessageCode(),
                                Messages.INVALID_RESOURCE_GROUP_TYPE.getErrorMessage(),
                                resourceName, resourceDefType.toString()),
                LoggerTragetServiceName.VALIDATE_RESOURCE_GROUP_TYPE,
                "Invalid resource group type");
        return true;
      }
    }
    return false;
  }
}
