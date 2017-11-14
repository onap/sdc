package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration;

import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.GENERAL_COMPONENT_ID;
import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME;

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
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadData;
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

public class OrchestrationUtil {

  public static final String ORCHESTRATION_CONFIG_NAMESPACE = "orchestration";
  public static final String ORCHESTRATION_IMPL_KEY = "orchestration_impl";


  private VendorSoftwareProductDao vendorSoftwareProductDao;
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
    this (VendorSoftwareProductDaoFactory.getInstance().createInterface(),
          NicDaoFactory.getInstance().createInterface(),
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
      VendorSoftwareProductDao vendorSoftwareProductDao,
      NicDao nicDao,
      ComponentArtifactDao componentArtifactDao,
      ProcessDao processDao,
      OrchestrationTemplateDao orchestrationTemplateDataDao,
      ComponentDao componentDao,
      ServiceModelDao serviceModelDao,
      ComponentDependencyModelDao componentDependencyModelDao,
      CompositionEntityDataManager compositionEntityDataManager,
      CompositionDataExtractor compositionDataExtractor) {
    this.vendorSoftwareProductDao = vendorSoftwareProductDao;
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
                                                               UploadFileResponse
                                                                   uploadFileResponse,
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

  public void backupComponentsQuestionnaireBeforeDelete(String vspId, Version activeVersion,
                                           Map<String, String> componentsQustanniare,
                                           Map<String, Map<String, String>>
                                               componentNicsQustanniare,
                                           Map<String, Collection<ComponentMonitoringUploadEntity>>
                                               componentMibList,
                                           Map<String, Collection<ProcessEntity>>
                                               componentProcesses,
                                           Map<String, ProcessEntity> processArtifact) {
    //backup VSP processes
    backupProcess(vspId, activeVersion, GENERAL_COMPONENT_ID, GENERAL_COMPONENT_ID,
        componentProcesses, processArtifact);
    Collection<ComponentEntity> componentsCompositionAndQuestionnaire = vendorSoftwareProductDao
        .listComponentsCompositionAndQuestionnaire(vspId,
            activeVersion);
    componentsCompositionAndQuestionnaire.forEach(componentEntity ->
        backupComponentQuestionnaire(vspId, activeVersion, componentEntity, componentsQustanniare,
            componentNicsQustanniare, componentMibList, componentProcesses, processArtifact));
  }

  private void backupComponentQuestionnaire(String vspId, Version activeVersion,
                                            ComponentEntity componentEntity,
                                            Map<String, String> componentsQustanniare,
                                            Map<String, Map<String, String>>
                                                componentNicsQustanniare,
                                            Map<String, Collection<ComponentMonitoringUploadEntity>>
                                                componentMibList,
                                            Map<String, Collection<ProcessEntity>>
                                                componentProcesses,
                                            Map<String, ProcessEntity> processArtifact) {
    String componentName = componentEntity.getComponentCompositionData().getName();
    backupMibData(componentsQustanniare, componentMibList, componentName, componentEntity, vspId,
        activeVersion);
    backupComponentProcessData(componentNicsQustanniare, vspId, activeVersion, componentName,
        componentEntity, componentProcesses, processArtifact);
  }

  private void backupMibData(Map<String, String> componentsQustanniare,
                             Map<String, Collection<ComponentMonitoringUploadEntity>>
                                 componentMibList,
                             String componentName, ComponentEntity componentEntity,
                             String vspId, Version activeVersion) {
    componentsQustanniare.put(componentName, componentEntity.getQuestionnaireData());
    //backup mib
    Collection<ComponentMonitoringUploadEntity> componentMib =
        componentArtifactDao.listArtifacts(new
            ComponentMonitoringUploadEntity(vspId, activeVersion, componentEntity.getId(), null));
    if (CollectionUtils.isNotEmpty(componentMib)) {
      componentMibList.put(componentName, componentMib);
    }
  }

  private void backupComponentProcessData(Map<String, Map<String, String>> componentNicsQustanniare,
                                          String vspId, Version activeVersion, String componentName,
                                          ComponentEntity componentEntity,
                                          Map<String, Collection<ProcessEntity>> componentProcesses,
                                          Map<String, ProcessEntity> processArtifact) {
    Collection<NicEntity>
        nics = nicDao.list(new NicEntity(vspId, activeVersion, componentEntity.getId(), null));
    //backup component processes
    backupProcess(vspId, activeVersion, componentEntity.getId(), componentName,
        componentProcesses, processArtifact);
    if (CollectionUtils.isNotEmpty(nics)) {
      Map<String, String> nicsQustanniare = new HashMap<>();
      nics.forEach(nicEntity -> {
        NicEntity nic = nicDao.get(new NicEntity(vspId, activeVersion, componentEntity.getId(),
            nicEntity.getId()));
        NicEntity nicQuestionnaire = nicDao.getQuestionnaireData(vspId, activeVersion,
            componentEntity.getId(), nicEntity.getId());

        nicsQustanniare
            .put(nicEntity.getNicCompositionData().getName(),
                nicQuestionnaire.getQuestionnaireData());
      });
      componentNicsQustanniare.put(componentName, nicsQustanniare);
    }
  }

  private void backupProcess(String vspId, Version activeVersion, String componentId,
                                    String componentName, Map<String,
      Collection<ProcessEntity>> processes,
                                    Map<String, ProcessEntity> processArtifact) {
    Collection<ProcessEntity> processList = vendorSoftwareProductDao.listProcesses(vspId,
        activeVersion, componentId);
    if (!processList.isEmpty()) {
      processes.put(componentName, processList);
      processList.forEach(process -> {
        ProcessEntity artifact =
            processDao.get(new ProcessEntity(vspId, activeVersion, componentId, process.getId()));
        if (artifact.getArtifact() != null) {
          processArtifact.put(process.getId(), artifact);
        }
      });
    }
  }

  public void retainComponentQuestionnaireData(String vspId, Version activeVersion,
                          Map<String, String> componentsQustanniare,
                          Map<String, Map<String, String>>
                              componentNicsQustanniare,
                          Map<String, Collection<ComponentMonitoringUploadEntity>> componentMibList,
                          Map<String, Collection<ProcessEntity>> processes,
                          Map<String, ProcessEntity> processArtifact) {
    //VSP processes
    restoreProcess(vspId, activeVersion, GENERAL_COMPONENT_ID, GENERAL_COMPONENT_ID, processes,
        processArtifact);
    Collection<ComponentEntity>
        components = vendorSoftwareProductDao.listComponents(vspId, activeVersion);
    components.forEach(componentEntity -> {
      String componentName = componentEntity.getComponentCompositionData().getName();
      if (componentsQustanniare.containsKey(componentName)) {
        //Restore component questionnaire
        componentDao.updateQuestionnaireData(vspId, activeVersion,
            componentEntity.getId(),
            componentsQustanniare.get(componentEntity.getComponentCompositionData()
                .getName()));
        //Restore component nic questionnaire
        if (componentNicsQustanniare.containsKey(componentName)) {
          restoreComponentNicQuestionnaire(vspId, activeVersion, componentName, componentEntity,
              componentNicsQustanniare);
        }
        //MIB //todo add for VES_EVENTS
        if (componentMibList.containsKey(componentName)) {
          restoreComponentMibData(componentName, componentEntity, componentMibList);
        }
        //VFC processes
        restoreProcess(vspId, activeVersion, componentEntity.getId(), componentName, processes,
            processArtifact);
      }
    });
  }

  private void restoreComponentNicQuestionnaire(String vspId, Version activeVersion,
                                                String componentName,
                                                ComponentEntity componentEntity,
                                                Map<String, Map<String, String>>
                                                    componentNicsQustanniare) {
    Map<String, String> nicsQustanniare = componentNicsQustanniare.get(componentName);
    Collection<NicEntity> nics =
        nicDao.list(new NicEntity(vspId, activeVersion, componentEntity.getId(), null));
    nics.forEach(nicEntity -> {
      if (nicsQustanniare.containsKey(nicEntity.getNicCompositionData().getName())) {
        nicDao.updateQuestionnaireData(vspId, activeVersion,
            componentEntity.getId(), nicEntity.getId(),
            nicsQustanniare.get(nicEntity.getNicCompositionData().getName()));
      }
    });
  }

  private void restoreComponentMibData(String componentName,
                                       ComponentEntity componentEntity,
                                       Map<String, Collection<ComponentMonitoringUploadEntity>>
                                           componentMibList) {
    Collection<ComponentMonitoringUploadEntity> mibList = componentMibList.get(componentName);
    mibList.forEach(mib -> {
      mib.setComponentId(componentEntity.getId());
      componentArtifactDao.create(mib);
    });
  }

  private void restoreProcess(String vspId, Version activeVersion, String componentId,
                                     String componentName,
                                     Map<String, Collection<ProcessEntity>> processes,
                                     Map<String, ProcessEntity> processArtifact) {
    if (processes.containsKey(componentName)) {
      Collection<ProcessEntity> processList = processes.get(componentName);
      processList.forEach(process -> {
        //Reatin VFC process
        if (!GENERAL_COMPONENT_ID.equals(componentId)
            && processArtifact.containsKey(process.getId())) {
          ProcessEntity artifact = processArtifact.get(process.getId());
          artifact.setComponentId(componentId);
          UniqueValueUtil.createUniqueValue(PROCESS_NAME, vspId, activeVersion.toString(),
              componentId, process.getName());
          vendorSoftwareProductDao.createProcess(artifact);
        }
      });
    }
  }

  public void deleteUploadDataAndContent(String vspId, Version version) {
    //fixme change this when more tables are zusammenized
    vendorSoftwareProductDao.deleteUploadData(vspId, version);
  }

  public void saveUploadData(String vspId, Version activeVersion,
                                    InputStream uploadedFileData,
                                    FileContentHandler fileContentMap, HeatStructureTree tree) {
    Map<String, Object> manifestAsMap =
        fileContentMap.containsFile(SdcCommon.MANIFEST_NAME)
            ? (Map<String, Object>) JsonUtil.json2Object(fileContentMap.getFileContent(
            SdcCommon.MANIFEST_NAME), Map.class)
            : new HashMap<>();

    UploadData uploadData = new UploadData();
    uploadData.setContentData(ByteBuffer.wrap(FileUtils.toByteArray(uploadedFileData)));
    uploadData.setValidationDataStructure(new ValidationStructureList(tree));
    uploadData.setPackageName(Objects.isNull(manifestAsMap.get("name")) ? null :
        (String) manifestAsMap.get("name"));
    uploadData.setPackageVersion(Objects.isNull(manifestAsMap.get("version")) ? null :
        (String) manifestAsMap.get("version"));
    orchestrationTemplateDataDao.updateOrchestrationTemplateData(vspId, uploadData);
  }

  public void saveServiceModel(String vspId,
                                      Version version,
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

  public void updateVspComponentDependencies(String vspId, Version activeVersion,
                                                    Map<String, String>
                                                        vspComponentIdNameInfoBeforeProcess) {
    Map<String, String> updatedVspComponentNameIdInfo = getVspComponentNameIdInfo(vspId,
        activeVersion);
    if (MapUtils.isNotEmpty(updatedVspComponentNameIdInfo)) {
      Set<String> updatedVspComponentNames = updatedVspComponentNameIdInfo.keySet();
      Collection<ComponentDependencyModelEntity> componentDependencies =
          vendorSoftwareProductDao.listComponentDependencies(vspId, activeVersion);
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

  public Map<String, String> getVspComponentIdNameInfo(String vspId, Version activeVersion) {
    Collection<ComponentEntity> updatedVspComponents =
        vendorSoftwareProductDao.listComponents(vspId, activeVersion);
    Map<String, String> vspComponentIdNameMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(updatedVspComponents)) {
      vspComponentIdNameMap = updatedVspComponents.stream()
          .filter(componentEntity -> componentEntity.getComponentCompositionData() != null)
          .collect(Collectors.toMap(componentEntity -> componentEntity.getId(),
              componentEntity -> componentEntity.getComponentCompositionData().getName()));

    }
    return vspComponentIdNameMap;
  }

  private Map<String, String> getVspComponentNameIdInfo(String vspId,
                                                              Version activeVersion) {
    Collection<ComponentEntity> updatedVspComponents =
        vendorSoftwareProductDao.listComponents(vspId, activeVersion);
    Map<String, String> vspComponentNameIdMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(updatedVspComponents)) {
      vspComponentNameIdMap = updatedVspComponents.stream()
          .filter(componentEntity -> componentEntity.getComponentCompositionData() != null)
          .collect(Collectors.toMap(componentEntity -> componentEntity
            .getComponentCompositionData().getName(), componentEntity -> componentEntity.getId()));
    }
    return vspComponentNameIdMap;
  }

}
