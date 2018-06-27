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

import static org.junit.Assert.assertEquals;

import java.util.EnumMap;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import org.slf4j.MDC;

/**
 * Unit-tests context replacement on MDC.
 *
 * @author evitaliy
 * @since 23 Mar 2018
 */
public class ContextTest {

    private static final ContextField FIELD = ContextField.SERVICE_NAME;
    private static final String KEY = FIELD.asKey();
    private static final String VALUE = "service-name-value";

    @After
    public void clearMdc() {
        MDC.clear();
    }

    @Test
    public void mdcUpdatedWhenContextReplaced() {

        MDC.put(KEY, VALUE);
        Context context = new Context();
        MDC.put(KEY, "modified-" + VALUE);

        context.replace();
        assertEquals(VALUE, MDC.get(KEY));
    }

    @Test
    public void oldValueReturnedWhenContextReplaced() {

        MDC.put(KEY, VALUE);
        Map<ContextField, String> old = new Context().replace();
        assertEquals(1, old.size());
        assertEquals(VALUE, old.get(FIELD));
    }

    @Test
    public void mdcUpdatedWhenContextReverted() {

        Context context = new Context();
        Map<ContextField, String> values = new EnumMap<>(ContextField.class);
        values.put(FIELD, VALUE);
        context.revert(values);
        assertEquals(VALUE, MDC.get(KEY));
    }
}
