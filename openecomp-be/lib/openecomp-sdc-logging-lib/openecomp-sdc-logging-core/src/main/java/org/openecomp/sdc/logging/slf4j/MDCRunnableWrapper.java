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

/**
 * Carries MDC values over to a Runnable from the instantiating thread to the moment the callable will run.
 *
 * @author evitaliy
 * @since 08 Jan 18
 */
class MDCRunnableWrapper implements Runnable {

    private final Context context = new Context();
    private final Runnable task;

    MDCRunnableWrapper(Runnable task) {
        this.task = task;
    }

    @Override
    public void run() {
        Map<ContextField, String> oldContext = context.replace();
        try {
            task.run();
        } finally {
            context.revert(oldContext);
        }
    }
}
