/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.OrchestrationTemplateCandidateDaoZusammenImpl.InfoPropertyName.ORIGINAL_FILE_CONTENT;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.OrchestrationTemplateCandidateDaoZusammenImpl.InfoPropertyName.ORIGINAL_FILE_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.OrchestrationTemplateCandidateDaoZusammenImpl.InfoPropertyName.ORIGINAL_FILE_SUFFIX;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
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
                                        UploadFileResponse uploadFileResponse,
                                        Map<String, Object> originalFileToUploadDetails) {
    try {
      OrchestrationTemplateCandidateData candidateData =
          new CandidateEntityBuilder(candidateService)
              .buildCandidateEntityFromZip(vspDetails, uploadedFileData, contentMap,
                  uploadFileResponse.getErrors());
      candidateData.setFileSuffix(fileSuffix);
      candidateData.setFileName(networkPackageName);
      candidateData.setOriginalFileName((String) originalFileToUploadDetails.get(ORIGINAL_FILE_NAME.getVal()));
      candidateData.setOriginalFileSuffix((String) originalFileToUploadDetails.get(ORIGINAL_FILE_SUFFIX.getVal()));
      candidateData.setOriginalFileContentData(ByteBuffer.wrap((byte[]) originalFileToUploadDetails.get(ORIGINAL_FILE_CONTENT.getVal())));

      candidateService
          .updateCandidateUploadData(vspDetails.getId(), vspDetails.getVersion(), candidateData);
    } catch (Exception exception) {
      logger.error(getErrorWithParameters(Messages.FILE_CONTENT_MAP.getErrorMessage(),
          getHandlerType().toString()), exception);
      uploadFileResponse.addStructureError(SdcCommon.UPLOAD_FILE,
          new ErrorMessage(ErrorLevel.ERROR, exception.getMessage()));
      return true;
    }
    return false;
  }

  @Override
  protected OnboardingTypesEnum getHandlerType() {
    return OnboardingTypesEnum.ZIP;
  }
}
