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

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Response;
import org.easymock.EasyMock;
import org.easymock.LogicalOperator;
import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.LoggingContext;
import org.openecomp.sdc.logging.api.StatusCode;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;


/**
 * Unit testing JAX-RS response filter.
 *
 * @author evitaliy
 * @since 19 Mar 2018
 */
@PrepareForTest({LoggingContext.class, LoggerFactory.class})
public class LoggingResponseFilterTest extends PowerMockTestCase {

    /**
     * Verify all mocks after each test.
     */
    @AfterMethod
    public void verifyMocks(ITestResult result) {

        try {
            PowerMock.verifyAll();
        } catch (AssertionError e) {
            throw new AssertionError("Expectations failed in: " + result.getMethod().getMethodName(), e);
        }
    }

    @Test
    public void noAuditWhenAuditDisabled() {
        mockLogger(false, AuditData.builder().build());
        mockLoggingContext();
        new LoggingResponseFilter().filter(mockDisabledRequestContext(), mockDisabledResponseContext());
    }

    private void mockLogger(boolean enabled, AuditData auditData, Consumer<Logger>... additionalMockings) {

        Logger logger = EasyMock.mock(Logger.class);

        EasyMock.expect(logger.isAuditEnabled()).andReturn(enabled).atLeastOnce();

        if (enabled) {
            logger.audit(EasyMock.cmp(auditData, new AuditDataComparator(), LogicalOperator.EQUAL));
            EasyMock.expectLastCall();
        }

        for (Consumer<Logger> mocking : additionalMockings) {
            mocking.accept(logger);
        }

        EasyMock.replay(logger);

        PowerMock.mockStatic(LoggerFactory.class);
        LoggerFactory.getLogger(LoggingResponseFilter.class);
        PowerMock.expectLastCall().andReturn(logger);
        PowerMock.replay(LoggerFactory.class);
    }

    private void mockLoggingContext() {
        PowerMock.mockStatic(LoggingContext.class);
        LoggingContext.clear();
        EasyMock.expectLastCall().once();
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

    @Test
    public void startTimeReadWhenPresentInRequestContext() {

        final String clientIp = "10.56.56.10";
        final long startTime = 12345L;
        final Response.Status ok = Response.Status.OK;

        mockLogger(true, buildAuditData(startTime, clientIp, ok, StatusCode.COMPLETE));

        mockLoggingContext();
        LoggingResponseFilter filter = new LoggingResponseFilter();
        filter.setHttpRequest(mockHttpRequest(clientIp));

        filter.filter(mockRequestContext(startTime), mockResponseContext(ok));
    }

    private AuditData buildAuditData(long startTime, String clientIp, Response.Status responseStatus,
            StatusCode status) {
        return AuditData.builder().startTime(startTime).responseCode(Integer.toString(responseStatus.getStatusCode()))
                        .responseDescription(responseStatus.getReasonPhrase()).clientIpAddress(clientIp)
                        .statusCode(status).build();
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

    @Test
    public void startTimeZeroWhenNotPresentInRequestContext() {

        final String clientIp = "10.56.56.12";
        final Response.Status ok = Response.Status.OK;

        AuditData expectedAuditData = buildAuditData(0, clientIp, ok, StatusCode.COMPLETE);

        mockLogger(true, expectedAuditData, logger -> {
            logger.error(anyString(), anyObject(Object[].class));
            EasyMock.expectLastCall();
        });

        mockLoggingContext();
        LoggingResponseFilter filter = new LoggingResponseFilter();
        filter.setHttpRequest(mockHttpRequest(clientIp));

        filter.filter(mockRequestContext(null), mockResponseContext(ok));
    }

    @Test
    public void startTimeZeroWhenIncorrectObjectType() {

        final String clientIp = "10.56.56.13";
        final Response.Status accepted = Response.Status.ACCEPTED;

        AuditData expectedAuditData = buildAuditData(0, clientIp, accepted, StatusCode.COMPLETE);

        mockLogger(true, expectedAuditData, logger -> {
            logger.error(anyString(), new Object[] {anyString(), anyString()});
            EasyMock.expectLastCall();
        });

        mockLoggingContext();
        LoggingResponseFilter filter = new LoggingResponseFilter();
        filter.setHttpRequest(mockHttpRequest(clientIp));

        filter.filter(mockRequestContext("string object"), mockResponseContext(accepted));
    }

    @Test
    public void statusErrorWhenHttpResponseGreaterThan399() {

        final Response.Status error = Response.Status.BAD_REQUEST;
        final String clientIp = "10.56.56.13";
        final long startTime = 88668603L;

        AuditData expectedAuditData = buildAuditData(startTime, clientIp, error, StatusCode.ERROR);

        mockLogger(true, expectedAuditData);

        mockLoggingContext();
        LoggingResponseFilter filter = new LoggingResponseFilter();
        filter.setHttpRequest(mockHttpRequest(clientIp));

        filter.filter(mockRequestContext(startTime), mockResponseContext(error));
    }

    private static class AuditDataComparator implements Comparator<AuditData> {

        @Override
        public int compare(AuditData one, AuditData two) {

            // don't compare end time as it changes
            if (Objects.equals(one.getClientIpAddress(), two.getClientIpAddress()) && Objects
                    .equals(one.getResponseCode(), two.getResponseCode()) && Objects
                    .equals(one.getResponseDescription(), one.getResponseDescription()) && one.getStartTime() == two
                    .getStartTime() && Objects.equals(one.getStatusCode(), two.getStatusCode())) {

                return 0;
            }

            return -1;
        }
    }
}