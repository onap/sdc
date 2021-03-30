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
package org.openecomp.sdc.be.model.tosca.validators;

import java.math.BigInteger;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerValidator implements PropertyTypeValidator {

    private static final Logger log = LoggerFactory.getLogger(IntegerValidator.class);

    private static final IntegerValidator integerValidator = new IntegerValidator();
    private final PatternBase base8Pattern = new PatternBase(Pattern.compile("([-+])?0o([0-7]+)"), 8);
    private final PatternBase base10Pattern = new PatternBase(Pattern.compile("([-+])?(0|[1-9][0-9]*)"), 10);
    private final PatternBase base16Pattern = new PatternBase(Pattern.compile("([-+])?0x([0-9a-fA-F]+)"), 16);
    private final PatternBase[] patterns = {base10Pattern, base8Pattern, base16Pattern};

    private IntegerValidator() {
    }

    public static IntegerValidator getInstance() {
        return integerValidator;
    }

    @Override
    public boolean isValid(final String value, final String innerType, final Map<String, DataTypeDefinition> allDataTypes) {

        if (value == null || value.isEmpty()) {
            return true;
        }
        for (final PatternBase patternBase : patterns) {
            final Matcher matcher = patternBase.getPattern().matcher(value);
            if (matcher.matches()) {
                try {
                    new BigInteger(matcher.group(2), patternBase.base);
                    return true;
                } catch (Exception e) {
                    log.warn("Failed to build BigInteger {}", value, e);
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isValid(final String value, final String innerType) {
        return isValid(value, innerType, null);
    }

    @Getter
    @AllArgsConstructor
    private class PatternBase {

        private final Pattern pattern;
        private final Integer base;

    }
}
