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
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.translator.api.HeatToToscaTranslator;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.translator.factory.HeatToToscaTranslatorFactory;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.HeatBoolean;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaElementTypes;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.ParameterDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.Template;
import org.openecomp.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedPropertyVal;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.ReferenceType;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.FileDataCollection;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.errors.ResourceNotFoundInHeatFileErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.openecomp.sdc.translator.services.heattotosca.helper.FunctionTranslationHelper;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.io.IOException;
import java.io.InputStream;
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

/**
 * The type Heat to tosca util.
 */
public class HeatToToscaUtil {

  protected static Logger logger = (Logger) LoggerFactory.getLogger(HeatToToscaUtil.class);
  protected static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static final String forwarder = "forwarder";


  /**
   * Load and translate template data translator output.
   *
   * @param fileNameContentMap the file name content map
   * @return the translator output
   */
  public static TranslatorOutput loadAndTranslateTemplateData(
      FileContentHandler fileNameContentMap) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    HeatToToscaTranslator heatToToscaTranslator =
        HeatToToscaTranslatorFactory.getInstance().createInterface();

    try (InputStream fileContent = fileNameContentMap.getFileContent(SdcCommon.MANIFEST_NAME)) {
      heatToToscaTranslator.addManifest(SdcCommon.MANIFEST_NAME, FileUtils.toByteArray(fileContent));
    } catch (IOException e) {
      throw new RuntimeException("Failed to read manifest", e);
    }

    fileNameContentMap.getFileList().stream()
        .filter(fileName -> !(fileName.equals(SdcCommon.MANIFEST_NAME))).forEach(
            fileName -> heatToToscaTranslator
                .addFile(fileName, FileUtils.toByteArray
                    (fileNameContentMap.getFileContent(fileName))));

