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

import org.openecomp.sdcrests.common.types.ErrorCodeDto;
import org.openecomp.sdcrests.common.types.ErrorMessageDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ValidationResponseDto {
  private boolean valid;
  private Collection<ErrorCodeDto> vspErrors;
  private Collection<ErrorCodeDto> licensingDataErrors;
  private Map<String, List<ErrorMessageDto>> uploadDataErrors;
  private QuestionnaireValidationResultDto questionnaireValidationResult;
  private ComponentValidationResultDto componentValidationResult;
  private DeploymentFlavorValidationResultDto deploymentFlavorValidationResult;

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public Collection<ErrorCodeDto> getVspErrors() {
    return vspErrors;
  }

  public void setVspErrors(Collection<ErrorCodeDto> vspErrors) {
    this.vspErrors = vspErrors;
  }

  public Collection<ErrorCodeDto> getLicensingDataErrors() {
    return licensingDataErrors;
  }

  public void setLicensingDataErrors(Collection<ErrorCodeDto> licensingDataErrors) {
    this.licensingDataErrors = licensingDataErrors;
  }

  public Map<String, List<ErrorMessageDto>> getUploadDataErrors() {
    return uploadDataErrors;
  }

  public void setUploadDataErrors(Map<String, List<ErrorMessageDto>> uploadDataErrors) {
    this.uploadDataErrors = uploadDataErrors;
  }

  public QuestionnaireValidationResultDto getQuestionnaireValidationResult() {
    return questionnaireValidationResult;
  }

  public void setQuestionnaireValidationResult(
      QuestionnaireValidationResultDto questionnaireValidationResult) {
    this.questionnaireValidationResult = questionnaireValidationResult;
  }

  public ComponentValidationResultDto getComponentValidationResult() {
    return componentValidationResult;
  }

  public void setComponentValidationResult(
      ComponentValidationResultDto componentValidationResult) {
    this.componentValidationResult = componentValidationResult;
  }

  public DeploymentFlavorValidationResultDto getDeploymentFlavorValidationResult() {
    return deploymentFlavorValidationResult;
  }

  public void setDeploymentFlavorValidationResult(
      DeploymentFlavorValidationResultDto deploymentFlavorValidationResult) {
    this.deploymentFlavorValidationResult = deploymentFlavorValidationResult;
  }

}
