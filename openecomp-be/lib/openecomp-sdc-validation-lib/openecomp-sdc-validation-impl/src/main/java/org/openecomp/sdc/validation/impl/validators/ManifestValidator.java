/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.validation.impl.validators;

import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ManifestValidator implements Validator {
  public static final MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static Logger logger = (Logger) LoggerFactory.getLogger(YamlValidator.class);

  @Override
  public void validate(GlobalValidationContext globalContext) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Optional<InputStream> content = globalContext.getFileContent(SdcCommon.MANIFEST_NAME);
    ManifestContent manifestContent;

    try {
      if (content.isPresent()) {
        manifestContent = JsonUtil.json2Object(content.get(), ManifestContent.class);
      } else {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
            LoggerTragetServiceName.VALIDATE_MANIFEST_CONTENT, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.EMPTY_FILE);
        throw new Exception("The manifest file '" + SdcCommon.MANIFEST_NAME + "' has no content");
      }
    } catch (Exception re) {
      logger.debug("",re);
      globalContext.addMessage(SdcCommon.MANIFEST_NAME, ErrorLevel.ERROR,
          Messages.INVALID_MANIFEST_FILE.getErrorMessage(),
          LoggerTragetServiceName.VALIDATE_MANIFEST_CONTENT,
          LoggerErrorDescription.INVALID_MANIFEST);
      return;
    }

    List<String> manifestFiles = getManifestFileList(manifestContent, globalContext);
    manifestFiles.stream().filter(name ->
        !globalContext.getFileContextMap().containsKey(name)
    ).forEach(name -> globalContext
        .addMessage(name, ErrorLevel.ERROR, Messages.MISSING_FILE_IN_ZIP.getErrorMessage(),
            LoggerTragetServiceName.VALIDATE_FILE_IN_ZIP, LoggerErrorDescription.MISSING_FILE));

    globalContext.getFileContextMap().keySet().stream().filter(name ->
        isNotManifestFiles(manifestFiles, name) && isNotManifestName(name)
    ).forEach(name ->
        globalContext.addMessage(name, ErrorLevel.WARNING,
            Messages.MISSING_FILE_IN_MANIFEST.getErrorMessage(),
            LoggerTragetServiceName.VALIDATE_FILE_IN_MANIFEST, LoggerErrorDescription.MISSING_FILE)
    );

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private boolean isNotManifestFiles(List<String> manifestFiles, String name) {
    return !manifestFiles.contains(name);
  }

  private boolean isNotManifestName(String name) {
    return !SdcCommon.MANIFEST_NAME.equals(name);
  }


  private List<String> getManifestFileList(ManifestContent manifestContent,
                                           GlobalValidationContext context) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    ManifestScanner manifestScanner = new ManifestScanner();
    manifestScanner.init(context);
    manifestScanner.scan(null, manifestContent.getData(), context);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return manifestScanner.getFileList();
  }


  private class ManifestScanner {
    private GlobalValidationContext globalValidationContext;
    private List<String> fileList;

    public void init(GlobalValidationContext globalValidationContext) {
      this.globalValidationContext = globalValidationContext;
      this.fileList = new ArrayList<>();
    }


    public void scan(FileData fileData, List<FileData> data,
                     GlobalValidationContext globalContext) {
      if (fileData == null) {
        for (FileData childFileData : data) {
          if (childFileData.getType() != null
              && childFileData.getType().equals(FileData.Type.HEAT_ENV)) {
            globalContext.addMessage(childFileData.getFile(), ErrorLevel.ERROR,
                ErrorMessagesFormatBuilder
                    .getErrorWithParameters(Messages.ENV_NOT_ASSOCIATED_TO_HEAT.getErrorMessage()),
                LoggerTragetServiceName.SCAN_MANIFEST_STRUCTURE,
                "env file is not associated to HEAT file");
          }
        }
      }
      if (fileData != null) {
        fileList.add(fileData.getFile());
        validateFileTypeVsFileName(fileData);
      }
      if (data == null) {
        return;
      }
      data.forEach(chileFileData -> scan(chileFileData, chileFileData.getData(), globalContext));
    }


    public List<String> getFileList() {
      return this.fileList;
    }

    private void validateFileTypeVsFileName(FileData fileData) {
      String fileName = fileData.getFile();
      if (fileName == null) {
        this.globalValidationContext.addMessage(SdcCommon.MANIFEST_NAME, ErrorLevel.ERROR,
            Messages.MISSING_FILE_NAME_IN_MANIFEST.getErrorMessage(),
            LoggerTragetServiceName.VALIDATE_FILE_TYPE_AND_NAME, "Missing file name in manifest");

      }
      FileData.Type type = fileData.getType();
      if (type == null) {
        this.globalValidationContext
            .addMessage(fileName, ErrorLevel.ERROR, Messages.INVALID_FILE_TYPE.getErrorMessage(),
                LoggerTragetServiceName.VALIDATE_FILE_TYPE_AND_NAME, "Invalid file type");
      } else if (type.equals(FileData.Type.HEAT_NET) || type.equals(FileData.Type.HEAT_VOL)
          || type.equals(FileData.Type.HEAT)) {
        if (fileName != null && !fileName.endsWith(".yml") && !fileName.endsWith(".yaml")) {
          this.globalValidationContext.addMessage(fileName, ErrorLevel.ERROR,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.WRONG_HEAT_FILE_EXTENSION.getErrorMessage(),
                      fileName), LoggerTragetServiceName.VALIDATE_FILE_TYPE_AND_NAME,
              "Wrong HEAT file extention");
        }
      } else if (type.equals(FileData.Type.HEAT_ENV)) {
        if (fileName != null && !fileName.endsWith(".env")) {
          this.globalValidationContext.addMessage(fileName, ErrorLevel.ERROR,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.WRONG_ENV_FILE_EXTENSION.getErrorMessage(),
                      fileName), LoggerTragetServiceName.VALIDATE_FILE_TYPE_AND_NAME,
              "Wrong env file extention");
        }
      }
    }
  }


}
