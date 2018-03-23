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
 * Does not store a state other than initial context values. Objects of this class may be reused by multiple threads,
 * therefore they must be stateless to prevent inadvertent exchange of context values between threads.
 *
 * @author evitaliy
 * @since 08 Jan 2018
 */
final class Context {

    private final Map<ContextField, String> originalCtx;

    Context() {
        this.originalCtx = MDCDelegate.copy();
    }

    /**
     * Pushes the initial context onto current thread, and returns the existing context. The result cannot be stored as
     * local state (see the class comments), and must be kept in a local variable to work properly.
     *
     * @return previous context values
     */
    final Map<ContextField, String> replace() {
        Map<ContextField, String> old = MDCDelegate.copy();
        MDCDelegate.replace(this.originalCtx);
        return old;
    }

    /**
     * Pushes an old context onto current thread.
     *
     * @param old copy of the old context returned by {@link #replace()}
     */
    final void revert(Map<ContextField, String> old) {
        MDCDelegate.replace(old);
    }
}
