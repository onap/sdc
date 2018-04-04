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

import static org.openecomp.sdc.logging.LoggingConstants.DEFAULT_PARTNER_NAME_HEADER;
import static org.openecomp.sdc.logging.LoggingConstants.DEFAULT_REQUEST_ID_HEADER;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.openecomp.sdc.logging.api.ContextData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.LoggingContext;
import org.openecomp.sdc.logging.servlet.HttpHeader;

/**
 * <p>Takes care of logging initialization an HTTP request hits the application. This includes populating logging
 * context and storing the request processing start time, so that it can be used for audit. The filter was built
 * <b>works in tandem</b> with {@link LoggingResponseFilter} or a similar implementation.</p>
 * <p>The filter requires a few HTTP header names to be configured. These HTTP headers are used for propagating logging
 * and tracing information between ONAP components.</p>
 * <p>Sample configuration for a Spring environment:</p>
 * <pre>
 *     &lt;jaxrs:providers&gt;
 *         &lt;bean class="org.openecomp.sdc.logging.ws.rs.LoggingRequestFilter"&gt;
 *             &lt;property name="requestIdHeaders" value="X-ONAP-RequestID"/&gt;
 *             &lt;property name="partnerNameHeaders" value="X-ONAP-InstanceID"/&gt;
 *         &lt;/bean&gt;
 *     &lt;/jaxrs:providers&gt;
 * </pre>
 * <p>Keep in mind that the filters does nothing in case when a request cannot be mapped to a working JAX-RS resource
 * (implementation). For instance, when the path is invalid (404), or there is no handler for a particular method (405).
 * </p>
 *
 * @author evitaliy, katyr
 * @since 29 Oct 17
 *
 * @see ContainerRequestFilter
 */
@Provider
public class LoggingRequestFilter implements ContainerRequestFilter {

    static final String MULTI_VALUE_SEPARATOR = ",";

    static final String START_TIME_KEY = "audit.start.time";

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingRequestFilter.class);

    private ResourceInfo resource;

    private HttpHeader requestIdHeader = new HttpHeader(DEFAULT_REQUEST_ID_HEADER);
    private HttpHeader partnerNameHeader = new HttpHeader(DEFAULT_PARTNER_NAME_HEADER);

    /**
     * Injection of a resource that matches the request from JAX-RS context.
     *
     * @param resource automatically injected by JAX-RS container
     */
    @Context
    public void setResource(ResourceInfo resource) {
        this.resource = resource;
    }

    /**
     * Configuration parameter for request ID HTTP header.
     */
    public void setRequestIdHeaders(String requestIdHeaders) {
        LOGGER.debug("Valid request ID headers: {}", requestIdHeaders);
        this.requestIdHeader = new HttpHeader(requestIdHeaders.split(MULTI_VALUE_SEPARATOR));
    }

    /**
     * Configuration parameter for partner name HTTP header.
     */
    public void setPartnerNameHeaders(String partnerNameHeaders) {
        LOGGER.debug("Valid partner name headers: {}", partnerNameHeaders);
        this.partnerNameHeader = new HttpHeader(partnerNameHeaders.split(MULTI_VALUE_SEPARATOR));
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {

        if (resource == null) {
            // JAX-RS could not find a mapping this response, probably due to HTTP 404 (not found),
            // 405 (method not allowed), 415 (unsupported media type), etc. with a message in Web server log

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No matching resource was found for URI '{}' and method '{}'",
                        containerRequestContext.getUriInfo().getPath(), containerRequestContext.getMethod());
            }

            return;
        }

        containerRequestContext.setProperty(START_TIME_KEY, System.currentTimeMillis());

        LoggingContext.clear();

        ContextData.ContextDataBuilder contextData = ContextData.builder();
        contextData.serviceName(getServiceName());

        String partnerName = partnerNameHeader.getAny(containerRequestContext::getHeaderString);
        if (partnerName != null) {
            contextData.partnerName(partnerName);
        }

        String requestId = requestIdHeader.getAny(containerRequestContext::getHeaderString);
        contextData.requestId(requestId == null ? UUID.randomUUID().toString() : requestId);

        LoggingContext.put(contextData.build());
    }

    private String getServiceName() {

        Class<?> resourceClass = resource.getResourceClass();
        Method resourceMethod = resource.getResourceMethod();

        if (Proxy.isProxyClass(resourceClass)) {
            LOGGER.debug("Proxy class injected for JAX-RS resource");
            return getServiceNameFromJavaProxy(resourceClass, resourceMethod);
        }

        return formatServiceName(resourceClass, resourceMethod);
    }

    private String getServiceNameFromJavaProxy(Class<?> proxyType, Method resourceMethod) {

        for (Class<?> interfaceType : proxyType.getInterfaces()) {

            if (isMatchingInterface(interfaceType, resourceMethod)) {
                return formatServiceName(interfaceType, resourceMethod);
            }
        }

        LOGGER.debug("Failed to find method '{}' in interfaces implemented by injected Java proxy", resourceMethod);
        return formatServiceName(proxyType, resourceMethod);
    }

    private String formatServiceName(Class<?> resourceClass, Method resourceMethod) {
        return resourceClass.getName() + "#" + resourceMethod.getName();
    }

    private boolean isMatchingInterface(Class<?> candidateType, Method requestedMethod) {

        try {

            Method candidate = candidateType.getDeclaredMethod(requestedMethod.getName(),
                    requestedMethod.getParameterTypes());
            return candidate != null;

        } catch (NoSuchMethodException ignored) {
            // ignore and move on to the next
            LOGGER.debug("Failed to find method '{}' in interface '{}'", requestedMethod, candidateType);
        }

        return false;
    }
}
