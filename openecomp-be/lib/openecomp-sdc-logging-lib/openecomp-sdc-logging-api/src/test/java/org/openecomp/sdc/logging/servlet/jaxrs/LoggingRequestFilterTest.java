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
import static org.openecomp.sdc.logging.servlet.jaxrs.LoggingRequestFilter.START_TIME_KEY;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import org.easymock.EasyMock;
import org.openecomp.sdc.logging.LoggingConstants;
import org.openecomp.sdc.logging.api.ContextData;
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
@PrepareForTest({LoggingContext.class, ContextData.class})
public class LoggingRequestFilterTest extends PowerMockTestCase {

    private static final Class DEFAULT_RESOURCE_CLASS = MockResource.class;
    private static final Method DEFAULT_RESOURCE_METHOD = MockResource.class.getDeclaredMethods()[0];
    private static final String DEFAULT_SERVICE_NAME =
            formatServiceName(DEFAULT_RESOURCE_CLASS, DEFAULT_RESOURCE_METHOD);

    private static final String RANDOM_REQUEST_ID = UUID.randomUUID().toString();

    private static final String RANDOM_PARTNER_NAME = UUID.randomUUID().toString();

    private static String formatServiceName(Class resourceClass, Method resourceMethod) {
        return resourceClass.getName() + "#" + resourceMethod.getName();
    }

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
    public void serviceNamePopulatedWhenThereIsMatchingResourceAndConcreteType() {

        mockContextDataBuilder(null, DEFAULT_SERVICE_NAME, null);
        mockLoggingContext();

        LoggingRequestFilter filter = new LoggingRequestFilter();
        filter.setResource(mockResource());

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(null),
                new PartnerHeader(null)));
    }

    @Test
    public void serviceNamePopulatedWhenThereIsMatchingResourceAndJavaProxyType() throws NoSuchMethodException {

        Object proxyResource = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] {MockResource.class}, (proxy, method, arguments) -> null);

        final String serviceName = formatServiceName(MockResource.class, DEFAULT_RESOURCE_METHOD);

        mockContextDataBuilder(null, serviceName, null);
        mockLoggingContext();

        LoggingRequestFilter filter = new LoggingRequestFilter();

        Class<?> proxyClass = proxyResource.getClass();
        Method proxyMethod =
                proxyClass.getMethod(DEFAULT_RESOURCE_METHOD.getName(), DEFAULT_RESOURCE_METHOD.getParameterTypes());

        filter.setResource(mockResource(proxyClass, proxyMethod));

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(null),
                new PartnerHeader(null)));
    }

    @Test
    public void serviceNameIncludesProxyClassnameWhenJavaProxyTypeAndNoMatchingInterface() {

        Object proxyResource = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] {Comparable.class}, (proxy, method, arguments) -> null);

        final String serviceName = formatServiceName(proxyResource.getClass(), DEFAULT_RESOURCE_METHOD);

        mockContextDataBuilder(null, serviceName, null);
        mockLoggingContext();

        LoggingRequestFilter filter = new LoggingRequestFilter();

        Class<?> proxyClass = proxyResource.getClass();
        filter.setResource(mockResource(proxyClass, DEFAULT_RESOURCE_METHOD));

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(null),
                new PartnerHeader(null)));
    }

    @Test
    public void partnerNamePopulatedWhenPresentInDefaultHeader() {

        mockContextDataBuilder(null, DEFAULT_SERVICE_NAME, RANDOM_PARTNER_NAME);
        mockLoggingContext();

        LoggingRequestFilter filter = new LoggingRequestFilter();
        filter.setResource(mockResource());

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(null),
                new PartnerHeader(RANDOM_PARTNER_NAME)));
    }

    @Test
    public void partnerNamePopulatedWhenPresentInCustomHeader() {

        final String partnerHeader = "x-partner-header";
        mockContextDataBuilder(null, DEFAULT_SERVICE_NAME, RANDOM_PARTNER_NAME);
        mockLoggingContext();

        LoggingRequestFilter filter = new LoggingRequestFilter();
        filter.setResource(mockResource());
        filter.setPartnerNameHeaders(partnerHeader);

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(null),
                new PartnerHeader(partnerHeader, RANDOM_PARTNER_NAME)));
    }

    @Test
    public void requestIdPopulatedWhenPresentInDefaultHeader() {

        mockContextDataBuilder(RANDOM_REQUEST_ID, DEFAULT_SERVICE_NAME, null);
        mockLoggingContext();

        LoggingRequestFilter filter = new LoggingRequestFilter();
        filter.setResource(mockResource());

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(RANDOM_REQUEST_ID),
                new PartnerHeader(null)));
    }

    @Test
    public void requestIdPopulatedWhenPresentInCustomHeader() {

        final String requestIdHeader = "x-request-id";
        mockContextDataBuilder(RANDOM_REQUEST_ID, DEFAULT_SERVICE_NAME, null);
        mockLoggingContext();

        LoggingRequestFilter filter = new LoggingRequestFilter();
        filter.setResource(mockResource());
        filter.setRequestIdHeaders(requestIdHeader);

        filter.filter(mockContainerRequestContext(
                new RequestIdHeader(requestIdHeader, RANDOM_REQUEST_ID),
                new PartnerHeader(null)));
    }

    private ResourceInfo mockResource() {
        return mockResource(DEFAULT_RESOURCE_CLASS, DEFAULT_RESOURCE_METHOD);
    }

    private ResourceInfo mockResource(Class resourceType, Method resourceMethod) {
        ResourceInfo resource = EasyMock.mock(ResourceInfo.class);
        //noinspection unchecked
        EasyMock.expect(resource.getResourceClass()).andReturn(resourceType);
        EasyMock.expect(resource.getResourceMethod()).andReturn(resourceMethod);
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

    private void mockContextDataBuilder(String requestId, String serviceName, String partnerName) {

        ContextData.ContextDataBuilder mockBuilder = EasyMock.mock(ContextData.ContextDataBuilder.class);

        if (requestId != null) {
            EasyMock.expect(mockBuilder.requestId(requestId)).andReturn(mockBuilder);
        } else {
            EasyMock.expect(mockBuilder.requestId(anyString())).andReturn(mockBuilder);
        }

        if (serviceName != null) {
            EasyMock.expect(mockBuilder.serviceName(serviceName)).andReturn(mockBuilder);
        }

        if (partnerName != null) {
            EasyMock.expect(mockBuilder.partnerName(partnerName)).andReturn(mockBuilder);
        }

        EasyMock.expect(mockBuilder.build()).andReturn(EasyMock.mock(ContextData.class));
        EasyMock.replay(mockBuilder);

        PowerMock.mockStatic(ContextData.class);

        ContextData.builder();
        PowerMock.expectLastCall().andReturn(mockBuilder);

        PowerMock.replay(ContextData.class);
    }

    private void mockLoggingContext() {

        PowerMock.mockStatic(LoggingContext.class);

        LoggingContext.clear();
        EasyMock.expectLastCall().once();

        LoggingContext.put(anyObject(ContextData.class));
        EasyMock.expectLastCall().once();

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

    private interface MockResource {

        @SuppressWarnings("EmptyMethod")
        void process();
    }

    private static class MockResourceImpl implements MockResource {

        @Override
        public void process() {
            // no-op
        }
    }


}