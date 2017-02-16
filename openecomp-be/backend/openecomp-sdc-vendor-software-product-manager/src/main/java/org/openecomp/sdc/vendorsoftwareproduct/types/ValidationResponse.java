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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The type Validation response.
 */
public class ValidationResponse {
  private boolean valid = true;
  private Collection<ErrorCode> vspErrors;
  private Collection<ErrorCode> licensingDataErrors;
  private Map<String, List<ErrorMessage>> uploadDataErrors;
  private Map<String, List<ErrorMessage>> compilationErrors;
  private QuestionnaireValidationResult questionnaireValidationResult;

  /**
   * Is valid boolean.
   *
   * @return the boolean
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * Gets vsp errors.
   *
   * @return the vsp errors
   */
  public Collection<ErrorCode> getVspErrors() {
    return vspErrors;
  }

  /**
   * Sets vsp errors.
   *
   * @param vspErrors the vsp errors
   */
  public void setVspErrors(Collection<ErrorCode> vspErrors) {
    this.vspErrors = vspErrors;
    if (CollectionUtils.isNotEmpty(vspErrors)) {
      valid = false;
    }
  }

  /**
   * Gets licensing data errors.
   *
   * @return the licensing data errors
   */
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

  /**
   * Gets upload data errors.
   *
   * @return the upload data errors
   */
  public Map<String, List<ErrorMessage>> getUploadDataErrors() {
    return uploadDataErrors;
  }

  /**
   * Sets upload data errors.
   *
   * @param uploadDataErrors the upload data errors
   */
  public void setUploadDataErrors(Map<String, List<ErrorMessage>> uploadDataErrors) {
    this.uploadDataErrors = uploadDataErrors;
    if (MapUtils.isNotEmpty(uploadDataErrors)) {
      valid = false;
    }
  }

  /**
   * Gets compilation errors.
   *
   * @return the compilation errors
   */
  public Map<String, List<ErrorMessage>> getCompilationErrors() {
    return compilationErrors;
  }

  /**
   * Sets compilation errors.
   *
   * @param compilationErrors the compilation errors
   */
  public void setCompilationErrors(Map<String, List<ErrorMessage>> compilationErrors) {
    this.compilationErrors = compilationErrors;
    if (MapUtils.isNotEmpty(compilationErrors)) {
      valid = false;
    }
  }

  /**
   * Gets questionnaire validation result.
   *
   * @return the questionnaire validation result
   */
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
}
