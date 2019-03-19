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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.*;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.*;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionDataExtractorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME;

public class OrchestrationUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrchestrationUtil.class);
  public static final String ORCHESTRATION_CONFIG_NAMESPACE = "orchestration";
  public static final String ORCHESTRATION_IMPL_KEY = "orchestration_impl";

  private final NicDao nicDao;
  private final ComponentArtifactDao componentArtifactDao;
  private final ProcessDao processDao;
  private final OrchestrationTemplateDao orchestrationTemplateDataDao;
  private final ComponentDao componentDao;
  private final ServiceModelDao serviceModelDao;
  private final ComponentDependencyModelDao componentDependencyModelDao;
  private final CompositionEntityDataManager compositionEntityDataManager;
  private final CompositionDataExtractor compositionDataExtractor;

  public OrchestrationUtil() {
    this(NicDaoFactory.getInstance().createInterface(),
            MonitoringUploadDaoFactory.getInstance().createInterface(),
            ProcessDaoFactory.getInstance().createInterface(),
            OrchestrationTemplateDaoFactory.getInstance().createInterface(),
            ComponentDaoFactory.getInstance().createInterface(),
            ServiceModelDaoFactory.getInstance().createInterface(),
            ComponentDependencyModelDaoFactory.getInstance().createInterface(),
            CompositionEntityDataManagerFactory.getInstance().createInterface(),
            CompositionDataExtractorFactory.getInstance().createInterface());
  }

  private OrchestrationUtil(
          NicDao nicDao,
          ComponentArtifactDao componentArtifactDao,
          ProcessDao processDao,
          OrchestrationTemplateDao orchestrationTemplateDataDao,
          ComponentDao componentDao,
          ServiceModelDao serviceModelDao,
          ComponentDependencyModelDao componentDependencyModelDao,
          CompositionEntityDataManager compositionEntityDataManager,
          CompositionDataExtractor compositionDataExtractor) {
    this.nicDao = nicDao;
    this.componentArtifactDao = componentArtifactDao;
    this.processDao = processDao;
    this.orchestrationTemplateDataDao = orchestrationTemplateDataDao;
    this.componentDao = componentDao;
    this.serviceModelDao = serviceModelDao;
    this.componentDependencyModelDao = componentDependencyModelDao;
    this.compositionEntityDataManager = compositionEntityDataManager;
    this.compositionDataExtractor = compositionDataExtractor;
  }

  public static Optional<FileContentHandler> getFileContentMap(OnboardingTypesEnum type,
                                                               UploadFileResponse uploadFileResponse,
                                                               byte[] uploadedFileData) {
    FileContentHandler contentMap = null;
    try {
      contentMap = CommonUtil.validateAndUploadFileContent(type, uploadedFileData);
    } catch (IOException exception) {
      uploadFileResponse.addStructureError(
          SdcCommon.UPLOAD_FILE,
          new ErrorMessage(ErrorLevel.ERROR, Messages.INVALID_ZIP_FILE.getErrorMessage()));
      LOGGER.error("{}\n{}", Messages.INVALID_ZIP_FILE.getErrorMessage(),
              exception.getMessage(), exception);
    } catch (CoreException coreException) {
      uploadFileResponse.addStructureError(
          SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR, coreException.getMessage()));
      LOGGER.error(coreException.getMessage(), coreException);
    }
    return Optional.ofNullable(contentMap);
  }

  public void backupComponentsQuestionnaireBeforeDelete(String vspId, Version version,
                                                        Map<String, String> componentsQustanniare,
                                                        Map<String, Map<String, String>>
                                                            componentNicsQustanniare,
                                                        Map<String, Collection<ComponentMonitoringUploadEntity>>
                                                            componentMibList,
                                                        Map<String, Collection<ProcessEntity>>
                                                            componentProcesses,
                                                        Map<String, ProcessEntity> processArtifact) {

    Collection<ComponentEntity> componentsCompositionAndQuestionnaire =
        componentDao.listCompositionAndQuestionnaire(vspId, version);
    componentsCompositionAndQuestionnaire.forEach(componentEntity -> {
      String componentName = componentEntity.getComponentCompositionData().getName();
      componentsQustanniare.put(componentName, componentEntity.getQuestionnaireData());
      backupMibData(vspId, version, componentEntity, componentName, componentMibList);
      backupProcess(vspId, version, componentEntity.getId(), componentName, componentProcesses,
              processArtifact);
      backupNicsQuestionnaire(vspId, version, componentEntity, componentName,
              componentNicsQustanniare);
    });
  }

  private void backupMibData(String vspId, Version version, ComponentEntity componentEntity,
                             String componentName,
                             Map<String, Collection<ComponentMonitoringUploadEntity>> componentMibList) {
    Collection<ComponentMonitoringUploadEntity> componentMib =
        componentArtifactDao.listArtifacts(new
            ComponentMonitoringUploadEntity(vspId, version, componentEntity.getId(),
            null));
    if (CollectionUtils.isNotEmpty(componentMib)) {
      componentMibList.put(componentName, componentMib);
    }
  }

  private void backupProcess(String vspId, Version version, String componentId,
                             String componentName, Map<String,
      Collection<ProcessEntity>> processes,
                             Map<String, ProcessEntity> processArtifact) {
    Collection<ProcessEntity> processList =
        processDao.list(new ProcessEntity(vspId, version, componentId, null));
    if (!processList.isEmpty()) {
      processes.put(componentName, processList);
      processList.forEach(process -> {
        ProcessEntity artifact = processDao
            .getArtifact(new ProcessEntity(vspId, version, componentId, process.getId()));
        if (artifact.getArtifact() != null) {
          processArtifact.put(process.getId(), artifact);
        }
      });
    }
  }

  private void backupNicsQuestionnaire(String vspId, Version version,
                                       ComponentEntity componentEntity,
                                       String componentName,
                                       Map<String, Map<String, String>> componentNicsQustanniare) {
    Collection<NicEntity>
        nics = nicDao.list(new NicEntity(vspId, version, componentEntity.getId(), null));
    if (CollectionUtils.isNotEmpty(nics)) {
      Map<String, String> nicsQuestionnaire = new HashMap<>();
      nics.forEach(nicEntity -> {
        NicEntity nicQuestionnaire = nicDao.getQuestionnaireData(vspId, version,
            componentEntity.getId(), nicEntity.getId());

        nicsQuestionnaire.put(nicEntity.getNicCompositionData().getName(),
            nicQuestionnaire.getQuestionnaireData());
      });
      componentNicsQustanniare.put(componentName, nicsQuestionnaire);
    }
  }

  public void retainComponentQuestionnaireData(String vspId, Version version,
                                               Map<String, String> componentsQustanniare,
                                               Map<String, Map<String, String>>
                                                   componentNicsQustanniare,
                                               Map<String, Collection<ComponentMonitoringUploadEntity>> componentMibList,
                                               Map<String, Collection<ProcessEntity>> processes,
                                               Map<String, ProcessEntity> processArtifact) {
    //VSP processes
    restoreProcess(vspId, version, null, null, processes, processArtifact);
    Collection<ComponentEntity> components =
        componentDao.list(new ComponentEntity(vspId, version, null));
    components.forEach(componentEntity -> {
      String componentName = componentEntity.getComponentCompositionData().getName();
      if (componentsQustanniare.containsKey(componentName)) {
        componentDao.updateQuestionnaireData(vspId, version,
            componentEntity.getId(),
            componentsQustanniare.get(componentEntity.getComponentCompositionData()
                .getName()));
        if (componentNicsQustanniare.containsKey(componentName)) {
          restoreComponentNicQuestionnaire(vspId, version, componentName, componentEntity,
              componentNicsQustanniare);
        }
        //MIB //todo add for VES_EVENTS
        if (componentMibList.containsKey(componentName)) {
          restoreComponentMibData(componentName, componentEntity, componentMibList);
        }
        //VFC processes
        restoreProcess(vspId, version, componentEntity.getId(), componentName, processes,
            processArtifact);
      }
    });
  }

  private void restoreComponentNicQuestionnaire(String vspId, Version version,
                                                String componentName,
                                                ComponentEntity componentEntity,
                                                Map<String, Map<String, String>> componentNicsQustanniare) {
    Map<String, String> nicsQustanniare = componentNicsQustanniare.get(componentName);
    Collection<NicEntity> nics =
        nicDao.list(new NicEntity(vspId, version, componentEntity.getId(), null));
    nics.forEach(nicEntity -> {
      if (nicsQustanniare.containsKey(nicEntity.getNicCompositionData().getName())) {
        nicDao.updateQuestionnaireData(vspId, version,
            componentEntity.getId(), nicEntity.getId(),
            nicsQustanniare.get(nicEntity.getNicCompositionData().getName()));
      }
    });
  }

  private void restoreComponentMibData(String componentName, ComponentEntity componentEntity,
                                       Map<String, Collection<ComponentMonitoringUploadEntity>> componentMibList) {
    Collection<ComponentMonitoringUploadEntity> mibList = componentMibList.get(componentName);
    mibList.forEach(mib -> {
      mib.setComponentId(componentEntity.getId());
      componentArtifactDao.create(mib);
    });
  }

  private void restoreProcess(String vspId, Version version, String componentId,
                              String componentName,
                              Map<String, Collection<ProcessEntity>> processes,
                              Map<String, ProcessEntity> processArtifact) {
    if (processes.containsKey(componentName)) {
      Collection<ProcessEntity> processList = processes.get(componentName);
      processList.forEach(process -> {
        process.setComponentId(componentId);
        UniqueValueUtil uniqueValueUtil = new UniqueValueUtil(UniqueValueDaoFactory.getInstance()
            .createInterface());
        uniqueValueUtil.createUniqueValue(PROCESS_NAME, vspId, version.getId(), componentId,
            process.getName());
        processDao.create(process);
        if (processArtifact.containsKey(process.getId())) {
          ProcessEntity artifact = processArtifact.get(process.getId());
          processDao.uploadArtifact(artifact);
        }
      });
    }
  }

  public void deleteUploadDataAndContent(String vspId, Version version) {
    VendorSoftwareProductInfoDaoFactory.getInstance().createInterface()
        .delete(new VspDetails(vspId, version));
  }

  public void saveUploadData(VspDetails vspDetails,
                             OrchestrationTemplateCandidateData candidateData,
                             InputStream uploadedFileData,
                             FileContentHandler fileContentMap, HeatStructureTree tree) {
    Map<String, Object> manifestAsMap =
        fileContentMap.containsFile(SdcCommon.MANIFEST_NAME)
            ? (Map<String, Object>) JsonUtil.json2Object(fileContentMap.getFileContent(
            SdcCommon.MANIFEST_NAME), Map.class)
            : new HashMap<>();

    OrchestrationTemplateEntity uploadData = new OrchestrationTemplateEntity();
    uploadData.setFileSuffix(candidateData.getFileSuffix());
    uploadData.setFileName(candidateData.getFileName());
    uploadData.setContentData(ByteBuffer.wrap(FileUtils.toByteArray(uploadedFileData)));
    uploadData.setValidationDataStructure(new ValidationStructureList(tree));
    uploadData.setPackageName(Objects.isNull(manifestAsMap.get("name")) ? null :
        (String) manifestAsMap.get("name"));
    uploadData.setPackageVersion(Objects.isNull(manifestAsMap.get("version")) ? null :
        (String) manifestAsMap.get("version"));
    uploadData.setFilesDataStructure(candidateData.getFilesDataStructure());
    orchestrationTemplateDataDao.update(vspDetails.getId(), vspDetails.getVersion(), uploadData);

    VspMergeDaoFactory.getInstance().createInterface()
        .updateHint(vspDetails.getId(), vspDetails.getVersion());
  }

  public void saveServiceModel(String vspId, Version version,
                               ToscaServiceModel serviceModelToExtract,
                               ToscaServiceModel serviceModelToStore) {
    if (serviceModelToExtract != null) {
      serviceModelDao.storeServiceModel(vspId, version, serviceModelToStore);
      //Extracting the compostion data from the output service model of the first phase of
      // translation
      compositionEntityDataManager.saveCompositionData(vspId, version,
          compositionDataExtractor.extractServiceCompositionData(serviceModelToExtract));
    }
  }

  public static HeatStructureTree createHeatTree(FileContentHandler fileContentMap,
                                                 Map<String, List<ErrorMessage>> validationErrors) {
    HeatTreeManager heatTreeManager = HeatTreeManagerUtil.initHeatTreeManager(fileContentMap);
    heatTreeManager.createTree();
    heatTreeManager.addErrors(validationErrors);
    return heatTreeManager.getTree();
  }

  public void updateVspComponentDependencies(String vspId, Version version,
                                             Map<String, String>
                                                 vspComponentIdNameInfoBeforeProcess,
                                             Collection<ComponentDependencyModelEntity>
                                                 componentDependenciesBeforeDelete) {
    Map<String, String> updatedVspComponentNameIdInfo = getVspComponentNameIdInfo(vspId, version);
    if (MapUtils.isNotEmpty(updatedVspComponentNameIdInfo)) {
      Set<String> updatedVspComponentNames = updatedVspComponentNameIdInfo.keySet();
      if (CollectionUtils.isNotEmpty(componentDependenciesBeforeDelete)) {
        restoreComponentDependencies(vspId, version, vspComponentIdNameInfoBeforeProcess,
            componentDependenciesBeforeDelete, updatedVspComponentNames,
            updatedVspComponentNameIdInfo);
      }
    }
  }

  private void restoreComponentDependencies(String vspId, Version version,
                                            Map<String, String> vspComponentIdNameInfoBeforeProcess,
                                            Collection<ComponentDependencyModelEntity>
                                             componentDependenciesBeforeDelete,
                                            Set<String> updatedVspComponentNames,
                                            Map<String, String> updatedVspComponentNameIdInfo) {
    for (ComponentDependencyModelEntity componentDependency : componentDependenciesBeforeDelete) {
      String sourceComponentName = vspComponentIdNameInfoBeforeProcess.get(componentDependency
          .getSourceComponentId());
      String targetComponentName = vspComponentIdNameInfoBeforeProcess.get(componentDependency
          .getTargetComponentId());
      if (updatedVspComponentNames.contains(sourceComponentName)
          && (updatedVspComponentNames.contains(targetComponentName))) {
        ComponentDependencyModelEntity restoredDependency =
            new ComponentDependencyModelEntity(vspId, version, null);
        String newSourceComponentId = updatedVspComponentNameIdInfo.get(sourceComponentName);
        restoredDependency.setSourceComponentId(newSourceComponentId);
        String newTargetComponentId = updatedVspComponentNameIdInfo.get(targetComponentName);
        restoredDependency.setTargetComponentId(newTargetComponentId);
        restoredDependency.setRelation(componentDependency.getRelation());
        componentDependencyModelDao.create(restoredDependency);
      }
    }
  }

  public Map<String, String> getVspComponentIdNameInfo(String vspId, Version version) {
    Collection<ComponentEntity> updatedVspComponents =
        componentDao.list(new ComponentEntity(vspId, version, null));
    Map<String, String> vspComponentIdNameMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(updatedVspComponents)) {
      vspComponentIdNameMap = updatedVspComponents.stream()
          .filter(componentEntity -> componentEntity.getComponentCompositionData() != null)
          .collect(Collectors.toMap(ComponentEntity::getId,
              componentEntity -> componentEntity.getComponentCompositionData().getName()));

    }
    return vspComponentIdNameMap;
  }

  public Collection<ComponentDependencyModelEntity> getComponentDependenciesBeforeDelete(String
                                                                         vspId, Version version) {
    return componentDependencyModelDao.list(new ComponentDependencyModelEntity(vspId,
            version, null));
  }

  private Map<String, String> getVspComponentNameIdInfo(String vspId,
                                                        Version version) {
    Collection<ComponentEntity> updatedVspComponents =
        componentDao.list(new ComponentEntity(vspId, version, null));
    Map<String, String> vspComponentNameIdMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(updatedVspComponents)) {
      vspComponentNameIdMap = updatedVspComponents.stream()
          .filter(componentEntity -> componentEntity.getComponentCompositionData() != null)
          .collect(Collectors
              .toMap(componentEntity -> componentEntity.getComponentCompositionData().getName(),
                  ComponentEntity::getId));
    }
    return vspComponentNameIdMap;
  }

}
