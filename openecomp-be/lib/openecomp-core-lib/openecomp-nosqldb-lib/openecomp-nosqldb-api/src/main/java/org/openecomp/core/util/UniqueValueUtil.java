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

package org.openecomp.core.util;

import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.core.dao.types.UniqueValueEntity;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import java.util.Optional;

/**
 * The type Unique value util.
 */
public class UniqueValueUtil {
  /**
   * The constant UNIQUE_VALUE_VIOLATION.
   */
  public static final String UNIQUE_VALUE_VIOLATION = "UNIQUE_VALUE_VIOLATION";
  private static final String UNIQUE_VALUE_VIOLATION_MSG = "%s with the value '%s' already exists.";

  private static final UniqueValueDao uniqueValueDao =
      UniqueValueDaoFactory.getInstance().createInterface();

  /**
   * Create unique value.
   *
   * @param type              the type
   * @param uniqueCombination the unique combination
   */
  public static void createUniqueValue(String type, String... uniqueCombination) {
    Optional<String> value = formatValue(uniqueCombination);
    if (!value.isPresent()) {
      return;
    }
    validateUniqueValue(type, value.get(), uniqueCombination);
    uniqueValueDao.create(new UniqueValueEntity(type, value.get()));
  }

  /**
   * Delete unique value.
   *
   * @param type              the type
   * @param uniqueCombination the unique combination
   */
  public static void deleteUniqueValue(String type, String... uniqueCombination) {
    Optional<String> value = formatValue(uniqueCombination);
    if (!value.isPresent()) {
      return;
    }
    uniqueValueDao.delete(new UniqueValueEntity(type, value.get()));
  }

  /**
   * Update unique value.
   *
   * @param type          the type
   * @param oldValue      the old value
   * @param newValue      the new value
   * @param uniqueContext the unique context
   */
  public static void updateUniqueValue(String type, String oldValue, String newValue,
                                       String... uniqueContext) {
    if ((newValue != null && oldValue != null
        && !newValue.toLowerCase().equals(oldValue.toLowerCase()))
        || newValue == null || oldValue == null) {
      createUniqueValue(type, CommonMethods.concat(uniqueContext, new String[]{newValue}));
      deleteUniqueValue(type, CommonMethods.concat(uniqueContext, new String[]{oldValue}));
    }
  }

  /**
   * Validate unique value.
   *
   * @param type              the type
   * @param uniqueCombination the unique combination
   */
  public static void validateUniqueValue(String type, String... uniqueCombination) {
    Optional<String> value = formatValue(uniqueCombination);
    if (!value.isPresent()) {
      return;
    }
    validateUniqueValue(type, value.get(), uniqueCombination);
  }

  private static void validateUniqueValue(String type, String value, String... uniqueCombination) {
    if (uniqueValueDao.get(new UniqueValueEntity(type, value)) != null) {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder()
          .withCategory(ErrorCategory.APPLICATION)
          .withId(UNIQUE_VALUE_VIOLATION)
          .withMessage(String.format(UNIQUE_VALUE_VIOLATION_MSG, type,
              uniqueCombination[uniqueCombination.length - 1])).build());
    }
  }

  private static Optional<String> formatValue(String[] uniqueCombination) {
    if (uniqueCombination == null || uniqueCombination.length == 0
        || uniqueCombination[uniqueCombination.length - 1] == null) {
      return Optional.empty();
    }

    uniqueCombination[uniqueCombination.length - 1] =
        uniqueCombination[uniqueCombination.length - 1].toLowerCase();
    return Optional.of(CommonMethods.arrayToSeparatedString(uniqueCombination, '_'));
  }
}
