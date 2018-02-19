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

package org.openecomp.sdc.logging.ws.rs;

import static org.easymock.EasyMock.anyString;
import static org.openecomp.sdc.logging.ws.rs.LoggingRequestFilter.START_TIME_KEY;

import java.lang.reflect.Method;
import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import org.easymock.EasyMock;
import org.openecomp.sdc.logging.LoggingConstants;
import org.openecomp.sdc.logging.api.LoggingContext;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Unit testing JAX-RS request filter.
 *
 * @author evitaliy
 * @since 19 Mar 2018
 */
@PrepareForTest(LoggingContext.class)
public class LoggingRequestFilterTest extends PowerMockTestCase {

    private static final Class RESOURCE_CLASS = MockResource.class;
    private static final Method RESOURCE_METHOD = MockResource.class.getDeclaredMethods()[0];
    private static final String RESOURCE_NAME = RESOURCE_CLASS.getName() + "." + RESOURCE_METHOD.getName();

    private static final String RANDOM_REQUEST_ID = UUID.randomUUID().toString();
    private static final String RANDOM_PARTNER_NAME = UUID.randomUUID().toString();

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
    public void notHandledWhenNoMatchingResource() {

        PowerMock.mockStatic(LoggingContext.class);
        PowerMock.replay(LoggingContext.class);

        new LoggingRequestFilter().filter(mockEmptyContainerRequestContext());
    }

    @Test
    public void serviceNamePopulatedWhenThereIsMatchingResource() {

        mockLoggingContext(null, RESOURCE_NAME, null);

        LoggingRequestFilter filter = new LoggingRequestFilter();
        filter.setResource(mockResource());

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(null),
                new PartnerHeader(null)));
    }

    @Test
    public void partnerNamePopulatedWhenPresentInDefaultHeader() {

        mockLoggingContext(null, RESOURCE_NAME, RANDOM_PARTNER_NAME);

        LoggingRequestFilter filter = new LoggingRequestFilter();
        filter.setResource(mockResource());

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(null),
                new PartnerHeader(RANDOM_PARTNER_NAME)));
    }

    @Test
    public void partnerNamePopulatedWhenPresentInCustomHeader() {

        final String partnerHeader = "x-partner-header";
        mockLoggingContext(null, RESOURCE_NAME, RANDOM_PARTNER_NAME);

        LoggingRequestFilter filter = new LoggingRequestFilter();
        filter.setResource(mockResource());
        filter.setPartnerNameHeader(partnerHeader);

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(null),
                new PartnerHeader(partnerHeader, RANDOM_PARTNER_NAME)));
    }

    @Test
    public void requestIdPopulatedWhenPresentInDefaultHeader() {

        mockLoggingContext(RANDOM_REQUEST_ID, RESOURCE_NAME, null);

        LoggingRequestFilter filter = new LoggingRequestFilter();
        filter.setResource(mockResource());

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(RANDOM_REQUEST_ID),
                new PartnerHeader(null)));
    }

    @Test
    public void requestIdPopulatedWhenPresentInCustomHeader() {

        final String requestIdHeader = "x-request-id";
        mockLoggingContext(RANDOM_REQUEST_ID, RESOURCE_NAME, null);

        LoggingRequestFilter filter = new LoggingRequestFilter();
        filter.setResource(mockResource());
        filter.setRequestIdHeader(requestIdHeader);

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(requestIdHeader, RANDOM_REQUEST_ID),
                new PartnerHeader(null)));
    }

    private ResourceInfo mockResource() {
        ResourceInfo resource = EasyMock.mock(ResourceInfo.class);
        EasyMock.expect(resource.getResourceClass()).andReturn(RESOURCE_CLASS);
        EasyMock.expect(resource.getResourceMethod()).andReturn(RESOURCE_METHOD);
        EasyMock.replay(resource);
        return resource;
    }

    private ContainerRequestContext mockEmptyContainerRequestContext() {
        ContainerRequestContext requestContext = EasyMock.mock(ContainerRequestContext.class);
        EasyMock.replay(requestContext);
        return requestContext;
    }

    private ContainerRequestContext mockContainerRequestContext(Header... headers) {

        ContainerRequestContext requestContext = EasyMock.mock(ContainerRequestContext.class);

        for (Header h : headers) {
            EasyMock.expect(requestContext.getHeaderString(h.key)).andReturn(h.value);
        }

        requestContext.setProperty(EasyMock.eq(START_TIME_KEY), EasyMock.anyLong());
        EasyMock.expectLastCall();

        EasyMock.replay(requestContext);
        return requestContext;
    }

    private void mockLoggingContext(String requestId, String serviceId, String partnerId) {

        PowerMock.mockStatic(LoggingContext.class);

        LoggingContext.clear();
        EasyMock.expectLastCall().once();

        LoggingContext.putRequestId(requestId == null ? anyString() : requestId);
        EasyMock.expectLastCall().once();

        if (serviceId != null) {
            LoggingContext.putServiceName(serviceId);
            EasyMock.expectLastCall().once();
        }

        if (partnerId != null) {
            LoggingContext.putPartnerName(partnerId);
            EasyMock.expectLastCall().once();
        }

        PowerMock.replay(LoggingContext.class);
    }

    private abstract static class Header {

        private final String key;
        private final String value;

        private Header(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private static class PartnerHeader extends Header {

        private PartnerHeader(String value) {
            super(LoggingConstants.DEFAULT_PARTNER_NAME_HEADER, value);
        }

        private PartnerHeader(String key, String value) {
            super(key, value);
        }
    }

    private static class RequestIdHeader extends Header {

        private RequestIdHeader(String value) {
            super(LoggingConstants.DEFAULT_REQUEST_ID_HEADER, value);
        }

        private RequestIdHeader(String key, String value) {
            super(key, value);
        }
    }

    private static class MockResource {

        @SuppressWarnings("EmptyMethod")
        void process() {
            // no-op
        }
    }
}