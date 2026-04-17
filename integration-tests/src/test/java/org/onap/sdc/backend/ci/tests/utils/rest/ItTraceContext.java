package org.onap.sdc.backend.ci.tests.utils.rest;

import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight per-test trace context used to correlate:
 * - integration-test stdout ([IT-TRACE] lines)
 * - REST requests (header {@link #TRACE_HEADER})
 * - backend app logs (via servlet filters that log this header)
 */
public final class ItTraceContext {

    public static final String TRACE_HEADER = "X-ONAP-IT-TRACE-ID";

    private static final InheritableThreadLocal<String> TRACE_ID = new InheritableThreadLocal<>();

    private ItTraceContext() {
    }

    public static String get() {
        return TRACE_ID.get();
    }

    public static void set(final String traceId) {
        TRACE_ID.set(traceId);
    }

    public static void clear() {
        TRACE_ID.remove();
    }

    public static String initIfAbsent(final String hint) {
        if (get() == null || get().isBlank()) {
            set(newTraceId(hint));
        }
        return get();
    }

    public static String prefix() {
        final String id = get();
        return "[IT-TRACE traceId=" + (id == null ? "NONE" : id) + "] ";
    }

    private static String newTraceId(final String hint) {
        final String sanitizedHint = sanitize(hint);
        final String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return Instant.now().toString() + "|" + sanitizedHint + "|" + shortUuid;
    }

    private static String sanitize(final String value) {
        if (value == null || value.isBlank()) {
            return "NA";
        }
        final String compact = value.trim().replaceAll("[\\s\\t\\r\\n]+", "_").replaceAll("[^A-Za-z0-9_\\-\\.]", "");
        return compact.length() > 48 ? compact.substring(0, 48) : compact;
    }
}
