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

package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileStatus;

import java.util.List;
import java.util.Map;

/**
 * Created by TALIO on 4/27/2016.
 */
public class OrchestrationTemplateActionResponseDto {
  private List<String> fileNames;
  private Map<String, List<ErrorMessage>> errors;
  private UploadFileStatus status;

  public Map<String, List<ErrorMessage>> getErrors() {
    return errors;
  }

  public void setErrors(Map<String, List<ErrorMessage>> errors) {
    this.errors = errors;
  }

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

}
