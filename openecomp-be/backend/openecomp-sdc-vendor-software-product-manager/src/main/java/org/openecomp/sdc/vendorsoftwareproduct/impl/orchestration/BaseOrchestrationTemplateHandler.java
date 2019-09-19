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

import java.io.ByteArrayInputStream;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.utils.SdcCommon;
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

    final UploadFileResponse validateResponse = validate(onboardPackageInfo);

    if (!MapUtils.isEmpty(validateResponse.getErrors())) {
      uploadFileResponse.addStructureErrors(validateResponse.getErrors());
      return uploadFileResponse;
    }

    final UploadFileResponse responseFromUpdate = updateCandidateData(vspDetails, onboardPackageInfo,
        candidateService);
    if (!MapUtils.isEmpty(responseFromUpdate.getErrors())) {
      uploadFileResponse.addStructureErrors(responseFromUpdate.getErrors());
    }

    return uploadFileResponse;
  }

  protected abstract UploadFileResponse updateCandidateData(final VspDetails vspDetails,
                                                 final OnboardPackageInfo onboardPackageInfo,
                                                 final CandidateService candidateService);

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

  public abstract UploadFileResponse validate(final OnboardPackageInfo onboardPackageInfo);

  protected abstract OnboardingTypesEnum getHandlerType();
}
