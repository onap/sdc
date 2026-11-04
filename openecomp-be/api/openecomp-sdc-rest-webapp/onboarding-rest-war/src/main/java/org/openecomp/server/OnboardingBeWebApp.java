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

package org.openecomp.server;

import ch.qos.logback.classic.servlet.LogbackServletContextListener;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.SymlinkAllowedResourceAliasChecker;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.onap.config.impl.ConfigurationFilter;
import org.onap.config.impl.ContextListener;
import org.openecomp.sdc.be.togglz.TogglzConfiguration;
import org.openecomp.sdc.common.filters.ContentSecurityPolicyHeaderFilter;
import org.openecomp.sdc.common.filters.DataValidatorFilter;
import org.openecomp.sdc.itempermissions.servlet.PermissionsFilter;
import org.openecomp.server.filters.ActionAuthenticationFilter;
import org.openecomp.server.filters.ActionAuthorizationFilter;
import org.openecomp.server.filters.BasicAuthenticationFilter;
import org.openecomp.server.filters.OnboardingSessionContextFilter;
import org.openecomp.server.filters.OnboardingTenantTokenFilter;
import org.openecomp.server.filters.RestrictionAccessFilter;
import org.togglz.console.TogglzConsoleServlet;
import org.togglz.servlet.TogglzFilter;

/**
 * The onboarding-be web application, as the Jetty distribution assembled it from the WAR's {@code web.xml} and
 * startup listener, the {@code web-fragment.xml} files of togglz-console and togglz-servlet, the {@code @WebFilter}
 * and {@code @WebListener} classes of onap-configuration-management-core, logback's initializer, and Jetty's
 * {@code webdefault.xml}. Boot reads none of these.
 */
public final class OnboardingBeWebApp extends AbstractConfiguration {

    /**
     * The default servlet of Jetty's {@code webdefault.xml}. CXFServlet hands the requests for {@code /docs/*.json}
     * to it.
     */
    private static final Map<String, String> WEBDEFAULT_DEFAULT_SERVLET = Map.of(
        "acceptRanges", "true",
        "dirAllowed", "true",
        "welcomeServlets", "false",
        "redirectWelcome", "false",
        "maxCacheSize", "256000000",
        "maxCachedFileSize", "200000000",
        "maxCachedFiles", "2048",
        "etags", "false",
        "useFileMappedBuffer", "true");

    @Override
    public void configure(WebAppContext context) {
        // Boot clears the alias checks of a WebAppContext; this is the one Jetty installs by default.
        context.addAliasCheck(new SymlinkAllowedResourceAliasChecker(context));
        context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        context.setInitParameter("org.togglz.core.manager.TogglzConfig", TogglzConfiguration.class.getName());
        context.setWelcomeFiles(new String[]{"index.html", "index.htm", "index.jsp"});
        // Boot's own error handler would also render error pages for PUT and DELETE, which Jetty's leaves empty.
        context.setErrorHandler(new ErrorPageErrorHandler());
        context.setSecurityHandler(traceDisabled());

        context.addEventListener(new ContextListener());
        context.addEventListener(new LogbackServletContextListener());

        addFilters(context);
        addServlets(context);
    }

    private static void addFilters(ServletContextHandler context) {
        // Both exact forms, because the UI sends the trailing slash; "/*" would also guard validation-vsp,
        // which is not tenant-aware.
        addFilter(context, "TenantTokenFilter", OnboardingTenantTokenFilter.class, false,
            "/v1.0/vendor-license-models/*", "/v1.0/vendor-software-products", "/v1.0/vendor-software-products/", "/v1.0/items/*");
        addFilter(context, "dataValidatorFilter", DataValidatorFilter.class, false, "/v1.0/*");
        addFilter(context, "contentSecurityPolicyHeaderFilter", ContentSecurityPolicyHeaderFilter.class, true, "/*");
        addFilter(context, "PermissionsFilter", PermissionsFilter.class, false,
            "/v1.0/vendor-license-models/*", "/v1.0/vendor-software-products/*");
        FilterHolder crossOrigin = addFilter(context, "cross-origin", CrossOriginFilter.class, false, "/*");
        crossOrigin.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        crossOrigin.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "*");
        crossOrigin.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "*");
        addFilter(context, "RestrictionAccessFilter", RestrictionAccessFilter.class, true, "/*");
        addFilter(context, "BasicAuth", BasicAuthenticationFilter.class, false, "/1.0/*");
        addFilter(context, "AuthN", ActionAuthenticationFilter.class, false, "/workflow/v1.0/actions/*");
        addFilter(context, "AuthZ", ActionAuthorizationFilter.class, false, "/workflow/v1.0/actions/*");
        addFilter(context, "SessionContextFilter", OnboardingSessionContextFilter.class, false, "/*");
        addFilter(context, ConfigurationFilter.class.getName(), ConfigurationFilter.class, false, "/");
        addFilter(context, "TogglzFilter", TogglzFilter.class, true, "/*");
    }

    private static void addServlets(ServletContextHandler context) {
        ServletHolder defaultServlet = addServlet(context, "default", DefaultServlet.class, "/");
        defaultServlet.setInitParameters(WEBDEFAULT_DEFAULT_SERVLET);
        defaultServlet.setInitOrder(0);

        addServlet(context, "TogglzConsoleServlet", TogglzConsoleServlet.class, "/togglz/*");

        ServletHolder cxf = addServlet(context, "CXFServlet", CXFServlet.class, "/*");
        cxf.setInitParameter("redirects-list", "/docs/(\\S)+\\.json");
        cxf.setInitParameter("redirect-attributes", "javax.servlet.include.request_uri");
        cxf.setInitParameter("redirect-servlet-name", "default");
        cxf.setInitOrder(1);
    }

    /**
     * TRACE refused with 403, as {@code webdefault.xml} declared it.
     */
    private static ConstraintSecurityHandler traceDisabled() {
        Constraint forbidden = new Constraint();
        forbidden.setName("Disable TRACE");
        forbidden.setAuthenticate(true);
        forbidden.setRoles(new String[0]);
        ConstraintMapping trace = new ConstraintMapping();
        trace.setPathSpec("/");
        trace.setMethod("TRACE");
        trace.setConstraint(forbidden);

        Constraint open = new Constraint();
        open.setName("Enable everything but TRACE");
        ConstraintMapping everythingElse = new ConstraintMapping();
        everythingElse.setPathSpec("/");
        everythingElse.setMethodOmissions(new String[]{"TRACE"});
        everythingElse.setConstraint(open);

        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        security.setConstraintMappings(List.of(trace, everythingElse));
        return security;
    }

    /**
     * Without dispatcher types, as the servlet API registered the filters: REQUEST, and ASYNC as well for a filter that
     * supports it.
     */
    private static FilterHolder addFilter(ServletContextHandler context, String name, Class<? extends Filter> filterClass,
                                          boolean asyncSupported, String... pathSpecs) {
        FilterHolder holder = new FilterHolder(filterClass);
        holder.setName(name);
        holder.setAsyncSupported(asyncSupported);
        for (String pathSpec : pathSpecs) {
            context.addFilter(holder, pathSpec, null);
        }
        return holder;
    }

    private static ServletHolder addServlet(ServletContextHandler context, String name, Class<? extends Servlet> servletClass,
                                            String pathSpec) {
        ServletHolder holder = new ServletHolder(name, servletClass);
        holder.setAsyncSupported(false);
        context.addServlet(holder, pathSpec);
        return holder;
    }
}
