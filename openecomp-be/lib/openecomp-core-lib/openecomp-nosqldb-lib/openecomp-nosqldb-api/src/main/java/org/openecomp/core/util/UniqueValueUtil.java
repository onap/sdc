/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.core.util;

import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.core.dao.types.UniqueValueEntity;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class UniqueValueUtil {

    private static final String UNIQUE_VALUE_VIOLATION = "UNIQUE_VALUE_VIOLATION";
    private static final String UNIQUE_VALUE_VIOLATION_MSG = "%s with the value '%s' already exists.";
    private static final char FORMATTED_UNIQUE_VALUE_SEPARATOR = '_';
    private final UniqueValueDao uniqueValueDao;

    public UniqueValueUtil(UniqueValueDao uniqueValueDao) {
        this.uniqueValueDao = uniqueValueDao;
    }

    private static Optional<String> formatValue(String[] uniqueCombination) {
        if (uniqueCombination == null || uniqueCombination.length == 0 || getValueWithoutContext(uniqueCombination) == null) {
            return Optional.empty();
        }
        uniqueCombination[uniqueCombination.length - 1] = getValueWithoutContext(uniqueCombination).toLowerCase();
        return Optional.of(CommonMethods.arrayToSeparatedString(uniqueCombination, FORMATTED_UNIQUE_VALUE_SEPARATOR));
    }

    private static String getValueWithoutContext(String... uniqueCombination) {
        return uniqueCombination[uniqueCombination.length - 1];
    }

    /**
     * Create unique value.
     *
     * @param type              the type
     * @param uniqueCombination the unique combination
     */
    public void createUniqueValue(String type, String... uniqueCombination) {
        String originalEntityName = null;
        if (ArrayUtils.isNotEmpty(uniqueCombination)) {
            originalEntityName = uniqueCombination[uniqueCombination.length - 1];
        }
        Optional<String> formattedValue = formatValue(uniqueCombination);
        if (formattedValue.isPresent()) {
            validateUniqueValue(type, formattedValue.get(), originalEntityName);
            uniqueValueDao.create(new UniqueValueEntity(type, formattedValue.get()));
        }
    }

    /**
     * Delete unique value.
     *
     * @param type              the type
     * @param uniqueCombination the unique combination
     */
    public void deleteUniqueValue(String type, String... uniqueCombination) {
        formatValue(uniqueCombination).ifPresent(formattedValue -> uniqueValueDao.delete(new UniqueValueEntity(type, formattedValue)));
    }

    /**
     * Update unique value.
     *
     * @param type          the type
     * @param oldValue      the old value
     * @param newValue      the new value
     * @param uniqueContext the unique context
     */
    public void updateUniqueValue(String type, String oldValue, String newValue, String... uniqueContext) {
        if (newValue == null || !newValue.equalsIgnoreCase(oldValue)) {
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
    public void validateUniqueValue(String type, String... uniqueCombination) {
        String originalEntityName = null;
        if (ArrayUtils.isNotEmpty(uniqueCombination)) {
            originalEntityName = uniqueCombination[uniqueCombination.length - 1];
        }
        Optional<String> formattedValue = formatValue(uniqueCombination);
        if (formattedValue.isPresent()) {
            validateUniqueValue(type, formattedValue.get(), originalEntityName);
        }
    }

    private void validateUniqueValue(String type, String formattedValue, String originalEntityName) {
        if (isUniqueValueOccupied(type, formattedValue)) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION).withId(UNIQUE_VALUE_VIOLATION)
                .withMessage(String.format(UNIQUE_VALUE_VIOLATION_MSG, type, originalEntityName)).build());
        }
    }

    /**
     * Checks if a unique value is taken.
     *
     * @return true if the unique value is occupied, false otherwise
     */
    public boolean isUniqueValueOccupied(String type, String... uniqueCombination) {
        return formatValue(uniqueCombination).map(formattedValue -> isUniqueValueOccupied(type, formattedValue)).orElse(false);
    }

    private boolean isUniqueValueOccupied(String type, String formattedValue) {
        return uniqueValueDao.get(new UniqueValueEntity(type, formattedValue)) != null;
    }
}
