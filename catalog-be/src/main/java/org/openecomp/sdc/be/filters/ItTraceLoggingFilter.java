package org.openecomp.sdc.be.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Logs a concise line per request when the integration-test trace header is present (or on HTTP errors),
 * and echoes the trace id back in the response.
 */
public class ItTraceLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItTraceLoggingFilter.class);

    private static final String TRACE_HEADER = "X-ONAP-IT-TRACE-ID";
    private static final String USER_ID_HEADER = "USER_ID";
    private static final String MDC_KEY = "itTraceId";

    @Override
    public void init(final FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final StatusCaptureResponseWrapper httpResponse = new StatusCaptureResponseWrapper((HttpServletResponse) response);

        final String traceId = httpRequest.getHeader(TRACE_HEADER);
        final long startNs = System.nanoTime();

        if (traceId != null && !traceId.isBlank()) {
            MDC.put(MDC_KEY, traceId);
            httpResponse.setHeader(TRACE_HEADER, traceId);
        }

        try {
            chain.doFilter(httpRequest, httpResponse);
        } catch (Throwable t) {
            final long durationMs = (System.nanoTime() - startNs) / 1_000_000;
            final String userId = httpRequest.getHeader(USER_ID_HEADER);
            LOGGER.error("IT_TRACE error traceId={} status={} durationMs={} {} {} userId={}",
                traceId, httpResponse.getStatus(), durationMs, httpRequest.getMethod(), safePath(httpRequest), userId, t);
            System.out.println("[IT_TRACE][CATALOG-BE] traceId=" + traceId + " ERROR " + httpRequest.getMethod() + " " + safePath(httpRequest) + " status=" + httpResponse.getStatus());
            throw t;
        } finally {
            final long durationMs = (System.nanoTime() - startNs) / 1_000_000;
            final int status = httpResponse.getStatus();
            final boolean shouldLog = (traceId != null && !traceId.isBlank()) || status >= 400;
            if (shouldLog) {
                final String userId = httpRequest.getHeader(USER_ID_HEADER);
                LOGGER.info("IT_TRACE traceId={} status={} durationMs={} {} {} userId={}",
                    traceId, status, durationMs, httpRequest.getMethod(), safePath(httpRequest), userId);
                if (status >= 400) {
                    System.out.println("[IT_TRACE][CATALOG-BE] traceId=" + traceId + " " + httpRequest.getMethod() + " " + safePath(httpRequest) + " status=" + status + " durationMs=" + durationMs);
                }
            }
            MDC.remove(MDC_KEY);
        }
    }

    @Override
    public void destroy() {
        // no-op
    }

    private static String safePath(final HttpServletRequest request) {
        final String uri = request.getRequestURI();
        final String qs = request.getQueryString();
        return qs == null ? uri : uri + "?" + qs;
    }

    private static final class StatusCaptureResponseWrapper extends HttpServletResponseWrapper {
        private int status = 200;

        StatusCaptureResponseWrapper(final HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(final int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        @Override
        public void sendError(final int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(final int sc, final String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        @Override
        public void sendRedirect(final String location) throws IOException {
            this.status = 302;
            super.sendRedirect(location);
        }

        @Override
        public int getStatus() {
            return status;
        }
    }
}
