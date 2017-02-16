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

package org.openecomp.sdc.vendorsoftwareproduct.errors;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes
    .UPLOAD_INVALID;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.List;
import java.util.Map;

/**
 * The type Upload invalid error builder.
 */
public class UploadInvalidErrorBuilder extends BaseErrorBuilder {
  private static final String UPLOAD_INVALID_DETAILED_MSG =
      "File uploaded for vendor software product with Id %s and version %s is invalid: %s";
  private static final String UPLOAD_INVALID_MSG = "Uploaded file is invalid";

  /**
   * Instantiates a new Upload invalid error builder.
   *
   * @param vendorSoftwareProductId the vendor software product id
   * @param version                 the version
   * @param errors                  the errors
   */
  public UploadInvalidErrorBuilder(String vendorSoftwareProductId, Version version,
                                   Map<String, List<ErrorMessage>> errors) {
    getErrorCodeBuilder().withId(UPLOAD_INVALID);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(String
        .format(UPLOAD_INVALID_DETAILED_MSG, vendorSoftwareProductId, version.toString(),
            toString(errors)));
  }

  /**
   * Instantiates a new Upload invalid error builder.
   */
  public UploadInvalidErrorBuilder() {
    getErrorCodeBuilder().withId(UPLOAD_INVALID);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(UPLOAD_INVALID_MSG);
  }

  private String toString(Map<String, List<ErrorMessage>> errors) {
    StringBuffer sb = new StringBuffer();
    errors.entrySet().stream()
        .forEach(entry -> singleErrorToString(sb, entry.getKey(), entry.getValue()));
    return sb.toString();
  }

  private void singleErrorToString(StringBuffer sb, String fileName, List<ErrorMessage> errors) {
    sb.append(System.lineSeparator());
    sb.append(fileName);
    sb.append(sb.append(": "));
    errors.stream().forEach(
        error -> sb.append(error.getMessage()).append("[").append(error.getLevel()).append("], "));
  }

}
