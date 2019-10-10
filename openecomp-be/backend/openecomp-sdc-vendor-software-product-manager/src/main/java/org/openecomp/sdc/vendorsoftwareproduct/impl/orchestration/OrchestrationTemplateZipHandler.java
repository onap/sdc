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

import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.services.utils.CandidateEntityBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackage;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;

public class OrchestrationTemplateZipHandler extends BaseOrchestrationTemplateHandler
    implements OrchestrationTemplateFileHandler {

    @Override
    public UploadFileResponse validate(final OnboardPackageInfo onboardPackageInfo) {
        final UploadFileResponse uploadFileResponse = new UploadFileResponse();
        final OnboardPackage onboardPackage = onboardPackageInfo.getOnboardPackage();
        OrchestrationUtil
            .getFileContentMap(OnboardingTypesEnum.ZIP, uploadFileResponse, onboardPackage.getFileContent().array());
        return uploadFileResponse;
    }

  @Override
  protected UploadFileResponse updateCandidateData(final VspDetails vspDetails,
                                        final OnboardPackageInfo onboardPackageInfo,
                                        final CandidateService candidateService) {
    final UploadFileResponse uploadFileResponse = new UploadFileResponse();
    try {
      final OnboardPackage zipPackage = onboardPackageInfo.getOnboardPackage();
      final OrchestrationTemplateCandidateData candidateData =
          new CandidateEntityBuilder(candidateService)
              .buildCandidateEntityFromZip(vspDetails, zipPackage.getFileContent().array(), zipPackage.getFileContentHandler(),
                  uploadFileResponse.getErrors());
      candidateData.setFileName(zipPackage.getFilename());
      candidateData.setFileSuffix(zipPackage.getFileExtension());
      candidateService
          .updateCandidateUploadData(vspDetails.getId(), vspDetails.getVersion(), candidateData);
    } catch (final Exception exception) {
      logger.error(getErrorWithParameters(Messages.FILE_LOAD_CONTENT_ERROR.getErrorMessage(),
          getHandlerType().toString()), exception);
      uploadFileResponse.addStructureError(SdcCommon.UPLOAD_FILE,
          new ErrorMessage(ErrorLevel.ERROR, exception.getMessage()));
    }
    return uploadFileResponse;
  }

  @Override
  protected OnboardingTypesEnum getHandlerType() {
    return OnboardingTypesEnum.ZIP;
  }
}
