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

import static org.openecomp.sdc.logging.slf4j.SLF4JLoggingServiceProvider.ALL_KEYS;

import java.util.HashMap;
import org.slf4j.MDC;

import java.util.Map;

/**
 * Because we don't know which information should be carried over from MDC, and which shouldn't, copy just the keys that
 * the logging service uses.
 *
 * @author evitaliy
 * @since 08 Jan 2018
 */
abstract class BaseMDCCopyingWrapper {

    private final Map<String, String> context;

    BaseMDCCopyingWrapper() {
        this.context = fromMdc();
    }

    final Map<String, String> replace() {
        Map<String, String> old = fromMdc();
        toMdc(this.context);
        return old;
    }

    final void revert(Map<String, String> old) {
        toMdc(old);
    }

    private Map<String, String> fromMdc() {

        Map<String, String> copy = new HashMap<>(ALL_KEYS.length);
        for (String k : ALL_KEYS) {
            String v = MDC.get(k);
            if (v != null) {
                copy.put(k, v);
            }
        }

        return copy;
    }

    private static void toMdc(Map<String, String> context) {

        for (String k : ALL_KEYS) {
            String v = context.get(k);
            if (v != null) {
                MDC.put(k, v);
            } else {
                MDC.remove(k);
            }
        }
    }
}
