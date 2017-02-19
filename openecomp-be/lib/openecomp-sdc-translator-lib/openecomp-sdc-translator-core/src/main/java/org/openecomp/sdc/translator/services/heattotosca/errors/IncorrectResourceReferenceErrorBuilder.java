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

package org.openecomp.sdc.translator.services.heattotosca.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

public class IncorrectResourceReferenceErrorBuilder extends BaseErrorBuilder {

  private static final String INCORRECT_RESOURCE_REFERENCE_MSG =
      "resource id '%s' with type '%s' has reference to resource '%s' with"
              + " type '%s' in property '%s'. Invalid type, resource type should be type of '%s'.";

  /**
   * Instantiates a new Incorrect resource reference error builder.
   *
   * @param sourceResourceId   the source resource id
   * @param sourceResourceType the source resource type
   * @param targetResourceId   the target resource id
   * @param targetResourceType the target resource type
   * @param property           the property
   * @param validType          the valid type
   */
  public IncorrectResourceReferenceErrorBuilder(String sourceResourceId, String sourceResourceType,
                                                String targetResourceId, String targetResourceType,
                                                String property, String validType) {
    getErrorCodeBuilder().withId(TranslatorErrorCodes.INCORRECT_RESOURCE_REFERENCE);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(String
        .format(INCORRECT_RESOURCE_REFERENCE_MSG, sourceResourceId, sourceResourceType,
            targetResourceId, targetResourceType, property, validType));
  }
}
