package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.process;

import static org.openecomp.sdc.logging.messages.AuditMessages.HEAT_VALIDATION_ERROR;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activityLog.ActivityLogManagerFactory;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.messages.AuditMessages;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.validation.util.ValidationManagerUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CandidateServiceFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionDataExtractorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.vendorsoftwareproduct.utils.VendorSoftwareProductUtils;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.activitylog.types.ActivityType;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrchestrationTemplateProcessZipHandler implements OrchestrationTemplateProcessHandler {
  Logger logger = LoggerFactory.getLogger(OrchestrationTemplateProcessZipHandler.class);
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private CandidateService candidateService =
      CandidateServiceFactory.getInstance().createInterface();
  private ServiceModelDao serviceModelDao = ServiceModelDaoFactory.getInstance().createInterface();
  private CompositionEntityDataManager compositionEntityDataManager =
      CompositionEntityDataManagerFactory.getInstance().createInterface();
  private CompositionDataExtractor compositionDataExtractor =
      CompositionDataExtractorFactory.getInstance().createInterface();
  private ActivityLogManager activityLogManager =
      ActivityLogManagerFactory.getInstance().createInterface();


  public OrchestrationTemplateProcessZipHandler(){}

  public OrchestrationTemplateActionResponse process(VspDetails vspDetails,
                                                     OrchestrationTemplateCandidateData
                                                         candidateData,
                                                     String user) {
    String vspId = vspDetails.getId();
    Version version = vspDetails.getVersion();
    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.HEAT_VALIDATION_STARTED
        + vspId);
    OrchestrationTemplateActionResponse response = new OrchestrationTemplateActionResponse();
    UploadFileResponse uploadFileResponse = new UploadFileResponse();
    Optional<FileContentHandler> fileContent =
        OrchestrationUtil
            .getFileContentMap(
                OnboardingTypesEnum.ZIP, uploadFileResponse,
                candidateData.getContentData().array());
    if (!fileContent.isPresent()) {
      response.addStructureErrors(uploadFileResponse.getErrors());
      mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
      response.getErrors().values().forEach(errorList -> printAuditForErrors(errorList,vspId,
          HEAT_VALIDATION_ERROR));
      return response;
    }

    Map<String, List<ErrorMessage>> uploadErrors = uploadFileResponse.getErrors();
    FileContentHandler fileContentMap = fileContent.get();
    FilesDataStructure structure =
        JsonUtil.json2Object(candidateData.getFilesDataStructure(), FilesDataStructure.class);

    if (CollectionUtils.isNotEmpty(structure.getUnassigned())) {
      response.addErrorMessageToMap(SdcCommon.UPLOAD_FILE,
          Messages.FOUND_UNASSIGNED_FILES.getErrorMessage(), ErrorLevel.ERROR);

      mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
      response.getErrors().values().forEach(errorList -> printAuditForErrors(errorList,vspId,
          HEAT_VALIDATION_ERROR));
      return response;
    }


    String manifest = candidateService.createManifest(vspDetails, structure);
    fileContentMap.addFile(SdcCommon.MANIFEST_NAME, manifest.getBytes());

    Optional<ByteArrayInputStream> zipByteArrayInputStream = candidateService
        .fetchZipFileByteArrayInputStream(
            vspId, candidateData, manifest, OnboardingTypesEnum.ZIP, uploadErrors);
    if (!zipByteArrayInputStream.isPresent()) {
      response.getErrors().values().forEach(errorList -> printAuditForErrors(errorList,vspId,
          HEAT_VALIDATION_ERROR));
      return response;
    }

    HeatStructureTree tree = createAndValidateHeatTree(response, fileContentMap);

    Map<String, String> componentsQuestionnaire = new HashMap<>();
    Map<String, Map<String, String>> componentNicsQuestionnaire = new HashMap<>();
    Map<String, Collection<ComponentMonitoringUploadEntity>> componentMibList = new HashMap<>();
    Map<String, Collection<ProcessEntity>> processes = new HashMap<>();
    Map<String, ProcessEntity> processArtifact = new HashMap<>();

    OrchestrationUtil orchestrationUtil = new OrchestrationUtil();
    Map<String, String> vspComponentIdNameInfoBeforeProcess =
        orchestrationUtil.getVspComponentIdNameInfo(vspId, version);
    orchestrationUtil.backupComponentsQuestionnaireBeforeDelete(vspId,
        version, componentsQuestionnaire,
        componentNicsQuestionnaire, componentMibList, processes, processArtifact);

    orchestrationUtil.deleteUploadDataAndContent(vspId, version);
    orchestrationUtil.saveUploadData(vspId, version, zipByteArrayInputStream.get(), fileContentMap,
        tree);

    response.getErrors().values().forEach(errorList -> printAuditForErrors(errorList,vspId,
        HEAT_VALIDATION_ERROR));
    if ( MapUtils.isEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR,
        response.getErrors()))) {
      logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.HEAT_VALIDATION_COMPLETED + vspId);
    }

    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.HEAT_TRANSLATION_STARTED + vspId);

    TranslatorOutput translatorOutput =
        HeatToToscaUtil.loadAndTranslateTemplateData(fileContentMap);

    ToscaServiceModel toscaServiceModel = translatorOutput.getToscaServiceModel();
    orchestrationUtil.saveServiceModel(vspId, version, translatorOutput
        .getNonUnifiedToscaServiceModel(), toscaServiceModel);
    orchestrationUtil.retainComponentQuestionnaireData(vspId, version, componentsQuestionnaire,
        componentNicsQuestionnaire, componentMibList, processes, processArtifact);
    orchestrationUtil.updateVspComponentDependencies(vspId, version,
        vspComponentIdNameInfoBeforeProcess);
    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.HEAT_TRANSLATION_COMPLETED + vspId);
    uploadFileResponse.addStructureErrors(uploadErrors);

    ActivityLogEntity activityLogEntity =
        new ActivityLogEntity(vspId, String.valueOf(version.getMajor() + 1),
            ActivityType.UPLOAD_HEAT.toString(), user, true, "", "");
    activityLogManager.addActionLog(activityLogEntity, user);

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
    return response;
  }

  private HeatStructureTree createAndValidateHeatTree(OrchestrationTemplateActionResponse response,
                                                      FileContentHandler fileContentMap) {
    VendorSoftwareProductUtils.addFileNamesToUploadFileResponse(fileContentMap, response);
    Map<String, List<ErrorMessage>> validationErrors =
        ValidationManagerUtil.initValidationManager(fileContentMap).validate();
    response.getErrors().putAll(validationErrors);

    return OrchestrationUtil.createHeatTree(fileContentMap, validationErrors);
  }

  private void printAuditForErrors(List<ErrorMessage> errorList, String vspId, String auditType) {

    errorList.forEach(errorMessage -> {
      if (errorMessage.getLevel().equals(ErrorLevel.ERROR)) {
        logger.audit(AuditMessages.AUDIT_MSG + String.format(auditType, errorMessage.getMessage(),
            vspId));
      }
    });
  }
}
