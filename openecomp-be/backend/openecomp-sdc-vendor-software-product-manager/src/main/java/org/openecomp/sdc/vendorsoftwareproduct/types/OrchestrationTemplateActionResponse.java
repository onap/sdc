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

package org.openecomp.sdc.vendorsoftwareproduct.types;


import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrchestrationTemplateActionResponse {
  private List<String> fileNames;
  private Map<String, List<ErrorMessage>> errors = new HashMap<>();
  private UploadFileStatus status = UploadFileStatus.Success;

  public UploadFileStatus getStatus() {
    return status;
  }

  public void setStatus(UploadFileStatus status) {
    this.status = status;
  }

  public List<String> getFileNames() {
    return fileNames;
  }

  public void setFileNames(List<String> fileNames) {
    this.fileNames = fileNames;
  }

  public void addNewFileToList(String filename) {
    this.fileNames.add(filename);
  }

  public void removeFileFromList(String toRemove) {
    this.fileNames.remove(toRemove);
  }

  /**
   * Add structure errors.
   *
   * @param errorsByFileName the errors by file name
   */
  public void addStructureErrors(Map<String, List<ErrorMessage>> errorsByFileName) {
    if (errorsByFileName == null) {
      return;
    }

    errors.putAll(errorsByFileName);

    if (status == UploadFileStatus.Failure) {
      return;
    }
    for (Map.Entry<String, List<ErrorMessage>> entry : errorsByFileName.entrySet()) {
      for (ErrorMessage errorMessage : entry.getValue()) {
        if (errorMessage.getLevel() == ErrorLevel.ERROR) {
          status = UploadFileStatus.Failure;
          return;
        }
      }
    }
  }

  /**
   * Add error message to map.
   *
   * @param key   the key
   * @param error the error
   * @param level the level
   */
  public void addErrorMessageToMap(String key, String error, ErrorLevel level) {
    ErrorMessage errorMessage = new ErrorMessage(level, error);
    List<ErrorMessage> errorMessages = getErrorList(key);

    errorMessages.add(errorMessage);
    this.errors.put(key, errorMessages);

    if (level.equals(ErrorLevel.ERROR)) {
      status = UploadFileStatus.Failure;
    }
  }

  private List<ErrorMessage> getErrorList(String key) {
    List<ErrorMessage> errorMessages = this.errors.get(key);
    if (CollectionUtils.isEmpty(errorMessages)) {
      errorMessages = new ArrayList<>();
    }

    return errorMessages;
  }

  public Map<String, List<ErrorMessage>> getErrors() {
    return errors;
  }
}
