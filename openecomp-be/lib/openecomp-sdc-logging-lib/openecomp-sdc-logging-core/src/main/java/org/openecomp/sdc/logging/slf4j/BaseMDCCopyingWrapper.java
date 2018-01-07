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

import org.slf4j.MDC;

import java.util.Map;

/**
 * @author EVITALIY
 * @since 08 Jan 18
 */
abstract class BaseMDCCopyingWrapper {

    private final Map<String, String> context;

    BaseMDCCopyingWrapper() {
        this.context = MDC.getCopyOfContextMap();
    }

    final Map<String, String> replace() {
        Map<String, String> old = MDC.getCopyOfContextMap();
        replaceMDC(this.context);
        return old;
    }

    final void revert(Map<String, String> old) {
        replaceMDC(old);
    }

    private static void replaceMDC(Map<String, String> context) {

        if (context == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(context);
        }
    }
}
