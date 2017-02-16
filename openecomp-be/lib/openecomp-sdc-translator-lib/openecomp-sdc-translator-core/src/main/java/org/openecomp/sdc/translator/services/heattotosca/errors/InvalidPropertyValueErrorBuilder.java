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


public class InvalidPropertyValueErrorBuilder extends BaseErrorBuilder {

  private static final String INVALID_FILED_VALUE_MSG =
      "'%s' property has invalid value. Actual value is '%s' while '%s' value expected.";

  /**
   * Instantiates a new Invalid property value error builder.
   *
   * @param propertyName  the property name
   * @param actualValue   the actual value
   * @param expectedValue the expected value
   */
  public InvalidPropertyValueErrorBuilder(String propertyName, String actualValue,
                                          String expectedValue) {
    getErrorCodeBuilder().withId(TranslatorErrorCodes.INVALID_PROPERTY_VALUE);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(
        String.format(INVALID_FILED_VALUE_MSG, propertyName, actualValue, expectedValue));
  }

}
