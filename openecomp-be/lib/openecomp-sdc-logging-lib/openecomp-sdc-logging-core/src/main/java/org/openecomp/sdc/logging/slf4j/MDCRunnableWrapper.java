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

import java.util.Map;
import org.openecomp.sdc.logging.slf4j.SLF4JLoggingServiceProvider.ContextField;

/**
 * @author EVITALIY
 * @since 08 Jan 18
 */
class MDCRunnableWrapper extends BaseMDCCopyingWrapper implements Runnable {

    private final Runnable task;

    MDCRunnableWrapper(Runnable task) {
        super();
        this.task = task;
    }

    @Override
    public void run() {

        Map<ContextField, String> oldContext = replace();

        try {
            task.run();
        } finally {
            revert(oldContext);
        }
    }
}
