/*
 * Copyright © 2018 European Support Limited
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
package org.openecomp.sdc.validation.impl.validators;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.validation.Validator;

public class ManifestValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlValidator.class);
    private static final ErrorMessageCode ERROR_CODE_MNF_1 = new ErrorMessageCode("MNF1");
    private static final ErrorMessageCode ERROR_CODE_MNF_2 = new ErrorMessageCode("MNF2");
    private static final ErrorMessageCode ERROR_CODE_MNF_3 = new ErrorMessageCode("MNF3");
    private static final ErrorMessageCode ERROR_CODE_MNF_4 = new ErrorMessageCode("MNF4");
    private static final ErrorMessageCode ERROR_CODE_MNF_5 = new ErrorMessageCode("MNF5");
    private static final ErrorMessageCode ERROR_CODE_MNF_6 = new ErrorMessageCode("MNF6");
    private static final ErrorMessageCode ERROR_CODE_MNF_7 = new ErrorMessageCode("MNF7");
    private static final ErrorMessageCode ERROR_CODE_MNF_8 = new ErrorMessageCode("MNF8");

    @Override
    public void validate(GlobalValidationContext globalContext) {
        Optional<InputStream> content = globalContext.getFileContent(SdcCommon.MANIFEST_NAME);
        ManifestContent manifestContent;
        try {
            if (content.isPresent()) {
                manifestContent = JsonUtil.json2Object(content.get(), ManifestContent.class);
            } else {
                throw new Exception("The manifest file '" + SdcCommon.MANIFEST_NAME + "' has no content");
            }
        } catch (Exception re) {
            LOGGER.error("Invalid manifest file", re);
            globalContext.addMessage(SdcCommon.MANIFEST_NAME, ErrorLevel.ERROR,
                ErrorMessagesFormatBuilder.getErrorWithParameters(ERROR_CODE_MNF_6, Messages.INVALID_MANIFEST_FILE.getErrorMessage()));
            return;
        }
        List<String> manifestFiles = getManifestFileList(manifestContent, globalContext);
        manifestFiles.stream().filter(name -> !globalContext.getFileContextMap().containsKey(name)).forEach(name -> globalContext
            .addMessage(name, ErrorLevel.ERROR,
                ErrorMessagesFormatBuilder.getErrorWithParameters(ERROR_CODE_MNF_4, Messages.MISSING_FILE_IN_ZIP.getErrorMessage())));
        globalContext.getFileContextMap().keySet().stream().filter(name -> isNotManifestFiles(manifestFiles, name) && isNotManifestName(name))
            .forEach(name -> globalContext.addMessage(name, ErrorLevel.WARNING,
                ErrorMessagesFormatBuilder.getErrorWithParameters(ERROR_CODE_MNF_5, Messages.MISSING_FILE_IN_MANIFEST.getErrorMessage())));
    }

    private boolean isNotManifestFiles(List<String> manifestFiles, String name) {
        return !manifestFiles.contains(name);
    }

    private boolean isNotManifestName(String name) {
        return !SdcCommon.MANIFEST_NAME.equals(name);
    }

    private List<String> getManifestFileList(ManifestContent manifestContent, GlobalValidationContext context) {
        ManifestScanner manifestScanner = new ManifestScanner();
        manifestScanner.scan(null, manifestContent.getData(), context);
        return manifestScanner.getFileList();
    }

    private class ManifestScanner {

        private final List<String> fileList = new ArrayList<>();

        public void scan(FileData fileData, List<FileData> data, GlobalValidationContext globalContext) {
            if (fileData == null) {
                for (FileData childFileData : data) {
                    validateIfEnvIsAssociatedToHeat(globalContext, childFileData);
                }
            }
            if (fileData != null) {
                fileList.add(fileData.getFile());
                validateFileTypeVsFileName(globalContext, fileData);
            }
            if (data == null) {
                return;
            }
            data.forEach(chileFileData -> scan(chileFileData, chileFileData.getData(), globalContext));
        }

        public List<String> getFileList() {
            return this.fileList;
        }

        private void validateFileTypeVsFileName(GlobalValidationContext globalValidationContext, FileData fileData) {
            String fileName = fileData.getFile();
            validateIfFileExists(globalValidationContext, fileName);
            FileData.Type type = fileData.getType();
            if (type == null) {
                globalValidationContext.addMessage(fileName, ErrorLevel.ERROR,
                    ErrorMessagesFormatBuilder.getErrorWithParameters(ERROR_CODE_MNF_8, Messages.INVALID_FILE_TYPE.getErrorMessage()));
            } else if (type.equals(FileData.Type.HEAT_NET) || type.equals(FileData.Type.HEAT_VOL) || type.equals(FileData.Type.HEAT)) {
                validateIfFileHasYamlExtenstion(globalValidationContext, fileName);
            } else if (type.equals(FileData.Type.HEAT_ENV)) {
                validateIfFileHasEnvExtension(globalValidationContext, fileName);
            }
        }

        private void validateIfEnvIsAssociatedToHeat(GlobalValidationContext globalContext, FileData childFileData) {
            if (childFileData.getType() != null && childFileData.getType().equals(FileData.Type.HEAT_ENV)) {
                globalContext.addMessage(childFileData.getFile(), ErrorLevel.ERROR,
                    ErrorMessagesFormatBuilder.getErrorWithParameters(ERROR_CODE_MNF_1, Messages.ENV_NOT_ASSOCIATED_TO_HEAT.getErrorMessage()));
            }
        }

        private void validateIfFileHasEnvExtension(GlobalValidationContext globalValidationContext, String fileName) {
            if (fileName != null && !fileName.endsWith(".env")) {
                globalValidationContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                    .getErrorWithParameters(ERROR_CODE_MNF_3, Messages.WRONG_ENV_FILE_EXTENSION.getErrorMessage(), fileName));
            }
        }

        private void validateIfFileHasYamlExtenstion(GlobalValidationContext globalValidationContext, String fileName) {
            if (fileName != null && !fileName.endsWith(".yml") && !fileName.endsWith(".yaml")) {
                globalValidationContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                    .getErrorWithParameters(ERROR_CODE_MNF_2, Messages.WRONG_HEAT_FILE_EXTENSION.getErrorMessage(), fileName));
            }
        }

        private void validateIfFileExists(GlobalValidationContext globalValidationContext, String fileName) {
            if (fileName == null) {
                globalValidationContext.addMessage(SdcCommon.MANIFEST_NAME, ErrorLevel.ERROR,
                    ErrorMessagesFormatBuilder.getErrorWithParameters(ERROR_CODE_MNF_7, Messages.MISSING_FILE_NAME_IN_MANIFEST.getErrorMessage()));
            }
        }
    }
}
