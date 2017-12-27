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

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;

import java.io.InputStream;
import java.util.Optional;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;

public abstract class BaseOrchestrationTemplateHandler implements OrchestrationTemplateFileHandler {
  protected static final Logger LOGGER =
      LoggerFactory.getLogger(BaseOrchestrationTemplateHandler.class);
  protected static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();
  private static final String VSP_ID = "VSP id";

  @Override
  public UploadFileResponse upload(VspDetails vspDetails, InputStream fileToUpload,
                                   String fileSuffix, String networkPackageName,
                                   CandidateService candidateService) {
    UploadFileResponse uploadFileResponse = new UploadFileResponse();
    uploadFileResponse.setOnboardingType(getHandlerType());
    if (isNotEmptyFileToUpload(fileToUpload, uploadFileResponse, candidateService)) {
      return uploadFileResponse;
    }

    byte[] uploadedFileData = FileUtils.toByteArray(fileToUpload);
    if (isInvalidRawZipData(uploadFileResponse, uploadedFileData, candidateService)) {
      return uploadFileResponse;
    }

    Optional<FileContentHandler> optionalContentMap =
        getFileContentMap(uploadFileResponse, uploadedFileData);
    if (validateOptionalContentMap(vspDetails, uploadFileResponse, optionalContentMap))
      return uploadFileResponse;

    if (optionalContentMap.isPresent()
      && updateCandidateData(vspDetails, uploadedFileData, optionalContentMap.get(),
            fileSuffix,
              networkPackageName, candidateService, uploadFileResponse)) {
        return uploadFileResponse;
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(VSP_ID, vspDetails.getId());
    return uploadFileResponse;

  }

  private boolean validateOptionalContentMap(VspDetails vspDetails,
                                             UploadFileResponse uploadFileResponse,
                                             Optional<FileContentHandler> optionalContentMap) {
    if (!optionalContentMap.isPresent()) {
      LOGGER.error(getErrorWithParameters(Messages.FILE_CONTENT_MAP.getErrorMessage(),
          getHandlerType().toString()));
      uploadFileResponse.addStructureError(SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR,
          getErrorWithParameters(Messages.FILE_CONTENT_MAP.getErrorMessage(),
              getHandlerType().toString())));

      MDC_DATA_DEBUG_MESSAGE.debugExitMessage(VSP_ID, vspDetails.getId());
      return true;
    }

    if (!MapUtils.isEmpty(uploadFileResponse.getErrors())) {
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage(VSP_ID, vspDetails.getId());
      return true;
    }
    return false;
  }

  protected abstract boolean updateCandidateData(VspDetails vspDetails,
                                                 byte[] uploadedFileData,
                                                 FileContentHandler contentMap,
                                                 String fileSuffix,
                                                 String networkPackageName,
                                                 CandidateService candidateService,
                                                 UploadFileResponse uploadFileResponse);

  private boolean isNotEmptyFileToUpload(InputStream fileToUpload,
                                         UploadFileResponse uploadFileResponse,
                                         CandidateService candidateService) {
    Optional<ErrorMessage> errorMessage =
        candidateService.validateNonEmptyFileToUpload(fileToUpload);
    if (errorMessage.isPresent()) {
      uploadFileResponse.addStructureError(SdcCommon.UPLOAD_FILE, errorMessage.get());
      return true;
    }
    return false;
  }

  protected boolean isInvalidRawZipData(UploadFileResponse uploadFileResponse,
                                        byte[] uploadedFileData,
                                        CandidateService candidateService) {
    Optional<ErrorMessage> errorMessage;
    errorMessage = candidateService.validateRawZipData(uploadedFileData);
    if (errorMessage.isPresent()) {
      uploadFileResponse.addStructureError(SdcCommon.UPLOAD_FILE, errorMessage.get());
      return true;
    }
    return false;
  }

  public abstract Optional<FileContentHandler> getFileContentMap(
      UploadFileResponse uploadFileResponse,
      byte[] uploadedFileData);

  protected abstract OnboardingTypesEnum getHandlerType();
}
