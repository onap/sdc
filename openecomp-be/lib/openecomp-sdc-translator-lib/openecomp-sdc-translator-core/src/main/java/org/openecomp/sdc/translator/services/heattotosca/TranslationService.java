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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.GroupDefinition;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.PropertyType;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.onap.sdc.tosca.datatypes.model.heatextend.ParameterDefinitionExt;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.Environment;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.AsdPackageHelper;
import org.openecomp.sdc.tosca.csar.ManifestUtils;
import org.openecomp.sdc.tosca.datatypes.ToscaGroupType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.FileDataCollection;
import org.openecomp.sdc.translator.services.heattotosca.errors.ResourceNotFoundInHeatFileErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaParameterConverter;

public class TranslationService {

    protected static Logger logger = LoggerFactory.getLogger(TranslationService.class);

    /**
     * Gets types to process by translator.
     *
     * @return the types to process by translator
     */
    public static Set<FileData.Type> getTypesToProcessByTranslator() {
        Set<FileData.Type> types = new HashSet<>();
        types.add(FileData.Type.HEAT);
        types.add(FileData.Type.HEAT_VOL);
        return types;
    }

    public static Set<FileData.Type> getTypesToProcessByHelmTranslator() {
        Set<FileData.Type> types = new HashSet<>();
        types.add(FileData.Type.HELM);
        return types;
    }

    /**
     * Translate heat files translator output.
     *
     * @param translationContext the translation context
     * @return the translator output
     */
    public TranslatorOutput translateHeatFiles(TranslationContext translationContext) {
        ServiceTemplate mainServiceTemplate = createMainServiceTemplate(translationContext);
        List<FileData> fileDataList = translationContext.getManifest().getContent().getData();
        FileDataCollection fileDataCollection = HeatToToscaUtil
            .getFileCollectionsByFilter(fileDataList, TranslationService.getTypesToProcessByTranslator(), translationContext);
        FileDataCollection fileDataCollectionHelm = HeatToToscaUtil
            .getFileCollectionsByHelmFilter(fileDataList, TranslationService.getTypesToProcessByHelmTranslator());
        if (fileDataCollection.getBaseFile() != null) {
            for (FileData fileData : fileDataCollection.getBaseFile()) {
                translateHeatFile(mainServiceTemplate, fileData, translationContext);
            }
        }
        if (fileDataCollection.getAddOnFiles() != null) {
            for (FileData fileData : fileDataCollection.getAddOnFiles()) {
                translateHeatFile(mainServiceTemplate, fileData, translationContext);
            }
        }
        if (fileDataCollectionHelm.getHelmFile() != null) {
            for (FileData fileData : fileDataCollectionHelm.getHelmFile()) {
                translateHelmFile(mainServiceTemplate, fileData, translationContext);
            }
        }
        ToscaServiceModel toscaServiceModel = HeatToToscaUtil.createToscaServiceModel(mainServiceTemplate, translationContext);
        TranslatorOutput translatorOutput = new TranslatorOutput();
        //Keeping a copy of tosca service model after first stage of translation for extraction of

        // composition data
        translatorOutput.setNonUnifiedToscaServiceModel(ToscaServiceModel.getClonedServiceModel(toscaServiceModel));
        translatorOutput.setToscaServiceModel(toscaServiceModel);
        return translatorOutput;
    }

