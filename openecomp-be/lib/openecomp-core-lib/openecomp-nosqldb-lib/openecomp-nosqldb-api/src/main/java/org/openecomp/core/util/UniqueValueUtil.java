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
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;

import java.util.Optional;

public class UniqueValueUtil {
  private static final String UNIQUE_VALUE_VIOLATION = "UNIQUE_VALUE_VIOLATION";
  private static final String UNIQUE_VALUE_VIOLATION_MSG = "%s with the value '%s' already exists.";

  private static final UniqueValueDao uniqueValueDao =
      UniqueValueDaoFactory.getInstance().createInterface();
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  /**
   * Create unique value.
   *
   * @param type              the type
   * @param uniqueCombination the unique combination
   */
  public static void createUniqueValue(String type, String... uniqueCombination) {
    mdcDataDebugMessage.debugEntryMessage(null);

    Optional<String> value = formatValue(uniqueCombination);
    if (!value.isPresent()) {
      return;
    }
    validateUniqueValue(type, value.get(), uniqueCombination);
    uniqueValueDao.create(new UniqueValueEntity(type, value.get()));

    mdcDataDebugMessage.debugExitMessage(null);
  }

  /**
   * Delete unique value.
   *
   * @param type              the type
   * @param uniqueCombination the unique combination
   */
  public static void deleteUniqueValue(String type, String... uniqueCombination) {


    mdcDataDebugMessage.debugEntryMessage(null);

    Optional<String> value = formatValue(uniqueCombination);
    if (!value.isPresent()) {
      return;
    }
    uniqueValueDao.delete(new UniqueValueEntity(type, value.get()));

    mdcDataDebugMessage.debugExitMessage(null);
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


    mdcDataDebugMessage.debugEntryMessage(null);

    if (newValue == null || oldValue == null || !newValue.equalsIgnoreCase(oldValue)) {
      createUniqueValue(type, CommonMethods.concat(uniqueContext, new String[]{newValue}));
      deleteUniqueValue(type, CommonMethods.concat(uniqueContext, new String[]{oldValue}));
    }

    mdcDataDebugMessage.debugExitMessage(null);
  }

  /**
   * Validate unique value.
   *
   * @param type              the type
   * @param uniqueCombination the unique combination
   */
  public static void validateUniqueValue(String type, String... uniqueCombination) {
    mdcDataDebugMessage.debugEntryMessage(null);

    Optional<String> value = formatValue(uniqueCombination);
    if (!value.isPresent()) {
      return;
    }
    validateUniqueValue(type, value.get(), uniqueCombination);

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private static void validateUniqueValue(String type, String value, String... uniqueCombination) {
    mdcDataDebugMessage.debugEntryMessage(null);

    if (uniqueValueDao.get(new UniqueValueEntity(type, value)) != null) {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder()
          .withCategory(ErrorCategory.APPLICATION)
          .withId(UNIQUE_VALUE_VIOLATION)
          .withMessage(String.format(UNIQUE_VALUE_VIOLATION_MSG, type,
              uniqueCombination[uniqueCombination.length - 1])).build());
    }

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private static Optional<String> formatValue(String[] uniqueCombination) {


    mdcDataDebugMessage.debugEntryMessage(null);

    if (uniqueCombination == null || uniqueCombination.length == 0
        || uniqueCombination[uniqueCombination.length - 1] == null) {
      return Optional.empty();
    }

    uniqueCombination[uniqueCombination.length - 1] =
        uniqueCombination[uniqueCombination.length - 1].toLowerCase();

    mdcDataDebugMessage.debugExitMessage(null);
    return Optional.of(CommonMethods.arrayToSeparatedString(uniqueCombination, '_'));
  }
}
