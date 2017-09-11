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
    public  Optional<FileContentHandler> getFileContentMap(UploadFileResponse uploadFileResponse,
                                                           byte[] uploadedFileData) {
       return   OrchestrationUtil.getFileContentMap(OnboardingTypesEnum.ZIP, uploadFileResponse, uploadedFileData);
    }

    @Override
    protected boolean updateCandidateData(String vspId, String user, CandidateService candidateService,
                                          VspDetails vspDetails, UploadFileResponse uploadFileResponse,
                                          byte[] uploadedFileData, Optional<FileContentHandler> optionalContentMap) {
        try {
            OrchestrationTemplateCandidateData candidateData =
                    new CandidateEntityBuilder(candidateService)
                            .buildCandidateEntityFromZip(vspDetails, uploadedFileData, optionalContentMap.get(),
                                    uploadFileResponse.getErrors(), user);
            candidateService.updateCandidateUploadData(candidateData, vspDetails.getId());
        } catch (Exception exception) {
            logger.error(getErrorWithParameters(Messages.FILE_CONTENT_MAP.getErrorMessage(),
                    getHandlerType().toString()),exception);
            uploadFileResponse
                    .addStructureError(
                            SdcCommon.UPLOAD_FILE,
                            new ErrorMessage(ErrorLevel.ERROR, exception.getMessage()));

            mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
            return true;
        }
        return false;
    }

    @Override
    protected OnboardingTypesEnum getHandlerType() {
        return OnboardingTypesEnum.ZIP;
    }
}
