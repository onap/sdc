package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.MonitoringUploadDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspMergeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionDataExtractorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME;

public class OrchestrationUtil {

  public static final String ORCHESTRATION_CONFIG_NAMESPACE = "orchestration";
  public static final String ORCHESTRATION_IMPL_KEY = "orchestration_impl";

  private NicDao nicDao;
  private ComponentArtifactDao componentArtifactDao;
  private ProcessDao processDao;
  private OrchestrationTemplateDao orchestrationTemplateDataDao;
  private ComponentDao componentDao;
  private ServiceModelDao serviceModelDao;
  private ComponentDependencyModelDao componentDependencyModelDao;
  private CompositionEntityDataManager compositionEntityDataManager;
  private CompositionDataExtractor compositionDataExtractor;

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

  public OrchestrationUtil(
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
    } catch (CoreException coreException) {
      uploadFileResponse.addStructureError(
          SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR, coreException.getMessage()));
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
    componentsCompositionAndQuestionnaire.forEach(componentEntity ->
        backupComponentQuestionnaire(vspId, version, componentEntity, componentsQustanniare,
            componentNicsQustanniare, componentMibList, componentProcesses, processArtifact));
  }

  private void backupComponentQuestionnaire(
      String vspId, Version version,
      ComponentEntity componentEntity,
      Map<String, String> componentsQustanniare,
      Map<String, Map<String, String>> componentNicsQustanniare,
      Map<String, Collection<ComponentMonitoringUploadEntity>> componentMibList,
      Map<String, Collection<ProcessEntity>> componentProcesses,
      Map<String, ProcessEntity> processArtifact) {
    String componentName = componentEntity.getComponentCompositionData().getName();
    componentsQustanniare.put(componentName, componentEntity.getQuestionnaireData());
    backupMibData(vspId, version, componentEntity, componentName, componentMibList);
    backupProcess(vspId, version, componentEntity.getId(), componentName, componentProcesses,
        processArtifact);
    backupNicsQuestionnaire(vspId, version, componentEntity, componentName,
        componentNicsQustanniare);
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
        UniqueValueUtil.createUniqueValue(PROCESS_NAME, vspId, version.getId(), componentId,
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
    orchestrationTemplateDataDao.update(vspDetails.getId(), vspDetails.getVersion(), uploadData);

    VspMergeDaoFactory.getInstance().createInterface()
        .updateVspModelId(vspDetails.getId(), vspDetails.getVersion());
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
                                             Map<String, String> vspComponentIdNameInfoBeforeProcess) {
    Map<String, String> updatedVspComponentNameIdInfo = getVspComponentNameIdInfo(vspId, version);
    if (MapUtils.isNotEmpty(updatedVspComponentNameIdInfo)) {
      Set<String> updatedVspComponentNames = updatedVspComponentNameIdInfo.keySet();
      Collection<ComponentDependencyModelEntity> componentDependencies =
          componentDependencyModelDao.list(new ComponentDependencyModelEntity(vspId,
              version, null));
      if (CollectionUtils.isNotEmpty(componentDependencies)) {
        updateComponentDependency(vspComponentIdNameInfoBeforeProcess, componentDependencies,
            updatedVspComponentNames, updatedVspComponentNameIdInfo);
      }
    }
  }

  private void updateComponentDependency(Map<String, String> vspComponentIdNameInfoBeforeProcess,
                                         Collection<ComponentDependencyModelEntity>
                                             componentDependencies,
                                         Set<String> updatedVspComponentNames,
                                         Map<String, String> updatedVspComponentNameIdInfo) {
    for (ComponentDependencyModelEntity componentDependency : componentDependencies) {
      String sourceComponentName = vspComponentIdNameInfoBeforeProcess.get(componentDependency
          .getSourceComponentId());
      String targetComponentName = vspComponentIdNameInfoBeforeProcess.get(componentDependency
          .getTargetComponentId());
      if (updatedVspComponentNames.contains(sourceComponentName)
          && (updatedVspComponentNames.contains(targetComponentName))) {
        String newSourceComponentId = updatedVspComponentNameIdInfo.get(sourceComponentName);
        componentDependency.setSourceComponentId(newSourceComponentId);
        String newTargetComponentId = updatedVspComponentNameIdInfo.get(targetComponentName);
        componentDependency.setTargetComponentId(newTargetComponentId);
        componentDependencyModelDao.update(componentDependency);
      } else {
        componentDependencyModelDao.delete(componentDependency);
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
