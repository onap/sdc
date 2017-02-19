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

import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.yaml.YamlUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.Environment;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.tosca.datatypes.ToscaGroupType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.GroupDefinition;
import org.openecomp.sdc.tosca.datatypes.model.Metadata;
import org.openecomp.sdc.tosca.datatypes.model.ParameterDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.FileDataCollection;
import org.openecomp.sdc.translator.services.heattotosca.errors.ResourceNotFoundInHeatFileErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaParameterConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


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

  /**
   * Translate heat files translator output.
   *
   * @param translationContext the translation context
   * @return the translator output
   */
  public TranslatorOutput translateHeatFiles(TranslationContext translationContext) {
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

    ToscaServiceModel toscaServiceModel =
        createToscaServiceModel(mainServiceTemplate, translationContext);

    TranslatorOutput translatorOutput = new TranslatorOutput();
    translatorOutput.setToscaServiceModel(toscaServiceModel);
    return translatorOutput;
  }

  private ToscaServiceModel createToscaServiceModel(ServiceTemplate entryDefinitionServiceTemplate,
                                                    TranslationContext translationContext) {
    return new ToscaServiceModel(getCsarArtifactFiles(translationContext),
        getServiceTemplates(translationContext),
        ToscaUtil.getServiceTemplateFileName(entryDefinitionServiceTemplate));
  }

  private Map<String, ServiceTemplate> getServiceTemplates(TranslationContext translationContext) {
    List<ServiceTemplate> serviceTemplates = new ArrayList<>();
    serviceTemplates.addAll(GlobalTypesGenerator.getGlobalTypesServiceTemplate().values());
    serviceTemplates.addAll(translationContext.getTranslatedServiceTemplates().values());
    Map<String, ServiceTemplate> serviceTemplatesMap = new HashMap<>();

    for (ServiceTemplate template : serviceTemplates) {
      serviceTemplatesMap.put(ToscaUtil.getServiceTemplateFileName(template), template);
    }
    return serviceTemplatesMap;
  }

  private FileContentHandler getCsarArtifactFiles(TranslationContext translationContext) {
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

    return artifactFiles;
  }

  private ServiceTemplate createMainServiceTemplate(TranslationContext translationContext) {
    ServiceTemplate mainServiceTemplate = new ServiceTemplate();
    translationContext.getTranslatedServiceTemplates().put("Main", mainServiceTemplate);
    Metadata templateMetadata = new Metadata();
    templateMetadata.setTemplate_name("Main");
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
  public void translateHeatFile(ServiceTemplate serviceTemplate, FileData heatFileData,
                                TranslationContext context) {
    String heatFileName = heatFileData.getFile();
    HeatOrchestrationTemplate heatOrchestrationTemplate = new YamlUtil()
        .yamlToObject(context.getFileContent(heatFileName), HeatOrchestrationTemplate.class);

    translateInputParameters(serviceTemplate, heatOrchestrationTemplate, heatFileData, context,
        heatFileName);
    translateResources(heatFileName, serviceTemplate, heatOrchestrationTemplate, context);
    translateOutputParameters(serviceTemplate, heatOrchestrationTemplate, heatFileData,
        heatFileName, context);
    createHeatStackGroup(serviceTemplate, heatFileData, heatOrchestrationTemplate, context);

    if (Objects.nonNull(heatFileData.getData())) {
      heatFileData.getData().stream().filter(data -> data.getType() == FileData.Type.HEAT_VOL)
          .forEach(data -> translateHeatFile(serviceTemplate, data, context));
    }
  }

  private void createHeatStackGroup(ServiceTemplate serviceTemplate, FileData heatFileData,
                                    HeatOrchestrationTemplate heatOrchestrationTemplate,
                                    TranslationContext context) {
    ToscaFileOutputService toscaFileOutputService = new ToscaFileOutputServiceCsarImpl();
    String fileName = heatFileData.getFile();
    final String fileNameWoExtension =
        FileUtils.getFileWithoutExtention(fileName);//.heatFileData.getFile().split("\\.")[0];

    GroupDefinition groupDefinition = new GroupDefinition();
    groupDefinition.setType(ToscaGroupType.HEAT_STACK.getDisplayName());
    groupDefinition.setProperties(new HashMap<>());
    groupDefinition.getProperties()
        .put("heat_file", "../" + toscaFileOutputService.getArtifactsFolderName() + "/" + fileName);
    String hotDescription = heatOrchestrationTemplate.getDescription();
    if (hotDescription != null && !hotDescription.isEmpty()) {
      groupDefinition.getProperties().put(Constants.DESCRIPTION_PROPERTY_NAME, hotDescription);
    }
    groupDefinition.setMembers(new ArrayList<>());
    Map<String, Set<String>> heatStackGroupMembers = context.getHeatStackGroupMembers();
    if (heatStackGroupMembers.get(fileName) == null) {
      return; //not creating a group when no resources are present in the heat input
    }
    groupDefinition.getMembers().addAll(heatStackGroupMembers.get(fileName));
    if (serviceTemplate.getTopology_template().getGroups() == null) {
      Map<String, GroupDefinition> groups = new HashMap<>();
      serviceTemplate.getTopology_template().setGroups(groups);
    }
    serviceTemplate.getTopology_template().getGroups().put(fileNameWoExtension, groupDefinition);
  }

  private void translateInputParameters(ServiceTemplate serviceTemplate,
                                        HeatOrchestrationTemplate heatOrchestrationTemplate,
                                        FileData heatFileData, TranslationContext context,
                                        String heatFileName) {

    if (heatOrchestrationTemplate.getParameters() == null) {
      return;
    }

    Map<String, ParameterDefinition> parameterDefinitionMap =
        TranslatorHeatToToscaParameterConverter
            .parameterConverter(heatOrchestrationTemplate.getParameters(),
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
              .getToscaParameterDefaultValue(parameterValue, entry.getValue().getType(),
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
  }

  private void translateOutputParameters(ServiceTemplate serviceTemplate,
                                         HeatOrchestrationTemplate heatOrchestrationTemplate,
                                         FileData heatFileData, String heatFileName,
                                         TranslationContext context) {
    if (heatOrchestrationTemplate.getOutputs() == null) {
      return;
    }
    Map<String, ParameterDefinition> parameterDefinitionMap =
        TranslatorHeatToToscaParameterConverter
            .parameterOutputConverter(heatOrchestrationTemplate.getOutputs(),
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
  }

  private void updateSharedResources(ServiceTemplate serviceTemplate, String heatFileName,
                                     HeatOrchestrationTemplate heatOrchestrationTemplate,
                                     Map<String, Output> outputs, TranslationContext context) {
    for (Map.Entry<String, Output> paramName : outputs.entrySet()) {
      Optional<AttachedResourceId> attachedSharedResourceId = HeatToToscaUtil
          .extractAttachedResourceId(heatFileName, heatOrchestrationTemplate, context,
              paramName.getValue().getValue());
      if (attachedSharedResourceId.isPresent() && attachedSharedResourceId.get().isGetResource()
          && attachedSharedResourceId.get().getTranslatedId() != null) {
        String sharedTranslatedResourceId =
            attachedSharedResourceId.get().getTranslatedId().toString();
        updateSharedResource(serviceTemplate, context, paramName, sharedTranslatedResourceId,
            heatOrchestrationTemplate.getResources()
                .get(attachedSharedResourceId.get().getEntityId()));
      } else {
        String contrailSharedResourceId = HeatToToscaUtil
            .extractContrailGetResourceAttachedHeatResourceId(paramName.getValue().getValue());
        if (contrailSharedResourceId != null
            && context.getTranslatedIds().get(heatFileName).get(contrailSharedResourceId) != null) {
          updateSharedResource(serviceTemplate, context, paramName,
              context.getTranslatedIds().get(heatFileName).get(contrailSharedResourceId),
              heatOrchestrationTemplate.getResources().get(contrailSharedResourceId));
        }
      }
    }
    if (serviceTemplate.getTopology_template().getOutputs() != null
        && serviceTemplate.getTopology_template().getOutputs().size() == 0) {
      serviceTemplate.getTopology_template().setOutputs(null);
    }
  }

  private void updateSharedResource(ServiceTemplate serviceTemplate, TranslationContext context,
                                    Map.Entry<String, Output> paramName,
                                    String sharedTranslatedResourceId, Resource resource) {
    context.addHeatSharedResourcesByParam(paramName.getKey(), sharedTranslatedResourceId, resource);
    serviceTemplate.getTopology_template().getOutputs().remove(paramName.getKey());
  }

  private void translateResources(String heatFileName, ServiceTemplate serviceTemplate,
                                  HeatOrchestrationTemplate heatOrchestrationTemplate,
                                  TranslationContext context) {
    for (String resourceId : heatOrchestrationTemplate.getResources().keySet()) {
      Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
      if (resource == null) {
        throw new CoreException(
            new ResourceNotFoundInHeatFileErrorBuilder(resourceId, heatFileName).build());
      }
      ResourceTranslationFactory.getInstance(resource)
          .translateResource(heatFileName, serviceTemplate, heatOrchestrationTemplate, resource,
              resourceId, context);
    }
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
