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

package org.openecomp.sdc.logging.servlet.jaxrs;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import org.easymock.EasyMock;
import org.easymock.LogicalOperator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.LoggingContext;
import org.openecomp.sdc.logging.api.StatusCode;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


/**
 * Unit testing JAX-RS response filter.
 *
 * @author evitaliy
 * @since 19 Mar 2018
 */
@RunWith(PowerMockRunner.class)
public class LoggingResponseFilterTest {

    private static final Class RESOURCE_TYPE = Resource.class;
    private static final Method RESOURCE_METHOD = Resource.class.getDeclaredMethods()[0];

    @Rule
    public TestName testName = new TestName();

    @Before
    public void prepareLoggingContext() {
        mockLoggingContext();
    }

    /**
     * Verify all mocks after each test.
     */
    @After
    public void verifyMocks() {

        try {
            PowerMock.verifyAll();
        } catch (AssertionError e) {
            throw new AssertionError("Expectations failed in " + testName.getMethodName() + "()", e);
        }
    }

    @Test
    @PrepareForTest({LoggingContext.class, LoggerFactory.class})
    public void noAuditWhenNoMatchingResource() {

        PowerMock.mockStatic(LoggerFactory.class);
        mockFilterLogger(logger -> {
            logger.debug(anyString());
            EasyMock.expectLastCall();
        });
        PowerMock.replay(LoggerFactory.class);

        LoggingResponseFilter responseFilter = new LoggingResponseFilter();
        responseFilter.filter(mockDisabledRequestContext(), mockDisabledResponseContext());
    }

    @Test
    @PrepareForTest({LoggingContext.class, LoggerFactory.class})
    public void noAuditWhenNullResource() {

        PowerMock.mockStatic(LoggerFactory.class);
        mockFilterLogger(logger -> {
            logger.debug(anyString());
            EasyMock.expectLastCall();
        });
        PowerMock.replay(LoggerFactory.class);

        LoggingResponseFilter responseFilter = new LoggingResponseFilter();
        responseFilter.setResource(null);
        responseFilter.filter(mockDisabledRequestContext(), mockDisabledResponseContext());
    }

    @Test
    @PrepareForTest({LoggingContext.class, LoggerFactory.class})
    public void noAuditWhenAuditDisabled() {

        PowerMock.mockStatic(LoggerFactory.class);
        mockFilterLogger();
        mockResourceLogger(false, AuditData.builder().build());
        PowerMock.replay(LoggerFactory.class);

        LoggingResponseFilter responseFilter = new LoggingResponseFilter();
        responseFilter.setResource(mockResource());
        responseFilter.filter(mockDisabledRequestContext(), mockDisabledResponseContext());
    }

    @Test
    @PrepareForTest({LoggingContext.class, LoggerFactory.class})
    public void startTimeReadWhenPresentInRequestContext() {

        final String clientIp = "IP1";
        final long startTime = 12345L;
        final Response.Status ok = Response.Status.OK;

        PowerMock.mockStatic(LoggerFactory.class);
        mockResourceLogger(true, buildAuditData(startTime, clientIp, ok, StatusCode.COMPLETE));
        mockFilterLogger();
        PowerMock.replay(LoggerFactory.class);

        LoggingResponseFilter filter = new LoggingResponseFilter();
        filter.setResource(mockResource());
        filter.setHttpRequest(mockHttpRequest(clientIp));

        filter.filter(mockRequestContext(startTime), mockResponseContext(ok));
    }

    @Test
    @PrepareForTest({LoggingContext.class, LoggerFactory.class})
    public void startTimeZeroWhenNotPresentInRequestContext() {

        final String clientIp = "IP2";
        final Response.Status ok = Response.Status.OK;

        AuditData expectedAuditData = buildAuditData(0, clientIp, ok, StatusCode.COMPLETE);

        PowerMock.mockStatic(LoggerFactory.class);
        mockResourceLogger(true, expectedAuditData);
        mockFilterLogger(logger -> {
            logger.error(anyString(), anyObject(Object[].class));
            EasyMock.expectLastCall();
        });
        PowerMock.replay(LoggerFactory.class);

        LoggingResponseFilter filter = new LoggingResponseFilter();
        filter.setResource(mockResource());
        filter.setHttpRequest(mockHttpRequest(clientIp));

        filter.filter(mockRequestContext(null), mockResponseContext(ok));
    }

    @Test
    @PrepareForTest({LoggingContext.class, LoggerFactory.class})
    public void startTimeZeroWhenIncorrectObjectType() {

        final String clientIp = "IP3";
        final Response.Status accepted = Response.Status.ACCEPTED;

        AuditData expectedAuditData = buildAuditData(0, clientIp, accepted, StatusCode.COMPLETE);

        PowerMock.mockStatic(LoggerFactory.class);
        mockFilterLogger(logger -> {
            logger.error(anyString(), anyString(), anyString(), anyObject());
            EasyMock.expectLastCall();
        });
        mockResourceLogger(true, expectedAuditData);
        PowerMock.replay(LoggerFactory.class);

        LoggingResponseFilter filter = new LoggingResponseFilter();
        filter.setResource(mockResource());
        filter.setHttpRequest(mockHttpRequest(clientIp));

        filter.filter(mockRequestContext("string object"), mockResponseContext(accepted));
    }

