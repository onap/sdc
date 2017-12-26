/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.healing.healers;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CandidateServiceFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.utils.CandidateEntityBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.HashMap;
import java.util.Optional;

public class FileDataStructureHealer implements Healer {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  @Override
  public Optional<FilesDataStructure> heal(String vspId,
                                           Version version) throws Exception {
    mdcDataDebugMessage.debugEntryMessage(null);

    OrchestrationTemplateCandidateDao candidateDao =
        OrchestrationTemplateCandidateDaoFactory.getInstance().createInterface();

    OrchestrationTemplateCandidateData candidateData = candidateDao.get(vspId, version);

    if (candidateData == null || candidateData.getContentData() == null ||
        candidateData.getFilesDataStructure() != null) {
      return Optional.of(new FilesDataStructure());
    }

    Optional<FilesDataStructure> filesDataStructure =
        healFilesDataStructure(vspId, version, candidateData);

    filesDataStructure
        .ifPresent(structure -> candidateDao.updateStructure(vspId, version, structure));

    mdcDataDebugMessage.debugExitMessage(null);
    return filesDataStructure;
  }

  private Optional<FilesDataStructure> healFilesDataStructure(
      String vspId, Version version, OrchestrationTemplateCandidateData candidateData)
      throws Exception {
    mdcDataDebugMessage.debugEntryMessage(null);

    Optional<FilesDataStructure> healingResult;
    byte[] byteContentData = candidateData.getContentData().array();
    FileContentHandler fileContentHandler;
    try {
      fileContentHandler =
          CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.ZIP, byteContentData);

      String filesDataStructure =
          new CandidateEntityBuilder(CandidateServiceFactory.getInstance().createInterface())
              .buildCandidateEntityFromZip(new VspDetails(vspId, version), byteContentData,
                  fileContentHandler, new HashMap<>()).getFilesDataStructure();

      healingResult =
          Optional.of(JsonUtil.json2Object(filesDataStructure, FilesDataStructure.class));
    } catch (Exception e) {
      healingResult = Optional.empty();
    }

    mdcDataDebugMessage.debugExitMessage(null);
    return healingResult;
  }
}
