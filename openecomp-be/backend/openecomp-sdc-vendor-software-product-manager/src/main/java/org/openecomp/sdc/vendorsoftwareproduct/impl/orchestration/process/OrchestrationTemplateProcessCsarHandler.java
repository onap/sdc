package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.process;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.converter.ToscaConverter;
import org.openecomp.core.impl.ToscaConverterImpl;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.services.tree.ToscaTreeManager;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.messages.AuditMessages;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CandidateServiceFactory;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrchestrationTemplateProcessCsarHandler implements OrchestrationTemplateProcessHandler {

  private static Logger logger =
      LoggerFactory.getLogger(OrchestrationTemplateProcessCsarHandler.class);
  private ToscaConverter toscaConverter = new ToscaConverterImpl();
  private CandidateService candidateService =
      CandidateServiceFactory.getInstance().createInterface();
  ToscaTreeManager toscaTreeManager = new ToscaTreeManager();

  @Override
  public OrchestrationTemplateActionResponse process(VspDetails vspDetails,
                                                     OrchestrationTemplateCandidateData candidateData,
                                                     String user) {
    String vspId = vspDetails.getId();
    Version version = vspDetails.getVersion();
    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.CSAR_VALIDATION_STARTED + vspId);
    OrchestrationTemplateActionResponse response = new OrchestrationTemplateActionResponse();
    UploadFileResponse uploadFileResponse = new UploadFileResponse();
    Optional<FileContentHandler> fileContent =
        OrchestrationUtil
            .getFileContentMap(
                OnboardingTypesEnum.CSAR, uploadFileResponse, candidateData.getContentData().array());

    if(fileContent.isPresent()){
      try {
        FileContentHandler fileContentHandler = fileContent.get();
        processCsar(vspId, version, fileContentHandler, candidateData, response);
      } catch (CoreException e){
        logger.error(e.getMessage());
        throw e;
      } catch (Exception e){
        logger.error(e.getMessage());
      }
    }

    return response;
  }

  private void processCsar(String vspId, Version version,
                           FileContentHandler fileContentHandler,
                           OrchestrationTemplateCandidateData candidateData,
                           OrchestrationTemplateActionResponse response) throws IOException {


    response.setFileNames(new ArrayList<>(fileContentHandler.getFileList()));
    Map<String, List<ErrorMessage>> errors = validateCsar(fileContentHandler, response);
    if(!isValid(errors)){
      return;
    }

    HeatStructureTree tree = toscaTreeManager.getTree();

    Map<String, String> componentsQuestionnaire = new HashMap<>();
    Map<String, Map<String, String>> componentNicsQuestionnaire = new HashMap<>();
    Map<String, Collection<ComponentMonitoringUploadEntity>> componentMibList = new HashMap<>();
    Map<String, Collection<ProcessEntity>> processes = new HashMap<>();
    Map<String, ProcessEntity> processArtifact = new HashMap<>();

    OrchestrationUtil.backupComponentsQuestionnaireBeforeDelete(vspId,
        version, componentsQuestionnaire,
        componentNicsQuestionnaire, componentMibList, processes, processArtifact);

    Optional<ByteArrayInputStream> zipByteArrayInputStream = candidateService
        .fetchZipFileByteArrayInputStream(vspId, candidateData, null, OnboardingTypesEnum.CSAR, errors);

    OrchestrationUtil.deleteUploadDataAndContent(vspId, version);
    OrchestrationUtil.saveUploadData(
        vspId, version, zipByteArrayInputStream.get(), fileContentHandler, tree);

    ToscaServiceModel toscaServiceModel = toscaConverter.convert(fileContentHandler);
    OrchestrationUtil.saveServiceModel(vspId, version, toscaServiceModel, toscaServiceModel);

  }

  private void addFiles(FileContentHandler fileContentHandler){
    for(Map.Entry<String, byte[]> fileEntry : fileContentHandler.getFiles().entrySet()){
      toscaTreeManager.addFile(fileEntry.getKey(), fileEntry.getValue());
    }
  }

  private Map<String, List<ErrorMessage>> validateCsar(FileContentHandler fileContentHandler,
                                         OrchestrationTemplateActionResponse response){


    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    addFiles(fileContentHandler);
    toscaTreeManager.createTree();
    toscaTreeManager.addErrors(errors);
    //todo - add tosca validation here to the existing validation framework
    return errors;
  }

  private boolean isValid(Map<String, List<ErrorMessage>> errors){
    return MapUtils.isEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, errors));
  }
}
