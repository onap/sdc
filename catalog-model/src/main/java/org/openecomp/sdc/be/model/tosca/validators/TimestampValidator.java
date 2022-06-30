/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 *
 *
 */
package org.openecomp.sdc.be.model.tosca.validators;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimestampValidator implements PropertyTypeValidator {

    private static final Logger log = Logger.getLogger(TimestampValidator.class.getName());

    private static TimestampValidator timestampValidator = new TimestampValidator();

    public static TimestampValidator getInstance() {
        return timestampValidator;
    }

    @Override
    public boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        boolean isValid = true;
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ISO_DATE_TIME)
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss.SS"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .optionalStart()
                .appendOffset("+HH", "0000")
                .optionalEnd()
                .optionalStart()
                .appendLiteral(" ")
                .appendOffset("+H", "0000")
                .optionalEnd()
                .toFormatter();
        try {
            dateFormatter.parse(value);
        } catch (DateTimeParseException e) {
                isValid = false;
        }
        if (!isValid && log.isDebugEnabled()) {
            log.debug("parameter Timestamp value {} is not valid.", value);
        }
        return isValid;
    }

    @Override
    public boolean isValid(String value, String innerType) {
        return isValid(value, innerType, null);
    }
}
