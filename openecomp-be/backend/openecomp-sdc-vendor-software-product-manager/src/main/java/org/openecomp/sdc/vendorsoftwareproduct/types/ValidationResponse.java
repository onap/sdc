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
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.utils.VendorSoftwareProductUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ValidationResponse {
  protected static Logger logger = (Logger) LoggerFactory.getLogger(ValidationResponse.class);
  private boolean valid = true;
  private Collection<ErrorCode> vspErrors;
  private Collection<ErrorCode> licensingDataErrors;
  private Map<String, List<ErrorMessage>> uploadDataErrors;
  private Map<String, List<ErrorMessage>> compilationErrors;
  private QuestionnaireValidationResult questionnaireValidationResult;
  private ComponentValidationResult componentValidationResult;
  private DeploymentFlavorValidationResult deploymentFlavorValidationResult;


  public boolean isValid() {
    return valid;
  }

  public Collection<ErrorCode> getVspErrors() {
    return vspErrors;
  }

  /**
   * Sets vsp errors.
   *
   * @param vspErrors         the vsp errors
   * @param serviceName       the service name
   * @param targetServiceName the target service name
   */
  public void setVspErrors(Collection<ErrorCode> vspErrors, LoggerServiceName serviceName,
                           String targetServiceName) {
    this.vspErrors = vspErrors;
    if (CollectionUtils.isNotEmpty(vspErrors)) {
      valid = false;
    }

    VendorSoftwareProductUtils.setErrorsIntoLogger(vspErrors, serviceName, targetServiceName);
  }


  public Collection<ErrorCode> getLicensingDataErrors() {
    return licensingDataErrors;
  }

  /**
   * Sets licensing data errors.
   *
   * @param licensingDataErrors the licensing data errors
   */
  public void setLicensingDataErrors(Collection<ErrorCode> licensingDataErrors) {
    this.licensingDataErrors = licensingDataErrors;
    if (CollectionUtils.isNotEmpty(licensingDataErrors)) {
      valid = false;
    }
  }

  public Map<String, List<ErrorMessage>> getUploadDataErrors() {
    return uploadDataErrors;
  }

  /**
   * Sets upload data errors.
   *
   * @param uploadDataErrors  the upload data errors
   * @param serviceName       the service name
   * @param targetServiceName the target service name
   */
  public void setUploadDataErrors(Map<String, List<ErrorMessage>> uploadDataErrors,
                                  LoggerServiceName serviceName, String targetServiceName) {
    this.uploadDataErrors = uploadDataErrors;
    if (MapUtils.isNotEmpty(uploadDataErrors)) {
      valid = false;
    }

    VendorSoftwareProductUtils
        .setErrorsIntoLogger(uploadDataErrors, serviceName, targetServiceName);
  }

  public Map<String, List<ErrorMessage>> getCompilationErrors() {
    return compilationErrors;
  }

  /**
   * Sets compilation errors.
   *
   * @param compilationErrors the compilation errors
   * @param serviceName       the service name
   * @param targetServiceName the target service name
   */
  public void setCompilationErrors(Map<String, List<ErrorMessage>> compilationErrors,
                                   LoggerServiceName serviceName, String targetServiceName) {
    this.compilationErrors = compilationErrors;
    if (MapUtils.isNotEmpty(compilationErrors)) {
      valid = false;
    }

    VendorSoftwareProductUtils
        .setErrorsIntoLogger(uploadDataErrors, serviceName, targetServiceName);
  }

  public QuestionnaireValidationResult getQuestionnaireValidationResult() {
    return questionnaireValidationResult;
  }

  /**
   * Sets questionnaire validation result.
   *
   * @param questionnaireValidationResult the questionnaire validation result
   */
  public void setQuestionnaireValidationResult(
      QuestionnaireValidationResult questionnaireValidationResult) {
    this.questionnaireValidationResult = questionnaireValidationResult;
    if (questionnaireValidationResult != null && !questionnaireValidationResult.isValid()) {
      valid = false;
    }
  }


  public ComponentValidationResult getComponentValidationResult() {
    return componentValidationResult;
  }

  /**
   * Sets Component validation result.
   *
   * @param componentValidationResult the Component validation result
   */
  public void setComponentValidationResult(
      ComponentValidationResult componentValidationResult) {
    this.componentValidationResult = componentValidationResult;
    if (componentValidationResult != null && !componentValidationResult.isValid()) {
      valid = false;
    }
  }


  public DeploymentFlavorValidationResult getDeploymentFlavorValidationResult() {
    return deploymentFlavorValidationResult;
  }

  /**
   * Sets Deployment validation result.
   *
   * @param deploymentFlavorValidationResult the Deployment validation result
   */
  public void setDeploymentFlavorValidationResult(
      DeploymentFlavorValidationResult deploymentFlavorValidationResult) {
    this.deploymentFlavorValidationResult = deploymentFlavorValidationResult;
    if (deploymentFlavorValidationResult != null && !deploymentFlavorValidationResult.isValid()) {
      valid = false;
    }
  }


}
