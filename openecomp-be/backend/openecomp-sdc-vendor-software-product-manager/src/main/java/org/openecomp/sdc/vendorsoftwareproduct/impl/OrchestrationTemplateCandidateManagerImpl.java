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

package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.healing.types.HealCode;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.annotations.Metrics;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.messages.AuditMessages;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.OrchestrationTemplateNotFoundErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationTemplateFileHandler;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUploadFactory;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.process.OrchestrationProcessFactory;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.process.OrchestrationTemplateProcessHandler;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrchestrationTemplateCandidateManagerImpl
    implements OrchestrationTemplateCandidateManager {
  private static final Logger logger =
      LoggerFactory.getLogger(OrchestrationTemplateCandidateManagerImpl.class);
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private VendorSoftwareProductDao vendorSoftwareProductDao;
  private VendorSoftwareProductInfoDao vspInfoDao;
  private OrchestrationTemplateDao orchestrationTemplateDataDao;
  private CandidateService candidateService;
  private HealingManager healingManager;
  private CompositionDataExtractor compositionDataExtractor;
  private ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao;
  private CompositionEntityDataManager compositionEntityDataManager;
  private NicDao nicDao;
  private ComponentDao componentDao;
  private ComponentArtifactDao componentArtifactDao;
  private ActivityLogManager activityLogManager;
  private ProcessDao processDao;

  /**
   * Instantiates a new Orchestration template candidate manager.
   *
   * @param vendorSoftwareProductDao     the vendor software product dao
   * @param vspInfoDao                   the vsp info dao
   * @param orchestrationTemplateDataDao the orchestration template data dao
   * @param candidateService             the candidate service
   * @param healingManager               the healing manager
   * @param compositionDataExtractor     the composition data extractor
   * @param serviceModelDao              the service model dao
   * @param compositionEntityDataManager the composition entity data manager
   * @param nicDao                       the nic dao
   * @param componentDao                 the component dao
   * @param componentArtifactDao                       the mib dao
   * @param processDao                   the process dao
   * @param activityLogManager           the activity log manager
   */
  public OrchestrationTemplateCandidateManagerImpl(
      VendorSoftwareProductDao vendorSoftwareProductDao, VendorSoftwareProductInfoDao
      vspInfoDao,
      OrchestrationTemplateDao orchestrationTemplateDataDao,
      CandidateService candidateService, HealingManager healingManager,
      CompositionDataExtractor compositionDataExtractor,
      ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao,
      CompositionEntityDataManager compositionEntityDataManager,
      NicDao nicDao,
      ComponentDao componentDao,
      ComponentArtifactDao componentArtifactDao,
      ProcessDao processDao,
      ActivityLogManager activityLogManager) {
    this.vendorSoftwareProductDao = vendorSoftwareProductDao;
    this.vspInfoDao = vspInfoDao;
    this.orchestrationTemplateDataDao = orchestrationTemplateDataDao;
    this.candidateService = candidateService;
    this.healingManager = healingManager;
    this.compositionDataExtractor = compositionDataExtractor;
    this.serviceModelDao = serviceModelDao;
    this.compositionEntityDataManager = compositionEntityDataManager;
    this.nicDao = nicDao;
    this.componentDao = componentDao;
    this.componentArtifactDao = componentArtifactDao;
    this.processDao = processDao;
    this.activityLogManager = activityLogManager;
  }

  @Override
  @Metrics
  public UploadFileResponse upload(String vspId, Version version, InputStream fileToUpload,
                                   String user, String filePrefix,
                                   String networkPackageName) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);
    OrchestrationTemplateFileHandler orchestrationTemplateFileHandler =
            OrchestrationUploadFactory.createOrchestrationTemplateFileHandler(filePrefix);
    VspDetails vspDetails = getVspDetails(vspId, version);
    UploadFileResponse uploadResponse = orchestrationTemplateFileHandler
        .upload(vspId, version, fileToUpload, user, candidateService,
            vspDetails);
    vspDetails.setOnboardingOrigin(filePrefix);
    vspDetails.setNetworkPackageName(networkPackageName);
    vspInfoDao.update(vspDetails);

    uploadResponse.setNetworkPackageName(networkPackageName);
    return uploadResponse;
  }



  @Override
  public OrchestrationTemplateActionResponse process(String vspId,
                                                     Version version, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);
    OrchestrationTemplateActionResponse response = new OrchestrationTemplateActionResponse();

    Optional<OrchestrationTemplateCandidateData> candidate =
        fetchCandidateDataEntity(vspId, version);
    if (!candidate.isPresent()) {
      throw new CoreException(new OrchestrationTemplateNotFoundErrorBuilder(vspId).build());
    }

    VspDetails vspDetails =
        getVspDetails(vspId, version);
    Optional<OrchestrationTemplateProcessHandler> processInstance =
        OrchestrationProcessFactory.getInstance(vspDetails.getOnboardingOrigin());

    if(processInstance.isPresent()){
      response = processInstance.get().process(vspDetails, candidate.get(), user);
    }

    return response;
  }

  @Override
  public Optional<FilesDataStructure> getFilesDataStructure(
      String vspId, Version version, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

    Optional<FilesDataStructure> candidateFileDataStructure =
        candidateService.getOrchestrationTemplateCandidateFileDataStructure(vspId, version);
    if (candidateFileDataStructure.isPresent()) {
      return candidateFileDataStructure;
    } else {
      Map<String, Object> healingParams = getHealingParamsAsMap(vspId, version, user);

      mdcDataDebugMessage
          .debugExitMessage("VSP id", vspId);
      return (Optional<FilesDataStructure>) healingManager
          .heal(HealCode.FILE_DATA_STRUCTURE_HEALER, healingParams);
    }
  }

  @Override

  public ValidationResponse updateFilesDataStructure(String vspId,
                                                     Version version, String user,
                                                     FilesDataStructure fileDataStructure) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

    ValidationResponse response = new ValidationResponse();
    Optional<List<ErrorMessage>> validateErrors =
        candidateService.validateFileDataStructure(fileDataStructure);
    if (validateErrors.isPresent()) {
      List<ErrorMessage> errorMessages = validateErrors.get();
      if (CollectionUtils.isNotEmpty(errorMessages)) {
        Map<String, List<ErrorMessage>> errorsMap = new HashMap<>();
        errorsMap.put(SdcCommon.UPLOAD_FILE, errorMessages);
        response.setUploadDataErrors(errorsMap, LoggerServiceName.Update_Manifest,
            LoggerTragetServiceName.VALIDATE_FILE_DATA_STRUCTURE);

        mdcDataDebugMessage
            .debugExitMessage("VSP id", vspId);
        return response;
      }
    }
    candidateService.updateOrchestrationTemplateCandidateFileDataStructure(vspId, version,
        fileDataStructure);

    mdcDataDebugMessage
        .debugExitMessage("VSP id", vspId);
    return response;
  }

  @Override

  public Optional<Pair<String, byte[]>> get(String vspId, Version version, String user)
      throws IOException {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

    VspDetails vspDetails =
        getVspDetails(vspId, version);

    Optional<OrchestrationTemplateCandidateData> candidateDataEntity =
        fetchCandidateDataEntity(vspId, version);

    if (!candidateDataEntity.isPresent()) {
      ErrorMessage errorMessage = new ErrorMessage(ErrorLevel.ERROR,
          Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage());
      logger.error(errorMessage.getMessage());

      mdcDataDebugMessage
          .debugExitMessage("VSP id", vspId);
      return Optional.empty();
    }
    OnboardingTypesEnum type =
        OnboardingTypesEnum.getOnboardingTypesEnum(vspDetails.getOnboardingOrigin());

    if(vspDetails.getOnboardingOrigin().equals(OnboardingTypesEnum.ZIP.toString())) {
      FilesDataStructure structure = JsonUtil
          .json2Object(candidateDataEntity.get().getFilesDataStructure(), FilesDataStructure.class);
      String manifest = candidateService.createManifest(vspDetails, structure);

      mdcDataDebugMessage
          .debugExitMessage("VSP id", vspId);
      return Optional.ofNullable(
          new ImmutablePair<>(OnboardingTypesEnum.ZIP.toString(),candidateService
              .replaceManifestInZip(candidateDataEntity.get().getContentData(),
              manifest, vspId, type)));
    }

    return Optional.ofNullable(
        new ImmutablePair<>(vspDetails.getOnboardingOrigin(),candidateDataEntity.get()
            .getContentData().array()));
  }

  private Optional<OrchestrationTemplateCandidateData> fetchCandidateDataEntity(
      String vspId, Version version) {
    return Optional
        .ofNullable(candidateService.getOrchestrationTemplateCandidate(vspId, version));
  }


  // todo *************************** move to reusable place! *************************

  private Map<String, Object> getHealingParamsAsMap(String vspId, Version version, String user) {
    Map<String, Object> healingParams = new HashMap<>();

    healingParams.put(SdcCommon.VSP_ID, vspId);
    healingParams.put(SdcCommon.VERSION, version);
    healingParams.put(SdcCommon.USER, user);

    return healingParams;
  }

  private VspDetails getVspDetails(String vspId, Version version) {
    VspDetails vspDetails = vspInfoDao.get(new VspDetails(vspId, version));
    vspDetails.setValidationData(orchestrationTemplateDataDao.getValidationData(vspId, version));
    return vspDetails;
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