    private ServiceTemplate createMainServiceTemplate(TranslationContext translationContext) {
        ServiceTemplate mainServiceTemplate = new ServiceTemplate();
        translationContext.getTranslatedServiceTemplates().put(Constants.MAIN_TEMPLATE_NAME, mainServiceTemplate);
        Map<String, String> templateMetadata = new HashMap<>();
        templateMetadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, Constants.MAIN_TEMPLATE_NAME);
        mainServiceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
        mainServiceTemplate.setMetadata(templateMetadata);
        mainServiceTemplate.setTopology_template(new TopologyTemplate());
        mainServiceTemplate.setImports(GlobalTypesGenerator.getGlobalTypesImportList());
        return mainServiceTemplate;
    }

    /**
     * Translate heat file.
     *
     * @param serviceTemplate the service template
     * @param heatFileData    the heat file data
     * @param context         the context
     */
    public void translateHeatFile(ServiceTemplate serviceTemplate, FileData heatFileData, TranslationContext context) {
        String heatFileName = heatFileData.getFile();
        HeatOrchestrationTemplate heatOrchestrationTemplate = new YamlUtil()
            .yamlToObject(context.getFileContentAsStream(heatFileName), HeatOrchestrationTemplate.class);
        translateInputParameters(serviceTemplate, heatOrchestrationTemplate, heatFileData, context, heatFileName);
        translateResources(heatFileName, serviceTemplate, heatOrchestrationTemplate, context);
        translateOutputParameters(serviceTemplate, heatOrchestrationTemplate, heatFileData, heatFileName, context);
        createHeatStackGroup(serviceTemplate, heatFileData, heatOrchestrationTemplate, context);
        handleHeatPseudoParam(heatFileName, serviceTemplate, context);
        if (Objects.nonNull(heatFileData.getData())) {
            heatFileData.getData().stream().filter(data -> FileData.Type.canBeAssociated(data.getType()))
                .forEach(data -> translateHeatFile(serviceTemplate, data, context));
        }
    }

    public void translateHelmFile(ServiceTemplate serviceTemplate, FileData heatFileData, TranslationContext context) {
        String heatFileName = heatFileData.getFile();
        Map<String, ParameterDefinition> inputs = serviceTemplate.getTopology_template().getInputs();
        if (!Objects.isNull(inputs)) {
            inputs.entrySet().forEach(stringParameterDefinitionEntry -> {
                List inputParamVFModuleList = getVFModulesList(inputs.get(stringParameterDefinitionEntry.getKey()));
                if (!inputParamVFModuleList.contains(FileUtils.getFileWithoutExtention(heatFileName))) {
                    inputParamVFModuleList.add(FileUtils.getFileWithoutExtention(heatFileName));
                }
            });
        }
        inputs.putAll(inputs);
    }

    private void handleHeatPseudoParam(String heatFileName, ServiceTemplate serviceTemplate, TranslationContext context) {
        Map<String, String> translatedHeatPseudoParam = context.getUsedHeatPseudoParams().get(heatFileName);
        if (Objects.nonNull(translatedHeatPseudoParam)) {
            for (String heatPseudoParam : translatedHeatPseudoParam.keySet()) {
                if (!serviceTemplate.getTopology_template().getInputs().containsKey(heatPseudoParam)) {
                    ParameterDefinition parameterDefinition = new ParameterDefinition();
                    parameterDefinition.setType(PropertyType.STRING.getDisplayName());
                    parameterDefinition.setRequired(false);
                    String parameterDefinitionId = translatedHeatPseudoParam.get(heatPseudoParam);
                    DataModelUtil.addInputParameterToTopologyTemplate(serviceTemplate, parameterDefinitionId, parameterDefinition);
                }
            }
        }
    }

    private void createHeatStackGroup(ServiceTemplate serviceTemplate, FileData heatFileData, HeatOrchestrationTemplate heatOrchestrationTemplate,
                                      TranslationContext context) {
        ToscaFileOutputService toscaFileOutputService = new ToscaFileOutputServiceCsarImpl(new AsdPackageHelper(new ManifestUtils()));
        final String fileName = heatFileData.getFile();
        final String heatStackGroupId = FileUtils.getFileWithoutExtention(fileName) + "_group";
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setType(ToscaGroupType.HEAT_STACK);
        groupDefinition.setProperties(new HashMap<>());
        groupDefinition.getProperties().put("heat_file", "../" + toscaFileOutputService.getArtifactsFolderName() + "/" + fileName);
        String hotDescription = heatOrchestrationTemplate.getDescription();
        if (hotDescription != null && !hotDescription.isEmpty()) {
            groupDefinition.getProperties().put(Constants.DESCRIPTION_PROPERTY_NAME, hotDescription);
        }
        groupDefinition.setMembers(new ArrayList<>());
        Set<String> heatStackGroupMembersIds = getHeatStackGroupMembers(fileName, serviceTemplate, context);
        if (CollectionUtils.isEmpty(heatStackGroupMembersIds)) {
            return; //not creating a group when no resources are present in the heat input
        }
        groupDefinition.getMembers().addAll(heatStackGroupMembersIds);
        DataModelUtil.addGroupDefinitionToTopologyTemplate(serviceTemplate, heatStackGroupId, groupDefinition);
    }

    private Set<String> getHeatStackGroupMembers(String heatFileName, ServiceTemplate serviceTemplate, TranslationContext context) {
        Map<String, Set<String>> heatStackGroupMembers = context.getHeatStackGroupMembers();
        Set<String> groupMembers = MapUtils.isEmpty(heatStackGroupMembers) ? new HashSet<>() : heatStackGroupMembers.get(heatFileName);
        if (CollectionUtils.isEmpty(groupMembers)) {
            return new HashSet<>();
        }
        Set<String> updatedMembersIds = new HashSet<>();
        groupMembers.forEach(member -> {
            if (Objects.nonNull(DataModelUtil.getNodeTemplate(serviceTemplate, member))) {
                updatedMembersIds.add(member);
            } else {
                updateSubstitutableGroupMemberId(heatFileName, serviceTemplate, updatedMembersIds);
            }
        });
        return updatedMembersIds;
    }

    private void updateSubstitutableGroupMemberId(String heatFileName, ServiceTemplate serviceTemplate, Set<String> updatedMembersIds) {
        Optional<String> substitutableGroupMemberId = ToscaUtil.getSubstitutableGroupMemberId(heatFileName, serviceTemplate);
        substitutableGroupMemberId.ifPresent(updatedMembersIds::add);
    }

    private void translateInputParameters(ServiceTemplate serviceTemplate, HeatOrchestrationTemplate heatOrchestrationTemplate, FileData heatFileData,
                                          TranslationContext context, String heatFileName) {
        if (heatOrchestrationTemplate.getParameters() == null) {
            return;
        }
        final Environment heatEnvFile = getHeatEnvFile(heatFileData, context);
        final Map<String, Object> parameters = heatEnvFile.getParameters();
        Map<String, ParameterDefinition> parameterDefinitionMap = TranslatorHeatToToscaParameterConverter
            .parameterConverter(serviceTemplate, heatOrchestrationTemplate.getParameters(), heatOrchestrationTemplate, heatFileName,
                heatFileData.getParentFile(), context, parameters);
        Object parameterValue;
        if (parameters != null) {
            for (Map.Entry<String, ParameterDefinition> entry : parameterDefinitionMap.entrySet()) {
                String paramName = entry.getKey();
                parameterValue = parameters.get(paramName);
                if (parameterValue != null) {
                    entry.getValue().set_default(TranslatorHeatToToscaParameterConverter
                        .getToscaParameterDefaultValue(null, null, parameterValue, entry.getValue().getType(), heatFileName,
                            heatOrchestrationTemplate, context));
                }
            }
        }
        Map<String, ParameterDefinition> inputs = serviceTemplate.getTopology_template().getInputs();
        if (Objects.isNull(inputs)) {
            serviceTemplate.getTopology_template().setInputs(parameterDefinitionMap);
        } else {
            setInputs(inputs, parameterDefinitionMap);
        }
    }

    private void setInputs(Map<String, ParameterDefinition> inputs, Map<String, ParameterDefinition> newParameters) {
        updateAnnotations(inputs, newParameters);
        inputs.putAll(newParameters);
    }

    private void updateAnnotations(Map<String, ParameterDefinition> inputParameters, Map<String, ParameterDefinition> newParameters) {
        newParameters.entrySet().stream().filter(
            stringParameterDefinitionEntry -> inputParameters.containsKey(stringParameterDefinitionEntry.getKey()) && isHasAnnotation(inputParameters,
                stringParameterDefinitionEntry)).forEach(stringParameterDefinitionEntry -> {
            List inputParamVFModuleList = getVFModulesList(inputParameters.get(stringParameterDefinitionEntry.getKey()));
            List newParamVFModuleList = getVFModulesList(stringParameterDefinitionEntry.getValue());
            if (inputParamVFModuleList.contains(newParamVFModuleList.get(0))) {
                newParamVFModuleList.remove(0);
            }
            newParamVFModuleList.addAll(inputParamVFModuleList);
        });
    }

    private boolean isHasAnnotation(Map<String, ParameterDefinition> inputParameters, Map.Entry<String, ParameterDefinition> newParameterSet) {
        ParameterDefinitionExt inputParameter = (ParameterDefinitionExt) inputParameters.get(newParameterSet.getKey());
        ParameterDefinitionExt newParameter = (ParameterDefinitionExt) newParameterSet.getValue();
        return inputParameter.getAnnotations() != null && newParameter.getAnnotations() != null;
    }

    private List getVFModulesList(ParameterDefinition param) {
        ParameterDefinitionExt parameterDefinitionExt = (ParameterDefinitionExt) param;
        return (List) parameterDefinitionExt.getAnnotations().get(ToscaConstants.SOURCE_ANNOTATION_ID).getProperties()
            .get(ToscaConstants.VF_MODULE_LABEL_PROPERTY_NAME);
    }

    private void translateOutputParameters(ServiceTemplate serviceTemplate, HeatOrchestrationTemplate heatOrchestrationTemplate,
                                           FileData heatFileData, String heatFileName, TranslationContext context) {
        if (heatOrchestrationTemplate.getOutputs() == null) {
            return;
        }
        Map<String, ParameterDefinition> parameterDefinitionMap = TranslatorHeatToToscaParameterConverter
            .parameterOutputConverter(serviceTemplate, heatOrchestrationTemplate.getOutputs(), heatOrchestrationTemplate, heatFileName, context);
        if (serviceTemplate.getTopology_template().getOutputs() != null) {
            serviceTemplate.getTopology_template().getOutputs().putAll(parameterDefinitionMap);
        } else {
            serviceTemplate.getTopology_template().setOutputs(parameterDefinitionMap);
        }
        if (heatFileData.getBase() != null && heatFileData.getBase().equals(true)) {
            updateSharedResources(serviceTemplate, heatFileName, heatOrchestrationTemplate, heatOrchestrationTemplate.getOutputs(), context);
        }
    }

    private void updateSharedResources(ServiceTemplate serviceTemplate, String heatFileName, HeatOrchestrationTemplate heatOrchestrationTemplate,
                                       Map<String, Output> outputs, TranslationContext context) {
        for (Map.Entry<String, Output> parameter : outputs.entrySet()) {
            Optional<AttachedResourceId> attachedSharedResourceId = HeatToToscaUtil
                .extractAttachedResourceId(heatFileName, heatOrchestrationTemplate, context, parameter.getValue().getValue());
            if (attachedSharedResourceId.isPresent() && attachedSharedResourceId.get().isGetResource()
                && attachedSharedResourceId.get().getTranslatedId() != null) {
                String sharedTranslatedResourceId = attachedSharedResourceId.get().getTranslatedId().toString();
                updateSharedResource(serviceTemplate, context, parameter, sharedTranslatedResourceId,
                    heatOrchestrationTemplate.getResources().get(attachedSharedResourceId.get().getEntityId()));
            } else {
                Optional<String> contrailSharedResourceId = HeatToToscaUtil
                    .extractContrailGetResourceAttachedHeatResourceId(parameter.getValue().getValue());
                if (contrailSharedResourceId.isPresent()
                    && context.getTranslatedIds().get(heatFileName).get(contrailSharedResourceId.get()) != null) {
                    String sharedTranslatedResourceId = context.getTranslatedIds().get(heatFileName).get(contrailSharedResourceId.get());
                    ConsolidationDataUtil
                        .removeSharedResource(serviceTemplate, heatOrchestrationTemplate, context, parameter.getKey(), contrailSharedResourceId.get(),
                            sharedTranslatedResourceId);
                    updateSharedResource(serviceTemplate, context, parameter, sharedTranslatedResourceId,
                        heatOrchestrationTemplate.getResources().get(contrailSharedResourceId.get()));
                }
            }
        }
        if (serviceTemplate.getTopology_template().getOutputs() != null && serviceTemplate.getTopology_template().getOutputs().size() == 0) {
            serviceTemplate.getTopology_template().setOutputs(null);
        }
    }

    private void updateSharedResource(ServiceTemplate serviceTemplate, TranslationContext context, Map.Entry<String, Output> paramName,
                                      String sharedTranslatedResourceId, Resource resource) {
        context.addHeatSharedResourcesByParam(paramName.getKey(), sharedTranslatedResourceId, resource);
        serviceTemplate.getTopology_template().getOutputs().remove(paramName.getKey());
    }

    private void translateResources(String heatFileName, ServiceTemplate serviceTemplate, HeatOrchestrationTemplate heatOrchestrationTemplate,
                                    TranslationContext context) {
        if (MapUtils.isEmpty(heatOrchestrationTemplate.getResources())) {
            return;
        }
        for (String resourceId : heatOrchestrationTemplate.getResources().keySet()) {
            Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
            if (resource == null) {
                throw new CoreException(new ResourceNotFoundInHeatFileErrorBuilder(resourceId, heatFileName).build());
            }
            ResourceTranslationFactory.getInstance(resource)
                .translateResource(heatFileName, serviceTemplate, heatOrchestrationTemplate, resource, resourceId, context);
        }
    }

    private Environment getHeatEnvFile(FileData heatFileData, TranslationContext context) {
        List<FileData> fileRelatedDataList = heatFileData.getData();
        if (fileRelatedDataList == null) {
            return new Environment();
        }
        for (FileData fileRelatedData : fileRelatedDataList) {
            if (fileRelatedData.getType().equals(FileData.Type.HEAT_ENV)) {
                return new YamlUtil().yamlToObject(context.getFileContentAsStream(fileRelatedData.getFile()), Environment.class);
            }
        }
        return new Environment();
    }
}
