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

package org.openecomp.sdc.be.server;

import ch.qos.logback.classic.ViewStatusMessagesServlet;
import ch.qos.logback.classic.servlet.LogbackServletContextListener;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.SymlinkAllowedResourceAliasChecker;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.onap.config.impl.ConfigurationFilter;
import org.onap.config.impl.ContextListener;
import org.onap.portalsdk.core.onboarding.crossapi.PortalRestAPIProxy;
import org.onap.portalsdk.core.onboarding.listener.UserContextListener;
import org.onap.portalsdk.core.onboarding.listener.UserSessionListener;
import org.openecomp.sdc.be.filters.BasicAuthenticationFilter;
import org.openecomp.sdc.be.filters.BeServletFilter;
import org.openecomp.sdc.be.filters.CatalogTenantTokenFilter;
import org.openecomp.sdc.be.filters.ComponentsAvailabilityFilter;
import org.openecomp.sdc.be.filters.ContentSecurityPolicyHeaderFilter;
import org.openecomp.sdc.be.filters.DataValidatorFilter;
import org.openecomp.sdc.be.listen.BEAppContextListener;
import org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.ConstraintViolationExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.DefaultExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.StorageExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.WebApplicationExceptionMapper;
import org.openecomp.sdc.be.togglz.TogglzConfiguration;
import org.openecomp.sdc.be.view.MixinModelWriter;
import org.openecomp.sdc.config.ObjectMapperProvider;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.togglz.console.TogglzConsoleServlet;
import org.togglz.servlet.TogglzFilter;

/**
 * The catalog-be web application, as the Jetty distribution assembled it from the WAR's {@code web.xml}, the
 * {@code web-fragment.xml} files of togglz-console and togglz-servlet, the {@code @WebServlet}, {@code @WebFilter} and
 * {@code @WebListener} classes of epsdk-fw and onap-configuration-management-core, and Jetty's {@code webdefault.xml}.
 * Boot reads none of these.
 */
public final class CatalogBeWebApp extends AbstractConfiguration {

    private static final String[] API_PATHS = {"/sdc2/rest/*", "/sdc/*"};
    private static final EnumSet<DispatcherType> REQUEST = EnumSet.of(DispatcherType.REQUEST);

    /**
     * The default servlet of Jetty's {@code webdefault.xml}.
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
        context.setWelcomeFiles(new String[]{"swagger-ui/index.html"});
        // Boot's own error handler would also render error pages for PUT and DELETE, which Jetty's leaves empty.
        ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
        errorHandler.addErrorPage(RuntimeException.class, "/sdc2/rest/v1/catalog/handleException/");
        context.setErrorHandler(errorHandler);
        context.setSecurityHandler(traceDisabled());

        context.addEventListener(new BEAppContextListener());
        context.addEventListener(new UserSessionListener());
        context.addEventListener(new UserContextListener());
        context.addEventListener(new ContextListener());
        context.addEventListener(new LogbackServletContextListener());

        addFilters(context);
        addServlets(context);
    }

    private static void addFilters(ServletContextHandler context) {
        addFilter(context, "contentSecurityPolicyHeaderFilter", ContentSecurityPolicyHeaderFilter.class, true, "/*");
        for (String beanName : List.of("gatewayFilter", "reqValidationFilter")) {
            FilterHolder proxy = addFilter(context, beanName, DelegatingFilterProxy.class, false, API_PATHS);
            proxy.setInitParameter("targetFilterLifecycle", "true");
        }
        addFilter(context, "dataValidatorFilter", DataValidatorFilter.class, false, API_PATHS);
        addFilter(context, "TenantTokenFilter", CatalogTenantTokenFilter.class, false,
            "/sdc2/rest/v1/catalog/resources/*", "/sdc2/rest/v1/catalog/services/*", "/sdc2/rest/v1/followed");
        addFilter(context, "TogglzFilter", TogglzFilter.class, true, "/*");
        addFilter(context, ConfigurationFilter.class.getName(), ConfigurationFilter.class, false, "/");
    }

    private static void addServlets(ServletContextHandler context) {
        ServletHolder jersey = addServlet(context, "jersey", ServletContainer.class, false, "/sdc2/rest/*");
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
        jersey.setInitOrder(1);

        ServletHolder distribution = addServlet(context, "jerseyDistribution", ServletContainer.class, true, "/sdc/*");
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
        distribution.setInitOrder(1);

        // web.xml declared the portal servlet without a mapping; the @WebServlet annotation of the same class added a
        // second, mapped instance under the class name.
        addServlet(context, "ECOMPServlet", PortalRestAPIProxy.class, true).setInitOrder(3);
        addServlet(context, PortalRestAPIProxy.class.getName(), PortalRestAPIProxy.class, false, "/api/v3/*");
        addServlet(context, "ViewStatusMessages", ViewStatusMessagesServlet.class, true, "/lbClassicStatus");
        addServlet(context, "TogglzConsoleServlet", TogglzConsoleServlet.class, false, "/catalog/togglz/*");

        ServletHolder defaultServlet = addServlet(context, "default", DefaultServlet.class, false, "/");
        defaultServlet.setInitParameters(WEBDEFAULT_DEFAULT_SERVLET);
        defaultServlet.setInitOrder(0);
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

    private static FilterHolder addFilter(ServletContextHandler context, String name, Class<? extends Filter> filterClass,
                                          boolean asyncSupported, String... pathSpecs) {
        FilterHolder holder = new FilterHolder(filterClass);
        holder.setName(name);
        holder.setAsyncSupported(asyncSupported);
        for (String pathSpec : pathSpecs) {
            context.addFilter(holder, pathSpec, REQUEST);
        }
        return holder;
    }

    private static ServletHolder addServlet(ServletContextHandler context, String name, Class<? extends Servlet> servletClass,
                                            boolean asyncSupported, String... pathSpecs) {
        ServletHolder holder = new ServletHolder(name, servletClass);
        holder.setAsyncSupported(asyncSupported);
        if (pathSpecs.length == 0) {
            context.getServletHandler().addServlet(holder);
        }
        for (String pathSpec : pathSpecs) {
            context.addServlet(holder, pathSpec);
        }
        return holder;
    }

    private static String classNames(List<Class<?>> classes) {
        return classes.stream().map(Class::getName).collect(Collectors.joining(","));
    }
}
