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

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import org.openecomp.sdc.logging.slf4j.SLF4JLoggingServiceProvider.ContextField;
import org.openecomp.sdc.logging.spi.LoggingContextService;
import org.slf4j.MDC;

/**
 * @author evitaliy
 * @since 08 Mar 18
 */
class ContextPropagationTestHelper {

    private static final Map<ContextField, String> EMPTY_CONTEXT =
        Collections.unmodifiableMap(new EnumMap<>(ContextField.class));

    static Map<ContextField, String> putUniqueValues(LoggingContextService ctx) {

        Map<ContextField, String> values = new EnumMap<>(ContextField.class);

        String service = UUID.randomUUID().toString();
        ctx.putServiceName(service);
        values.put(ContextField.SERVICE_NAME, service);

        String partner = UUID.randomUUID().toString();
        ctx.putPartnerName(partner);
        values.put(ContextField.PARTNER_NAME, partner);

        String request = UUID.randomUUID().toString();
        ctx.putRequestId(request);
        values.put(ContextField.REQUEST_ID, request);

        return values;
    }

    static void assertContextFields(Map<ContextField, String> values, String error) {

        for (ContextField f : ContextField.values()) {
            assertEquals(MDC.get(f.asKey()), values.get(f), error);
        }
    }

    static void assertContextEmpty(String error) {
        assertContextFields(EMPTY_CONTEXT, error);
    }
}
