/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.logging.slf4j;

import org.slf4j.MDC;

import java.util.EnumMap;
import java.util.Map;

/**
 * Because we don't know which information should be carried over from MDC, and which shouldn't, copy just the keys that
 * the logging service uses.
 *
 * @author evitaliy
 * @since 23 Mar 2018
 */
class MDCDelegate {

    private MDCDelegate() {
        // static methods only, prevent instantiation
    }

    /**
     * Get a copy of logging MDC fields.
     */
    static Map<ContextField, String> copy() {

        Map<ContextField, String> copy = new EnumMap<>(ContextField.class);
        for (ContextField k : ContextField.values()) {
            String v = MDC.get(k.asKey());
            if (v != null) {
                copy.put(k, v);
            }
        }

        return copy;
    }

    /**
     * Reads all context fields from MDC.
     */
    static Map<ContextField, String> get() {
        return get(ContextField.values());
    }

    /**
     * Reads selected fields from MDC.
     */
    static Map<ContextField, String> get(ContextField... fields) {

        Map<ContextField, String> values = new EnumMap<>(ContextField.class);

        for (ContextField key : fields) {
            values.put(key, MDC.get(key.asKey()));
        }

        return values;
    }

    /**
     * Entirely replaces the logging MDC context with the content of the argument. Logging keys that are not present in
     * the input map will be cleared from MDC.
     */
    static void replace(Map<ContextField, String> values) {

        for (ContextField key : ContextField.values()) {
            updateKey(key, values.get(key));
        }
    }

    /**
     * Push data by multiple data providers on MDC.
     */
    static void put(ContextProvider... dataProviders) {

        clear();

        for (ContextProvider provider : dataProviders) {
            push(provider.values());
        }
    }

    /**
     * Updates the logging MDC context with the content of the argument. Logging keys that are not present in the input
     * map will remain "as is", keys with null values will be cleared from MDC.
     */
    private static void push(Map<ContextField, String> values) {

        for (Map.Entry<ContextField, String> entry : values.entrySet()) {
            updateKey(entry.getKey(), entry.getValue());
        }
    }

    private static void updateKey(ContextField key, String value) {

        if (value != null) {
            MDC.put(key.asKey(), value);
        } else {
            MDC.remove(key.asKey());
        }
    }

    static void clear() {

        for (ContextField field : ContextField.values()) {
            MDC.remove(field.asKey());
        }
    }
}
