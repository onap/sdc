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
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.interfaces.Validator;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.utils.AsdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ManifestValidator implements Validator {

  private static Logger logger = LoggerFactory.getLogger(YamlValidator.class);


  @Override
  public void validate(GlobalValidationContext globalContext) {


    InputStream content = globalContext.getFileContent(AsdcCommon.MANIFEST_NAME);
    ManifestContent manifestContent;

    try {
      manifestContent = JsonUtil.json2Object(content, ManifestContent.class);
    } catch (RuntimeException re) {
      globalContext.addMessage(AsdcCommon.MANIFEST_NAME, ErrorLevel.ERROR,
          Messages.INVALID_MANIFEST_FILE.getErrorMessage());
      return;
    }

    List<String> manifestFiles = getManifestFileList(manifestContent, globalContext);
    manifestFiles.stream().filter(name ->
        !globalContext.getFileContextMap().containsKey(name)
    ).forEach(name -> globalContext
        .addMessage(name, ErrorLevel.ERROR, Messages.MISSING_FILE_IN_ZIP.getErrorMessage()));

    globalContext.getFileContextMap().keySet().stream().filter(name ->
        !manifestFiles.contains(name) && !AsdcCommon.MANIFEST_NAME.equals(name)
    ).forEach(name ->
        globalContext.addMessage(name, ErrorLevel.WARNING,
            Messages.MISSING_FILE_IN_MANIFEST.getErrorMessage())
    );

  }

  private List<String> getManifestFileList(ManifestContent manifestContent,
                                           GlobalValidationContext context) {
    ManifestScanner manifestScanner = new ManifestScanner();
    manifestScanner.init(context);
    manifestScanner.scan(null, manifestContent.getData(), context);
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
                    .getErrorWithParameters(Messages.ENV_NOT_ASSOCIATED_TO_HEAT.getErrorMessage()));
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
      data.stream().forEach(chileFileData -> {
        scan(chileFileData, chileFileData.getData(), globalContext);
      });
    }


    public List<String> getFileList() {
      return this.fileList;
    }

    private void validateFileTypeVsFileName(FileData fileData) {
      String fileName = fileData.getFile();
      if (fileName == null) {
        this.globalValidationContext.addMessage(AsdcCommon.MANIFEST_NAME, ErrorLevel.ERROR,
            Messages.MISSING_FILE_NAME_IN_MANIFEST.getErrorMessage());

      }
      FileData.Type type = fileData.getType();
      if (type == null) {
        this.globalValidationContext
            .addMessage(fileName, ErrorLevel.ERROR, Messages.INVALID_FILE_TYPE.getErrorMessage());
      } else if (type.equals(FileData.Type.HEAT_NET) || type.equals(FileData.Type.HEAT_VOL)
          || type.equals(FileData.Type.HEAT)) {
        if (fileName != null && !fileName.endsWith(".yml") && !fileName.endsWith(".yaml")) {
          this.globalValidationContext.addMessage(fileName, ErrorLevel.ERROR,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.WRONG_HEAT_FILE_EXTENSION.getErrorMessage(),
                      fileName));
        }
      } else if (type.equals(FileData.Type.HEAT_ENV)) {
        if (fileName != null && !fileName.endsWith(".env")) {
          this.globalValidationContext.addMessage(fileName, ErrorLevel.ERROR,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.WRONG_ENV_FILE_EXTENSION.getErrorMessage(),
                      fileName));
        }
      }
    }
  }


}
