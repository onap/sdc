/*
 * Copyright © 2016-2018 European Support Limited
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
package org.openecomp.sdc.translator.services.heattotosca;

import static org.openecomp.sdc.heat.services.HeatResourceUtil.extractNetworkRoleFromSubInterfaceId;
import static org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation.FunctionTranslator.getFunctionTranslateTo;

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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.onap.sdc.tosca.datatypes.model.AttributeDefinition;
import org.onap.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.onap.sdc.tosca.datatypes.model.Import;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.PropertyDefinition;
import org.onap.sdc.tosca.datatypes.model.PropertyType;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.Template;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.translator.api.HeatToToscaTranslator;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.translator.factory.HeatToToscaTranslatorFactory;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.HeatBoolean;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaElementTypes;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedPropertyVal;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.ReferenceType;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.FileDataCollection;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.errors.ResourceNotFoundInHeatFileErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.openecomp.sdc.translator.services.heattotosca.helper.ContrailV2VirtualMachineInterfaceHelper;
import org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation.FunctionTranslator;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationBase;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

/**
 * The type Heat to tosca util.
 */
public class HeatToToscaUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeatToToscaUtil.class);
    private static final String FQ_NAME = "fq_name";
    private static final String GET_PARAM = "get_param";
    private static final String GET_ATTR = "get_attr";
    private static final String GET_RESOURCE = "get_resource";
    private static final String UNDERSCORE = "_";

    /**
     * Load and translate template data translator output.
     *
     * @param fileNameContentMap the file name content map
     * @return the translator output
     */
    public static TranslatorOutput loadAndTranslateTemplateData(FileContentHandler fileNameContentMap) {
        HeatToToscaTranslator heatToToscaTranslator = HeatToToscaTranslatorFactory.getInstance().createInterface();
        try (InputStream fileContent = fileNameContentMap.getFileContentAsStream(SdcCommon.MANIFEST_NAME)) {
            heatToToscaTranslator.addManifest(SdcCommon.MANIFEST_NAME, FileUtils.toByteArray(fileContent));
        } catch (IOException e) {
            throw new SdcRuntimeException("Failed to read manifest", e);
        }
        fileNameContentMap.getFileList().stream().filter(fileName -> !(fileName.equals(SdcCommon.MANIFEST_NAME)))
            .forEach(fileName -> heatToToscaTranslator.addFile(fileName, fileNameContentMap.getFileContent(fileName)));
        Map<String, List<ErrorMessage>> errors = heatToToscaTranslator.validate();
        if (MapUtils.isNotEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, errors))) {
            TranslatorOutput translatorOutput = new TranslatorOutput();
            translatorOutput.setErrorMessages(errors);
            return translatorOutput;
        }
        try (InputStream structureFile = getHeatStructureTreeFile(fileNameContentMap)) {
            heatToToscaTranslator.addExternalArtifacts(SdcCommon.HEAT_META, structureFile);
            return heatToToscaTranslator.translate();
        } catch (IOException e) {
            // rethrow as a RuntimeException to keep the signature backward compatible
            throw new SdcRuntimeException("Failed to read Heat template tree", e);
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
    public static Optional<List<FileData>> buildListOfFilesToSearch(String heatFileName, List<FileData> filesDataList, FileData.Type... types) {
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
    public static List<FileData> getFilteredListOfFileDataByTypes(List<FileData> filesToSearch, FileData.Type... types) {
        return filesToSearch.stream().filter(FileData.buildFileDataPredicateByType(types)).collect(Collectors.toList());
    }

    /**
     * Gets file data from the list according to the input heat file name.
     *
     * @param heatFileName the heat file name
     * @param fileDataList the file data list
     * @return the file data
     */
    public static Optional<FileData> getFileData(String heatFileName, Collection<FileData> fileDataList) {
        for (FileData file : fileDataList) {
            if (file.getFile().equals(heatFileName)) {
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets file data which is supported by the translator, from the context according the input heat file name.
     *
     * @param heatFileName the heat file name
     * @param context      the translation context
     * @return the file data
     */
    public static FileData getFileData(String heatFileName, TranslationContext context) {
        List<FileData> fileDataList = context.getManifest().getContent().getData();
        for (FileData fileData : fileDataList) {
            if (TranslationService.getTypesToProcessByTranslator().contains(fileData.getType()) && fileData.getFile().equals(heatFileName)) {
                return fileData;
            }
        }
        return null;
    }

    static FileDataCollection getFileCollectionsByHelmFilter(List<FileData> fileDataList, Set<FileData.Type> typeFilter) {
        FileDataCollection fileDataCollection = new FileDataCollection();
        Map<String, FileData> filteredFiles = filterFileDataListByType(fileDataList, typeFilter);
        for (FileData fileData : filteredFiles.values()) {
            String fileName = fileData.getFile();
            if ((fileData.getType().equals(FileData.Type.HELM))) {
                fileDataCollection.addHelmFiles(fileData);
            }
        }
        return fileDataCollection;
    }

    static FileDataCollection getFileCollectionsByFilter(List<FileData> fileDataList, Set<FileData.Type> typeFilter,
                                                         TranslationContext translationContext) {
        FileDataCollection fileDataCollection = new FileDataCollection();
        Map<String, FileData> filteredFiles = filterFileDataListByType(fileDataList, typeFilter);
        Set<String> referenced = new HashSet<>();
        for (FileData fileData : filteredFiles.values()) {
            String fileName = fileData.getFile();
            if (FileData.isHeatFile(fileData.getType())) {
                if (fileData.getBase() != null && fileData.getBase()) {
                    fileDataCollection.addBaseFiles(fileData);
                }
                HeatOrchestrationTemplate heatOrchestrationTemplate = new YamlUtil()
                    .yamlToObject(translationContext.getFileContentAsStream(fileName), HeatOrchestrationTemplate.class);
                if (MapUtils.isNotEmpty(heatOrchestrationTemplate.getResources())) {
                    referenced.addAll(applyFilterOnFileCollection(heatOrchestrationTemplate, translationContext, fileDataCollection, filteredFiles));
                }
            } else {
                fileDataCollection.addArtifactFiles(fileData);
                filteredFiles.remove(fileData.getFile());
            }
        }
        referenced.addAll(getAssociatedFiles(filteredFiles.values()));
        referenced.forEach(filteredFiles::remove);
        if (!CollectionUtils.isEmpty(fileDataCollection.getBaseFile())) {
            for (FileData fileData : fileDataCollection.getBaseFile()) {
                filteredFiles.remove(fileData.getFile());
            }
        }
        fileDataCollection.setAddOnFiles(filteredFiles.values());
        return fileDataCollection;
    }

    private static Set<String> getAssociatedFiles(Collection<FileData> filteredFiles) {
        Set<String> associatedFiles = new HashSet<>();
        filteredFiles.stream().filter(file -> file.getParentFile() != null && FileData.Type.canBeAssociated(file.getType()))
            .forEach(file -> associatedFiles.add(file.getFile()));
        return associatedFiles;
    }

    private static Set<String> applyFilterOnFileCollection(HeatOrchestrationTemplate heatOrchestrationTemplate, TranslationContext translationContext,
                                                           FileDataCollection fileDataCollection, Map<String, FileData> filteredFiles) {
        Set<String> nestedFiles = new HashSet<>();
        List<String> filenames = extractFilenamesFromFileDataList(filteredFiles.values());
        for (Resource resource : heatOrchestrationTemplate.getResources().values()) {
            String resourceType = resource.getType();
            if (filenames.contains(resourceType)) {
                handleNestedFile(translationContext, fileDataCollection, filteredFiles, resourceType);
                nestedFiles.add(resourceType);
            } else if (resourceType.equals(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource())) {
                Optional<String> nestedFile = handleResourceGrpNestedFile(resource, translationContext, fileDataCollection, filteredFiles, filenames);
                nestedFile.ifPresent(nestedFiles::add);
            }
        }
        return nestedFiles;
    }

    private static Optional<String> handleResourceGrpNestedFile(Resource resource, TranslationContext translationContext,
                                                                FileDataCollection fileDataCollection, Map<String, FileData> filteredFiles,
                                                                List<String> filenames) {
        Object resourceDef = resource.getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME);
        Object innerTypeDef = ((Map) resourceDef).get(HeatConstants.RESOURCE_DEF_TYPE_PROPERTY_NAME);
        if (innerTypeDef instanceof String) {
            String internalResourceType = (String) innerTypeDef;
            if (filenames.contains(internalResourceType)) {
                handleNestedFile(translationContext, fileDataCollection, filteredFiles, internalResourceType);
                return Optional.of(internalResourceType);
            }
        }
        return Optional.empty();
    }

    private static void handleNestedFile(TranslationContext translationContext, FileDataCollection fileDataCollection,
                                         Map<String, FileData> filteredFiles, String nestedFileName) {
        fileDataCollection.addNestedFiles(filteredFiles.get(nestedFileName));
        translationContext.getNestedHeatsFiles().add(nestedFileName);
    }

    private static Map<String, FileData> filterFileDataListByType(List<FileData> fileDataList, Set<FileData.Type> typesToGet) {
        Map<String, FileData> filtered = new HashMap<>();
        filterFileDataListByType(fileDataList, filtered, typesToGet, null);
        return filtered;
    }

    private static void filterFileDataListByType(List<FileData> fileDataList, Map<String, FileData> filtered, Set<FileData.Type> typesToGet,
                                                 String parentFileName) {
        fileDataList.stream().filter(file -> typesToGet.contains(file.getType())).forEach(file -> {
            filtered.put(file.getFile(), file);
            file.setParentFile(parentFileName);
        });
        Set<FileData.Type> canBeAssociatedTypes = typesToGet.stream().filter(FileData.Type::canBeAssociated).collect(Collectors.toSet());
        fileDataList.stream().filter(file -> Objects.nonNull(file.getData()))
            .forEach(file -> filterFileDataListByType(file.getData(), filtered, canBeAssociatedTypes, file.getFile()));
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
    public static Optional<AttachedResourceId> extractAttachedResourceId(TranslateTo translateTo, String propertyName) {
        Object propertyValue = translateTo.getResource().getProperties().get(propertyName);
        if (propertyValue == null) {
            return Optional.empty();
        }
        return extractAttachedResourceId(translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(), translateTo.getContext(),
            propertyValue);
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
    public static Optional<AttachedResourceId> extractAttachedResourceId(String heatFileName, HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                                         TranslationContext context, Object propertyValue) {
        Object entity;
        Object translatedId = null;
        if (Objects.isNull(propertyValue)) {
            return Optional.empty();
        }
        ReferenceType referenceType = ReferenceType.OTHER;
        if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
            Map<String, Object> propMap = (Map) propertyValue;
            Map.Entry<String, Object> entry = propMap.entrySet().iterator().next();
            entity = entry.getValue();
            String key = entry.getKey();
            referenceType = getReferenceTypeFromAttachedResouce(key);
            if (FunctionTranslationFactory.getInstance(entry.getKey()).isPresent()) {
                FunctionTranslator functionTranslator = new FunctionTranslator(
                    getFunctionTranslateTo(null, null, heatFileName, heatOrchestrationTemplate, context), null, entry.getValue(), null);
                Optional<FunctionTranslation> optionalFunctionTranslation = FunctionTranslationFactory.getInstance(entry.getKey());
                if (optionalFunctionTranslation.isPresent()) {
                    translatedId = optionalFunctionTranslation.get().translateFunction(functionTranslator);
                }
                if (translatedId instanceof String && !new FunctionTranslator().isResourceSupported((String) translatedId)) {
                    translatedId = null;
                }
            }
        } else {
            translatedId = propertyValue;
            entity = propertyValue;
        }
        return Optional.of(new AttachedResourceId(translatedId, entity, referenceType));
    }

    private static ReferenceType getReferenceTypeFromAttachedResouce(String key) {
        ReferenceType referenceType;
        switch (key) {
            case GET_RESOURCE:
                referenceType = ReferenceType.GET_RESOURCE;
                break;
            case GET_PARAM:
                referenceType = ReferenceType.GET_PARAM;
                break;
            case GET_ATTR:
                referenceType = ReferenceType.GET_ATTR;
                break;
            default:
                referenceType = ReferenceType.OTHER;
                break;
        }
        return referenceType;
    }

    /**
     * Gets contrail attached heat resource id.
     *
     * @param attachedResource the attached resource
     * @return the contrail attached heat resource id
     */
    public static Optional<String> getContrailAttachedHeatResourceId(AttachedResourceId attachedResource) {
        if (attachedResource == null) {
            return Optional.empty();
        }
        if (attachedResource.isGetResource()) {
            return Optional.of((String) attachedResource.getEntityId());
        }
        if (attachedResource.isGetAttr()) {
            return getResourceId(attachedResource.getEntityId());
        }
        return Optional.empty();
    }

    /**
     * Extract property optional.
     *
     * @param propertyValue the property value
     * @return the optional
     */
    private static Optional<AttachedPropertyVal> extractProperty(Object propertyValue) {
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
                case GET_RESOURCE:
                    referenceType = ReferenceType.GET_RESOURCE;
                    break;
                case GET_PARAM:
                    referenceType = ReferenceType.GET_PARAM;
                    break;
                case GET_ATTR:
                    referenceType = ReferenceType.GET_ATTR;
                    break;
                default:
                    break;
            }
        } else {
            attachedPropertyVal = propertyValue;
        }
        return Optional.of(new AttachedPropertyVal(attachedPropertyVal, referenceType));
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
            List booleanList = (List) listValue;
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
        String extension = FilenameUtils.getExtension(filename);
        return "yaml".equalsIgnoreCase(extension) || "yml".equalsIgnoreCase(extension);
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
            if (!(((Map) resourceDef).get(HeatConstants.RESOURCE_DEF_TYPE_PROPERTY_NAME) instanceof String)) {
                //currently only resource group which is poinitng to nested heat file is supported

                //dynamic type is currently not supported
                return false;
            }
            String internalResourceType = (String) ((Map) resourceDef).get(HeatConstants.RESOURCE_DEF_TYPE_PROPERTY_NAME);
            if (isYamlFile(internalResourceType)) {
                return true;
            }
        } else if (isYamlFile(resourceType)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the current HEAT resource if of type sub interface.
     *
     * @param resource the resource
     * @return true if the resource is of sub interface type and false otherwise
     */
    public static boolean isSubInterfaceResource(Resource resource, TranslationContext context) {
        //Check if resource group is a nested resource
        if (!isNestedResource(resource)) {
            return false;
        }
        Optional<String> nestedHeatFileName = HeatToToscaUtil.getNestedHeatFileName(resource);
        return nestedHeatFileName.filter(fileName -> isNestedVlanResource(fileName, context)).isPresent();
    }

    private static boolean isNestedVlanResource(String nestedHeatFileName, TranslationContext translationContext) {
        HeatOrchestrationTemplate nestedHeatOrchestrationTemplate = new YamlUtil()
            .yamlToObject(translationContext.getFileContentAsStream(nestedHeatFileName), HeatOrchestrationTemplate.class);
        return Objects.nonNull(nestedHeatOrchestrationTemplate.getResources()) && nestedHeatOrchestrationTemplate.getResources().values().stream()
            .anyMatch(new ContrailV2VirtualMachineInterfaceHelper()::isVlanSubInterfaceResource);
    }

    public static Optional<String> getSubInterfaceParentPortNodeTemplateId(TranslateTo subInterfaceTo) {
        String subInterfaceResourceType = getSubInterfaceResourceType(subInterfaceTo.getResource());
        HeatOrchestrationTemplate nestedHeatOrchestrationTemplate = new YamlUtil()
            .yamlToObject(subInterfaceTo.getContext().getFileContentAsStream(subInterfaceResourceType), HeatOrchestrationTemplate.class);
        if (Objects.isNull(nestedHeatOrchestrationTemplate.getResources())) {
            return Optional.empty();
        }
        for (Map.Entry<String, Resource> resourceEntry : nestedHeatOrchestrationTemplate.getResources().entrySet()) {
            Resource resource = resourceEntry.getValue();
            if (isVmiRefsPropertyExists(resource)) {
                Object toscaPropertyValue = TranslatorHeatToToscaPropertyConverter
                    .getToscaPropertyValue(subInterfaceTo.getServiceTemplate(), resourceEntry.getKey(), HeatConstants.VMI_REFS_PROPERTY_NAME,
                        resource.getProperties().get(HeatConstants.VMI_REFS_PROPERTY_NAME), resource.getType(), subInterfaceResourceType,
                        nestedHeatOrchestrationTemplate, null, subInterfaceTo.getContext());
                return getParentNodeTemplateIdFromPropertyValue(toscaPropertyValue, subInterfaceTo);
            }
        }
        return Optional.empty();
    }

    private static boolean isVmiRefsPropertyExists(Resource resource) {
        return HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource().equals(resource.getType()) && MapUtils
            .isNotEmpty(resource.getProperties()) && resource.getProperties().containsKey(HeatConstants.VMI_REFS_PROPERTY_NAME);
    }

    public static boolean isValueSpecsPropertyExists(Resource resource) {
        return MapUtils.isNotEmpty(resource.getProperties()) && resource.getProperties().containsKey(HeatConstants.VALUE_SPECS_PROPERTY_NAME);
    }

    public static Optional<Object> getResourceProperty(Resource resource, String propertyName) {
        Map<String, Object> properties = resource.getProperties();
        if (MapUtils.isNotEmpty(properties) && properties.containsKey(propertyName)) {
            return Optional.ofNullable(resource.getProperties().get(propertyName));
        }
        return Optional.empty();
    }

    public static String getSubInterfaceResourceType(Resource resource) {
        if (!HeatToToscaUtil.isYamlFile(resource.getType())) {
            return ((Map) resource.getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME)).get(HeatConstants.RESOURCE_DEF_TYPE_PROPERTY_NAME)
                .toString();
        }
        return resource.getType();
    }

    private static Optional<String> getParentNodeTemplateIdFromPropertyValue(Object toscaPropertyValue, TranslateTo subInterfaceTo) {
        if (toscaPropertyValue instanceof List && ((List) toscaPropertyValue).get(0) instanceof Map) {
            Resource subInterfaceResource = subInterfaceTo.getResource();
            Map<String, String> toscaPropertyValueMap = (Map) ((List) toscaPropertyValue).get(0);
            String parentPortPropertyInput = toscaPropertyValueMap.get(ToscaFunctions.GET_INPUT.getFunctionName());
            Map<String, Object> resourceDefPropertiesMap;
            if (!isYamlFile(subInterfaceResource.getType())) {
                resourceDefPropertiesMap = (Map) ((Map) subInterfaceResource.getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME))
                    .get(HeatConstants.RESOURCE_DEF_PROPERTIES);
            } else {
                resourceDefPropertiesMap = subInterfaceResource.getProperties();
            }
            Object parentPortObj = resourceDefPropertiesMap.get(parentPortPropertyInput);
            if (parentPortObj instanceof Map) {
                Map<String, String> parentPortPropertyValue = (Map) parentPortObj;
                if (parentPortPropertyValue.keySet().contains(ResourceReferenceFunctions.GET_RESOURCE.getFunction())) {
                    return ResourceTranslationBase
                        .getResourceTranslatedId(subInterfaceTo.getHeatFileName(), subInterfaceTo.getHeatOrchestrationTemplate(),
                            parentPortPropertyValue.get(ResourceReferenceFunctions.GET_RESOURCE.getFunction()), subInterfaceTo.getContext());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if the nested resource represents a VFC or a complex VFC (Heat file should contain at least one or more compute nodes).
     *
     * @param resource the resource
     * @param context  the context
     * @return true if the resource represents a VFC and false otherwise.
     */
    public static boolean isNestedVfcResource(Resource resource, TranslationContext context) {
        Optional<String> nestedHeatFileName = getNestedHeatFileName(resource);
        if (nestedHeatFileName.isEmpty()) {
            return false;
        }
        HeatOrchestrationTemplate nestedHeatOrchestrationTemplate = new YamlUtil()
            .yamlToObject(context.getFileContentAsStream(nestedHeatFileName.get()), HeatOrchestrationTemplate.class);
        Map<String, Resource> resources = nestedHeatOrchestrationTemplate.getResources();
        return Objects.nonNull(resources) && resources.values().stream().anyMatch(ConsolidationDataUtil::isComputeResource);
    }

    /**
     * Get nested heat file name in case of nested resource.
     *
     * @param resource the resource
     * @return the nested heat file name
     */
    private static Optional<String> getNestedHeatFileName(Resource resource) {
        if (!isNestedResource(resource)) {
            return Optional.empty();
        }
        String resourceType = resource.getType();
        if (resourceType.equals(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource())) {
            Object resourceDef = resource.getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME);
            String internalResourceType = (String) ((Map) resourceDef).get(HeatConstants.RESOURCE_DEF_TYPE_PROPERTY_NAME);
            return Optional.of(internalResourceType);
        }
        return Optional.of(resourceType);
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
            String internalResourceType = (String) ((Map) resourceDef).get(HeatConstants.RESOURCE_DEF_TYPE_PROPERTY_NAME);
            return Optional.of(internalResourceType);
        } else {
            return Optional.of(resourceType);
        }
    }

    public static boolean isYamlFile(String fileName) {
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
    public static Resource getResource(HeatOrchestrationTemplate heatOrchestrationTemplate, String resourceId, String heatFileName) {
        Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
        if (resource == null) {
            throw new CoreException(new ResourceNotFoundInHeatFileErrorBuilder(resourceId, heatFileName).build());
        }
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
    public static String getResourceType(String resourceId, HeatOrchestrationTemplate heatOrchestrationTemplate, String heatFileName) {
        return HeatToToscaUtil.getResource(heatOrchestrationTemplate, resourceId, heatFileName).getType();
    }

    /**
     * Is heat file nested boolean.
     *
     * @param translateTo  the translate to
     * @param heatFileName the heat file name
     * @return the boolean
     */
    public static boolean isHeatFileNested(TranslateTo translateTo, String heatFileName) {
        return isHeatFileNested(translateTo.getContext(), heatFileName);
    }

    public static boolean isHeatFileNested(TranslationContext context, String heatFileName) {
        return context.getNestedHeatsFiles().contains(heatFileName);
    }

    /**
     * Extract contrail get resource attached heat resource id optional.
     *
     * @param propertyValue the property value
     * @return the optional
     */
    public static Optional<String> extractContrailGetResourceAttachedHeatResourceId(Object propertyValue) {
        if (propertyValue instanceof Map) {
            if (((Map) propertyValue).containsKey(GET_ATTR)) {
                return getResourceId(((Map) propertyValue).get(GET_ATTR));
            } else if (((Map) propertyValue).containsKey(GET_RESOURCE)) {
                return getHeatResourceIdFromResource((Map) propertyValue);
            } else {
                Collection valCollection = ((Map) propertyValue).values();
                return evaluateHeatResourceId(valCollection);
            }
        } else if (propertyValue instanceof List) {
            return evaluateHeatResourceId((List) propertyValue);
        }
        return Optional.empty();
    }

    private static Optional<String> getResourceId(Object data) {
        if (data instanceof List && CollectionUtils.size(data) > 1 && FQ_NAME.equals(((List) data).get(1)) && ((List) data)
            .get(0) instanceof String) {
            return Optional.of((String) ((List) data).get(0));
        } else {
            LOGGER.warn("invalid format of 'get_attr' function - " + data.toString());
            return Optional.empty();
        }
    }

    private static Optional<String> getHeatResourceIdFromResource(Map propertyValue) {
        Object value = propertyValue.get(GET_RESOURCE);
        if (value instanceof String) {
            return Optional.of((String) value);
        } else {
            LOGGER.warn("invalid format of 'get_resource' function - " + propertyValue.toString());
            return Optional.empty();
        }
    }

    private static Optional<String> evaluateHeatResourceId(Collection propertyValue) {
        for (Object prop : propertyValue) {
            Optional<String> ret = extractContrailGetResourceAttachedHeatResourceId(prop);
            if (ret.isPresent()) {
                return ret;
            }
        }
        return Optional.empty();
    }

    /**
     * Gets tosca service model.
     *
     * @param context translation context
     * @return the tosca service model
     */
    public static ToscaServiceModel getToscaServiceModel(TranslationContext context) {
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
    private static ToscaServiceModel getToscaServiceModel(TranslationContext context, Map<String, String> entryDefinitionMetadata) {
        Map<String, ServiceTemplate> serviceTemplates = new HashMap<>(context.getGlobalServiceTemplates());
        Collection<ServiceTemplate> tmpServiceTemplates = context.getTranslatedServiceTemplates().values();
        for (ServiceTemplate serviceTemplate : tmpServiceTemplates) {
            ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates, serviceTemplate);
        }
        return new ToscaServiceModel(null, serviceTemplates, ToscaUtil.getServiceTemplateFileName(entryDefinitionMetadata));
    }

    /**
     * Gets service template from context.
     *
     * @param serviceTemplateFileName the service template file name
     * @param context                 the context
     * @return the service template from context
     */
    public static Optional<ServiceTemplate> getServiceTemplateFromContext(String serviceTemplateFileName, TranslationContext context) {
        for (ServiceTemplate serviceTemplate : context.getTranslatedServiceTemplates().values()) {
            if (ToscaUtil.getServiceTemplateFileName(serviceTemplate).equals(serviceTemplateFileName)) {
                return Optional.of(serviceTemplate);
            }
        }
        return Optional.empty();
    }

    /**
     * Adding link requerment from port node template to network node template.
     *
     * @param portNodeTemplate    port node template
     * @param networkTranslatedId network node template id
     */
    public static RequirementAssignment addLinkReqFromPortToNetwork(NodeTemplate portNodeTemplate, String networkTranslatedId) {
        RequirementAssignment requirement = new RequirementAssignment();
        requirement.setCapability(ToscaCapabilityType.NATIVE_NETWORK_LINKABLE);
        requirement.setRelationship(ToscaRelationshipType.NATIVE_NETWORK_LINK_TO);
        requirement.setNode(networkTranslatedId);
        DataModelUtil.addRequirementAssignment(portNodeTemplate, ToscaConstants.LINK_REQUIREMENT_ID, requirement);
        return requirement;
    }

    /**
     * Adding binding requerment from sub interface node template to interface (port) node template.
     *
     * @param subInterfaceNodeTemplate sub interface template
     * @param interfaceTranslatedId    interface node template id
     */
    public static void addBindingReqFromSubInterfaceToInterface(NodeTemplate subInterfaceNodeTemplate, String interfaceTranslatedId) {
        RequirementAssignment requirement = new RequirementAssignment();
        requirement.setCapability(ToscaCapabilityType.NATIVE_NETWORK_BINDABLE);
        requirement.setRelationship(ToscaRelationshipType.NATIVE_NETWORK_BINDS_TO);
        requirement.setNode(interfaceTranslatedId);
        DataModelUtil.addRequirementAssignment(subInterfaceNodeTemplate, ToscaConstants.BINDING_REQUIREMENT_ID, requirement);
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

    public static String getToscaPropertyName(TranslationContext context, String heatResourceType, String heatPropertyName) {
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
        return translateTo.getContext().getElementMapping(translateTo.getResource().getType(), Constants.PROP, heatPropertyName);
    }

    /**
     * Gets tosca attribute name.
     *
     * @param context          the context
     * @param heatResourceType the heat resource type
     * @param heatAttrName     the heat attr name
     * @return the tosca attribute name
     */
    public static String getToscaAttributeName(TranslationContext context, String heatResourceType, String heatAttrName) {
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
        return translateTo.getContext().getElementMapping(translateTo.getResource().getType(), Constants.ATTR, heatAttrName);
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
        nestedSubstitutionServiceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
        nestedSubstitutionServiceTemplate.setTopology_template(new TopologyTemplate());
        List<Map<String, Import>> globalTypesImportList = GlobalTypesGenerator.getGlobalTypesImportList();
        globalTypesImportList.addAll(HeatToToscaUtil.createImportList(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME));
        nestedSubstitutionServiceTemplate.setImports(globalTypesImportList);
        return nestedSubstitutionServiceTemplate;
    }

    /**
     * Create init global substitution service template service template.
     *
     * @return the service template
     */
    private static ServiceTemplate createInitGlobalSubstitutionServiceTemplate() {
        ServiceTemplate globalSubstitutionServiceTemplate = new ServiceTemplate();
        Map<String, String> templateMetadata = new HashMap<>();
        templateMetadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
        globalSubstitutionServiceTemplate.setMetadata(templateMetadata);
        globalSubstitutionServiceTemplate.setImports(GlobalTypesGenerator.getGlobalTypesImportList());
        globalSubstitutionServiceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
        return globalSubstitutionServiceTemplate;
    }

    /**
     * . Create and add substitution mapping to the nested substitution service template, and update the subtitution node type accordingly with the
     * exposed requerments and capabilities
     *
     * @param context                           the translation context
     * @param substitutionNodeTypeKey           the substitution node type key
     * @param nestedSubstitutionServiceTemplate the nested substitution service template
     * @param substitutionNodeType              the substitution node type
     */
    public static void handleSubstitutionMapping(TranslationContext context, String substitutionNodeTypeKey,
                                                 ServiceTemplate nestedSubstitutionServiceTemplate, NodeType substitutionNodeType) {
        Map<String, Map<String, List<String>>> substitutionMapping = getSubstitutionNodeTypeExposedConnectionPoints(substitutionNodeType,
            nestedSubstitutionServiceTemplate, context);
        //add substitution mapping after capability and requirement expose calculation
        nestedSubstitutionServiceTemplate.getTopology_template().setSubstitution_mappings(
            DataModelUtil.createSubstitutionTemplateSubMapping(substitutionNodeTypeKey, substitutionNodeType, substitutionMapping));
    }

    /**
     * Gets node type with flat hierarchy.
     *
     * @param nodeTypeId      the node type id
     * @param serviceTemplate the service template
     * @param context         the context
     * @return the node type with flat hierarchy
     */
    public static NodeType getNodeTypeWithFlatHierarchy(String nodeTypeId, ServiceTemplate serviceTemplate, TranslationContext context) {
        ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
        ToscaServiceModel toscaServiceModel = HeatToToscaUtil.getToscaServiceModel(context, serviceTemplate.getMetadata());
        return (NodeType) toscaAnalyzerService.getFlatEntity(ToscaElementTypes.NODE_TYPE, nodeTypeId, serviceTemplate, toscaServiceModel)
            .getFlatEntity();
    }

    /**
     * Create abstract substitution node template.
     *
     * @param translateTo             the translate to
     * @param templateName            the template name
     * @param substitutionNodeTypeKey the substitution node type key
     * @return the abstract substitute node template
     */
    public static NodeTemplate createAbstractSubstitutionNodeTemplate(TranslateTo translateTo, String templateName, String substitutionNodeTypeKey) {
        NodeTemplate substitutionNodeTemplate = new NodeTemplate();
        List<String> directiveList = new ArrayList<>();
        directiveList.add(ToscaConstants.NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE);
        substitutionNodeTemplate.setDirectives(directiveList);
        substitutionNodeTemplate.setType(substitutionNodeTypeKey);
        substitutionNodeTemplate.setProperties(managerSubstitutionNodeTemplateProperties(translateTo, substitutionNodeTemplate, templateName));
        return substitutionNodeTemplate;
    }

    /**
     * Checks if the source and target resource is a valid candidate for adding tosca dependency relationship.
     *
     * @param sourceResource   the source resource
     * @param targetResource   the target resource
     * @param dependencyEntity the dependency entity
     * @return true if the candidate resources are a valid combination for the dependency relationship and false otherwise
     */
    public static boolean isValidDependsOnCandidate(Resource sourceResource, Resource targetResource, ConsolidationEntityType dependencyEntity,
                                                    TranslationContext context) {
        dependencyEntity.setEntityType(sourceResource, targetResource, context);
        ConsolidationEntityType sourceEntityType = dependencyEntity.getSourceEntityType();
        ConsolidationEntityType targetEntityType = dependencyEntity.getTargetEntityType();
        return ConsolidationTypesConnectivity.isDependsOnRelationshipValid(sourceEntityType, targetEntityType);
    }

    private static Map<String, Object> managerSubstitutionNodeTemplateProperties(TranslateTo translateTo, Template template, String templateName) {
        Map<String, Object> substitutionProperties = new HashMap<>();
        Map<String, Object> heatProperties = translateTo.getResource().getProperties();
        if (Objects.nonNull(heatProperties)) {
            for (Map.Entry<String, Object> entry : heatProperties.entrySet()) {
                Object property = TranslatorHeatToToscaPropertyConverter
                    .getToscaPropertyValue(translateTo.getServiceTemplate(), translateTo.getTranslatedId(), entry.getKey(), entry.getValue(), null,
                        translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(), template, translateTo.getContext());
                substitutionProperties.put(entry.getKey(), property);
            }
        }
        return addAbstractSubstitutionProperty(templateName, substitutionProperties);
    }

    private static Map<String, Object> addAbstractSubstitutionProperty(String templateName, Map<String, Object> substitutionProperties) {
        Map<String, Object> innerProps = new HashMap<>();
        innerProps.put(ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME, ToscaUtil.getServiceTemplateFileName(templateName));
        substitutionProperties.put(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME, innerProps);
        return substitutionProperties;
    }

    private static Map<String, Map<String, List<String>>> getSubstitutionNodeTypeExposedConnectionPoints(NodeType substitutionNodeType,
                                                                                                         ServiceTemplate substitutionServiceTemplate,
                                                                                                         TranslationContext context) {
        Map<String, NodeTemplate> nodeTemplates = substitutionServiceTemplate.getTopology_template().getNode_templates();
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
        Map<String, Map<String, RequirementAssignment>> fullFilledRequirementsDefinition = new HashMap<>();
        Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition = new HashMap<>();
        Map<String, CapabilityDefinition> exposedCapabilitiesDefinition;
        ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
        for (Map.Entry<String, NodeTemplate> entry : nodeTemplates.entrySet()) {
            nodeTemplateId = entry.getKey();
            nodeTemplate = entry.getValue();
            nodeType = nodeTemplate.getType();
            // get requirements
            nodeTypeRequirementsDefinition = getNodeTypeReqs(nodeType, nodeTemplateId, substitutionServiceTemplate, requirementSubstitutionMapping,
                context);
            nodeTemplateRequirementsAssignment = DataModelUtil.getNodeTemplateRequirements(nodeTemplate);
            fullFilledRequirementsDefinition.put(nodeTemplateId, nodeTemplateRequirementsAssignment);
            //set substitution node type requirements
            exposedRequirementsDefinition = toscaAnalyzerService
                .calculateExposedRequirements(nodeTypeRequirementsDefinition, nodeTemplateRequirementsAssignment);
            DataModelUtil.addSubstitutionNodeTypeRequirements(substitutionNodeType, exposedRequirementsDefinition, nodeTemplateId);
            //get capabilities
            addNodeTypeCapabilitiesToSubMapping(nodeTypeCapabilitiesDefinition, capabilitySubstitutionMapping, nodeType, nodeTemplateId,
                substitutionServiceTemplate, context);
        }
        exposedCapabilitiesDefinition = toscaAnalyzerService
            .calculateExposedCapabilities(nodeTypeCapabilitiesDefinition, fullFilledRequirementsDefinition);
        DataModelUtil.setNodeTypeCapabilitiesDef(substitutionNodeType, exposedCapabilitiesDefinition);
        return substitutionMapping;
    }

    private static void addNodeTypeCapabilitiesToSubMapping(Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition,
                                                            Map<String, List<String>> capabilitySubstitutionMapping, String type, String templateName,
                                                            ServiceTemplate serviceTemplate, TranslationContext context) {
        NodeType flatNodeType = getNodeTypeWithFlatHierarchy(type, serviceTemplate, context);
        if (flatNodeType.getCapabilities() != null) {
            flatNodeType.getCapabilities().entrySet().stream().forEach(
                capabilityNodeEntry -> addCapabilityToSubMapping(templateName, capabilityNodeEntry, nodeTypeCapabilitiesDefinition,
                    capabilitySubstitutionMapping));
        }
    }

    private static void addCapabilityToSubMapping(String templateName, Map.Entry<String, CapabilityDefinition> capabilityNodeEntry,
                                                  Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition,
                                                  Map<String, List<String>> capabilitySubstitutionMapping) {
        String capabilityKey;
        List<String> capabilityMapping;
        capabilityKey = capabilityNodeEntry.getKey() + UNDERSCORE + templateName;
        nodeTypeCapabilitiesDefinition.put(capabilityKey, capabilityNodeEntry.getValue().clone());
        capabilityMapping = new ArrayList<>();
        capabilityMapping.add(templateName);
        capabilityMapping.add(capabilityNodeEntry.getKey());
        capabilitySubstitutionMapping.put(capabilityKey, capabilityMapping);
    }

    private static List<Map<String, RequirementDefinition>> getNodeTypeReqs(String type, String templateName, ServiceTemplate serviceTemplate,
                                                                            Map<String, List<String>> requirementSubstitutionMapping,
                                                                            TranslationContext context) {
        List<Map<String, RequirementDefinition>> requirementList = new ArrayList<>();
        NodeType flatNodeType = getNodeTypeWithFlatHierarchy(type, serviceTemplate, context);
        List<String> requirementMapping;
        if (flatNodeType.getRequirements() == null) {
            return requirementList;
        }
        for (Map<String, RequirementDefinition> requirementMap : flatNodeType.getRequirements()) {
            for (Map.Entry<String, RequirementDefinition> requirementNodeEntry : requirementMap.entrySet()) {
                ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
                RequirementDefinition requirementNodeEntryValue = toscaExtensionYamlUtil
                    .yamlToObject(toscaExtensionYamlUtil.objectToYaml(requirementNodeEntry.getValue()), RequirementDefinition.class);
                if (Objects.isNull(requirementNodeEntryValue.getOccurrences())) {
                    requirementNodeEntryValue.setOccurrences(new Object[]{1, 1});
                }
                Map<String, RequirementDefinition> requirementDef = new HashMap<>();
                requirementDef.put(requirementNodeEntry.getKey(), requirementNodeEntryValue);
                DataModelUtil.addRequirementToList(requirementList, requirementDef);
                requirementMapping = new ArrayList<>();
                requirementMapping.add(templateName);
                requirementMapping.add(requirementNodeEntry.getKey());
                requirementSubstitutionMapping.put(requirementNodeEntry.getKey() + UNDERSCORE + templateName, requirementMapping);
                if (Objects.isNull(requirementNodeEntryValue.getNode())) {
                    requirementNodeEntryValue.setOccurrences(new Object[]{1, 1});
                }
            }
        }
        return requirementList;
    }

    /**
     * Fetch global substitution service template service template.
     *
     * @param serviceTemplate the service template
     * @param context         the context
     * @return the service template
     */
    public static ServiceTemplate fetchGlobalSubstitutionServiceTemplate(ServiceTemplate serviceTemplate, TranslationContext context) {
        ServiceTemplate globalSubstitutionServiceTemplate = context.getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
        if (globalSubstitutionServiceTemplate == null) {
            globalSubstitutionServiceTemplate = HeatToToscaUtil.createInitGlobalSubstitutionServiceTemplate();
            context.getTranslatedServiceTemplates().put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME, globalSubstitutionServiceTemplate);
        }
        boolean isImportAddedToServiceTemplate = DataModelUtil
            .isImportAddedToServiceTemplate(serviceTemplate.getImports(), Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
        if (!isImportAddedToServiceTemplate) {
            serviceTemplate.getImports().addAll(HeatToToscaUtil.createImportList(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME));
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
    private static Import createServiceTemplateImport(String metadataTemplateName) {
        Import serviceTemplateImport = new Import();
        serviceTemplateImport.setFile(ToscaUtil.getServiceTemplateFileName(metadataTemplateName));
        return serviceTemplateImport;
    }

    public static ToscaServiceModel createToscaServiceModel(ServiceTemplate entryDefinitionServiceTemplate, TranslationContext translationContext) {
        return new ToscaServiceModel(getCsarArtifactFiles(translationContext), getServiceTemplates(translationContext),
            ToscaUtil.getServiceTemplateFileName(entryDefinitionServiceTemplate));
    }

    private static FileContentHandler getCsarArtifactFiles(TranslationContext translationContext) {
        FileContentHandler artifactFiles = new FileContentHandler();
        artifactFiles.addAll(translationContext.getFiles());
        artifactFiles.addAll(translationContext.getExternalArtifacts());
        HeatTreeManager heatTreeManager = HeatTreeManagerUtil.initHeatTreeManager(translationContext.getFiles());
        heatTreeManager.createTree();
        ValidationStructureList validationStructureList = new ValidationStructureList(heatTreeManager.getTree());
        byte[] validationStructureFile = FileUtils.convertToBytes(validationStructureList, FileUtils.FileExtension.JSON);
        artifactFiles.addFile("HEAT.meta", validationStructureFile);
        return artifactFiles;
    }

    private static Map<String, ServiceTemplate> getServiceTemplates(TranslationContext translationContext) {
        List<ServiceTemplate> serviceTemplates = new ArrayList<>();
        serviceTemplates.addAll(GlobalTypesGenerator.getGlobalTypesServiceTemplate(OnboardingTypesEnum.ZIP).values());
        serviceTemplates.addAll(translationContext.getTranslatedServiceTemplates().values());
        Map<String, ServiceTemplate> serviceTemplatesMap = new HashMap<>();
        for (ServiceTemplate template : serviceTemplates) {
            serviceTemplatesMap.put(ToscaUtil.getServiceTemplateFileName(template), template);
        }
        return serviceTemplatesMap;
    }

    public static String getNestedResourceTypePrefix(TranslateTo translateTo) {
        if (isSubInterfaceResource(translateTo.getResource(), translateTo.getContext()) && isSubInterfaceBoundToPort(translateTo)) {
            return ToscaNodeType.VLAN_SUB_INTERFACE_RESOURCE_TYPE_PREFIX;
        }
        return ToscaNodeType.NESTED_HEAT_RESOURCE_TYPE_PREFIX;
    }

    private static boolean isSubInterfaceBoundToPort(TranslateTo translateTo) {
        return HeatToToscaUtil.getSubInterfaceParentPortNodeTemplateId(translateTo).isPresent();
    }

    // only single sub interface present in nested file else it will return null
    public static Optional<String> getNetworkRoleFromSubInterfaceId(Resource resource, TranslationContext translationContext) {
        Optional<String> networkRole = Optional.empty();
        Optional<String> nestedHeatFileName = HeatToToscaUtil.getNestedHeatFileName(resource);
        if (!nestedHeatFileName.isPresent()) {
            return networkRole;
        }
        HeatOrchestrationTemplate nestedHeatOrchestrationTemplate = new YamlUtil()
            .yamlToObject(translationContext.getFileContentAsStream(nestedHeatFileName.get()), HeatOrchestrationTemplate.class);
        if (MapUtils.isNotEmpty(nestedHeatOrchestrationTemplate.getResources())) {
            ContrailV2VirtualMachineInterfaceHelper contrailV2VirtualMachineInterfaceHelper = new ContrailV2VirtualMachineInterfaceHelper();
            Optional<Map.Entry<String, Resource>> vlanSubInterfaceResource = nestedHeatOrchestrationTemplate.getResources().entrySet().stream()
                .filter(resourceEntry -> contrailV2VirtualMachineInterfaceHelper.isVlanSubInterfaceResource(resourceEntry.getValue())).findFirst();
            if (vlanSubInterfaceResource.isPresent()) {
                Map.Entry<String, Resource> vlanSubInterfaceResourceEntry = vlanSubInterfaceResource.get();
                networkRole = extractNetworkRoleFromSubInterfaceId(vlanSubInterfaceResourceEntry.getKey(),
                    vlanSubInterfaceResourceEntry.getValue().getType());
            }
        }
        return networkRole;
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
        substitutionNodeType.setProperties(manageSubstitutionNodeTypeProperties(substitutionServiceTemplate));
        substitutionNodeType.setAttributes(manageSubstitutionNodeTypeAttributes(substitutionServiceTemplate));
        return substitutionNodeType;
    }

    private Map<String, PropertyDefinition> manageSubstitutionNodeTypeProperties(ServiceTemplate substitutionServiceTemplate) {
        Map<String, PropertyDefinition> substitutionNodeTypeProperties = new HashMap<>();
        Map<String, ParameterDefinition> properties = substitutionServiceTemplate.getTopology_template().getInputs();
        if (properties == null) {
            return null;
        }
        PropertyDefinition propertyDefinition;
        String toscaPropertyName;
        for (Map.Entry<String, ParameterDefinition> entry : properties.entrySet()) {
            toscaPropertyName = entry.getKey();
            propertyDefinition = new PropertyDefinition();
            ParameterDefinition parameterDefinition = substitutionServiceTemplate.getTopology_template().getInputs().get(toscaPropertyName);
            propertyDefinition.setType(parameterDefinition.getType());
            propertyDefinition.setDescription(parameterDefinition.getDescription());
            propertyDefinition.setRequired(parameterDefinition.getRequired());
            propertyDefinition.set_default(parameterDefinition.get_default());
            propertyDefinition.setConstraints(parameterDefinition.getConstraints());
            propertyDefinition.setEntry_schema(parameterDefinition.getEntry_schema());
            propertyDefinition.setStatus(parameterDefinition.getStatus());
            substitutionNodeTypeProperties.put(toscaPropertyName, propertyDefinition);
        }
        return substitutionNodeTypeProperties;
    }
    //Method evaluate the  network role from sub interface node template id, designed considering

    private Map<String, AttributeDefinition> manageSubstitutionNodeTypeAttributes(ServiceTemplate substitutionServiceTemplate) {
        Map<String, AttributeDefinition> substitutionNodeTypeAttributes = new HashMap<>();
        Map<String, ParameterDefinition> attributes = substitutionServiceTemplate.getTopology_template().getOutputs();
        if (attributes == null) {
            return null;
        }
        AttributeDefinition attributeDefinition;
        String toscaAttributeName;
        for (Map.Entry<String, ParameterDefinition> entry : attributes.entrySet()) {
            attributeDefinition = new AttributeDefinition();
            toscaAttributeName = entry.getKey();
            ParameterDefinition parameterDefinition = substitutionServiceTemplate.getTopology_template().getOutputs().get(toscaAttributeName);
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
        return substitutionNodeTypeAttributes;
    }
}
