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

package org.openecomp.server.configuration;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.openecomp.sdc.common.filters.ContentSecurityPolicyHeaderFilter;
import org.openecomp.sdc.common.filters.DataValidatorFilter;
import org.openecomp.sdc.itempermissions.servlet.PermissionsFilter;
import org.openecomp.server.filters.ActionAuthenticationFilter;
import org.openecomp.server.filters.ActionAuthorizationFilter;
import org.openecomp.server.filters.BasicAuthenticationFilter;
import org.openecomp.server.filters.OnboardingSessionContextFilter;
import org.openecomp.server.filters.OnboardingTenantTokenFilter;
import org.openecomp.server.filters.RestrictionAccessFilter;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * The servlets and filters of the onboarding web application. {@code web.xml} keeps the context parameters and the
 * listener.
 */
public final class OnboardingWebRegistrations {

    private OnboardingWebRegistrations() {
    }

    /**
     * Must run from a listener declared in {@code web.xml}: the servlet API rejects registrations from anywhere else
     * once the context is initialising. Filters run in the order they are registered here, and all of them before
     * {@code TogglzFilter}, which togglz-servlet declares in its web-fragment.xml; hence {@code isMatchAfter=false}.
     */
    public static void register(ServletContext servletContext) {
        registerFilters(servletContext);
        registerServlets(servletContext);
    }

    private static void registerFilters(ServletContext servletContext) {
        // Both exact forms, because the UI sends the trailing slash; "/*" would also guard validation-vsp,
        // which is not tenant-aware.
        servletContext.addFilter("TenantTokenFilter", OnboardingTenantTokenFilter.class).addMappingForUrlPatterns(null, false,
            "/v1.0/vendor-license-models/*", "/v1.0/vendor-software-products", "/v1.0/vendor-software-products/", "/v1.0/items/*");

        servletContext.addFilter("dataValidatorFilter", DataValidatorFilter.class).addMappingForUrlPatterns(null, false, "/v1.0/*");

        FilterRegistration.Dynamic contentSecurityPolicy =
            servletContext.addFilter("contentSecurityPolicyHeaderFilter", ContentSecurityPolicyHeaderFilter.class);
        contentSecurityPolicy.setAsyncSupported(true);
        contentSecurityPolicy.addMappingForUrlPatterns(null, false, "/*");

        servletContext.addFilter("PermissionsFilter", PermissionsFilter.class).addMappingForUrlPatterns(null, false,
            "/v1.0/vendor-license-models/*", "/v1.0/vendor-software-products/*");

        FilterRegistration.Dynamic crossOrigin = servletContext.addFilter("cross-origin", CrossOriginFilter.class);
        crossOrigin.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        crossOrigin.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "*");
        crossOrigin.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "*");
        crossOrigin.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic restrictionAccess = servletContext.addFilter("RestrictionAccessFilter", RestrictionAccessFilter.class);
        restrictionAccess.setAsyncSupported(true);
        restrictionAccess.addMappingForUrlPatterns(null, false, "/*");

        servletContext.addFilter("BasicAuth", BasicAuthenticationFilter.class).addMappingForUrlPatterns(null, false, "/1.0/*");
        servletContext.addFilter("AuthN", ActionAuthenticationFilter.class).addMappingForUrlPatterns(null, false, "/workflow/v1.0/actions/*");
        servletContext.addFilter("AuthZ", ActionAuthorizationFilter.class).addMappingForUrlPatterns(null, false, "/workflow/v1.0/actions/*");
        servletContext.addFilter("SessionContextFilter", OnboardingSessionContextFilter.class).addMappingForUrlPatterns(null, false, "/*");
    }

    private static void registerServlets(ServletContext servletContext) {
        // The name selects the servlet's own context file, WEB-INF/spring-mapper-servlet.xml.
        ServletRegistration.Dynamic springMapper = servletContext.addServlet("spring-mapper", DispatcherServlet.class);
        springMapper.setLoadOnStartup(1);
        springMapper.addMapping("/ws/*");

        ServletRegistration.Dynamic cxf = servletContext.addServlet("CXFServlet", CXFServlet.class);
        cxf.setInitParameter("redirects-list", "/docs/(\\S)+\\.json");
        cxf.setInitParameter("redirect-attributes", "javax.servlet.include.request_uri");
        cxf.setInitParameter("redirect-servlet-name", "default");
        cxf.setLoadOnStartup(1);
        cxf.addMapping("/*");
    }
}
