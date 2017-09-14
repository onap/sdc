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

package org.openecomp.sdc.healing.healers;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CandidateServiceFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.services.utils.CandidateEntityBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FileDataStructureHealer implements Healer {
  private static final OrchestrationTemplateDao orchestrationTemplateDataDao =
      OrchestrationTemplateDaoFactory.getInstance().createInterface();
  private static CandidateService candidateService =
      CandidateServiceFactory.getInstance().createInterface();
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  public FileDataStructureHealer() {
  }

  @Override
  public Optional<FilesDataStructure> heal(Map<String, Object> healingParams) throws Exception {


    mdcDataDebugMessage.debugEntryMessage(null);

    Optional<FilesDataStructure> healingResult = Optional.empty();
    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = (Version) healingParams.get(SdcCommon.VERSION);
    String user = (String) healingParams.get(SdcCommon.USER);

    UploadDataEntity uploadData =
        orchestrationTemplateDataDao.getOrchestrationTemplate(vspId,version);
    if (uploadData == null || uploadData.getContentData() == null) {
      FilesDataStructure emptyFilesDataStructure = new FilesDataStructure();
      return Optional.of(emptyFilesDataStructure);
    }

    Optional<FilesDataStructure> candidateFileDataStructure =
        candidateService.getOrchestrationTemplateCandidateFileDataStructure(vspId, version);

    if (!candidateFileDataStructure.isPresent()) {
      healingResult = healFilesDataStructure(vspId, version, user, uploadData);
    }

    mdcDataDebugMessage.debugExitMessage(null);
    return healingResult;
  }

  private Optional<FilesDataStructure> healFilesDataStructure(String vspId, Version version,
                                                              String user,
                                                              UploadDataEntity uploadData)
      throws Exception {


    mdcDataDebugMessage.debugEntryMessage(null);

    Optional<FilesDataStructure> healingResult;
    byte[] byteContentData = uploadData.getContentData().array();
    FileContentHandler fileContentHandler;
    try{
      fileContentHandler = CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.ZIP, byteContentData);
      Map<String, List<ErrorMessage>> errors = new HashMap<>();
      OrchestrationTemplateCandidateData candidateDataEntity =
        new CandidateEntityBuilder(candidateService)
            .buildCandidateEntityFromZip(new VspDetails(vspId, version), byteContentData,
                fileContentHandler, errors, user);

      healingResult = getFileDataStructureFromJson(candidateDataEntity.getFilesDataStructure());
    }catch (Exception e){
      log.debug("", e);
      return Optional.empty();
    }

    mdcDataDebugMessage.debugExitMessage(null);
    return healingResult;
  }

  private Optional<FilesDataStructure> getFileDataStructureFromJson(String fileDataStructureJson) {
    return Optional.of(JsonUtil.json2Object(fileDataStructureJson, FilesDataStructure.class));
  }
}
