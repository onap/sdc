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

package org.openecomp.sdc.logging.slf4j;

import java.util.EnumMap;
import java.util.Map;
import org.openecomp.sdc.logging.slf4j.SLF4JLoggingServiceProvider.ContextField;
import org.slf4j.MDC;

/**
 * Because we don't know which information should be carried over from MDC, and which shouldn't, copy just the keys that
 * the logging service uses.
 *
 * @author evitaliy
 * @since 08 Jan 2018
 */
abstract class BaseMDCCopyingWrapper {

    private final Map<ContextField, String> context;

    BaseMDCCopyingWrapper() {
        this.context = fromMdc();
    }

    final Map<ContextField, String> replace() {
        Map<ContextField, String> old = fromMdc();
        toMdc(this.context);
        return old;
    }

    final void revert(Map<ContextField, String> old) {
        toMdc(old);
    }

    private Map<ContextField, String> fromMdc() {

        Map<ContextField, String> copy = new EnumMap<>(ContextField.class);
        for (ContextField k : ContextField.values()) {
            String v = MDC.get(k.asKey());
            if (v != null) {
                copy.put(k, v);
            }
        }

        return copy;
    }

    private static void toMdc(Map<ContextField, String> context) {

        for (ContextField k : ContextField.values()) {
            String v = context.get(k);
            if (v != null) {
                MDC.put(k.asKey(), v);
            } else {
                MDC.remove(k.asKey());
            }
        }
    }
}
