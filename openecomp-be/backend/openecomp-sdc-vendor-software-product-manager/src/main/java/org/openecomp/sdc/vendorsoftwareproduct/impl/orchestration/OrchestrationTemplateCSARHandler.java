/*

 * Copyright (c) 2018 AT&T Intellectual Property.

  * Modifications Copyright (c) 2018 Verizon Property.

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
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.ToscaMetadata;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.csar.OnboardingManifest;
import org.openecomp.sdc.tosca.csar.OnboardingToscaMetadata;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.exceptions.OrchestrationTemplateHandlerException;
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
import static org.openecomp.sdc.tosca.csar.CSARConstants.ELIGBLE_FOLDERS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.ELIGIBLE_FILES;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_MF_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_PATH_FILE_NAME;

public class OrchestrationTemplateCSARHandler extends BaseOrchestrationTemplateHandler
    implements OrchestrationTemplateFileHandler {
  private static Logger logger = LoggerFactory.getLogger(OrchestrationTemplateCSARHandler.class);

  @Override
  public Optional<FileContentHandler> getFileContentMap(UploadFileResponse uploadFileResponse,
                                                        byte[] uploadedFileData) {
    FileContentHandler contentMap = null;
    List<String> folderList = new ArrayList<>();
    try {
      Pair<FileContentHandler, List<String>> fileContentMapFromOrchestrationCandidateZip =
          CommonUtil.getFileContentMapFromOrchestrationCandidateZip(uploadedFileData);
      contentMap = fileContentMapFromOrchestrationCandidateZip.getKey();
      folderList = fileContentMapFromOrchestrationCandidateZip.getRight();
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
    validateContent(uploadFileResponse, contentMap, folderList);
    return Optional.ofNullable(contentMap);
  }

  private void validateContent(UploadFileResponse uploadFileResponse, FileContentHandler contentMap,
                               List<String> folderList) {
    validateManifest(uploadFileResponse, contentMap);
    validateMetadata(uploadFileResponse, contentMap);
    validateNoExtraFiles(uploadFileResponse, contentMap);
    validateFolders(uploadFileResponse, folderList);
  }

  private void validateMetadata(UploadFileResponse uploadFileResponse,
                                FileContentHandler contentMap){
    if (!validateTOSCAYamlFileInRootExist(contentMap, MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME)) {
      try (InputStream metaFileContent = contentMap.getFileContent(TOSCA_META_PATH_FILE_NAME)) {

        ToscaMetadata onboardingToscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(metaFileContent);
        String entryDefinitionsPath = onboardingToscaMetadata.getMetaEntries().get(TOSCA_META_ENTRY_DEFINITIONS);
        if (entryDefinitionsPath != null) {
        validateFileExist(uploadFileResponse, contentMap, entryDefinitionsPath);
        } else {
        uploadFileResponse.addStructureError(
            SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR,
                Messages.METADATA_NO_ENTRY_DEFINITIONS.getErrorMessage()));
        }
      } catch (IOException exception) {
        logger.error(exception.getMessage(), exception);
        uploadFileResponse.addStructureError(
            SdcCommon.UPLOAD_FILE,
            new ErrorMessage(ErrorLevel.ERROR, Messages.FAILED_TO_VALIDATE_METADATA.getErrorMessage()));
      }
    } else {
        validateFileExist(uploadFileResponse, contentMap, MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME);
    }
  }

  private void validateManifest(UploadFileResponse uploadFileResponse,
                                FileContentHandler contentMap) {

    if (!validateFileExist(uploadFileResponse, contentMap, MAIN_SERVICE_TEMPLATE_MF_FILE_NAME)) {
      return;
    }

    try (InputStream fileContent = contentMap.getFileContent(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME)) {

      Manifest onboardingManifest = OnboardingManifest.parse(fileContent);
      if (!onboardingManifest.isValid()) {
        onboardingManifest.getErrors().forEach(error -> uploadFileResponse.addStructureError(
            SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR, error)));
      }

    } catch (IOException e) {
      // convert to runtime to keep the throws unchanged
      throw new OrchestrationTemplateHandlerException("Failed to validate manifest", e);
    }
  }

  private void validateNoExtraFiles(UploadFileResponse uploadFileResponse,
                                    FileContentHandler contentMap) {
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
    List<String> filterResult =
        folderList.stream().filter(this::filterFolders).collect(Collectors.toList());
    if (!filterResult.isEmpty()) {
      folderList.stream().filter(this::filterFolders).forEach(unwantedFolder ->
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

  private boolean validateTOSCAYamlFileInRootExist(FileContentHandler contentMap, String fileName) {
    return contentMap.containsFile(fileName);
  }

  private boolean validateFileExist(UploadFileResponse uploadFileResponse,
                                    FileContentHandler contentMap, String fileName) {

    boolean containsFile = contentMap.containsFile(fileName);
    if (!containsFile) {
      uploadFileResponse.addStructureError(
          SdcCommon.UPLOAD_FILE, new ErrorMessage(ErrorLevel.ERROR,
              getErrorWithParameters(Messages.CSAR_FILE_NOT_FOUND.getErrorMessage(), fileName)));
    }
    return containsFile;
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
