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
 * @author EVITALIY
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
