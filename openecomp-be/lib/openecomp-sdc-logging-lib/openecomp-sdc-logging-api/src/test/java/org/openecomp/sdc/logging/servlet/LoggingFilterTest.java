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
//package org.openecomp.sdc.logging.servlet;
//
//import org.easymock.EasyMock;
//import org.junit.After;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.TestName;
//import org.junit.runner.RunWith;
//import org.openecomp.sdc.logging.api.ContextData;
//import org.openecomp.sdc.logging.api.LoggingContext;
//import org.powermock.api.easymock.PowerMock;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import javax.servlet.FilterChain;
//import javax.servlet.FilterConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.UUID;
//
//import static org.easymock.EasyMock.anyObject;
//import static org.openecomp.sdc.logging.LoggingConstants.DEFAULT_PARTNER_NAME_HEADER;
//import static org.openecomp.sdc.logging.LoggingConstants.DEFAULT_REQUEST_ID_HEADER;
//import static org.openecomp.sdc.logging.servlet.LoggingFilter.PARTNER_NAME_HEADERS_PARAM;
//import static org.openecomp.sdc.logging.servlet.LoggingFilter.REQUEST_ID_HEADERS_PARAM;
//
///**
// * Unit-tests logging filter for initialization and data retrieval.
// *
// * @author evitaliy
// * @since 17 Aug 2016
// */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(LoggingContext.class)
//public class LoggingFilterTest {
//
//    private static final String RANDOM_REQUEST_URI = UUID.randomUUID().toString();
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
//
//    @Test
//    public void filterPopulatesValuesWhenNoInitParamsAndNoHeaders() throws IOException, ServletException {
//
//        mockLoggingContext();
//        LoggingFilter loggingFilter = new LoggingFilter();
//        loggingFilter.init(mockFilterConfig(null, null));
//        loggingFilter.doFilter(new MockRequestBuilder().build(), mockResponse(), mockChain());
//    }
//
//    @Test
//    public void filterPopulatesValuesWhenNoInitParamsAndExistingHeaders() throws IOException, ServletException {
//
//        mockLoggingContext();
//
//        LoggingFilter loggingFilter = new LoggingFilter();
//        loggingFilter.init(mockFilterConfig(null, null));
//
//        HttpServletRequest mockRequest = new MockRequestBuilder().partnerName(RANDOM_PARTNER_NAME)
//                                                                 .requestId(RANDOM_REQUEST_ID).build();
//        loggingFilter.doFilter(mockRequest, mockResponse(), mockChain());
//    }
//
//    @Test
//    public void filterPopulatesValuesWhenCustomInitParamsAndNoHeaders() throws IOException, ServletException {
//
//        mockLoggingContext();
//
//        final String requestIdHeader = "x-request";
//        final String partnerNameHeader = "x-partner";
//
//        LoggingFilter loggingFilter = new LoggingFilter();
//        FilterConfig mockConfig = mockFilterConfig(requestIdHeader, partnerNameHeader);
//        loggingFilter.init(mockConfig);
//
//        HttpServletRequest mockRequest = new MockRequestBuilder().requestIdHeader(requestIdHeader)
//                                                                 .partnerNameHeader(partnerNameHeader).build();
//        loggingFilter.doFilter(mockRequest, mockResponse(), mockChain());
//    }
//
//    @Test
//    public void filterPopulatesValuesWhenCustomInitParamsAndExistingHeaders() throws IOException, ServletException {
//
//        mockLoggingContext();
//
//        final String requestIdHeader = "x-request-id";
//        final String partnerNameHeader = "x-partner-name";
//
//        LoggingFilter loggingFilter = new LoggingFilter();
//        FilterConfig mockConfig = mockFilterConfig(requestIdHeader, partnerNameHeader);
//        loggingFilter.init(mockConfig);
//
//        HttpServletRequest mockRequest = new MockRequestBuilder()
//                .partnerNameHeader(partnerNameHeader).partnerName(RANDOM_PARTNER_NAME)
//                .requestIdHeader(requestIdHeader).requestId(RANDOM_REQUEST_ID).build();
//        loggingFilter.doFilter(mockRequest, mockResponse(), mockChain());
//    }
//
//    private FilterConfig mockFilterConfig(String requestIdHeader, String partnerNameHeader) {
//        FilterConfig config = EasyMock.mock(FilterConfig.class);
//        EasyMock.expect(config.getInitParameter(REQUEST_ID_HEADERS_PARAM)).andReturn(requestIdHeader);
//        EasyMock.expect(config.getInitParameter(PARTNER_NAME_HEADERS_PARAM)).andReturn(partnerNameHeader);
//        EasyMock.replay(config);
//        return config;
//    }
//
//    private FilterChain mockChain() throws IOException, ServletException {
//        FilterChain chain = EasyMock.mock(FilterChain.class);
//        chain.doFilter(anyObject(ServletRequest.class), anyObject(ServletResponse.class));
//        EasyMock.expectLastCall().once();
//        EasyMock.replay(chain);
//        return chain;
//    }
//
//    private ServletResponse mockResponse() {
//        HttpServletResponse servletResponse = EasyMock.mock(HttpServletResponse.class);
//        EasyMock.replay(servletResponse);
//        return servletResponse;
//    }
//
//    private void mockLoggingContext() {
//
//        PowerMock.mockStatic(LoggingContext.class);
//
//        LoggingContext.clear();
//        EasyMock.expectLastCall().times(2);
//
//        LoggingContext.put(anyObject(ContextData.class));
//        EasyMock.expectLastCall().once();
//
//        PowerMock.replay(LoggingContext.class);
//    }
//
//    private static class MockRequestBuilder {
//
//        private String requestIdHeader = DEFAULT_REQUEST_ID_HEADER;
//        private String partnerNameHeader = DEFAULT_PARTNER_NAME_HEADER;
//        private String requestId = null;
//        private String partnerName = null;
//
//        MockRequestBuilder requestIdHeader(String h) {
//            this.requestIdHeader = h;
//            return this;
//        }
//
//        MockRequestBuilder requestId(String id) {
//            this.requestId = id;
//            return this;
//        }
//
//        MockRequestBuilder partnerNameHeader(String h) {
//            this.partnerNameHeader = h;
//            return this;
//        }
//
//        MockRequestBuilder partnerName(String name) {
//            this.partnerName = name;
//            return this;
//        }
//
//        HttpServletRequest build() {
//            HttpServletRequest mockRequest = EasyMock.mock(HttpServletRequest.class);
//            EasyMock.expect(mockRequest.getRequestURI()).andReturn(RANDOM_REQUEST_URI);
//            EasyMock.expect(mockRequest.getHeader(requestIdHeader)).andReturn(requestId);
//            EasyMock.expect(mockRequest.getHeader(partnerNameHeader)).andReturn(partnerName);
//            EasyMock.replay(mockRequest);
//            return mockRequest;
//        }
//    }
//}
