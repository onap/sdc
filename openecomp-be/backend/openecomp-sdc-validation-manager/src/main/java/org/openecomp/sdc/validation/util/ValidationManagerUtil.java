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

package org.openecomp.sdc.validation.util;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.validation.api.ValidationManager;
import org.openecomp.core.validation.factory.ValidationManagerFactory;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


public class ValidationManagerUtil {

  /**
   * Handle missing manifest.
   *
   * @param fileContentMap the file content map
   * @param errors         the errors
   */
  public static void handleMissingManifest(FileContentHandler fileContentMap,
                                           Map<String, List<ErrorMessage>> errors) throws IOException {
    try (InputStream manifest = fileContentMap.getFileContent(SdcCommon.MANIFEST_NAME)) {
      if (manifest == null) {
        ErrorMessage.ErrorMessageUtil.addMessage(SdcCommon.MANIFEST_NAME, errors)
                .add(new ErrorMessage(ErrorLevel.ERROR, Messages.MANIFEST_NOT_EXIST.getErrorMessage()));
      }
    }
  }

  /**
   * Init validation manager validation manager.
   *
   * @param fileContentMap the file content map
   * @return the validation manager
   */
  public static ValidationManager initValidationManager(FileContentHandler fileContentMap) {
    ValidationManager validationManager = ValidationManagerFactory.getInstance().createInterface();
    fileContentMap.getFileList().forEach(fileName -> validationManager
        .addFile(fileName, FileUtils.toByteArray(fileContentMap.getFileContent(fileName))));
    return validationManager;
  }
}
