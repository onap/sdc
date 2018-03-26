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

import static org.openecomp.sdc.logging.slf4j.AuditField.BEGIN_TIMESTAMP;
import static org.openecomp.sdc.logging.slf4j.AuditField.CLIENT_IP_ADDRESS;
import static org.openecomp.sdc.logging.slf4j.AuditField.ELAPSED_TIME;
import static org.openecomp.sdc.logging.slf4j.AuditField.END_TIMESTAMP;
import static org.openecomp.sdc.logging.slf4j.AuditField.RESPONSE_CODE;
import static org.openecomp.sdc.logging.slf4j.AuditField.RESPONSE_DESCRIPTION;
import static org.openecomp.sdc.logging.slf4j.AuditField.STATUS_CODE;
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
 * Unit-test of SLF4J implementation of Logger.
 *
 * @author evitaliy
 * @since 05 Mar 18
 */
@SuppressWarnings("CheckStyle")
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
        assertNotNull(spy.mdc().get(BEGIN_TIMESTAMP.asKey()));
    }

    @Test
    public void entTimeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        long end = System.currentTimeMillis();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().endTime(end).build());
        assertNotNull(spy.mdc().get(END_TIMESTAMP.asKey()));
    }

    @Test
    public void elapsedTimeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        long start = System.currentTimeMillis();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder()
            .startTime(start).endTime(start).build());
        assertNotNull(spy.mdc().get(ELAPSED_TIME.asKey()));
    }

    @Test
    public void statusCodeAvailableWhenPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().statusCode(StatusCode.COMPLETE).build());
        assertEquals(spy.mdc().get(STATUS_CODE.asKey()), StatusCode.COMPLETE.name());
    }

    @Test
    public void statusCodeEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().build());
        assertNull(spy.mdc().get(STATUS_CODE.asKey()));
    }

    @Test
    public void responseCodeAvailableWhenPassed() {
        final String responseCode = "SpyResponse";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().responseCode(responseCode).build());
        assertEquals(spy.mdc().get(RESPONSE_CODE.asKey()), responseCode);
    }

    @Test
    public void responseCodeEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().build());
        assertNull(spy.mdc().get(RESPONSE_CODE.asKey()));
    }

    @Test
    public void responseDescriptionAvailableWhenPassed() {
        final String responseDescription = "SpyDescription";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().responseDescription(responseDescription).build());
        assertEquals(spy.mdc().get(RESPONSE_DESCRIPTION.asKey()), responseDescription);
    }

    @Test
    public void responseDescriptionEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().build());
        assertNull(spy.mdc().get(RESPONSE_DESCRIPTION.asKey()));
    }

    @Test
    public void clientIpAddressAvailableWhenPassed() {
        final String ipAddress = "10.56.20.20";
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().clientIpAddress(ipAddress).build());
        assertEquals(spy.mdc().get(CLIENT_IP_ADDRESS.asKey()), ipAddress);
    }

    @Test
    public void clientIpAddressEmptyWhenNotPassed() {
        SpyLogger spy = createSpy();
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().build());
        assertNull(spy.mdc().get(CLIENT_IP_ADDRESS.asKey()));
    }

    @Test
    public void elapsedTimeEqualsDifferenceBetweenStartAndEnd() {
        SpyLogger spy = createSpy();
        final long diff = 1024;
        long start = System.currentTimeMillis();
        long end = start + diff;
        new SLF4JLoggerWrapper(spy).audit(AuditData.builder().startTime(start).endTime(end).build());
        assertEquals(spy.mdc().get(ELAPSED_TIME.asKey()), Long.toString(diff));
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
        return SpyLogger.class.cast(
                Proxy.newProxyInstance(classLoader, new Class<?>[]{SpyLogger.class}, new SpyingInvocationHandler()));
    }

    private static class SpyingInvocationHandler implements InvocationHandler {

        private Map<String, String> lastMdc;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {

            if (isReturnMdcMethod(method)) {
                return lastMdc;
            }

            if (!isAuditMethod(method, args)) {
                throw new UnsupportedOperationException("Method " + method.getName() + " with arguments "
                        + Arrays.toString(args) + " wasn't supposed to be called");
            }

            storeEffectiveMdc();
            return null;
        }

        private boolean isAuditMethod(Method method, Object[] args) {
            return (method.getName().equals("info") && args.length > 0 && args[0].equals(Markers.AUDIT));
        }

        private void storeEffectiveMdc() {
            lastMdc = MDC.getCopyOfContextMap();
        }

        private boolean isReturnMdcMethod(Method method) {
            return method.equals(SpyLogger.class.getDeclaredMethods()[0]);
        }
    }
}