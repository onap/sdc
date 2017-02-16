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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadFileResponse {
  private List<String> fileNames;
  private Map<String, List<org.openecomp.sdc.datatypes.error.ErrorMessage>> errors =
      new HashMap<>();
  private UploadFileStatus status = UploadFileStatus.Success;

  /**
   * Gets status.
   *
   * @return the status
   */
  public UploadFileStatus getStatus() {
    return status;
  }

  /**
   * Sets status.
   *
   * @param status the status
   */
  public void setStatus(UploadFileStatus status) {
    this.status = status;
  }

  /**
   * Gets file names.
   *
   * @return the file names
   */
  public List<String> getFileNames() {
    return fileNames;
  }

  /**
   * Sets file names.
   *
   * @param fileNames the file names
   */
  public void setFileNames(List<String> fileNames) {
    this.fileNames = fileNames;
  }

  /**
   * Add new file to list.
   *
   * @param filename the filename
   */
  public void addNewFileToList(String filename) {
    this.fileNames.add(filename);
  }

  /**
   * Remove file from list.
   *
   * @param toRemove the to remove
   */
  public void removeFileFromList(String toRemove) {
    this.fileNames.remove(toRemove);
  }

  /**
   * Add structure error.
   *
   * @param fileName     the file name
   * @param errorMessage the error message
   */
  public void addStructureError(String fileName,
                                org.openecomp.sdc.datatypes.error.ErrorMessage errorMessage) {
    List<org.openecomp.sdc.datatypes.error.ErrorMessage> errorList = errors.get(fileName);
    if (errorList == null) {
      errorList = new ArrayList<>();
      errors.put(fileName, errorList);
    }
    errorList.add(errorMessage);
    if (org.openecomp.sdc.datatypes.error.ErrorLevel.ERROR.equals(errorMessage.getLevel())) {
      status = UploadFileStatus.Failure;
    }
  }

  /**
   * Add structure errors.
   *
   * @param errorsByFileName the errors by file name
   */
  public void addStructureErrors(
      Map<String, List<org.openecomp.sdc.datatypes.error.ErrorMessage>> errorsByFileName) {
    if (errorsByFileName == null) {
      return;
    }

    errors.putAll(errorsByFileName);

    if (status == UploadFileStatus.Failure) {
      return;
    }
    for (Map.Entry<String, List<org.openecomp.sdc.datatypes.error.ErrorMessage>> entry
        : errorsByFileName.entrySet()) {
      for (org.openecomp.sdc.datatypes.error.ErrorMessage errorMessage : entry.getValue()) {
        if (errorMessage.getLevel() == org.openecomp.sdc.datatypes.error.ErrorLevel.ERROR) {
          status = UploadFileStatus.Failure;
          return;
        }
      }
    }
  }

  /**
   * Gets errors.
   *
   * @return the errors
   */
  public Map<String, List<org.openecomp.sdc.datatypes.error.ErrorMessage>> getErrors() {
    return errors;
  }
}
