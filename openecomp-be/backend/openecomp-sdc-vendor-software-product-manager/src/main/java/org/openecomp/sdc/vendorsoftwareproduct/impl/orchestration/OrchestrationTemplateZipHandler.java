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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.services.utils.CandidateEntityBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;

import java.util.Optional;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;

public class OrchestrationTemplateZipHandler extends BaseOrchestrationTemplateHandler
    implements OrchestrationTemplateFileHandler {

  @Override
  public Optional<FileContentHandler> getFileContentMap(UploadFileResponse uploadFileResponse,
                                                        byte[] uploadedFileData) {
    return OrchestrationUtil
        .getFileContentMap(OnboardingTypesEnum.ZIP, uploadFileResponse, uploadedFileData);
  }

  @Override
  protected boolean updateCandidateData(VspDetails vspDetails, byte[] uploadedFileData,
                                        FileContentHandler contentMap,
                                        String fileSuffix, String networkPackageName,
                                        CandidateService candidateService,
                                        UploadFileResponse uploadFileResponse) {
    try {
      OrchestrationTemplateCandidateData candidateData =
          new CandidateEntityBuilder(candidateService)
              .buildCandidateEntityFromZip(vspDetails, uploadedFileData, contentMap,
                  uploadFileResponse.getErrors());
      candidateData.setFileSuffix(fileSuffix);
      candidateData.setFileName(networkPackageName);

      candidateService
          .updateCandidateUploadData(vspDetails.getId(), vspDetails.getVersion(), candidateData);
    } catch (Exception exception) {
      LOGGER.error(getErrorWithParameters(Messages.FILE_CONTENT_MAP.getErrorMessage(),
          getHandlerType().toString()), exception);
      uploadFileResponse.addStructureError(SdcCommon.UPLOAD_FILE,
          new ErrorMessage(ErrorLevel.ERROR, exception.getMessage()));

      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspDetails.getId());
      return true;
    }
    return false;
  }

  @Override
  protected OnboardingTypesEnum getHandlerType() {
    return OnboardingTypesEnum.ZIP;
  }
}
