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

package org.openecomp.sdc.translator.services.heattotosca;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.yaml.YamlUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.HeatBoolean;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.ResourceReferenceType;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.FileDataCollection;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.errors.ResourceNotFoundInHeatFileErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaFunctionConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class HeatToToscaUtil {

  protected static Logger logger = LoggerFactory.getLogger(HeatToToscaUtil.class);


  /**
   * Build list of files to search optional.
   *
   * @param heatFileName  the heat file name
   * @param filesDataList the files data list
   * @param types         the types
   * @return the optional
   */
  public static Optional<List<FileData>> buildListOfFilesToSearch(String heatFileName,
                                                                  List<FileData> filesDataList,
                                                                  FileData.Type... types) {
    List<FileData> list = new ArrayList<>(filesDataList);
    Optional<FileData> resourceFileData = HeatToToscaUtil.getFileData(heatFileName, filesDataList);
    if (resourceFileData.isPresent() && Objects.nonNull(resourceFileData.get().getData())) {
      list.addAll(resourceFileData.get().getData());
    }
    return Optional.ofNullable(HeatToToscaUtil.getFilteredListOfFileDataByTypes(list, types));
  }

  public static List<FileData> getFilteredListOfFileDataByTypes(List<FileData> filesToSearch,
                                                                FileData.Type... types) {
    return filesToSearch.stream().filter(FileData.buildFileDataPredicateByType(types))
        .collect(Collectors.toList());
  }

  /**
   * Gets file data.
   *
   * @param heatFileName the heat file name
   * @param fileDataList the file data list
   * @return the file data
   */
  public static Optional<FileData> getFileData(String heatFileName,
                                               Collection<FileData> fileDataList) {
    for (FileData file : fileDataList) {
      if (file.getFile().equals(heatFileName)) {
        return Optional.of(file);
      }
    }
    return Optional.empty();
  }

  static FileDataCollection getFileCollectionsByFilter(List<FileData> fileDataList,
                                                       Set<FileData.Type> typeFilter,
                                                       TranslationContext translationContext) {
    FileDataCollection fileDataCollection = new FileDataCollection();
    Map<String, FileData> filteredFiles = filterFileDataListByType(fileDataList, typeFilter);
    Set<String> referenced = new HashSet<>();
    List<String> filenames = extractFilenamesFromFileDataList(filteredFiles.values());

    for (FileData fileData : filteredFiles.values()) {
      String fileName = fileData.getFile();

      if (FileData.isHeatFile(fileData.getType())) {
        if (fileData.getBase() != null && fileData.getBase().equals(true)) {
          fileDataCollection.addBaseFiles(fileData);
        }
        HeatOrchestrationTemplate heatOrchestrationTemplate = new YamlUtil()
            .yamlToObject(translationContext.getFileContent(fileName),
                HeatOrchestrationTemplate.class);
        for (Resource resource : heatOrchestrationTemplate.getResources().values()) {
          if (filenames.contains(resource.getType())) {
            handleNestedFile(translationContext, fileDataCollection, filteredFiles, referenced,
                resource.getType());
          } else if (resource.getType()
              .equals(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource())) {
            Object resourceDef =
                resource.getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME);
            Object innerTypeDef = ((Map) resourceDef).get("type");
            if (innerTypeDef instanceof String) {
              String internalResourceType = (String) innerTypeDef;
              if (filenames.contains(internalResourceType)) {
                handleNestedFile(translationContext, fileDataCollection, filteredFiles, referenced,
                    internalResourceType);
              }
            }
          }
        }

      } else {
        fileDataCollection.addArtifactFiles(fileData);
        filteredFiles.remove(fileData.getFile());
      }
    }

    referenced.forEach(filteredFiles::remove);
    if (!CollectionUtils.isEmpty(fileDataCollection.getBaseFile())) {
      for (FileData fileData : fileDataCollection.getBaseFile()) {
        filteredFiles.remove(fileData.getFile());
      }
    }
    fileDataCollection.setAddOnFiles(filteredFiles.values());
    return fileDataCollection;
  }

  private static void handleNestedFile(TranslationContext translationContext,
                                       FileDataCollection fileDataCollection,
                                       Map<String, FileData> filteredFiles, Set<String> referenced,
                                       String nestedFileName) {
    referenced.add(nestedFileName);
    fileDataCollection.addNestedFiles(filteredFiles.get(nestedFileName));
    translationContext.getNestedHeatsFiles().add(nestedFileName);
  }

  private static Map<String, FileData> filterFileDataListByType(List<FileData> fileDataList,
                                                                Set<FileData.Type> typesToGet) {
    Map<String, FileData> filtered = new HashMap<>();
    fileDataList.stream().filter(file -> typesToGet.contains(file.getType()))
        .forEach(file -> filtered.put(file.getFile(), file));
    return filtered;
  }

  private static List<String> extractFilenamesFromFileDataList(Collection<FileData> fileDataList) {
    return fileDataList.stream().map(FileData::getFile).collect(Collectors.toList());
  }

  /**
   * Extract attached resource id optional.
   *
   * @param translateTo  the translate to
   * @param propertyName the property name
   * @return the optional
   */
  public static Optional<AttachedResourceId> extractAttachedResourceId(TranslateTo translateTo,
                                                                       String propertyName) {
    Object propertyValue = translateTo.getResource().getProperties().get(propertyName);
    if (propertyValue == null) {
      return Optional.empty();
    }
    return extractAttachedResourceId(translateTo.getHeatFileName(),
        translateTo.getHeatOrchestrationTemplate(), translateTo.getContext(), propertyValue);
  }

  /**
   * Extract attached resource id optional.
   *
   * @param heatFileName              the heat file name
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param context                   the context
   * @param propertyValue             the property value
   * @return the optional
   */
  public static Optional<AttachedResourceId> extractAttachedResourceId(String heatFileName,
                                      HeatOrchestrationTemplate heatOrchestrationTemplate,
                                      TranslationContext context,
                                      Object propertyValue) {

    Object entity;
    Object translatedId;

    if (Objects.isNull(propertyValue)) {
      return Optional.empty();
    }

    ResourceReferenceType referenceType = ResourceReferenceType.OTHER;
    if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
      Map<String, Object> propMap = (Map) propertyValue;
      Map.Entry<String, Object> entry = propMap.entrySet().iterator().next();
      entity = entry.getValue();
      String key = entry.getKey();
      switch (key) {
        case "get_resource":
          referenceType = ResourceReferenceType.GET_RESOURCE;
          break;
        case "get_param":
          referenceType = ResourceReferenceType.GET_PARAM;
          break;
        case "get_attr":
          referenceType = ResourceReferenceType.GET_ATTR;
          break;
        default:
      }
      translatedId = TranslatorHeatToToscaFunctionConverter
          .getToscaFunction(entry.getKey(), entry.getValue(), heatFileName,
              heatOrchestrationTemplate, null, context);
      if (translatedId instanceof String
          && !TranslatorHeatToToscaFunctionConverter.isResourceSupported((String) translatedId)) {
        translatedId = null;
      }

    } else {
      translatedId = propertyValue;
      entity = propertyValue;
    }

    return Optional.of(new AttachedResourceId(translatedId, entity, referenceType));
  }

  /**
   * Gets contrail attached heat resource id.
   *
   * @param attachedResource the attached resource
   * @return the contrail attached heat resource id
   */
  public static Optional<String> getContrailAttachedHeatResourceId(
      AttachedResourceId attachedResource) {
    if (attachedResource == null) {
      return Optional.empty();
    }

    if (attachedResource.isGetResource()) {
      return Optional.of((String) attachedResource.getEntityId());
    }
    if (attachedResource.isGetAttr() && (attachedResource.getEntityId() instanceof List)
        && ((List) attachedResource.getEntityId()).size() > 1
        && ((List) attachedResource.getEntityId()).get(1).equals("fq_name")) {
      return Optional.of((String) ((List) attachedResource.getEntityId()).get(0));
    }

    return Optional.empty();
  }

  /**
   * Extract property optional.
   *
   * @param propertyValue the property value
   * @return the optional
   */
  public static Optional<AttachedResourceId> extractProperty(Object propertyValue) {

    Object entity;
    if (Objects.isNull(propertyValue)) {
      return Optional.empty();
    }

    ResourceReferenceType referenceType = ResourceReferenceType.OTHER;
    if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
      Map<String, Object> propMap = (Map) propertyValue;
      Map.Entry<String, Object> entry = propMap.entrySet().iterator().next();
      entity = entry.getValue();
      String key = entry.getKey();
      switch (key) {
        case "get_resource":
          referenceType = ResourceReferenceType.GET_RESOURCE;
          break;
        case "get_param":
          referenceType = ResourceReferenceType.GET_PARAM;
          break;
        case "get_attr":
          referenceType = ResourceReferenceType.GET_ATTR;
          break;
        default:
      }

    } else {
      entity = propertyValue;
    }

    return Optional.of(new AttachedResourceId(null, entity, referenceType));
  }

  /**
   * Map boolean.
   *
   * @param nodeTemplate the node template
   * @param propertyKey  the property key
   */
  public static void mapBoolean(NodeTemplate nodeTemplate, String propertyKey) {
    Object value = nodeTemplate.getProperties().get(propertyKey);
    if (value != null && !(value instanceof Map)) {
      nodeTemplate.getProperties().put(propertyKey, HeatBoolean.eval(value));
    }
  }

  /**
   * Map boolean list.
   *
   * @param nodeTemplate    the node template
   * @param propertyListKey the property list key
   */
  public static void mapBooleanList(NodeTemplate nodeTemplate, String propertyListKey) {
    Object listValue = nodeTemplate.getProperties().get(propertyListKey);
    if (listValue instanceof List) {
      List booleanList = ((List) listValue);
      for (int i = 0; i < booleanList.size(); i++) {
        Object value = booleanList.get(i);
        if (value != null && !(value instanceof Map)) {
          booleanList.set(i, HeatBoolean.eval(value));
        }
      }
    }
  }


  public static boolean isYmlFileType(String filename) {
    return (filename.indexOf("yaml") > 0 || filename.indexOf("yml") > 0);
  }

  /**
   * Is nested resource boolean.
   *
   * @param resource the resource
   * @return the boolean
   */
  public static boolean isNestedResource(Resource resource) {
    String resourceType = resource.getType();

    if (resourceType.equals(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource())) {
      Object resourceDef = resource.getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME);
      String internalResourceType = (String) ((Map) resourceDef).get("type");
      if (isYamlFile(internalResourceType)) {
        return true;
      }
    } else if (isYamlFile(resourceType)) {
      return true;
    }
    return false;
  }

  /**
   * Gets nested file.
   *
   * @param resource the resource
   * @return the nested file
   */
  public static Optional<String> getNestedFile(Resource resource) {
    if (!isNestedResource(resource)) {
      return Optional.empty();
    }
    String resourceType = resource.getType();
    if (resourceType.equals(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource())) {
      Object resourceDef = resource.getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME);
      String internalResourceType = (String) ((Map) resourceDef).get("type");
      return Optional.of(internalResourceType);
    } else {
      return Optional.of(resourceType);
    }
  }

  private static boolean isYamlFile(String fileName) {
    return fileName.endsWith(".yaml") || fileName.endsWith(".yml");
  }

  /**
   * Gets resource.
   *
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param resourceId                the resource id
   * @param heatFileName              the heat file name
   * @return the resource
   */
  public static Resource getResource(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                     String resourceId, String heatFileName) {
    Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
    if (resource == null) {
      throw new CoreException(
          new ResourceNotFoundInHeatFileErrorBuilder(resourceId, heatFileName).build());
    }
    return resource;
  }

  public static boolean isHeatFileNested(TranslateTo translateTo, String heatFileName) {
    return translateTo.getContext().getNestedHeatsFiles().contains(heatFileName);
  }

  /**
   * Extract contrail get resource attached heat resource id string.
   *
   * @param propertyValue the property value
   * @return the string
   */
  public static String extractContrailGetResourceAttachedHeatResourceId(Object propertyValue) {
    if (propertyValue == null) {
      return null;
    }

    Object value;
    if (propertyValue instanceof Map) {
      if (((Map) propertyValue).containsKey("get_attr")) {
        value = ((Map) propertyValue).get("get_attr");
        if (value instanceof List) {
          if (((List) value).size() == 2 && ((List) value).get(1).equals("fq_name")) {
            if (((List) value).get(0) instanceof String) {
              return (String) ((List) value).get(0);
            } else {
              logger.warn("invalid format of 'get_attr' function - " + propertyValue.toString());
            }
          }
        }
      } else if (((Map) propertyValue).containsKey("get_resource")) {
        value = ((Map) propertyValue).get("get_resource");
        if (value instanceof String) {
          return (String) value;
        } else {
          logger.warn("invalid format of 'get_resource' function - " + propertyValue.toString());
        }
      } else {
        Collection<Object> valCollection = ((Map) propertyValue).values();
        for (Object entryValue : valCollection) {
          String ret = extractContrailGetResourceAttachedHeatResourceId(entryValue);
          if (ret != null) {
            return ret;
          }

        }
      }
    } else if (propertyValue instanceof List) {
      for (Object prop : (List) propertyValue) {
        String ret = extractContrailGetResourceAttachedHeatResourceId(prop);
        if (ret != null) {
          return ret;
        }
      }
    }
    return null;
  }

  /**
   * Gets tosca service model.
   *
   * @param translateTo the translate to
   * @return the tosca service model
   */
  public static ToscaServiceModel getToscaServiceModel(TranslateTo translateTo) {
    Map<String, ServiceTemplate> serviceTemplates =
        new HashMap<>(translateTo.getContext().getGlobalServiceTemplates());
    Collection<ServiceTemplate> tmpServiceTemplates =
        translateTo.getContext().getTranslatedServiceTemplates().values();
    for (ServiceTemplate serviceTemplate : tmpServiceTemplates) {
      ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates, serviceTemplate);
    }
    return new ToscaServiceModel(null, serviceTemplates,
        ToscaUtil.getServiceTemplateFileName(translateTo.getResource().getType()));
  }

  /**
   * Gets service template from context.
   *
   * @param serviceTemplateFileName the service template file name
   * @param context                 the context
   * @return the service template from context
   */
  public static Optional<ServiceTemplate> getServiceTemplateFromContext(
      String serviceTemplateFileName, TranslationContext context) {
    for (ServiceTemplate serviceTemplate : context.getTranslatedServiceTemplates().values()) {
      if (ToscaUtil.getServiceTemplateFileName(serviceTemplate).equals(serviceTemplateFileName)) {
        return Optional.of(serviceTemplate);
      }
    }

    return Optional.empty();
  }
}
