package org.openecomp.sdc.logging.slf4j;

import static org.openecomp.sdc.logging.slf4j.SLF4JLoggerWrapper.BEGIN_TIMESTAMP;
import static org.openecomp.sdc.logging.slf4j.SLF4JLoggerWrapper.CLIENT_IP_ADDRESS;
import static org.openecomp.sdc.logging.slf4j.SLF4JLoggerWrapper.ELAPSED_TIME;
import static org.openecomp.sdc.logging.slf4j.SLF4JLoggerWrapper.END_TIMESTAMP;
import static org.openecomp.sdc.logging.slf4j.SLF4JLoggerWrapper.RESPONSE_CODE;
import static org.openecomp.sdc.logging.slf4j.SLF4JLoggerWrapper.RESPONSE_DESCRIPTION;
import static org.openecomp.sdc.logging.slf4j.SLF4JLoggerWrapper.STATUS_CODE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.StatusCode;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.testng.annotations.Test;

/**
 * @author EVITALIY
 * @since 05 Mar 18
 */
public class SLF4JLoggerWrapperTest {

    @Test
    public void auditDoesNotFailWhenInputNull() {
        new SLF4JLoggerWrapper(this.getClass()).audit(null);
    }

    @Test
    public void beginTimeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        long start = System.currentTimeMillis();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().startTime(start).build());
        assertNotNull(spy.mdc().get(BEGIN_TIMESTAMP));
    }

    @Test
    public void entTimeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        long end = System.currentTimeMillis();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().endTime(end).build());
        assertNotNull(spy.mdc().get(END_TIMESTAMP));
    }

    @Test
    public void elapsedTimeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        long start = System.currentTimeMillis();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder()
            .startTime(start).endTime(start).build());
        assertNotNull(spy.mdc().get(ELAPSED_TIME));
    }

    @Test
    public void statusCodeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().statusCode(StatusCode.COMPLETE).build());
        assertEquals(spy.mdc().get(STATUS_CODE), StatusCode.COMPLETE.name());
    }

    @Test
    public void statusCodeEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().build());
        assertNull(spy.mdc().get(STATUS_CODE));
    }

    @Test
    public void responseCodeAvailableWhenPassed() {
        final String responseCode = "SpyResponse";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().responseCode(responseCode).build());
        assertEquals(spy.mdc().get(RESPONSE_CODE), responseCode);
    }

    @Test
    public void responseCodeEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().build());
        assertNull(spy.mdc().get(RESPONSE_CODE));
    }

    @Test
    public void responseDescriptionAvailableWhenPassed() {
        final String responseDescription = "SpyDescription";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().responseDescription(responseDescription).build());
        assertEquals(spy.mdc().get(RESPONSE_DESCRIPTION), responseDescription);
    }

    @Test
    public void responseDescriptionEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().build());
        assertNull(spy.mdc().get(RESPONSE_DESCRIPTION));
    }

    @Test
    public void clientIpAddressAvailableWhenPassed() {
        final String ipAddress = "10.56.20.20";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().clientIpAddress(ipAddress).build());
        assertEquals(spy.mdc().get(CLIENT_IP_ADDRESS), ipAddress);
    }

    @Test
    public void clientIpAddressEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().build());
        assertNull(spy.mdc().get(CLIENT_IP_ADDRESS));
    }

    @Test
    public void elapsedTimeEqualsDifferenceBetweenStartAndEnd() {
        SpyLogger spy = createSpy();
        final long diff = 1024;
        long start = System.currentTimeMillis();
        long end = start + diff;
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().startTime(start).endTime(end).build());
        assertEquals(spy.mdc().get(ELAPSED_TIME), Long.toString(diff));
    }

    interface SpyLogger extends Logger {

        Map<String, String> mdc();
    }

    private static SpyLogger createSpy() {

        return SpyLogger.class.cast(
            Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{SpyLogger.class}, new InvocationHandler() {

                    private Map<String, String> mdc;

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {

                        // return the remembered MDC for spying
                        if (method.getName().equals("mdc")) {
                            return mdc;
                        }

                        if (!method.getName().equals("info") || args.length != 2 || !args[0].equals(Markers.AUDIT)) {
                            throw new UnsupportedOperationException("Method " + method.getName() + " with arguments " +
                                Arrays.toString(args) + " wasn't supposed to be called");
                        }

                        // remember the effective MDC
                        mdc = MDC.getCopyOfContextMap();

                        return null;
                    }
                }));
    }
}