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

package org.openecomp.sdc.logging.context;

import org.openecomp.sdc.logging.api.context.ContextPropagationService;
import org.slf4j.MDC;

import java.util.Map;

/**
 * Propagates the <a href="http://www.slf4j.org/manual.html#mdc">SLF4J Mapped Diagnostic Context (MDC)</a>
 * of a thread onto a runnable created by that thread, so that the context is available when the runnable is executed
 * in a new thread.
 *
 * @author evitaliy
 * @since 12/09/2016.
 */
public class MDCPropagationService implements ContextPropagationService {

    public Runnable create(Runnable task) {
        return new MDCCopyingWrapper(task);
    }

    private static class MDCCopyingWrapper implements Runnable {

        private final Runnable task;
        private final Map<String, String> context;

        private MDCCopyingWrapper(Runnable task) {
            this.task = task;
            this.context = MDC.getCopyOfContextMap();
        }

        @Override
        public void run() {

            Map<String, String> oldContext = MDC.getCopyOfContextMap();
            replaceMDC(this.context);

            try {
                task.run();
            } finally {
                replaceMDC(oldContext);
            }
        }

        private static void replaceMDC(Map<String, String> context) {

            if (context == null) {
                MDC.clear();
            } else  {
                MDC.setContextMap(context);
            }
        }
    }
}
