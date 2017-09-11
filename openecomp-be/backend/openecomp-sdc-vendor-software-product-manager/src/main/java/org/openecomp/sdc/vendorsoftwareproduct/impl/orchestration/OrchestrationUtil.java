package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration;

import org.apache.commons.collections4.CollectionUtils;
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
import org.openecomp.sdc.vendorsoftwareproduct.dao.MonitoringUploadDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
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

import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.GENERAL_COMPONENT_ID;
import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME;

public class OrchestrationUtil {

    public static final String ORCHESTRATION_CONFIG_NAMESPACE = "orchestration";
    public static final String ORCHESTRATION_Impl_Key = "orchestration_impl";


    private static VendorSoftwareProductDao vendorSoftwareProductDao =
        VendorSoftwareProductDaoFactory.getInstance().createInterface();
    private static NicDao nicDao = NicDaoFactory.getInstance().createInterface();
    private static ComponentArtifactDao componentArtifactDao = MonitoringUploadDaoFactory.getInstance().createInterface();
    private static ProcessDao processDao = ProcessDaoFactory.getInstance().createInterface();
    private static OrchestrationTemplateDao orchestrationTemplateDataDao =
        OrchestrationTemplateDaoFactory.getInstance().createInterface();
    private static ComponentDao componentDao =
        ComponentDaoFactory.getInstance().createInterface();
    private static ServiceModelDao serviceModelDao = ServiceModelDaoFactory.getInstance()
        .createInterface();
    private static CompositionEntityDataManager compositionEntityDataManager =
        CompositionEntityDataManagerFactory.getInstance().createInterface();
    private static CompositionDataExtractor compositionDataExtractor =
        CompositionDataExtractorFactory.getInstance().createInterface();

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

    public static void backupComponentsQuestionnaireBeforeDelete(String vspId, Version activeVersion,
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
        componentsCompositionAndQuestionnaire.forEach(componentEntity -> {
            String componentName = componentEntity.getComponentCompositionData().getName();
            componentsQustanniare.put(componentName, componentEntity
                .getQuestionnaireData());
            Collection<NicEntity>
                nics = nicDao.list(new NicEntity(vspId, activeVersion, componentEntity.getId(), null));
            //backup mib
            Collection<ComponentMonitoringUploadEntity> componentMib =
                componentArtifactDao.listArtifacts(new
                    ComponentMonitoringUploadEntity(vspId, activeVersion, componentEntity.getId(),
                    null));
            if (CollectionUtils.isNotEmpty(componentMib)) {
                componentMibList.put(componentName,componentMib);
            }

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
        });
    }

    private static void backupProcess(String vspId, Version activeVersion, String componentId,
                               String componentName, Map<String,
        Collection<ProcessEntity>> processes,
                               Map<String, ProcessEntity> processArtifact) {
        Collection<ProcessEntity> processList = vendorSoftwareProductDao.listProcesses(vspId,
            activeVersion, componentId);
        if (!processList.isEmpty()) {
            processes.put(componentName, processList);
            processList.forEach(process -> {
                //ProcessArtifactEntity artifact = vendorSoftwareProductDao.getProcessArtifact(vspId,
                //    activeVersion, componentId, process.getId());
                ProcessEntity artifact =
                    processDao.get(new ProcessEntity(vspId, activeVersion, componentId, process.getId()));
                if (artifact.getArtifact() != null) {
                    processArtifact.put(process.getId(), artifact);
                }
            });
        }
    }

    public static void retainComponentQuestionnaireData(String vspId, Version activeVersion,
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
                componentDao.updateQuestionnaireData(vspId, activeVersion,
                    componentEntity.getId(),
                    componentsQustanniare.get(componentEntity.getComponentCompositionData()
                        .getName()));
                if (componentNicsQustanniare.containsKey(componentName)) {
                    Map<String, String> nicsQustanniare = componentNicsQustanniare.get(componentName);
                    Collection<NicEntity>
                        nics =
                        nicDao.list(new NicEntity(vspId, activeVersion, componentEntity.getId(), null));
                    nics.forEach(nicEntity -> {
                        if (nicsQustanniare.containsKey(nicEntity.getNicCompositionData().getName())) {
                            nicDao.updateQuestionnaireData(vspId, activeVersion,
                                componentEntity.getId(), nicEntity.getId(),
                                nicsQustanniare.get(nicEntity.getNicCompositionData().getName()));
                        }
                    });
                }
                //MIB //todo add for VES_EVENTS
                if (componentMibList.containsKey(componentName)) {
                    Collection<ComponentMonitoringUploadEntity> mibList =
                        componentMibList.get(componentName);
                    mibList.forEach(mib -> {
                        mib.setComponentId(componentEntity.getId());
                        componentArtifactDao.create(mib);
                    });
                }
                //VFC processes
                restoreProcess(vspId, activeVersion, componentEntity.getId(), componentName, processes,
                    processArtifact);
            }
        });
    }

    private static void restoreProcess(String vspId, Version activeVersion, String componentId,
                                String componentName,
                                Map<String, Collection<ProcessEntity>> processes,
                                Map<String, ProcessEntity> processArtifact) {
        if (processes.containsKey(componentName)) {
            Collection<ProcessEntity> processList = processes.get(componentName);
            processList.forEach(process -> {
                //Reatin VFC process
                if (!GENERAL_COMPONENT_ID.equals(componentId) && processArtifact.containsKey(process.getId
                    ())) {
                    ProcessEntity artifact = processArtifact.get(process.getId());
                    artifact.setComponentId(componentId);
                    UniqueValueUtil.createUniqueValue(PROCESS_NAME, vspId, activeVersion.toString(),
                        componentId, process.getName());
                    vendorSoftwareProductDao.createProcess(artifact);
                }
            });
        }
    }

    public static void deleteUploadDataAndContent(String vspId, Version version) {
        //fixme change this when more tables are zusammenized
        vendorSoftwareProductDao.deleteUploadData(vspId, version);
    }

    public static void saveUploadData(String vspId, Version activeVersion, InputStream uploadedFileData,
                                      FileContentHandler fileContentMap, HeatStructureTree tree) {
        Map<String, Object> manifestAsMap =
            fileContentMap.containsFile(SdcCommon.MANIFEST_NAME) ?
            (Map<String, Object>) JsonUtil.json2Object(fileContentMap.getFileContent(
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

    public static void saveServiceModel(String vspId,
                                        Version version,
                                        ToscaServiceModel serviceModelToExtract,
                                        ToscaServiceModel serviceModelToStore){
        if (serviceModelToExtract != null) {
            serviceModelDao.storeServiceModel(vspId, version, serviceModelToStore);
            //Extracting the compostion data from the output service model of the first phase of
            // translation
            compositionEntityDataManager.saveCompositionData(vspId, version,
                compositionDataExtractor.extractServiceCompositionData(serviceModelToExtract));
//            OrchestrationUtil.retainComponentQuestionnaireData(vspId, version, componentsQuestionnaire,
//                componentNicsQuestionnaire, componentMibList, processes, processArtifact);
        }
    }

    public static HeatStructureTree createHeatTree(FileContentHandler fileContentMap,
                                                   Map<String, List<ErrorMessage>> validationErrors){
        HeatTreeManager heatTreeManager = HeatTreeManagerUtil.initHeatTreeManager(fileContentMap);
        heatTreeManager.createTree();
        heatTreeManager.addErrors(validationErrors);
        return heatTreeManager.getTree();
    }

}
