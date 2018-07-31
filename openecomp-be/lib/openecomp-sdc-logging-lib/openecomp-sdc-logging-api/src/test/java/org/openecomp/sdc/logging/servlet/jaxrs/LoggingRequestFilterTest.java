///*
// * Copyright © 2016-2018 European Support Limited
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.openecomp.sdc.logging.servlet.jaxrs;
//
//import static org.easymock.EasyMock.anyObject;
//import static org.easymock.EasyMock.anyString;
//import static org.openecomp.sdc.logging.servlet.jaxrs.LoggingRequestFilter.START_TIME_KEY;
//
//import java.util.UUID;
//import javax.servlet.http.HttpServletRequest;
//import javax.ws.rs.container.ContainerRequestContext;
//import org.easymock.EasyMock;
//import org.junit.After;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.TestName;
//import org.junit.runner.RunWith;
//import org.openecomp.sdc.logging.LoggingConstants;
//import org.openecomp.sdc.logging.api.ContextData;
//import org.openecomp.sdc.logging.api.LoggingContext;
//import org.powermock.api.easymock.PowerMock;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//
///**
// * Unit testing JAX-RS request filter.
// *
// * @author evitaliy
// * @since 19 Mar 2018
// */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LoggingContext.class, ContextData.class})
//public class LoggingRequestFilterTest {
//
//    private static final String REQUEST_URI = "/test";
//    private static final String REQUEST_METHOD = "GET";
//    private static final String RANDOM_REQUEST_ID = UUID.randomUUID().toString();
//    private static final String RANDOM_PARTNER_NAME = UUID.randomUUID().toString();
//
//    @Rule
//    public TestName testName = new TestName();
//
//    /**
//     * Verify all mocks after each test.
//     */
//    @After
//    public void verifyMocks() {
//
//        try {
//            PowerMock.verifyAll();
//        } catch (AssertionError e) {
//            throw new AssertionError("Expectations failed in " + testName.getMethodName() + "()", e);
//        }
//    }
//
//    @Test
//    public void serviceNamePopulatedWhenThereIsMatchingResource() {
//
//        mockContextDataBuilder(null, null, LoggingRequestFilter.formatServiceName(REQUEST_METHOD, REQUEST_URI));
//        mockLoggingContext();
//
//        LoggingRequestFilter filter = new LoggingRequestFilter();
//        filter.setHttpRequest(mockHttpRequest(true));
//
//        filter.filter(mockContainerRequestContext(
//                new RequestIdHeader(null),
//                new PartnerHeader(null)));
//    }
//
//    @Test
//    public void serviceNameDoesNotIncludeHttpMethodWhenHttpMethodDisabled() {
//
//        mockContextDataBuilder(null, null, REQUEST_URI);
//        mockLoggingContext();
//
//        LoggingRequestFilter filter = new LoggingRequestFilter();
//        filter.setHttpMethodInServiceName(false);
//        filter.setHttpRequest(mockHttpRequest(false));
//
//        filter.filter(mockContainerRequestContext(
//                new RequestIdHeader(null),
//                new PartnerHeader(null)));
//    }
//
//    @Test
//    public void partnerNamePopulatedWhenPresentInDefaultHeader() {
//
//        mockContextDataBuilder(null, RANDOM_PARTNER_NAME,
//                LoggingRequestFilter.formatServiceName(REQUEST_METHOD, REQUEST_URI));
//        mockLoggingContext();
//
//        LoggingRequestFilter filter = new LoggingRequestFilter();
//        filter.setHttpRequest(mockHttpRequest(true));
//
//        filter.filter(mockContainerRequestContext(
//                new RequestIdHeader(null),
//                new PartnerHeader(RANDOM_PARTNER_NAME)));
//    }
//
//    @Test
//    public void partnerNamePopulatedWhenPresentInCustomHeader() {
//
//        final String partnerHeader = "x-partner-header";
//        mockContextDataBuilder(null, RANDOM_PARTNER_NAME,
//                LoggingRequestFilter.formatServiceName(REQUEST_METHOD, REQUEST_URI));
//        mockLoggingContext();
//
//        LoggingRequestFilter filter = new LoggingRequestFilter();
//        filter.setHttpRequest(mockHttpRequest(true));
//        filter.setPartnerNameHeaders(partnerHeader);
//
//        filter.filter(mockContainerRequestContext(
//                new RequestIdHeader(null),
//                new PartnerHeader(partnerHeader, RANDOM_PARTNER_NAME)));
//    }
//
//    @Test
//    public void requestIdPopulatedWhenPresentInDefaultHeader() {
//
//        mockContextDataBuilder(RANDOM_REQUEST_ID, null,
//                LoggingRequestFilter.formatServiceName(REQUEST_METHOD, REQUEST_URI));
//        mockLoggingContext();
//
//        LoggingRequestFilter filter = new LoggingRequestFilter();
//        filter.setHttpRequest(mockHttpRequest(true));
//
//        filter.filter(mockContainerRequestContext(
//                new RequestIdHeader(RANDOM_REQUEST_ID),
//                new PartnerHeader(null)));
//    }
//
//    @Test
//    public void requestIdPopulatedWhenPresentInCustomHeader() {
//
//        final String requestIdHeader = "x-request-id";
//        mockContextDataBuilder(RANDOM_REQUEST_ID, null,
//                LoggingRequestFilter.formatServiceName(REQUEST_METHOD, REQUEST_URI));
//        mockLoggingContext();
//
//        LoggingRequestFilter filter = new LoggingRequestFilter();
//        filter.setRequestIdHeaders(requestIdHeader);
//        filter.setHttpRequest(mockHttpRequest(true));
//
//        filter.filter(mockContainerRequestContext(
//                new RequestIdHeader(requestIdHeader, RANDOM_REQUEST_ID),
//                new PartnerHeader(null)));
//    }
//
//    private HttpServletRequest mockHttpRequest(boolean includeMethod) {
//
//        HttpServletRequest servletRequest = EasyMock.mock(HttpServletRequest.class);
//        EasyMock.expect(servletRequest.getRequestURI()).andReturn(REQUEST_URI);
//
//        if (includeMethod) {
//            EasyMock.expect(servletRequest.getMethod()).andReturn(REQUEST_METHOD);
//        }
//
//        EasyMock.replay(servletRequest);
//        return servletRequest;
//    }
//
//    private ContainerRequestContext mockContainerRequestContext(Header... headers) {
//
//        ContainerRequestContext requestContext = EasyMock.mock(ContainerRequestContext.class);
//
//        for (Header h : headers) {
//            EasyMock.expect(requestContext.getHeaderString(h.key)).andReturn(h.value);
//        }
//
//        requestContext.setProperty(EasyMock.eq(START_TIME_KEY), EasyMock.anyLong());
//        EasyMock.expectLastCall();
//
//        EasyMock.replay(requestContext);
//        return requestContext;
//    }
//
//    private void mockContextDataBuilder(String requestId, String partnerName, String serviceName) {
//
//        ContextData.ContextDataBuilder mockBuilder = EasyMock.mock(ContextData.ContextDataBuilder.class);
//
//        if (requestId != null) {
//            EasyMock.expect(mockBuilder.requestId(requestId)).andReturn(mockBuilder);
//        } else {
//            EasyMock.expect(mockBuilder.requestId(anyString())).andReturn(mockBuilder);
//        }
//
//        EasyMock.expect(mockBuilder.serviceName(serviceName)).andReturn(mockBuilder);
//
//        if (partnerName != null) {
//            EasyMock.expect(mockBuilder.partnerName(partnerName)).andReturn(mockBuilder);
//        }
//
//        EasyMock.expect(mockBuilder.build()).andReturn(EasyMock.mock(ContextData.class));
//        EasyMock.replay(mockBuilder);
//
//        PowerMock.mockStatic(ContextData.class);
//
//        ContextData.builder();
//        PowerMock.expectLastCall().andReturn(mockBuilder);
//
//        PowerMock.replay(ContextData.class);
//    }
//
//    private void mockLoggingContext() {
//
//        PowerMock.mockStatic(LoggingContext.class);
//
//        LoggingContext.clear();
//        EasyMock.expectLastCall();
//
//        LoggingContext.put(anyObject(ContextData.class));
//        EasyMock.expectLastCall();
//
//        PowerMock.replay(LoggingContext.class);
//    }
//
//    private abstract static class Header {
//
//        private final String key;
//        private final String value;
//
//        private Header(String key, String value) {
//            this.key = key;
//            this.value = value;
//        }
//    }
//
//    private static class PartnerHeader extends Header {
//
//        private PartnerHeader(String value) {
//            super(LoggingConstants.DEFAULT_PARTNER_NAME_HEADER, value);
//        }
//
//        private PartnerHeader(String key, String value) {
//            super(key, value);
//        }
//    }
//
//    private static class RequestIdHeader extends Header {
//
//        private RequestIdHeader(String value) {
//            super(LoggingConstants.DEFAULT_REQUEST_ID_HEADER, value);
//        }
//
//        private RequestIdHeader(String key, String value) {
//            super(key, value);
//        }
//    }
//}