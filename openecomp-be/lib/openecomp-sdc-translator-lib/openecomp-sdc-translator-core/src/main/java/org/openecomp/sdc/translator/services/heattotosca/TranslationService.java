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
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.Environment;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.ToscaGroupType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.GroupDefinition;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ParameterDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.FileDataCollection;
import org.openecomp.sdc.translator.services.heattotosca.errors.ResourceNotFoundInHeatFileErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaParameterConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class TranslationService {

  protected static Logger logger = (Logger) LoggerFactory.getLogger(TranslationService.class);
  protected static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

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

  /**
   * Translate heat files translator output.
   *
   * @param translationContext the translation context
   * @return the translator output
   */
  public TranslatorOutput translateHeatFiles(TranslationContext translationContext) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    ServiceTemplate mainServiceTemplate = createMainServiceTemplate(translationContext);
    List<FileData> fileDataList = translationContext.getManifest().getContent().getData();
    FileDataCollection fileDataCollection = HeatToToscaUtil.getFileCollectionsByFilter(fileDataList,
        TranslationService.getTypesToProcessByTranslator(), translationContext);

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

    try {
      ToscaServiceModel toscaServiceModel =
          HeatToToscaUtil.createToscaServiceModel(mainServiceTemplate, translationContext);

      TranslatorOutput translatorOutput = new TranslatorOutput();
      //Keeping a copy of tosca service model after first stage of translation for extraction of
      // composition data
      translatorOutput.setNonUnifiedToscaServiceModel(
          ToscaServiceModel.getClonedServiceModel(toscaServiceModel));
      translatorOutput.setToscaServiceModel(toscaServiceModel);

      mdcDataDebugMessage.debugExitMessage(null, null);
      return translatorOutput;
    } catch (IOException ioe){
      ErrorCode error = new ErrorCode.ErrorCodeBuilder().withMessage("Cannot create Tosca " +
          "service model. reason : " + ioe.getMessage()).build();
      throw new CoreException(error, ioe);
    }
  }

  private ServiceTemplate createMainServiceTemplate(TranslationContext translationContext) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    ServiceTemplate mainServiceTemplate = new ServiceTemplate();
    translationContext.getTranslatedServiceTemplates()
        .put(Constants.MAIN_TEMPLATE_NAME, mainServiceTemplate);
    Map<String, String> templateMetadata = new HashMap<>();
    templateMetadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, Constants.MAIN_TEMPLATE_NAME);
    mainServiceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    mainServiceTemplate.setMetadata(templateMetadata);
    mainServiceTemplate.setTopology_template(new TopologyTemplate());
    mainServiceTemplate.setImports(GlobalTypesGenerator.getGlobalTypesImportList());

    mdcDataDebugMessage.debugExitMessage(null, null);
    return mainServiceTemplate;
  }

  /**
   * Translate heat file.
   *
   * @param serviceTemplate the service template
   * @param heatFileData    the heat file data
   * @param context         the context
   */
  public void translateHeatFile(ServiceTemplate serviceTemplate, FileData heatFileData,
                                TranslationContext context) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    String heatFileName = heatFileData.getFile();
    HeatOrchestrationTemplate heatOrchestrationTemplate = new YamlUtil()
        .yamlToObject(context.getFileContent(heatFileName), HeatOrchestrationTemplate.class);

    translateInputParameters(serviceTemplate, heatOrchestrationTemplate, heatFileData, context,
        heatFileName);
    translateResources(heatFileName, serviceTemplate, heatOrchestrationTemplate, context);
    translateOutputParameters(serviceTemplate, heatOrchestrationTemplate, heatFileData,
        heatFileName, context);
    createHeatStackGroup(serviceTemplate, heatFileData, heatOrchestrationTemplate, context);
    handleHeatPseudoParam(heatFileName, serviceTemplate, context);

    if (Objects.nonNull(heatFileData.getData())) {
      heatFileData.getData().stream().filter(data -> data.getType() == FileData.Type.HEAT_VOL)
          .forEach(data -> translateHeatFile(serviceTemplate, data, context));
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void handleHeatPseudoParam(String heatFileName, ServiceTemplate serviceTemplate,
                                     TranslationContext context) {
    Map<String, String> translatedHeatPseudoParam =
        context.getUsedHeatPseudoParams().get(heatFileName);
    if (Objects.nonNull(translatedHeatPseudoParam)) {
      for (String heatPseudoParam : translatedHeatPseudoParam.keySet()) {
        if (!serviceTemplate.getTopology_template().getInputs().containsKey(heatPseudoParam)) {
          ParameterDefinition parameterDefinition = new ParameterDefinition();
          parameterDefinition.setType(PropertyType.STRING.getDisplayName());
          parameterDefinition.setRequired(false);
          String parameterDefinitionId = translatedHeatPseudoParam.get(heatPseudoParam);
          DataModelUtil.addInputParameterToTopologyTemplate(serviceTemplate, parameterDefinitionId,
              parameterDefinition);
        }
      }
    }
  }

  private void createHeatStackGroup(ServiceTemplate serviceTemplate, FileData heatFileData,
                                    HeatOrchestrationTemplate heatOrchestrationTemplate,
                                    TranslationContext context) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    ToscaFileOutputService toscaFileOutputService = new ToscaFileOutputServiceCsarImpl();
    final String fileName = heatFileData.getFile();
    final String heatStackGroupId = FileUtils.getFileWithoutExtention(fileName) + "_group";

    GroupDefinition groupDefinition = new GroupDefinition();
    groupDefinition.setType(ToscaGroupType.HEAT_STACK);
    groupDefinition.setProperties(new HashMap<>());
    groupDefinition.getProperties()
        .put("heat_file", "../" + toscaFileOutputService.getArtifactsFolderName() + "/" + fileName);
    String hotDescription = heatOrchestrationTemplate.getDescription();
    if (hotDescription != null && !hotDescription.isEmpty()) {
      groupDefinition.getProperties().put(Constants.DESCRIPTION_PROPERTY_NAME, hotDescription);
    }
    groupDefinition.setMembers(new ArrayList<>());
    Set<String> heatStackGroupMembersIds = getHeatStackGroupMembers(fileName,
        serviceTemplate, context);
    if (CollectionUtils.isEmpty(heatStackGroupMembersIds)) {
      return; //not creating a group when no resources are present in the heat input
    }
    groupDefinition.getMembers().addAll(heatStackGroupMembersIds);
    DataModelUtil
        .addGroupDefinitionToTopologyTemplate(serviceTemplate, heatStackGroupId, groupDefinition);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private Set<String> getHeatStackGroupMembers(String heatFileName,
                                                            ServiceTemplate serviceTemplate,
                                                            TranslationContext context){

    Map<String, Set<String>> heatStackGroupMembers = context.getHeatStackGroupMembers();
    Set<String> groupMembers = MapUtils.isEmpty(heatStackGroupMembers) ? new HashSet<>()
        : heatStackGroupMembers.get(heatFileName);

    if(CollectionUtils.isEmpty(groupMembers)){
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

  private void updateSubstitutableGroupMemberId(String heatFileName,
                                                ServiceTemplate serviceTemplate,
                                                Set<String> updatedMembersIds) {
    Optional<String> substitutableGroupMemberId =
        ToscaUtil.getSubstitutableGroupMemberId(heatFileName, serviceTemplate);

    if (substitutableGroupMemberId.isPresent()) {
      updatedMembersIds.add(substitutableGroupMemberId.get());
    }
  }

  private void translateInputParameters(ServiceTemplate serviceTemplate,
                                        HeatOrchestrationTemplate heatOrchestrationTemplate,
                                        FileData heatFileData, TranslationContext context,
                                        String heatFileName) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (heatOrchestrationTemplate.getParameters() == null) {
      return;
    }

    Map<String, ParameterDefinition> parameterDefinitionMap =
        TranslatorHeatToToscaParameterConverter
            .parameterConverter(serviceTemplate,heatOrchestrationTemplate.getParameters(),
                heatOrchestrationTemplate, heatFileName, context);
    Environment heatEnvFile = getHeatEnvFile(heatFileData, context);
    Map<String, Object> parameters = heatEnvFile.getParameters();
    Object parameterValue;
    if (parameters != null) {
      for (Map.Entry<String, ParameterDefinition> entry : parameterDefinitionMap.entrySet()) {
        String paramName = entry.getKey();
        parameterValue = parameters.get(paramName);
        if (parameterValue != null) {
          entry.getValue().set_default(TranslatorHeatToToscaParameterConverter
              .getToscaParameterDefaultValue(null, null, parameterValue, entry.getValue().getType(),
                  heatFileName, heatOrchestrationTemplate, context));
        }
      }
    }

    Map<String, ParameterDefinition> inputs = serviceTemplate.getTopology_template().getInputs();
    if (Objects.isNull(inputs)) {
      serviceTemplate.getTopology_template().setInputs(parameterDefinitionMap);
    } else {
      inputs.putAll(parameterDefinitionMap);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void translateOutputParameters(ServiceTemplate serviceTemplate,
                                         HeatOrchestrationTemplate heatOrchestrationTemplate,
                                         FileData heatFileData, String heatFileName,
                                         TranslationContext context) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (heatOrchestrationTemplate.getOutputs() == null) {
      return;
    }
    Map<String, ParameterDefinition> parameterDefinitionMap =
        TranslatorHeatToToscaParameterConverter
            .parameterOutputConverter(serviceTemplate,heatOrchestrationTemplate.getOutputs(),
                heatOrchestrationTemplate, heatFileName, context);
    if (serviceTemplate.getTopology_template().getOutputs() != null) {
      serviceTemplate.getTopology_template().getOutputs().putAll(parameterDefinitionMap);
    } else {
      serviceTemplate.getTopology_template().setOutputs(parameterDefinitionMap);
    }

    if (heatFileData.getBase() != null && heatFileData.getBase().equals(true)) {
      updateSharedResources(serviceTemplate, heatFileName, heatOrchestrationTemplate,
          heatOrchestrationTemplate.getOutputs(), context);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void updateSharedResources(ServiceTemplate serviceTemplate, String heatFileName,
                                     HeatOrchestrationTemplate heatOrchestrationTemplate,
                                     Map<String, Output> outputs, TranslationContext context) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    for (Map.Entry<String, Output> parameter : outputs.entrySet()) {
      Optional<AttachedResourceId> attachedSharedResourceId = HeatToToscaUtil
          .extractAttachedResourceId(heatFileName, heatOrchestrationTemplate, context,
              parameter.getValue().getValue());
      if (attachedSharedResourceId.isPresent() && attachedSharedResourceId.get().isGetResource()
          && attachedSharedResourceId.get().getTranslatedId() != null) {
        String sharedTranslatedResourceId =
            attachedSharedResourceId.get().getTranslatedId().toString();
        updateSharedResource(serviceTemplate, context, parameter, sharedTranslatedResourceId,
            heatOrchestrationTemplate.getResources()
                .get(attachedSharedResourceId.get().getEntityId()));
      } else {
        Optional<String> contrailSharedResourceId = HeatToToscaUtil
            .extractContrailGetResourceAttachedHeatResourceId(parameter.getValue().getValue());
        if (contrailSharedResourceId.isPresent()
            && context.getTranslatedIds().get(heatFileName).get(contrailSharedResourceId.get())
            != null) {
          String sharedTranslatedResourceId = context.getTranslatedIds().get(heatFileName).get
              (contrailSharedResourceId.get());
          ConsolidationDataUtil.removeSharedResource(serviceTemplate, heatOrchestrationTemplate,
              context, parameter.getKey(),contrailSharedResourceId.get(), sharedTranslatedResourceId);
          updateSharedResource(serviceTemplate, context, parameter,sharedTranslatedResourceId,
              heatOrchestrationTemplate.getResources().get(contrailSharedResourceId.get()));
        }
      }
    }
    if (serviceTemplate.getTopology_template().getOutputs() != null
        && serviceTemplate.getTopology_template().getOutputs().size() == 0) {
      serviceTemplate.getTopology_template().setOutputs(null);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void updateSharedResource(ServiceTemplate serviceTemplate, TranslationContext context,
                                    Map.Entry<String, Output> paramName,
                                    String sharedTranslatedResourceId, Resource resource) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    context.addHeatSharedResourcesByParam(paramName.getKey(), sharedTranslatedResourceId, resource);
    serviceTemplate.getTopology_template().getOutputs().remove(paramName.getKey());

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void translateResources(String heatFileName, ServiceTemplate serviceTemplate,
                                  HeatOrchestrationTemplate heatOrchestrationTemplate,
                                  TranslationContext context) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if(MapUtils.isEmpty(heatOrchestrationTemplate.getResources())){
      return;
    }

    for (String resourceId : heatOrchestrationTemplate.getResources().keySet()) {
      Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
      if (resource == null) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.TRANSLATE_RESOURCE, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.TRANSLATE_HEAT);
        throw new CoreException(
            new ResourceNotFoundInHeatFileErrorBuilder(resourceId, heatFileName).build());
      }
      ResourceTranslationFactory.getInstance(resource)
          .translateResource(heatFileName, serviceTemplate, heatOrchestrationTemplate, resource,
              resourceId, context);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private Environment getHeatEnvFile(FileData heatFileData, TranslationContext context) {
    List<FileData> fileRelatedDataList = heatFileData.getData();
    if (fileRelatedDataList == null) {
      return new Environment();
    }
    for (FileData fileRelatedData : fileRelatedDataList) {
      if (fileRelatedData.getType().equals(FileData.Type.HEAT_ENV)) {
        return new YamlUtil().yamlToObject(context.getFileContent(fileRelatedData.getFile()),
            Environment.class);
      }
    }
    return new Environment();
  }


}
