/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.config;

import ch.qos.logback.classic.ViewStatusMessagesServlet;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.onap.portalsdk.core.onboarding.crossapi.PortalRestAPIProxy;
import org.openecomp.sdc.be.filters.BasicAuthenticationFilter;
import org.openecomp.sdc.be.filters.BeServletFilter;
import org.openecomp.sdc.be.filters.CatalogTenantTokenFilter;
import org.openecomp.sdc.be.filters.ComponentsAvailabilityFilter;
import org.openecomp.sdc.be.filters.ContentSecurityPolicyHeaderFilter;
import org.openecomp.sdc.be.filters.DataValidatorFilter;
import org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.ConstraintViolationExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.DefaultExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.StorageExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.WebApplicationExceptionMapper;
import org.openecomp.sdc.be.view.MixinModelWriter;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * The servlets and filters of the catalog-be web application. {@code web.xml} keeps the context parameters, the
 * listeners, the error page, the welcome file and the Togglz console.
 */
public final class CatalogBeWebRegistrations {

    private static final String[] API_PATHS = {"/sdc2/rest/*", "/sdc/*"};

    private CatalogBeWebRegistrations() {
    }

    /**
     * Must run from a listener declared in {@code web.xml}: the servlet API rejects registrations from anywhere else
     * once the context is initialising. Filters run in the order they are registered here, and all of them before
     * {@code TogglzFilter}, which togglz-servlet declares in its web-fragment.xml; hence {@code isMatchAfter=false}.
     */
    public static void register(ServletContext servletContext) {
        registerServlets(servletContext);
        registerFilters(servletContext);
    }

    private static void registerServlets(ServletContext servletContext) {
        ServletRegistration.Dynamic jersey = servletContext.addServlet("jersey", ServletContainer.class);
        jersey.setInitParameter(ServerProperties.PROVIDER_PACKAGES, String.join(",",
            "io.swagger.v3.jaxrs2.integration.resources",
            "org.openecomp.sdc.be.servlets"));
        jersey.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, classNames(List.of(
            MultiPartFeature.class,
            BasicAuthenticationFilter.class,
            BeServletFilter.class,
            ComponentsAvailabilityFilter.class,
            RolesAllowedDynamicFeature.class,
            DefaultExceptionMapper.class,
            WebApplicationExceptionMapper.class,
            ComponentExceptionMapper.class,
            ConstraintViolationExceptionMapper.class,
            StorageExceptionMapper.class,
            MixinModelWriter.class,
            ObjectMapperProvider.class)));
        jersey.setInitParameter("exclude_url_endpoints", "/api/v3/analytics,/api/v3/storeAnalytics");
        jersey.setLoadOnStartup(1);
        jersey.addMapping("/sdc2/rest/*");

        ServletRegistration.Dynamic distribution = servletContext.addServlet("jerseyDistribution", ServletContainer.class);
        distribution.setInitParameter(ServerProperties.PROVIDER_PACKAGES, String.join(",",
            "io.swagger.v3.jaxrs2.integration.resources",
            "org.openecomp.sdc.be.distribution.servlet",
            "org.openecomp.sdc.be.externalapi.servlet"));
        distribution.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, classNames(List.of(
            MultiPartFeature.class,
            BeServletFilter.class,
            ComponentsAvailabilityFilter.class,
            DefaultExceptionMapper.class,
            WebApplicationExceptionMapper.class,
            ComponentExceptionMapper.class,
            StorageExceptionMapper.class,
            BasicAuthenticationFilter.class)));
        distribution.setLoadOnStartup(1);
        distribution.setAsyncSupported(true);
        distribution.addMapping("/sdc/*");

        ServletRegistration.Dynamic portal = servletContext.addServlet("ECOMPServlet", PortalRestAPIProxy.class);
        portal.setLoadOnStartup(3);
        portal.setAsyncSupported(true);

        ServletRegistration.Dynamic logbackStatus = servletContext.addServlet("ViewStatusMessages", ViewStatusMessagesServlet.class);
        logbackStatus.setAsyncSupported(true);
        logbackStatus.addMapping("/lbClassicStatus");

    }

    private static void registerFilters(ServletContext servletContext) {
        FilterRegistration.Dynamic contentSecurityPolicy =
            servletContext.addFilter("contentSecurityPolicyHeaderFilter", ContentSecurityPolicyHeaderFilter.class);
        contentSecurityPolicy.setAsyncSupported(true);
        contentSecurityPolicy.addMappingForUrlPatterns(null, false, "/*");

        for (String beanName : List.of("gatewayFilter", "reqValidationFilter")) {
            FilterRegistration.Dynamic proxy = servletContext.addFilter(beanName, DelegatingFilterProxy.class);
            proxy.setInitParameter("targetFilterLifecycle", "true");
            proxy.addMappingForUrlPatterns(null, false, API_PATHS);
        }

        servletContext.addFilter("dataValidatorFilter", DataValidatorFilter.class).addMappingForUrlPatterns(null, false, API_PATHS);

        servletContext.addFilter("TenantTokenFilter", CatalogTenantTokenFilter.class).addMappingForUrlPatterns(null, false,
            "/sdc2/rest/v1/catalog/resources/*", "/sdc2/rest/v1/catalog/services/*", "/sdc2/rest/v1/followed");
    }

    private static String classNames(List<Class<?>> classes) {
        return classes.stream().map(Class::getName).collect(Collectors.joining(","));
    }
}
