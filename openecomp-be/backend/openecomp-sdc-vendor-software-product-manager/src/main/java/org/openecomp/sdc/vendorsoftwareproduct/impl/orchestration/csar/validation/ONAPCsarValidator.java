/*-
 * ============LICENSE_START=======================================================
 *  Modification Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;
import static org.openecomp.sdc.tosca.csar.CSARConstants.ELIGBLE_FOLDERS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.ELIGIBLE_FILES;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_MF_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.csar.ONAPManifestOnboarding;
import org.openecomp.sdc.tosca.csar.OnboardingToscaMetadata;
import org.openecomp.sdc.tosca.csar.ToscaMetadata;

class ONAPCsarValidator implements Validator {

    private static Logger logger = LoggerFactory.getLogger(ONAPCsarValidator.class);

    private List<ErrorMessage> uploadFileErrors = new ArrayList<>();

    @Override
    public Map<String, List<ErrorMessage>> validateContent(final FileContentHandler contentHandler) {
        Map<String, List<ErrorMessage>> errors = new HashMap<>();
        validateManifest(contentHandler);
        validateMetadata(contentHandler);
        validateNoExtraFiles(contentHandler);
        validateFolders(contentHandler.getFolderList());

        if(uploadFileErrors == null || uploadFileErrors.isEmpty()){
            return errors;
        }
        errors.put(SdcCommon.UPLOAD_FILE, uploadFileErrors);
        return errors;
    }

    private void validateMetadata(FileContentHandler contentMap){
        if (!validateTOSCAYamlFileInRootExist(contentMap, MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME)) {
            try (InputStream metaFileContent = contentMap.getFileContentAsStream(TOSCA_META_PATH_FILE_NAME)) {

                ToscaMetadata onboardingToscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(metaFileContent);
                String entryDefinitionsPath = onboardingToscaMetadata.getMetaEntries().get(ENTRY_DEFINITIONS.getName());
                if (entryDefinitionsPath != null) {
                    validateFileExist(contentMap, entryDefinitionsPath);
                } else {
                    uploadFileErrors.add(new ErrorMessage(ErrorLevel.ERROR,
                            Messages.METADATA_NO_ENTRY_DEFINITIONS.getErrorMessage()));
                }
            } catch (IOException exception) {
                logger.error(exception.getMessage(), exception);
                uploadFileErrors.add(new ErrorMessage(ErrorLevel.ERROR,
                        Messages.FAILED_TO_VALIDATE_METADATA.getErrorMessage()));
            }
        } else {
            validateFileExist(contentMap, MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME);
        }
    }

    private void validateManifest(final FileContentHandler contentMap) {
        if (!validateFileExist(contentMap, MAIN_SERVICE_TEMPLATE_MF_FILE_NAME)) {
            return;
        }

        try (final InputStream fileContent = contentMap.getFileContentAsStream(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME)) {
            final Manifest onboardingManifest = new ONAPManifestOnboarding();
            onboardingManifest.parse(fileContent);
            if (!onboardingManifest.isValid()) {
                onboardingManifest.getErrors()
                    .forEach(error -> uploadFileErrors.add(new ErrorMessage(ErrorLevel.ERROR, error)));
            }
        } catch (final IOException ex) {
            final String errorMessage =
                Messages.MANIFEST_UNEXPECTED_ERROR.formatMessage(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME, ex.getMessage());
            uploadFileErrors.add(new ErrorMessage(ErrorLevel.ERROR, errorMessage));
            logger.error(errorMessage, ex);
        }
    }

    private void validateNoExtraFiles(FileContentHandler contentMap) {
        List<String> unwantedFiles = contentMap.getFileList().stream()
                .filter(this::filterFiles).collect(Collectors.toList());
        if (!unwantedFiles.isEmpty()) {
            unwantedFiles.stream().filter(this::filterFiles).forEach(unwantedFile ->
                    uploadFileErrors.add(new ErrorMessage(ErrorLevel.ERROR,
                            getErrorWithParameters(Messages.CSAR_FILES_NOT_ALLOWED.getErrorMessage(), unwantedFile))));
        }
    }

    private void validateFolders(Set<String> folderList) {
        List<String> filterResult =
                folderList.stream().filter(this::filterFolders).collect(Collectors.toList());
        if (!filterResult.isEmpty()) {
            folderList.stream().filter(this::filterFolders).forEach(unwantedFolder ->
                    uploadFileErrors.add(new ErrorMessage(ErrorLevel.ERROR,
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

    private boolean validateFileExist(FileContentHandler contentMap, String fileName) {

        boolean containsFile = contentMap.containsFile(fileName);
        if (!containsFile) {
            uploadFileErrors.add(new ErrorMessage(ErrorLevel.ERROR,
                    getErrorWithParameters(Messages.CSAR_FILE_NOT_FOUND.getErrorMessage(), fileName)));
        }
        return containsFile;
    }
}
