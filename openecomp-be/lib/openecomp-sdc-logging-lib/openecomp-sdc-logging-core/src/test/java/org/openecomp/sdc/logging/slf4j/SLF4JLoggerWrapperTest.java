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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus.COMPLETE;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import org.junit.Test;
import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.MetricsData;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;

/**
 * Unit-test of SLF4J implementation of Logger.
 *
 * @author evitaliy
 * @since 05 Mar 18
 */
@SuppressWarnings("CheckStyle")
public class SLF4JLoggerWrapperTest {

    @Test
    public void auditDoesNotFailWhenInputNull() {
        new SLF4JLoggerWrapper(this.getClass()).auditExit(null);
    }

    @Test
    public void metricsDoesNotFailWhenInputNull() {
        new SLF4JLoggerWrapper(this.getClass()).metrics((MetricsData) null);
    }

    @Test
    public void auditBeginTimeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        long start = System.currentTimeMillis();
        new SLF4JLoggerWrapper(spy).auditExit(AuditData.builder().startTime(start).build());
        assertNotNull(spy.mdc().get(AuditField.BEGIN_TIMESTAMP.asKey()));
    }

    @Test
    public void metricsBeginTimeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        long start = System.currentTimeMillis();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().startTime(start).build());
        assertNotNull(spy.mdc().get(MetricsField.BEGIN_TIMESTAMP.asKey()));
    }

    @Test
    public void auditEndTimeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        long end = System.currentTimeMillis();
        new SLF4JLoggerWrapper(spy).auditExit(AuditData.builder().endTime(end).build());
        assertNotNull(spy.mdc().get(AuditField.END_TIMESTAMP.asKey()));
    }

    @Test
    public void metricsEndTimeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        long end = System.currentTimeMillis();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().endTime(end).build());
        assertNotNull(spy.mdc().get(MetricsField.END_TIMESTAMP.asKey()));
    }

    @Test
    public void auditElapsedTimeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        long start = System.currentTimeMillis();
        new SLF4JLoggerWrapper(spy).auditExit(AuditData.builder()
                                                   .startTime(start).endTime(start + 777).build());
        assertEquals("777", spy.mdc().get(AuditField.ELAPSED_TIME.asKey()));
    }

    @Test
    public void metricsElapsedTimeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        long start = System.currentTimeMillis();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder()
                                                       .startTime(start).endTime(start + 1024).build());
        assertEquals("1024", spy.mdc().get(MetricsField.ELAPSED_TIME.asKey()));
    }

    @Test
    public void auditStatusCodeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).auditExit(AuditData.builder().statusCode(COMPLETE).build());
        assertEquals(COMPLETE.name(), spy.mdc().get(AuditField.STATUS_CODE.asKey()));
    }

    @Test
    public void metricsStatusCodeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().statusCode(COMPLETE).build());
        assertEquals(COMPLETE.name(), spy.mdc().get(MetricsField.STATUS_CODE.asKey()));
    }

    @Test
    public void auditStatusCodeEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).auditExit(AuditData.builder().build());
        assertNull(spy.mdc().get(AuditField.STATUS_CODE.asKey()));
    }

    @Test
    public void metricsStatusCodeEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().build());
        assertNull(spy.mdc().get(MetricsField.STATUS_CODE.asKey()));
    }

    @Test
    public void auditResponseCodeAvailableWhenPassed() {
        final String responseCode = "AuditSpyResponse";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).auditExit(AuditData.builder().responseCode(responseCode).build());
        assertEquals(responseCode, spy.mdc().get(AuditField.RESPONSE_CODE.asKey()));
    }

    @Test
    public void metricsResponseCodeAvailableWhenPassed() {
        final String responseCode = "MetricsSpyResponse";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().responseCode(responseCode).build());
        assertEquals(responseCode, spy.mdc().get(MetricsField.RESPONSE_CODE.asKey()));
    }

    @Test
    public void auditResponseCodeEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).auditExit(AuditData.builder().build());
        assertNull(spy.mdc().get(AuditField.RESPONSE_CODE.asKey()));
    }

    @Test
    public void metricsResponseCodeEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().build());
        assertNull(spy.mdc().get(MetricsField.RESPONSE_CODE.asKey()));
    }

    @Test
    public void auditResponseDescriptionAvailableWhenPassed() {
        final String responseDescription = "AuditSpyDescription";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).auditExit(AuditData.builder().responseDescription(responseDescription).build());
        assertEquals(responseDescription, spy.mdc().get(AuditField.RESPONSE_DESCRIPTION.asKey()));
    }

    @Test
    public void metricsResponseDescriptionAvailableWhenPassed() {
        final String responseDescription = "MetricsSpyDescription";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().responseDescription(responseDescription).build());
        assertEquals(responseDescription, spy.mdc().get(MetricsField.RESPONSE_DESCRIPTION.asKey()));
    }

    @Test
    public void auditResponseDescriptionEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).auditExit(AuditData.builder().build());
        assertNull(spy.mdc().get(AuditField.RESPONSE_DESCRIPTION.asKey()));
    }

    @Test
    public void metricsResponseDescriptionEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().build());
        assertNull(spy.mdc().get(MetricsField.RESPONSE_DESCRIPTION.asKey()));
    }

    @Test
    public void auditClientIpAddressAvailableWhenPassed() {
        final String ipAddress = "10.56.20.20";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).auditExit(AuditData.builder().clientIpAddress(ipAddress).build());
        assertEquals(ipAddress, spy.mdc().get(AuditField.CLIENT_IP_ADDRESS.asKey()));
    }

    @Test
    public void metricsClientIpAddressAvailableWhenPassed() {
        final String ipAddress = "10.56.20.22";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().clientIpAddress(ipAddress).build());
        assertEquals(ipAddress, spy.mdc().get(MetricsField.CLIENT_IP_ADDRESS.asKey()));
    }

    @Test
    public void auditClientIpAddressEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).auditExit(AuditData.builder().build());
        assertNull(spy.mdc().get(AuditField.CLIENT_IP_ADDRESS.asKey()));
    }

    @Test
    public void metricsClientIpAddressEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().build());
        assertNull(spy.mdc().get(MetricsField.CLIENT_IP_ADDRESS.asKey()));
    }

    @Test
    public void metricsTargetEntityAvailableWhenPassed() {
        final String targetEntity = "MetricsTargetEntity";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().targetEntity(targetEntity).build());
        assertEquals(targetEntity, spy.mdc().get(MetricsField.TARGET_ENTITY.asKey()));
    }

    @Test
    public void metricsTargetEntityEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().build());
        assertNull(spy.mdc().get(MetricsField.TARGET_ENTITY.asKey()));
    }

    @Test
    public void metricsTargetVirtualEntityAvailableWhenPassed() {
        final String targetEntity = "MetricsTargetVirtualEntity";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().targetVirtualEntity(targetEntity).build());
        assertEquals(targetEntity, spy.mdc().get(MetricsField.TARGET_VIRTUAL_ENTITY.asKey()));
    }

    @Test
    public void metricsTargetVirtualEntityEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).metrics(MetricsData.builder().build());
        assertNull(spy.mdc().get(MetricsField.TARGET_VIRTUAL_ENTITY.asKey()));
    }

    interface SpyLogger extends Logger {
        Map<String, String> mdc();
    }

    /**
     * Creates a in instance of Logger that can be used to track MDC changes as part of an invocation of
     * Logger.audit().
     *
     * @return object that implements {@link SpyLogger}
     */
    private static SpyLogger createSpy() {

        // build a dynamic proxy to avoid implementing the long list of Logger methods
        // when we actually need just Logger.info() with the audit marker
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (SpyLogger) Proxy.newProxyInstance(classLoader, new Class<?>[] {SpyLogger.class},
                new SpyingInvocationHandler());
    }

    private static class SpyingInvocationHandler implements InvocationHandler {

        private Map<String, String> lastMdc;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {

            if (isReturnMdcMethod(method)) {
                return lastMdc;
            }

            if (!isAuditMethod(method, args) && !isMetricsMethod(method, args)) {
                throw new UnsupportedOperationException("Method " + method.getName() + " with arguments "
                        + Arrays.toString(args) + " wasn't supposed to be called");
            }

            storeEffectiveMdc();
            return null;
        }

        private boolean isMetricsMethod(Method method, Object[] args) {
            return isSpecialLogMethod(method, args, Markers.METRICS);
        }

        private boolean isAuditMethod(Method method, Object[] args) {
            return isSpecialLogMethod(method, args, Markers.EXIT);
        }

        private boolean isSpecialLogMethod(Method method, Object[] args, Marker marker) {
            return method.getName().equals("info") && args.length > 0 && args[0].equals(marker);
        }

        private void storeEffectiveMdc() {
            lastMdc = MDC.getCopyOfContextMap();
        }

        private boolean isReturnMdcMethod(Method method) {
            return method.equals(SpyLogger.class.getDeclaredMethods()[0]);
        }
    }
}