    Map<String, List<ErrorMessage>> errors = heatToToscaTranslator.validate();
    if (MapUtils.isNotEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, errors))) {
      TranslatorOutput translatorOutput = new TranslatorOutput();
      translatorOutput.setErrorMessages(errors);
      return translatorOutput;
    }

    try (InputStream structureFile = getHeatStructureTreeFile(fileNameContentMap)) {
      heatToToscaTranslator.addExternalArtifacts(SdcCommon.HEAT_META, structureFile);

      mdcDataDebugMessage.debugExitMessage(null, null);
      return heatToToscaTranslator.translate();
    } catch (IOException e) {
      // rethrow as a RuntimeException to keep the signature backward compatible
      throw new RuntimeException("Failed to read Heat template tree", e);
    }
  }


  private static InputStream getHeatStructureTreeFile(FileContentHandler fileNameContentMap) {
    HeatTreeManager heatTreeManager = HeatTreeManagerUtil.initHeatTreeManager(fileNameContentMap);
    heatTreeManager.createTree();
    HeatStructureTree tree = heatTreeManager.getTree();
    ValidationStructureList validationStructureList = new ValidationStructureList(tree);
    return FileUtils.convertToInputStream(validationStructureList, FileUtils.FileExtension.JSON);
  }

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

  /**
   * Gets filtered list of file data by types.
   *
   * @param filesToSearch the files to search
   * @param types         the types
   * @return the filtered list of file data by types
   */
  public static List<FileData> getFilteredListOfFileDataByTypes(List<FileData> filesToSearch,
                                                                FileData.Type... types) {
    return filesToSearch.stream().filter(FileData.buildFileDataPredicateByType(types))
        .collect(Collectors.toList());
  }

  /**
   * Gets file data from the list according to the input heat file name.
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

  /**
   * Gets file data which is supported by the translator, from the context according the input heat
   * file name.
   *
   * @param heatFileName the heat file name
   * @param context      the translation context
   * @return the file data
   */
  public static FileData getFileData(String heatFileName, TranslationContext context) {

    List<FileData> fileDataList = context.getManifest().getContent().getData();
    for (FileData fileData : fileDataList) {
      if (TranslationService.getTypesToProcessByTranslator().contains(fileData.getType())
          && fileData.getFile().equals(heatFileName)) {
        return fileData;
      }
    }
    return null;
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
        if (!MapUtils.isEmpty(heatOrchestrationTemplate.getResources())) {
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
                  handleNestedFile(translationContext, fileDataCollection, filteredFiles,
                      referenced,
                      internalResourceType);
                }
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


    mdcDataDebugMessage.debugEntryMessage(null, null);

    referenced.add(nestedFileName);
    fileDataCollection.addNestedFiles(filteredFiles.get(nestedFileName));
    translationContext.getNestedHeatsFiles().add(nestedFileName);

    mdcDataDebugMessage.debugExitMessage(null, null);
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


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Object propertyValue = translateTo.getResource().getProperties().get(propertyName);
    if (propertyValue == null) {
      return Optional.empty();
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
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
  public static Optional<AttachedResourceId> extractAttachedResourceId(
      String heatFileName,
      HeatOrchestrationTemplate heatOrchestrationTemplate,
      TranslationContext context,
      Object propertyValue) {

    Object entity;
    Object translatedId;

    if (Objects.isNull(propertyValue)) {
      return Optional.empty();
    }

    ReferenceType referenceType = ReferenceType.OTHER;
    if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
      Map<String, Object> propMap = (Map) propertyValue;
      Map.Entry<String, Object> entry = propMap.entrySet().iterator().next();
      entity = entry.getValue();
      String key = entry.getKey();
      switch (key) {
        case "get_resource":
          referenceType = ReferenceType.GET_RESOURCE;
          break;
        case "get_param":
          referenceType = ReferenceType.GET_PARAM;
          break;
        case "get_attr":
          referenceType = ReferenceType.GET_ATTR;
          break;
        default:
          referenceType = ReferenceType.OTHER;
          break;
      }

      if (!FunctionTranslationFactory.getInstance(entry.getKey()).isPresent()) {
        translatedId = null;
      } else {
        translatedId = FunctionTranslationFactory.getInstance(entry.getKey()).get()
            .translateFunction(null, null, null, entry.getKey(), entry.getValue(), heatFileName,
                heatOrchestrationTemplate, null, context);
      }
      if (translatedId instanceof String
          && !FunctionTranslationHelper.isResourceSupported((String) translatedId)) {
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


    mdcDataDebugMessage.debugEntryMessage(null, null);

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

    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.empty();
  }

  /**
   * Extract property optional.
   *
   * @param propertyValue the property value
   * @return the optional
   */
  public static Optional<AttachedPropertyVal> extractProperty(Object propertyValue) {

    mdcDataDebugMessage.debugEntryMessage(null, null);
    Object attachedPropertyVal;
    if (Objects.isNull(propertyValue)) {
      return Optional.empty();
    }

    ReferenceType referenceType = ReferenceType.OTHER;
    if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
      Map<String, Object> propMap = (Map) propertyValue;
      Map.Entry<String, Object> entry = propMap.entrySet().iterator().next();
      attachedPropertyVal = entry.getValue();
      String key = entry.getKey();
      switch (key) {
        case "get_resource":
          referenceType = ReferenceType.GET_RESOURCE;
          break;
        case "get_param":
          referenceType = ReferenceType.GET_PARAM;
          break;
        case "get_attr":
          referenceType = ReferenceType.GET_ATTR;
          break;
        default:
          break;
      }

    } else {
      attachedPropertyVal = propertyValue;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.of(new AttachedPropertyVal(attachedPropertyVal, referenceType));
  }

  /**
   * Map boolean.
   *
   * @param nodeTemplate the node template
   * @param propertyKey  the property key
   */
  public static void mapBoolean(NodeTemplate nodeTemplate, String propertyKey) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Object value = nodeTemplate.getProperties().get(propertyKey);
    if (value != null && !(value instanceof Map)) {
      nodeTemplate.getProperties().put(propertyKey, HeatBoolean.eval(value));
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
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


  /**
   * Is yml file type boolean.
   *
   * @param filename the filename
   * @return the boolean
   */
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
    mdcDataDebugMessage.debugEntryMessage(null, null);

    String resourceType = resource.getType();

    if (resourceType.equals(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource())) {
      Object resourceDef = resource.getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME);
      if (!(((Map) resourceDef).get("type") instanceof String)) {
        //currently only resource group which is poinitng to nested heat file is supported
        //dynamic type is currently not supported
        return false;
      }
      String internalResourceType = (String) ((Map) resourceDef).get("type");
      if (isYamlFile(internalResourceType)) {
        return true;
      }
    } else if (isYamlFile(resourceType)) {
      return true;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return false;
  }

  /**
   * Checks if the nested resource represents a VFC or a complex VFC (Heat file should contain at
   * least one or more compute nodes).
   *
   * @param resource the resource
   * @param context the context
   * @return true if the resource represents a VFC and false otherwise.
   */
  public static boolean isNestedVfcResource(Resource resource, TranslationContext context) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    Optional<String> nestedHeatFileName = HeatToToscaUtil.getNestedHeatFileName(resource);
    HeatOrchestrationTemplate nestedHeatOrchestrationTemplate = new YamlUtil()
        .yamlToObject(context.getFileContent(nestedHeatFileName.get()),
            HeatOrchestrationTemplate.class);
    if (Objects.nonNull(nestedHeatOrchestrationTemplate.getResources())) {
      for (String innerResourceId : nestedHeatOrchestrationTemplate.getResources().keySet()) {
        if (ConsolidationDataUtil
            .isComputeResource(nestedHeatOrchestrationTemplate, innerResourceId)) {
          return true;
        }
      }
    }
    mdcDataDebugMessage.debugExitMessage(null, null);
    return false;
  }

  /**
   * Get nested heat file name in case of nested resource.
   *
   * @param resource the resource
   * @return the nested heat file name
   */
  public static Optional<String> getNestedHeatFileName(Resource resource) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (!isNestedResource(resource)) {
      return Optional.empty();
    }

    String resourceType = resource.getType();

    if (resourceType.equals(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource())) {
      Object resourceDef = resource.getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME);
      String internalResourceType = (String) ((Map) resourceDef).get("type");
      return Optional.of(internalResourceType);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.of(resourceType);
  }

  /**
   * Gets nested file.
   *
   * @param resource the resource
   * @return the nested file
   */
  public static Optional<String> getNestedFile(Resource resource) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (!isNestedResource(resource)) {
      return Optional.empty();
    }
    String resourceType = resource.getType();
    if (resourceType.equals(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource())) {
      Object resourceDef = resource.getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME);
      String internalResourceType = (String) ((Map) resourceDef).get("type");

      mdcDataDebugMessage.debugExitMessage(null, null);
      return Optional.of(internalResourceType);
    } else {
      mdcDataDebugMessage.debugExitMessage(null, null);
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


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
    if (resource == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_RESOURCE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.TRANSLATE_HEAT);
      throw new CoreException(
          new ResourceNotFoundInHeatFileErrorBuilder(resourceId, heatFileName).build());
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return resource;
  }


  /**
   * Get resource type.
   *
   * @param resourceId                the resource id
   * @param heatOrchestrationTemplate heat orchestration template
   * @param heatFileName              heat file name
   * @return resource type
   */
  public static String getResourceType(String resourceId,
                                       HeatOrchestrationTemplate heatOrchestrationTemplate,
                                       String heatFileName) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return HeatToToscaUtil.getResource(heatOrchestrationTemplate, resourceId, heatFileName)
        .getType();
  }

  /**
   * Is heat file nested boolean.
   *
   * @param translateTo  the translate to
   * @param heatFileName the heat file name
   * @return the boolean
   */
  public static boolean isHeatFileNested(TranslateTo translateTo, String heatFileName) {
    return translateTo.getContext().getNestedHeatsFiles().contains(heatFileName);
  }

  /**
   * Extract contrail get resource attached heat resource id optional.
   *
   * @param propertyValue the property value
   * @return the optional
   */
  public static Optional<String> extractContrailGetResourceAttachedHeatResourceId(
      Object propertyValue) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (propertyValue == null) {
      return Optional.empty();
    }

    Object value;
    if (propertyValue instanceof Map) {
      if (((Map) propertyValue).containsKey("get_attr")) {
        value = ((Map) propertyValue).get("get_attr");
        if (value instanceof List) {
          if (((List) value).size() == 2 && ((List) value).get(1).equals("fq_name")) {
            if (((List) value).get(0) instanceof String) {
              return Optional.of((String) ((List) value).get(0));
            } else {
              logger.warn("invalid format of 'get_attr' function - " + propertyValue.toString());
            }
          }
        }
      } else if (((Map) propertyValue).containsKey("get_resource")) {
        value = ((Map) propertyValue).get("get_resource");
        if (value instanceof String) {
          return Optional.of((String) value);
        } else {
          logger.warn("invalid format of 'get_resource' function - " + propertyValue.toString());
        }
      } else {
        Collection<Object> valCollection = ((Map) propertyValue).values();
        for (Object entryValue : valCollection) {
          Optional<String> ret = extractContrailGetResourceAttachedHeatResourceId(entryValue);
          if (ret.isPresent()) {
            return ret;
          }

        }
      }
    } else if (propertyValue instanceof List) {
      for (Object prop : (List) propertyValue) {
        Optional<String> ret = extractContrailGetResourceAttachedHeatResourceId(prop);
        if (ret.isPresent()) {
          return ret;
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.empty();
  }

  /**
   * Gets tosca service model.
   *
   * @param context translation context
   * @return the tosca service model
   */
  public static ToscaServiceModel getToscaServiceModel(TranslationContext context) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    mdcDataDebugMessage.debugExitMessage(null, null);

    Map<String, String> metadata = new HashMap<>();
    metadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, Constants.MAIN_TEMPLATE_NAME);
    return getToscaServiceModel(context, metadata);
  }

  /**
   * Gets tosca service model.
   *
   * @param context                 translation context
   * @param entryDefinitionMetadata template name of the entry definition servie template
   * @return the tosca service model
   */
  public static ToscaServiceModel getToscaServiceModel(
      TranslationContext context,
      Map<String, String> entryDefinitionMetadata) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, ServiceTemplate> serviceTemplates =
        new HashMap<>(context.getGlobalServiceTemplates());
    Collection<ServiceTemplate> tmpServiceTemplates =
        context.getTranslatedServiceTemplates().values();
    for (ServiceTemplate serviceTemplate : tmpServiceTemplates) {
      ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates, serviceTemplate);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return new ToscaServiceModel(null, serviceTemplates,
        ToscaUtil.getServiceTemplateFileName(entryDefinitionMetadata));
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


    mdcDataDebugMessage.debugEntryMessage(null, null);

    for (ServiceTemplate serviceTemplate : context.getTranslatedServiceTemplates().values()) {
      if (ToscaUtil.getServiceTemplateFileName(serviceTemplate).equals(serviceTemplateFileName)) {
        mdcDataDebugMessage.debugExitMessage(null, null);
        return Optional.of(serviceTemplate);
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.empty();
  }

  /**
   * Adding link requerment from port node template to network node template.
   *
   * @param portNodeTemplate    port node template
   * @param networkTranslatedId network node template id
   */
  public static RequirementAssignment addLinkReqFromPortToNetwork(NodeTemplate portNodeTemplate,
                                                                  String networkTranslatedId) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    RequirementAssignment requirement = new RequirementAssignment();
    requirement.setCapability(ToscaCapabilityType.NATIVE_NETWORK_LINKABLE);
    requirement.setRelationship(ToscaRelationshipType.NATIVE_NETWORK_LINK_TO);
    requirement.setNode(networkTranslatedId);
    DataModelUtil.addRequirementAssignment(portNodeTemplate,
        ToscaConstants.LINK_REQUIREMENT_ID, requirement);

    mdcDataDebugMessage.debugExitMessage(null, null);

    return requirement;
  }

  /**
   * Adding binding requerment from sub interface node template to interface (port) node template.
   *
   * @param subInterfaceNodeTemplate sub interface template
   * @param interfaceTranslatedId    interface node template id
   */
  public static void addBindingReqFromSubInterfaceToInterface(
      NodeTemplate subInterfaceNodeTemplate, String interfaceTranslatedId) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    RequirementAssignment requirement = new RequirementAssignment();
    requirement.setCapability(ToscaCapabilityType.NATIVE_NETWORK_BINDABLE);
    requirement.setRelationship(ToscaRelationshipType.NATIVE_NETWORK_BINDS_TO);
    requirement.setNode(interfaceTranslatedId);
    DataModelUtil
        .addRequirementAssignment(subInterfaceNodeTemplate,
            ToscaConstants.BINDING_REQUIREMENT_ID, requirement);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  /**
   * Get property Parameter Name Value.
   *
   * @param property property
   * @return Parameter name in case the property include "get_param" function
   */
  public static Optional<String> getPropertyParameterNameValue(Object property) {
    if (Objects.isNull(property)) {
      return Optional.empty();
    }
    Optional<AttachedPropertyVal> extractedProperty = extractProperty(property);
    if (extractedProperty.isPresent()) {
      return getParameterName(extractedProperty.get());
    }
    return Optional.empty();
  }

  private static Optional<String> getParameterName(AttachedPropertyVal extractedProperty) {
    if (!extractedProperty.isGetParam()) {
      return Optional.empty();
    }
    Object getParamFuncValue = extractedProperty.getPropertyValue();
    if (getParamFuncValue instanceof String) {
      return Optional.of((String) getParamFuncValue);
    } else {
      return Optional.of((String) ((List) getParamFuncValue).get(0));
    }
  }

  public static String getToscaPropertyName(TranslationContext context, String heatResourceType,
                                            String heatPropertyName) {
    return context.getElementMapping(heatResourceType, Constants.PROP, heatPropertyName);
  }

  /**
   * Gets tosca property name.
   *
   * @param translateTo      the translate to
   * @param heatPropertyName the heat property name
   * @return the tosca property name
   */
  public static String getToscaPropertyName(TranslateTo translateTo, String heatPropertyName) {
    return translateTo.getContext()
        .getElementMapping(translateTo.getResource().getType(), Constants.PROP, heatPropertyName);
  }

  /**
   * Gets tosca attribute name.
   *
   * @param context          the context
   * @param heatResourceType the heat resource type
   * @param heatAttrName     the heat attr name
   * @return the tosca attribute name
   */
  public static String getToscaAttributeName(TranslationContext context, String heatResourceType,
                                             String heatAttrName) {
    return context.getElementMapping(heatResourceType, Constants.ATTR, heatAttrName);
  }

  /**
   * Gets tosca attribute name.
   *
   * @param translateTo  the translate to
   * @param heatAttrName the heat attr name
   * @return the tosca attribute name
   */
  public static String getToscaAttributeName(TranslateTo translateTo, String heatAttrName) {
    return translateTo.getContext()
        .getElementMapping(translateTo.getResource().getType(), Constants.ATTR, heatAttrName);
  }

  /**
   * Create init substitution service template service template.
   *
   * @param templateName the template name
   * @return the service template
   */
  public static ServiceTemplate createInitSubstitutionServiceTemplate(String templateName) {
    ServiceTemplate nestedSubstitutionServiceTemplate = new ServiceTemplate();
    Map<String, String> templateMetadata = new HashMap<>();
    templateMetadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, templateName);
    nestedSubstitutionServiceTemplate.setMetadata(templateMetadata);
    nestedSubstitutionServiceTemplate
        .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    nestedSubstitutionServiceTemplate.setTopology_template(new TopologyTemplate());
    List<Map<String, Import>> globalTypesImportList =
        GlobalTypesGenerator.getGlobalTypesImportList();
    globalTypesImportList.addAll(
        HeatToToscaUtil.createImportList(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME));
    nestedSubstitutionServiceTemplate.setImports(globalTypesImportList);
    return nestedSubstitutionServiceTemplate;
  }

  /**
   * Create init global substitution service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createInitGlobalSubstitutionServiceTemplate() {
    ServiceTemplate globalSubstitutionServiceTemplate = new ServiceTemplate();
    Map<String, String> templateMetadata = new HashMap<>();
    templateMetadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME,
        Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
    globalSubstitutionServiceTemplate.setMetadata(templateMetadata);
    globalSubstitutionServiceTemplate
        .setImports(GlobalTypesGenerator.getGlobalTypesImportList());
    globalSubstitutionServiceTemplate
        .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    return globalSubstitutionServiceTemplate;
  }

  /**
   * Create substitution node type node type.
   *
   * @param substitutionServiceTemplate the substitution service template
   * @return the node type
   */
  public NodeType createSubstitutionNodeType(ServiceTemplate substitutionServiceTemplate) {
    NodeType substitutionNodeType = new NodeType();
    substitutionNodeType.setDerived_from(ToscaNodeType.ABSTRACT_SUBSTITUTE);
    substitutionNodeType.setDescription(substitutionServiceTemplate.getDescription());
    substitutionNodeType
        .setProperties(manageSubstitutionNodeTypeProperties(substitutionServiceTemplate));
    substitutionNodeType
        .setAttributes(manageSubstitutionNodeTypeAttributes(substitutionServiceTemplate));
    return substitutionNodeType;
  }

  private Map<String, PropertyDefinition> manageSubstitutionNodeTypeProperties(
      ServiceTemplate substitutionServiceTemplate) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, PropertyDefinition> substitutionNodeTypeProperties = new HashMap<>();
    Map<String, ParameterDefinition> properties =
        substitutionServiceTemplate.getTopology_template().getInputs();
    if (properties == null) {
      mdcDataDebugMessage.debugExitMessage(null, null);
      return null;
    }

    PropertyDefinition propertyDefinition;
    String toscaPropertyName;
    for (Map.Entry<String, ParameterDefinition> entry : properties.entrySet()) {
      toscaPropertyName = entry.getKey();
      propertyDefinition = new PropertyDefinition();
      ParameterDefinition parameterDefinition =
          substitutionServiceTemplate.getTopology_template().getInputs().get(toscaPropertyName);
      propertyDefinition.setType(parameterDefinition.getType());
      propertyDefinition.setDescription(parameterDefinition.getDescription());
      propertyDefinition.setRequired(parameterDefinition.getRequired());
      propertyDefinition.set_default(parameterDefinition.get_default());
      propertyDefinition.setConstraints(parameterDefinition.getConstraints());
      propertyDefinition.setEntry_schema(parameterDefinition.getEntry_schema());
      propertyDefinition.setStatus(parameterDefinition.getStatus());
      substitutionNodeTypeProperties.put(toscaPropertyName, propertyDefinition);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return substitutionNodeTypeProperties;
  }

  private Map<String, AttributeDefinition> manageSubstitutionNodeTypeAttributes(
      ServiceTemplate substitutionServiceTemplate) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, AttributeDefinition> substitutionNodeTypeAttributes = new HashMap<>();
    Map<String, ParameterDefinition> attributes =
        substitutionServiceTemplate.getTopology_template().getOutputs();
    if (attributes == null) {
      mdcDataDebugMessage.debugExitMessage(null, null);
      return null;
    }
    AttributeDefinition attributeDefinition;
    String toscaAttributeName;

    for (Map.Entry<String, ParameterDefinition> entry : attributes.entrySet()) {
      attributeDefinition = new AttributeDefinition();
      toscaAttributeName = entry.getKey();
      ParameterDefinition parameterDefinition =
          substitutionServiceTemplate.getTopology_template().getOutputs().get(toscaAttributeName);
      if (parameterDefinition.getType() != null && !parameterDefinition.getType().isEmpty()) {
        attributeDefinition.setType(parameterDefinition.getType());
      } else {
        attributeDefinition.setType(PropertyType.STRING.getDisplayName());
      }
      attributeDefinition.setDescription(parameterDefinition.getDescription());
      attributeDefinition.set_default(parameterDefinition.get_default());
      attributeDefinition.setEntry_schema(parameterDefinition.getEntry_schema());
      attributeDefinition.setStatus(parameterDefinition.getStatus());
      substitutionNodeTypeAttributes.put(toscaAttributeName, attributeDefinition);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return substitutionNodeTypeAttributes;
  }

  /**
   * .
   * Create and add substitution mapping to the nested substitution service template, and update
   * the subtitution node type accordingly with the exposed requerments and capabilities
   *
   * @param context                           the translation context
   * @param substitutionNodeTypeKey           the substitution node type key
   * @param nestedSubstitutionServiceTemplate the nested substitution service template
   * @param substitutionNodeType              the substitution node type
   */
  public static void handleSubstitutionMapping(
      TranslationContext context,
      String substitutionNodeTypeKey,
      ServiceTemplate nestedSubstitutionServiceTemplate,
      NodeType substitutionNodeType) {
    Map<String, Map<String, List<String>>> substitutionMapping =
        getSubstitutionNodeTypeExposedConnectionPoints(substitutionNodeType,
            nestedSubstitutionServiceTemplate, context);
    //add substitution mapping after capability and requirement expose calculation
    nestedSubstitutionServiceTemplate.getTopology_template().setSubstitution_mappings(
        DataModelUtil.createSubstitutionTemplateSubMapping(substitutionNodeTypeKey,
            substitutionNodeType, substitutionMapping));
  }

  /**
   * Gets node type with flat hierarchy.
   *
   * @param nodeTypeId      the node type id
   * @param serviceTemplate the service template
   * @param context         the context
   * @return the node type with flat hierarchy
   */
  public static NodeType getNodeTypeWithFlatHierarchy(String nodeTypeId,
                                                      ServiceTemplate serviceTemplate,
                                                      TranslationContext context) {
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
    ToscaServiceModel toscaServiceModel = HeatToToscaUtil
        .getToscaServiceModel(context, serviceTemplate.getMetadata());
    NodeType flatNodeType = (NodeType) toscaAnalyzerService
        .getFlatEntity(ToscaElementTypes.NODE_TYPE, nodeTypeId, serviceTemplate, toscaServiceModel);
    return flatNodeType;
  }

  /**
   * Create substitution node template node template.
   *
   * @param translateTo             the translate to
   * @param templateName            the template name
   * @param substitutionNodeTypeKey the substitution node type key
   * @return the node template
   */
  public NodeTemplate createSubstitutionNodeTemplate(TranslateTo translateTo, String templateName,
                                                     String substitutionNodeTypeKey) {
    NodeTemplate substitutionNodeTemplate = new NodeTemplate();
    List<String> directiveList = new ArrayList<>();
    directiveList.add(ToscaConstants.NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE);
    substitutionNodeTemplate.setDirectives(directiveList);
    substitutionNodeTemplate.setType(substitutionNodeTypeKey);
    substitutionNodeTemplate.setProperties(
        managerSubstitutionNodeTemplateProperties(translateTo, substitutionNodeTemplate,
            templateName));
    return substitutionNodeTemplate;
  }

  /**
   * Create abstract substitution node template.
   *
   * @param translateTo             the translate to
   * @param templateName            the template name
   * @param substitutionNodeTypeKey the substitution node type key
   * @return the abstract substitute node template
   */
  public static NodeTemplate createAbstractSubstitutionNodeTemplate(
      TranslateTo translateTo,
      String templateName,
      String substitutionNodeTypeKey) {
    NodeTemplate substitutionNodeTemplate = new NodeTemplate();
    List<String> directiveList = new ArrayList<>();
    directiveList.add(ToscaConstants.NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE);
    substitutionNodeTemplate.setDirectives(directiveList);
    substitutionNodeTemplate.setType(substitutionNodeTypeKey);
    substitutionNodeTemplate.setProperties(
        managerSubstitutionNodeTemplateProperties(translateTo, substitutionNodeTemplate,
            templateName));
    return substitutionNodeTemplate;
  }


  /**
   * Checks if the source and target resource is a valid candidate for adding tosca dependency
   * relationship.
   *
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param sourceResource          the source resource
   * @param targetResource          the target resource
   * @param dependencyEntity        the dependency entity
   * @return true if the candidate resources are a valid combination for the dependency relationship
   * and false otherwise
   */
  public static boolean isValidDependsOnCandidate(HeatOrchestrationTemplate
                                                      heatOrchestrationTemplate,
                                                  Resource sourceResource,
                                                  Resource targetResource,
                                                  ConsolidationEntityType dependencyEntity,
                                                  TranslationContext context) {
    dependencyEntity
        .setEntityType(heatOrchestrationTemplate, sourceResource, targetResource, context);
    ConsolidationEntityType sourceEntityType = dependencyEntity.getSourceEntityType();
    ConsolidationEntityType targetEntityType = dependencyEntity.getTargetEntityType();

    return ConsolidationTypesConnectivity.isDependsOnRelationshipValid(sourceEntityType, targetEntityType);
  }

  private static Map<String, Object> managerSubstitutionNodeTemplateProperties(
      TranslateTo translateTo,
      Template template,
      String templateName) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, Object> substitutionProperties = new HashMap<>();
    Map<String, Object> heatProperties = translateTo.getResource().getProperties();
    if (Objects.nonNull(heatProperties)) {
      for (Map.Entry<String, Object> entry : heatProperties.entrySet()) {
        Object property = TranslatorHeatToToscaPropertyConverter
            .getToscaPropertyValue(translateTo.getServiceTemplate(),
                translateTo.getTranslatedId(), entry.getKey(),
                entry.getValue(), null, translateTo.getHeatFileName(),
                translateTo.getHeatOrchestrationTemplate(), template, translateTo.getContext());
        substitutionProperties.put(entry.getKey(), property);
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return addAbstractSubstitutionProperty(templateName, substitutionProperties);
  }

  private static Map<String, Object> addAbstractSubstitutionProperty(String templateName,
                                                                     Map<String, Object>
                                                                         substitutionProperties) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, Object> innerProps = new HashMap<>();
    innerProps.put(ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME,
        ToscaUtil.getServiceTemplateFileName(templateName));
    substitutionProperties.put(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME, innerProps);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return substitutionProperties;
  }

  private static Map<String, Map<String, List<String>>>
  getSubstitutionNodeTypeExposedConnectionPoints(NodeType substitutionNodeType,
                                                 ServiceTemplate substitutionServiceTemplate,
                                                 TranslationContext context) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, NodeTemplate> nodeTemplates =
        substitutionServiceTemplate.getTopology_template().getNode_templates();
    String nodeTemplateId;
    NodeTemplate nodeTemplate;
    String nodeType;
    Map<String, Map<String, List<String>>> substitutionMapping = new HashMap<>();
    if (nodeTemplates == null) {
      return substitutionMapping;
    }

    Map<String, List<String>> capabilitySubstitutionMapping = new HashMap<>();
    Map<String, List<String>> requirementSubstitutionMapping = new HashMap<>();
    substitutionMapping.put("capability", capabilitySubstitutionMapping);
    substitutionMapping.put("requirement", requirementSubstitutionMapping);
    List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinition;
    Map<String, RequirementAssignment> nodeTemplateRequirementsAssignment;
    List<Map<String, RequirementDefinition>> exposedRequirementsDefinition;
    Map<String, Map<String, RequirementAssignment>> fullFilledRequirementsDefinition =
        new HashMap<>();
    Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition = new HashMap<>();
    Map<String, CapabilityDefinition> exposedCapabilitiesDefinition;
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();

    for (Map.Entry<String, NodeTemplate> entry : nodeTemplates.entrySet()) {
      nodeTemplateId = entry.getKey();
      nodeTemplate = entry.getValue();
      nodeType = nodeTemplate.getType();

      // get requirements
      nodeTypeRequirementsDefinition =
          getNodeTypeReqs(nodeType, nodeTemplateId, substitutionServiceTemplate,
              requirementSubstitutionMapping, context);
      nodeTemplateRequirementsAssignment = DataModelUtil.getNodeTemplateRequirements(nodeTemplate);
      fullFilledRequirementsDefinition.put(nodeTemplateId, nodeTemplateRequirementsAssignment);
      //set substitution node type requirements
      exposedRequirementsDefinition =
          toscaAnalyzerService.calculateExposedRequirements(nodeTypeRequirementsDefinition,
          nodeTemplateRequirementsAssignment);
      DataModelUtil
          .addSubstitutionNodeTypeRequirements(substitutionNodeType, exposedRequirementsDefinition,
          nodeTemplateId);

      //get capabilities
      addNodeTypeCapabilitiesToSubMapping(nodeTypeCapabilitiesDefinition,
          capabilitySubstitutionMapping, nodeType,
          nodeTemplateId, substitutionServiceTemplate, context);
    }

    exposedCapabilitiesDefinition =
        toscaAnalyzerService.calculateExposedCapabilities(nodeTypeCapabilitiesDefinition,
        fullFilledRequirementsDefinition);
    DataModelUtil.addNodeTypeCapabilitiesDef(substitutionNodeType, exposedCapabilitiesDefinition);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return substitutionMapping;
  }

  private static void addNodeTypeCapabilitiesToSubMapping(
      Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition,
      Map<String, List<String>> capabilitySubstitutionMapping, String type, String templateName,
      ServiceTemplate serviceTemplate, TranslationContext context) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    NodeType flatNodeType =
        getNodeTypeWithFlatHierarchy(type, serviceTemplate, context);

    if (flatNodeType.getCapabilities() != null) {
      flatNodeType.getCapabilities()
          .entrySet()
          .stream()
          .filter(capabilityNodeEntry -> shouldCapabilityNeedsToBeAdded(capabilityNodeEntry.getKey()))
          .forEach(capabilityNodeEntry ->
              addCapabilityToSubMapping(
              templateName, capabilityNodeEntry, nodeTypeCapabilitiesDefinition, capabilitySubstitutionMapping));
    }
    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private static boolean shouldCapabilityNeedsToBeAdded(String capabilityKey) {
    return !capabilityKey.contains(forwarder) || ToggleableFeature.FORWARDER_CAPABILITY.isActive();
  }

  private static void addCapabilityToSubMapping(String templateName,
                                                Map.Entry<String, CapabilityDefinition> capabilityNodeEntry,
                                                Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition,
                                                Map<String, List<String>> capabilitySubstitutionMapping) {
    String capabilityKey;
    List<String> capabilityMapping;
    capabilityKey = capabilityNodeEntry.getKey() + "_" + templateName;
    nodeTypeCapabilitiesDefinition.put(capabilityKey, capabilityNodeEntry.getValue().clone());
    capabilityMapping = new ArrayList<>();
    capabilityMapping.add(templateName);
    capabilityMapping.add(capabilityNodeEntry.getKey());
    capabilitySubstitutionMapping.put(capabilityKey, capabilityMapping);
  }

  private static List<Map<String, RequirementDefinition>> getNodeTypeReqs(
      String type,
      String templateName,
      ServiceTemplate serviceTemplate,
      Map<String, List<String>> requirementSubstitutionMapping,
      TranslationContext context) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    List<Map<String, RequirementDefinition>> requirementList = new ArrayList<>();
    NodeType flatNodeType =
        getNodeTypeWithFlatHierarchy(type, serviceTemplate, context);
    List<String> requirementMapping;

    if (flatNodeType.getRequirements() != null) {
      for (Map<String, RequirementDefinition> requirementMap : flatNodeType.getRequirements()) {
        for (Map.Entry<String, RequirementDefinition> requirementNodeEntry : requirementMap
            .entrySet()) {
          ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
          RequirementDefinition requirementNodeEntryValue = toscaExtensionYamlUtil
              .yamlToObject(toscaExtensionYamlUtil.objectToYaml(requirementNodeEntry.getValue()),
                  RequirementDefinition.class);
          if (requirementNodeEntryValue.getOccurrences() == null) {
            requirementNodeEntryValue.setOccurrences(new Object[]{1, 1});
          }
          Map<String, RequirementDefinition> requirementDef = new HashMap<>();
          requirementDef.put(requirementNodeEntry.getKey(), requirementNodeEntryValue);
          DataModelUtil.addRequirementToList(requirementList, requirementDef);
          requirementMapping = new ArrayList<>();
          requirementMapping.add(templateName);
          requirementMapping.add(requirementNodeEntry.getKey());
          requirementSubstitutionMapping
              .put(requirementNodeEntry.getKey() + "_" + templateName, requirementMapping);
          if (requirementNodeEntryValue.getNode() == null) {
            requirementNodeEntryValue.setOccurrences(new Object[]{1, 1});
          }
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return requirementList;
  }

  /**
   * Fetch global substitution service template service template.
   *
   * @param serviceTemplate the service template
   * @param context         the context
   * @return the service template
   */
  public static ServiceTemplate fetchGlobalSubstitutionServiceTemplate(
      ServiceTemplate serviceTemplate,
      TranslationContext context) {
    ServiceTemplate globalSubstitutionServiceTemplate =
        context.getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
    if (globalSubstitutionServiceTemplate == null) {
      globalSubstitutionServiceTemplate =
          HeatToToscaUtil.createInitGlobalSubstitutionServiceTemplate();
      context.getTranslatedServiceTemplates()
          .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
              globalSubstitutionServiceTemplate);
    }
    boolean isImportAddedToServiceTemplate =
        DataModelUtil.isImportAddedToServiceTemplate(serviceTemplate.getImports(), Constants
            .GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
    if (!isImportAddedToServiceTemplate) {
      serviceTemplate.getImports()
          .addAll(
              HeatToToscaUtil.createImportList(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME));
    }
    return globalSubstitutionServiceTemplate;
  }

  public static List<Map<String, Import>> createImportList(String templateName) {
    List<Map<String, Import>> imports = new ArrayList<>();
    Map<String, Import> importsMap = new HashMap<>();
    importsMap.put(templateName, HeatToToscaUtil.createServiceTemplateImport(templateName));
    imports.add(importsMap);
    return imports;
  }

  /**
   * Create service template import import.
   *
   * @param serviceTemplate the service template
   * @return the import
   */
  public static Import createServiceTemplateImport(ServiceTemplate serviceTemplate) {
    Import serviceTemplateImport = new Import();
    serviceTemplateImport.setFile(ToscaUtil.getServiceTemplateFileName(serviceTemplate));
    return serviceTemplateImport;
  }

  /**
   * Create service template import import.
   *
   * @param metadataTemplateName the service template name
   * @return the import
   */
  public static Import createServiceTemplateImport(String metadataTemplateName) {
    Import serviceTemplateImport = new Import();
    serviceTemplateImport.setFile(ToscaUtil.getServiceTemplateFileName(metadataTemplateName));
    return serviceTemplateImport;
  }

  public static ToscaServiceModel createToscaServiceModel(ServiceTemplate
                                                              entryDefinitionServiceTemplate,
                                                          TranslationContext translationContext) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return new ToscaServiceModel(getCsarArtifactFiles(translationContext),
        getServiceTemplates(translationContext),
        ToscaUtil.getServiceTemplateFileName(entryDefinitionServiceTemplate));
  }

  private static FileContentHandler getCsarArtifactFiles(TranslationContext translationContext) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    FileContentHandler artifactFiles = new FileContentHandler();
    artifactFiles.setFiles(translationContext.getFiles());
    artifactFiles.setFiles(translationContext.getExternalArtifacts());

    HeatTreeManager heatTreeManager =
        HeatTreeManagerUtil.initHeatTreeManager(translationContext.getFiles());
    heatTreeManager.createTree();
    ValidationStructureList validationStructureList =
        new ValidationStructureList(heatTreeManager.getTree());
    byte[] validationStructureFile =
        FileUtils.convertToBytes(validationStructureList, FileUtils.FileExtension.JSON);
    artifactFiles.addFile("HEAT.meta", validationStructureFile);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return artifactFiles;
  }


  private static Map<String, ServiceTemplate> getServiceTemplates(TranslationContext
                                                                      translationContext) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    List<ServiceTemplate> serviceTemplates = new ArrayList<>();
    serviceTemplates.addAll(GlobalTypesGenerator.getGlobalTypesServiceTemplate().values());
    serviceTemplates.addAll(translationContext.getTranslatedServiceTemplates().values());
    Map<String, ServiceTemplate> serviceTemplatesMap = new HashMap<>();

    for (ServiceTemplate template : serviceTemplates) {
      serviceTemplatesMap.put(ToscaUtil.getServiceTemplateFileName(template), template);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return serviceTemplatesMap;
  }

}
