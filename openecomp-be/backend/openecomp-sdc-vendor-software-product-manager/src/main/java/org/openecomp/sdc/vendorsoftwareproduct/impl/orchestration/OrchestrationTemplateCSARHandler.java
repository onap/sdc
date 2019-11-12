/*
 * Copyright (c) 2018 AT&T Intellectual Property.

  * Modifications Copyright (c) 2018 Verizon Property.
  * Modifications Copyright (c) 2019 Nordix Foundation.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;

import java.io.IOException;
import java.util.Optional;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.CsarSecurityValidator;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.Validator;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.ValidatorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManagerException;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackage;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardSignedPackage;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;

public class OrchestrationTemplateCSARHandler extends BaseOrchestrationTemplateHandler
    implements OrchestrationTemplateFileHandler {

    @Override
    public UploadFileResponse validate(final OnboardPackageInfo onboardPackageInfo) {
        final UploadFileResponse uploadFileResponse = new UploadFileResponse();
        if (onboardPackageInfo.getPackageType() == OnboardingTypesEnum.SIGNED_CSAR) {
            final OnboardSignedPackage originalOnboardPackage =
                (OnboardSignedPackage) onboardPackageInfo.getOriginalOnboardPackage();
            validatePackageSecurity(originalOnboardPackage).ifPresent(packageSignatureResponse -> {
                if (packageSignatureResponse.hasErrors()) {
                    uploadFileResponse.addStructureErrors(packageSignatureResponse.getErrors());
                }
            });

            if (uploadFileResponse.hasErrors()) {
                return uploadFileResponse;
            }
        }
        final OnboardPackage onboardPackage = onboardPackageInfo.getOnboardPackage();
        final FileContentHandler fileContentHandler = onboardPackage.getFileContentHandler();

        try {
            final Validator validator = ValidatorFactory.getValidator(fileContentHandler);
            uploadFileResponse.addStructureErrors(validator.validateContent(fileContentHandler));
        } catch (IOException exception) {
            logger.error(exception.getMessage(), exception);
            uploadFileResponse.addStructureError(
                SdcCommon.UPLOAD_FILE,
                new ErrorMessage(ErrorLevel.ERROR, Messages.INVALID_CSAR_FILE.getErrorMessage()));
        } catch (CoreException coreException) {
            logger.error(coreException.getMessage(), coreException);
            uploadFileResponse.addStructureError(
                SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR, coreException.getMessage()));
        }

        return uploadFileResponse;
    }

    private Optional<UploadFileResponse> validatePackageSecurity(final OnboardSignedPackage originalOnboardPackage) {
        final UploadFileResponse uploadFileResponseDto = new UploadFileResponse();
        try {
            final CsarSecurityValidator csarSecurityValidator = new CsarSecurityValidator();
            if (!csarSecurityValidator.verifyPackageSignature(originalOnboardPackage)) {
                final ErrorMessage errorMessage = new ErrorMessage(ErrorLevel.ERROR,
                    Messages.FAILED_TO_VERIFY_SIGNATURE.getErrorMessage());
                logger.error(errorMessage.getMessage());
                uploadFileResponseDto.addStructureError(SdcCommon.UPLOAD_FILE, errorMessage);
                return Optional.of(uploadFileResponseDto);
            }
        } catch (final SecurityManagerException e) {
            final ErrorMessage errorMessage = new ErrorMessage(ErrorLevel.ERROR, e.getMessage());
            logger.error("Could not validate package signature {}", originalOnboardPackage.getFilename(), e);
            uploadFileResponseDto.addStructureError(SdcCommon.UPLOAD_FILE, errorMessage);
            return Optional.of(uploadFileResponseDto);
        }
        return Optional.empty();
    }

    @Override
    protected UploadFileResponse updateCandidateData(final VspDetails vspDetails,
                                                     final OnboardPackageInfo onboardPackageInfo,
                                                     final CandidateService candidateService) {
        final UploadFileResponse uploadFileResponse = new UploadFileResponse();
        final OnboardPackage csarPackage = onboardPackageInfo.getOnboardPackage();
        final OnboardPackage originalOnboardPackage = onboardPackageInfo.getOriginalOnboardPackage();
        try {
            candidateService.updateCandidateUploadData(vspDetails.getId(), vspDetails.getVersion(),
                new OrchestrationTemplateCandidateData(csarPackage.getFileContent(),
                    "", csarPackage.getFileExtension(),
                    csarPackage.getFilename(), originalOnboardPackage.getFilename(),
                    originalOnboardPackage.getFileExtension(),
                    originalOnboardPackage.getFileContent()));
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
    return OnboardingTypesEnum.CSAR;
  }

}
