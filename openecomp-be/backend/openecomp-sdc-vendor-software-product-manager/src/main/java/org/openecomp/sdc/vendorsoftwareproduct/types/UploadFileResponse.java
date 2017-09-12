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


import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TALIO on 4/27/2016.
 */
public class UploadFileResponse {
  private Map<String, List<ErrorMessage>> errors = new HashMap<>();
  private UploadFileStatus status = UploadFileStatus.Success;
  private OnboardingTypesEnum onboardingType;
  private String networkPackageName;

  public UploadFileStatus getStatus() {
    return status;
  }

  public void setStatus(UploadFileStatus status) {
    this.status = status;
  }

  public OnboardingTypesEnum getOnboardingType() {
    return onboardingType;
  }

  public void setOnboardingType(OnboardingTypesEnum onboardingTypesEnum) {
    this.onboardingType = onboardingTypesEnum;
  }

  public String getNetworkPackageName() {
    return networkPackageName;
  }

  public void setNetworkPackageName(String networkPackageName) {
    this.networkPackageName = networkPackageName;
  }

  /**
   * Add structure error.
   *
   * @param fileName     the file name
   * @param errorMessage the error message
   */
  public void addStructureError(String fileName, ErrorMessage errorMessage) {
    List<ErrorMessage> errorList = errors.get(fileName);
    if (errorList == null) {
      errorList = new ArrayList<>();
      errors.put(fileName, errorList);
    }
    errorList.add(errorMessage);
    if (ErrorLevel.ERROR.equals(errorMessage.getLevel())) {
      status = UploadFileStatus.Failure;
    }
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

  public Map<String, List<ErrorMessage>> getErrors() {
    return errors;
  }
}
