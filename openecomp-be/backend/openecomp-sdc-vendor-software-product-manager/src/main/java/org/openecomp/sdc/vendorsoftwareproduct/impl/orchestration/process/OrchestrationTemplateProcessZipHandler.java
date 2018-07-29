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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.process;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.validation.util.ValidationManagerUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.*;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CandidateServiceFactory;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.vendorsoftwareproduct.utils.VendorSoftwareProductUtils;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.util.*;

public class OrchestrationTemplateProcessZipHandler implements OrchestrationTemplateProcessHandler {

  private final CandidateService candidateService =
      CandidateServiceFactory.getInstance().createInterface();

  @Override
  public OrchestrationTemplateActionResponse process(VspDetails vspDetails,
                                                     OrchestrationTemplateCandidateData candidateData) {
    String vspId = vspDetails.getId();
    Version version = vspDetails.getVersion();
    OrchestrationTemplateActionResponse response = new OrchestrationTemplateActionResponse();
    UploadFileResponse uploadFileResponse = new UploadFileResponse();
    Optional<FileContentHandler> fileContent = OrchestrationUtil
        .getFileContentMap(OnboardingTypesEnum.ZIP, uploadFileResponse,
            candidateData.getContentData().array());
    if (!fileContent.isPresent()) {
      response.addStructureErrors(uploadFileResponse.getErrors());
      return response;
    }

    Map<String, List<ErrorMessage>> uploadErrors = uploadFileResponse.getErrors();
    FileContentHandler fileContentMap = fileContent.get();
    FilesDataStructure structure =
        JsonUtil.json2Object(candidateData.getFilesDataStructure(), FilesDataStructure.class);

    if (CollectionUtils.isNotEmpty(structure.getUnassigned())) {
      response.addErrorMessageToMap(SdcCommon.UPLOAD_FILE,
          Messages.FOUND_UNASSIGNED_FILES.getErrorMessage(), ErrorLevel.ERROR);
      return response;
    }


    String manifest = candidateService.createManifest(vspDetails, structure);
    fileContentMap.addFile(SdcCommon.MANIFEST_NAME, manifest.getBytes());

    Optional<ByteArrayInputStream> zipByteArrayInputStream = candidateService
        .fetchZipFileByteArrayInputStream(
            vspId, candidateData, manifest, OnboardingTypesEnum.ZIP, uploadErrors);
    if (!zipByteArrayInputStream.isPresent()) {
      return response;
    }

    HeatStructureTree tree = createAndValidateHeatTree(response, fileContentMap);
    Map<String, List<ErrorMessage>> errors = getErrors(response);
    if (MapUtils.isNotEmpty(errors)) {
      response.addStructureErrors(errors);
      candidateService.updateValidationData(vspId, version, new ValidationStructureList(tree));
      return response;
    }
    Map<String, String> componentsQuestionnaire = new HashMap<>();
    Map<String, Map<String, String>> componentNicsQuestionnaire = new HashMap<>();
    Map<String, Collection<ComponentMonitoringUploadEntity>> componentMibList = new HashMap<>();
    Map<String, Collection<ProcessEntity>> processes = new HashMap<>();
    Map<String, ProcessEntity> processArtifact = new HashMap<>();

    OrchestrationUtil orchestrationUtil = new OrchestrationUtil();
    Map<String, String> vspComponentIdNameInfoBeforeProcess =
        orchestrationUtil.getVspComponentIdNameInfo(vspId, version);
    Collection<ComponentDependencyModelEntity> componentDependenciesBeforeDelete =
        orchestrationUtil.getComponentDependenciesBeforeDelete(vspId, version);
    orchestrationUtil
        .backupComponentsQuestionnaireBeforeDelete(vspId, version, componentsQuestionnaire,
            componentNicsQuestionnaire, componentMibList, processes, processArtifact);

    orchestrationUtil.deleteUploadDataAndContent(vspId, version);
    orchestrationUtil
        .saveUploadData(vspDetails, candidateData, zipByteArrayInputStream.get(), fileContentMap,
            tree);

    TranslatorOutput translatorOutput =
        HeatToToscaUtil.loadAndTranslateTemplateData(fileContentMap);

    ToscaServiceModel toscaServiceModel = translatorOutput.getToscaServiceModel();
    orchestrationUtil
        .saveServiceModel(vspId, version, translatorOutput.getNonUnifiedToscaServiceModel(),
            toscaServiceModel);
    orchestrationUtil.retainComponentQuestionnaireData(vspId, version, componentsQuestionnaire,
        componentNicsQuestionnaire, componentMibList, processes, processArtifact);
    orchestrationUtil.updateVspComponentDependencies(vspId, version,
        vspComponentIdNameInfoBeforeProcess, componentDependenciesBeforeDelete);

    uploadFileResponse.addStructureErrors(uploadErrors);
    candidateService.deleteOrchestrationTemplateCandidate(vspId, version);
    return response;
  }

  private Map<String, List<ErrorMessage>> getErrors(OrchestrationTemplateActionResponse
                                                        orchestrationTemplateActionResponse) {
    Map<String, List<ErrorMessage>> errors =
        MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR,
            orchestrationTemplateActionResponse.getErrors());
    return MapUtils.isEmpty(errors) ? null : orchestrationTemplateActionResponse.getErrors();
  }

  private HeatStructureTree createAndValidateHeatTree(OrchestrationTemplateActionResponse response,
                                                      FileContentHandler fileContentMap) {
    VendorSoftwareProductUtils.addFileNamesToUploadFileResponse(fileContentMap, response);
    Map<String, List<ErrorMessage>> validationErrors =
        ValidationManagerUtil.initValidationManager(fileContentMap).validate();
    response.getErrors().putAll(validationErrors);

    return OrchestrationUtil.createHeatTree(fileContentMap, validationErrors);
  }
}
