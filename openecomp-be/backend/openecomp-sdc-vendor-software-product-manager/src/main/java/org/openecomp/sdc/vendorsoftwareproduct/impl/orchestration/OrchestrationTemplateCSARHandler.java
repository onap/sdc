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
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.OnboardingManifest;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.CSARConstants.ELIGBLE_FOLDERS;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.CSARConstants.ELIGIBLE_FILES;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_MF_FILE_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME;

public class OrchestrationTemplateCSARHandler extends BaseOrchestrationTemplateHandler
        implements OrchestrationTemplateFileHandler {


    @Override
    public Optional<FileContentHandler> getFileContentMap(UploadFileResponse uploadFileResponse,
                                                          byte[] uploadedFileData) {
        FileContentHandler contentMap = null;
        List<String> folderList = new ArrayList<>();
        try {
            Pair<FileContentHandler, List<String>> fileContentMapFromOrchestrationCandidateZip = CommonUtil.getFileContentMapFromOrchestrationCandidateZip(uploadedFileData);
            contentMap = fileContentMapFromOrchestrationCandidateZip.getKey();
            folderList = fileContentMapFromOrchestrationCandidateZip.getRight();
        } catch (IOException exception) {
            uploadFileResponse.addStructureError(
                    SdcCommon.UPLOAD_FILE,
                    new ErrorMessage(ErrorLevel.ERROR, Messages.INVALID_CSAR_FILE.getErrorMessage()));
        } catch (CoreException coreException) {
            uploadFileResponse.addStructureError(
                    SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR, coreException.getMessage()));
        }
        validateContent(uploadFileResponse, contentMap, folderList);
        return Optional.ofNullable(contentMap);
    }

    private void validateContent(UploadFileResponse uploadFileResponse, FileContentHandler contentMap, List<String> folderList) {
        validateManifest(uploadFileResponse, contentMap);
        validateFileExist(uploadFileResponse, contentMap, MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME);
        validateNoExtraFiles(uploadFileResponse, contentMap);
        validateFolders(uploadFileResponse, folderList);
    }

    private void validateManifest(UploadFileResponse uploadFileResponse, FileContentHandler contentMap) {

        if (!validateFileExist(uploadFileResponse, contentMap, MAIN_SERVICE_TEMPLATE_MF_FILE_NAME)){
            return;
        }

        try (InputStream fileContent = contentMap.getFileContent(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME)) {

            OnboardingManifest onboardingManifest = new OnboardingManifest(fileContent);
            if (!onboardingManifest.isValid()) {
                onboardingManifest.getErrors().forEach(error -> uploadFileResponse.addStructureError(
                        SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR, error)));
            }

        } catch (IOException e) {
            // convert to runtime to keep the throws unchanged
            throw new RuntimeException("Failed to validate manifest", e);
        }
    }

    private void validateNoExtraFiles(UploadFileResponse uploadFileResponse,  FileContentHandler contentMap) {
        List<String> unwantedFiles = contentMap.getFileList().stream()
                .filter(this::filterFiles).collect(Collectors.toList());
        if (!unwantedFiles.isEmpty()) {
            unwantedFiles.stream().filter(this::filterFiles).forEach(unwantedFile ->
                    uploadFileResponse.addStructureError(
                            SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR,
                                    getErrorWithParameters(Messages.CSAR_FILES_NOT_ALLOWED.getErrorMessage(),
                                            unwantedFile))));
        }
    }

    private void validateFolders(UploadFileResponse uploadFileResponse, List<String> folderList) {
        List<String> filterResult = folderList.stream().filter(this::filterFolders).collect(Collectors.toList());
        if (!filterResult.isEmpty()) {
            folderList.stream().filter(this::filterFolders).forEach( unwantedFolder ->
                    uploadFileResponse.addStructureError(
                            SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR,
                                    getErrorWithParameters(Messages.CSAR_DIRECTORIES_NOT_ALLOWED.getErrorMessage(),
                                            unwantedFolder))));

        }
    }
    private boolean filterFiles(String inFileName) {
        boolean valid = ELIGIBLE_FILES.stream().anyMatch(fileName -> fileName.equals(inFileName));
        return !valid && filterFolders(inFileName);
    }

    private boolean filterFolders(String fileName) {
        return ELIGBLE_FOLDERS.stream().noneMatch(fileName::startsWith);
    }

    private boolean validateFileExist(UploadFileResponse uploadFileResponse, FileContentHandler contentMap, String fileName) {

        boolean containsFile = contentMap.containsFile(fileName);
        if (!containsFile) {
            uploadFileResponse.addStructureError(
                    SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR,
                            getErrorWithParameters(Messages.CSAR_FILE_NOT_FOUND.getErrorMessage(), fileName)));
        }
        return containsFile;
    }

    @Override
    protected boolean updateCandidateData(String vspId, String user, CandidateService candidateService,
                                          VspDetails vspDetails, UploadFileResponse uploadFileResponse,
                                          byte[] uploadedFileData, Optional<FileContentHandler> optionalContentMap) {
        try {
            candidateService.updateCandidateUploadData(new OrchestrationTemplateCandidateData(
                    ByteBuffer.wrap(uploadedFileData), ""), vspDetails.getId());
        } catch (Exception exception) {
            logger.error(getErrorWithParameters(Messages.FILE_CONTENT_MAP.getErrorMessage(),
                    getHandlerType().toString()), exception);
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
        return OnboardingTypesEnum.CSAR;
    }

    @Override
    protected boolean isInvalidRawZipData(UploadFileResponse uploadFileResponse,
                                          byte[] uploadedFileData, CandidateService candidateService) {
        return super.isInvalidRawZipData(uploadFileResponse, uploadedFileData, candidateService);

    }
}