    @Test
    @PrepareForTest({LoggingContext.class, LoggerFactory.class})
    public void statusErrorWhenHttpResponseGreaterThan399() {

        final Response.Status error = Response.Status.BAD_REQUEST;
        final String clientIp = "IP13";
        final long startTime = 88668603L;

        AuditData expectedAuditData = buildAuditData(startTime, clientIp, error, StatusCode.ERROR);

        PowerMock.mockStatic(LoggerFactory.class);
        mockResourceLogger(true, expectedAuditData);
        mockFilterLogger();
        PowerMock.replay(LoggerFactory.class);

        LoggingResponseFilter filter = new LoggingResponseFilter();
        filter.setResource(mockResource());
        filter.setHttpRequest(mockHttpRequest(clientIp));

        filter.filter(mockRequestContext(startTime), mockResponseContext(error));
    }

    private AuditData buildAuditData(long startTime, String clientIp, Response.Status responseStatus,
            StatusCode status) {
        return AuditData.builder().startTime(startTime).responseCode(Integer.toString(responseStatus.getStatusCode()))
                        .responseDescription(responseStatus.getReasonPhrase()).clientIpAddress(clientIp)
                        .statusCode(status).build();
    }

    private void mockResourceLogger(boolean enabled, AuditData auditData) {

        Logger resourceLogger = EasyMock.mock(Logger.class);

        EasyMock.expect(resourceLogger.isAuditEnabled()).andReturn(enabled).atLeastOnce();

        if (enabled) {
            resourceLogger.audit(EasyMock.cmp(auditData, new AuditDataComparator(), LogicalOperator.EQUAL));
            EasyMock.expectLastCall();
        }

        EasyMock.replay(resourceLogger);

        LoggerFactory.getLogger(RESOURCE_TYPE);
        PowerMock.expectLastCall().andReturn(resourceLogger);
    }

    @SafeVarargs
    private final void mockFilterLogger(Consumer<Logger>... expectations) {

        Logger filterLogger = EasyMock.mock(Logger.class);

        for (Consumer<Logger> expect : expectations) {
            expect.accept(filterLogger);
        }

        EasyMock.replay(filterLogger);

        LoggerFactory.getLogger(LoggingResponseFilter.class);
        PowerMock.expectLastCall().andReturn(filterLogger);
    }

    private void mockLoggingContext() {
        PowerMock.mockStatic(LoggingContext.class);
        LoggingContext.clear();
        EasyMock.expectLastCall();
        PowerMock.replay(LoggingContext.class);
    }

    private ContainerRequestContext mockDisabledRequestContext() {
        ContainerRequestContext requestContext = EasyMock.mock(ContainerRequestContext.class);
        EasyMock.replay(requestContext);
        return requestContext;
    }

    private ContainerResponseContext mockDisabledResponseContext() {
        ContainerResponseContext responseContext = EasyMock.mock(ContainerResponseContext.class);
        EasyMock.replay(responseContext);
        return responseContext;
    }

    private HttpServletRequest mockHttpRequest(String clientIp) {
        HttpServletRequest servletRequest = EasyMock.mock(HttpServletRequest.class);
        EasyMock.expect(servletRequest.getRemoteAddr()).andReturn(clientIp);
        EasyMock.replay(servletRequest);
        return servletRequest;
    }

    private ContainerRequestContext mockRequestContext(Object startTime) {
        ContainerRequestContext requestContext = EasyMock.mock(ContainerRequestContext.class);
        EasyMock.expect(requestContext.getProperty(LoggingRequestFilter.START_TIME_KEY)).andReturn(startTime);
        EasyMock.replay(requestContext);
        return requestContext;
    }

    private ContainerResponseContext mockResponseContext(Response.StatusType statusInfo) {
        ContainerResponseContext responseContext = EasyMock.mock(ContainerResponseContext.class);
        EasyMock.expect(responseContext.getStatusInfo()).andReturn(statusInfo);
        EasyMock.replay(responseContext);
        return responseContext;
    }

    private ResourceInfo mockResource() {
        ResourceInfo resource = EasyMock.mock(ResourceInfo.class);
        //noinspection unchecked
        EasyMock.expect(resource.getResourceClass()).andReturn(RESOURCE_TYPE).anyTimes();
        EasyMock.expect(resource.getResourceMethod()).andReturn(RESOURCE_METHOD).anyTimes();
        EasyMock.replay(resource);
        return resource;
    }

    private static class AuditDataComparator implements Comparator<AuditData> {

        @Override
        public int compare(AuditData one, AuditData two) {

            // don't compare end time as it changes
            if (Objects.equals(one.getClientIpAddress(), two.getClientIpAddress())
                        && Objects.equals(one.getResponseCode(), two.getResponseCode())
                        && Objects.equals(one.getResponseDescription(), two.getResponseDescription())
                        && one.getStartTime() == two.getStartTime()
                        && Objects.equals(one.getStatusCode(), two.getStatusCode())) {

                return 0;
            }

            return -1;
        }
    }

    interface Resource {
        @SuppressWarnings("unused")
        void method();
    }
}