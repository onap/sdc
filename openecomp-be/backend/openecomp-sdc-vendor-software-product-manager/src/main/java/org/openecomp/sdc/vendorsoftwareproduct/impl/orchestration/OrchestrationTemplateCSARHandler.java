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

import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.Validator;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.ValidatorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;

public class OrchestrationTemplateCSARHandler extends BaseOrchestrationTemplateHandler
    implements OrchestrationTemplateFileHandler {


  @Override
  public Optional<FileContentHandler> getFileContentMap(UploadFileResponse uploadFileResponse,
                                                        byte[] uploadedFileData) {
    FileContentHandler contentMap = null;
    List<String> folderList;
    try {
      Pair<FileContentHandler, List<String>> fileContentMapFromOrchestrationCandidateZip =
          CommonUtil.getFileContentMapFromOrchestrationCandidateZip(uploadedFileData);
      contentMap = fileContentMapFromOrchestrationCandidateZip.getKey();
      folderList = fileContentMapFromOrchestrationCandidateZip.getRight();
      Validator validator = ValidatorFactory.getValidator(contentMap);
      uploadFileResponse.addStructureErrors(validator.validateContent(contentMap, folderList));
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

    return Optional.ofNullable(contentMap);
  }

  @Override
  protected boolean updateCandidateData(VspDetails vspDetails, byte[] uploadedFileData,
                                        FileContentHandler contentMap,
                                        String fileSuffix, String networkPackageName,
                                        CandidateService candidateService,
                                        UploadFileResponse uploadFileResponse) {
    try {
      candidateService.updateCandidateUploadData(vspDetails.getId(), vspDetails.getVersion(),
          new OrchestrationTemplateCandidateData(ByteBuffer.wrap(uploadedFileData), "", fileSuffix,
              networkPackageName));
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
    return OnboardingTypesEnum.CSAR;
  }

}
