/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */
package org.openecomp.core.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.converter.errors.CreateToscaObjectErrorBuilder;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.errors.CoreException;

public class ToscaConverterUtil {

    private static final String DEFAULT = "default";
    private static final String DEFAULT_CAPITAL = "Default";
    private static final Set<String> DEFAULT_VALUE_KEYS;

    static {
        DEFAULT_VALUE_KEYS = Stream.of(DEFAULT, DEFAULT_CAPITAL).collect(Collectors.toSet());
    }

    private ToscaConverterUtil() {
        // static utility methods only, prevent instantiation
    }

    static <T> Optional<T> createObjectFromClass(String objectId, Object objectCandidate, Class<T> classToCreate) {
        try {
            return CommonUtil.createObjectUsingSetters(objectCandidate, classToCreate);
        } catch (Exception ex) {
            throw new CoreException(new CreateToscaObjectErrorBuilder(classToCreate.getSimpleName(), objectId).build(), ex);
        }
    }

    static Optional<Object> getDefaultValue(Object entryValue, Object objectToAssignDefaultValue) {
        if (!(entryValue instanceof Map) || Objects.isNull(objectToAssignDefaultValue)) {
            return Optional.empty();
        }
        return Optional.ofNullable(getDefaultParameterValue((Map<String, Object>) entryValue));
    }

    private static Object getDefaultParameterValue(Map<String, Object> entryValue) {
        Object defaultValue = null;
        Set<String> keys = new HashSet<>(entryValue.keySet());
        keys.retainAll(DEFAULT_VALUE_KEYS);
        if (CollectionUtils.isNotEmpty(keys)) {
            defaultValue = entryValue.get(keys.iterator().next());
        }
        return defaultValue;
    }
}
