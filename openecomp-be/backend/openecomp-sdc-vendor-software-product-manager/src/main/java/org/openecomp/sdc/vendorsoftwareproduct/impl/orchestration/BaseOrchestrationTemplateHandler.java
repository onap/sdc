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

import java.io.ByteArrayInputStream;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackage;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;

public abstract class BaseOrchestrationTemplateHandler implements OrchestrationTemplateFileHandler {
  protected static final Logger logger = LoggerFactory.getLogger(BaseOrchestrationTemplateHandler.class);

  @Override
  public UploadFileResponse upload(final VspDetails vspDetails,
                                   final OnboardPackageInfo onboardPackageInfo,
                                   final CandidateService candidateService) {
    final OnboardPackage onboardPackage = onboardPackageInfo.getOnboardPackage();
    final UploadFileResponse uploadFileResponse = new UploadFileResponse();
    uploadFileResponse.setOnboardingType(getHandlerType());
    if (isFileFileToUploadEmpty(onboardPackage, uploadFileResponse, candidateService)) {
      return uploadFileResponse;
    }

    final byte[] fileContentByteArray = onboardPackage.getFileContent().array();
    if (isInvalidRawZipData(onboardPackage.getFileExtension(),
        uploadFileResponse, fileContentByteArray, candidateService)) {
      return uploadFileResponse;
    }

    final Optional<FileContentHandler> optionalContentMap =
        getFileContentMap(uploadFileResponse, fileContentByteArray);
    if (!optionalContentMap.isPresent()) {
      logger.error(getErrorWithParameters(Messages.FILE_CONTENT_MAP.getErrorMessage(),
          getHandlerType().toString()));
      uploadFileResponse.addStructureError(SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR,
          getErrorWithParameters(Messages.FILE_CONTENT_MAP.getErrorMessage(),
              getHandlerType().toString())));
      return uploadFileResponse;
    }

    if (!MapUtils.isEmpty(uploadFileResponse.getErrors())) {
      return uploadFileResponse;
    }
    if (updateCandidateData(vspDetails, onboardPackageInfo, candidateService, uploadFileResponse,
        optionalContentMap.get())) {
      return uploadFileResponse;
    }
    return uploadFileResponse;

  }

  protected abstract boolean updateCandidateData(final VspDetails vspDetails,
                                                 final OnboardPackageInfo onboardPackageInfo,
                                                 final CandidateService candidateService,
                                                 final UploadFileResponse uploadFileResponse,
                                                 final FileContentHandler contentMap);

  private boolean isFileFileToUploadEmpty(final OnboardPackage onboardPackage,
                                          final UploadFileResponse uploadFileResponse,
                                          final CandidateService candidateService) {
    final ByteArrayInputStream fileToUpload = new ByteArrayInputStream(
        onboardPackage.getFileContent().array());
    Optional<ErrorMessage> errorMessage =
        candidateService.validateNonEmptyFileToUpload(fileToUpload, onboardPackage.getFileExtension());
    if (errorMessage.isPresent()) {
      uploadFileResponse.addStructureError(SdcCommon.UPLOAD_FILE, errorMessage.get());
      return true;
    }
    return false;
  }

  protected boolean isInvalidRawZipData(String fileSuffix,
                                        UploadFileResponse uploadFileResponse,
                                        byte[] uploadedFileData,
                                        CandidateService candidateService) {
    Optional<ErrorMessage> errorMessage;
    errorMessage = candidateService.validateRawZipData(fileSuffix, uploadedFileData);
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
