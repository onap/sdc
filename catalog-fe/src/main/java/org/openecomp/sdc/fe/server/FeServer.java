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

package org.openecomp.sdc.fe.server;

import ch.qos.logback.classic.ViewStatusMessagesServlet;
import ch.qos.logback.classic.servlet.LogbackServletContextListener;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.rewrite.handler.RedirectPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.onap.logging.filter.base.AuditLogServletFilter;
import org.openecomp.sdc.fe.filters.CacheControlFilter;
import org.openecomp.sdc.fe.filters.ContentSecurityPolicyHeaderFilter;
import org.openecomp.sdc.fe.filters.DefaultUserIdFilter;
import org.openecomp.sdc.fe.filters.GzipFilter;
import org.openecomp.sdc.fe.listen.FEAppContextListener;
import org.openecomp.sdc.fe.servlets.FeProxyServlet;
import org.openecomp.sdc.fe.servlets.PortalServlet;

/**
 * The sdc-frontend server: the catalog UI and its proxy at {@code /sdc1}, the onboarding UI at {@code /onboarding}.
 * It is configured the way the Jetty distribution configured it with the image's {@code start.d} files, and reads the
 * same {@code jetty.*} properties those files set, so a deployment that overrode them sets them as system properties.
 */
public final class FeServer {

    private static final String DIR_ALLOWED = "org.eclipse.jetty.servlet.Default.dirAllowed";
    private static final EnumSet<DispatcherType> REQUEST = EnumSet.of(DispatcherType.REQUEST);

    /**
     * The default servlet of Jetty's {@code webdefault.xml}, which the distribution added to every web application.
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

    private FeServer() {
    }

    public static void main(String[] args) throws Exception {
        // Jetty would otherwise log through logback into the application's log files instead of to stderr.
        if (System.getProperty("org.eclipse.jetty.util.log.class") == null) {
            System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        }
        Server server = create(System.getProperties());
        server.start();
        server.join();
    }

    static Server create(Properties properties) {
        Server server = new Server(new QueuedThreadPool(200, 10, 60000));
        server.setStopAtShutdown(true);
        server.setStopTimeout(5000);

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecurePort(8443);
        httpConfig.setSendServerVersion(false);
        httpConfig.setSendDateHeader(false);
        httpConfig.setHeaderCacheSize(1024);
        server.addConnector(httpConnector(server, httpConfig, properties));
        server.addConnector(httpsConnector(server, httpConfig, properties));

        DefaultHandler defaultHandler = new DefaultHandler();
        defaultHandler.setShowContexts(false);
        HandlerCollection handlers = new HandlerCollection(new ContextHandlerCollection(catalogContext(), onboardingContext()), defaultHandler);

        GzipHandler gzip = new GzipHandler();
        gzip.setMinGzipSize(1024);
        gzip.setIncludedMethodList("GET,POST");
        gzip.setHandler(handlers);

        RewriteHandler rewrite = new RewriteHandler();
        rewrite.setRewriteRequestURI(true);
        rewrite.setRewritePathInfo(false);
        rewrite.setOriginalPathAttribute("requestedPath");
        rewrite.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        rewrite.addRule(new RedirectPatternRule("", "/sdc1"));
        rewrite.setHandler(gzip);
        server.setHandler(rewrite);
        return server;
    }

    private static ServerConnector httpConnector(Server server, HttpConfiguration httpConfig, Properties properties) {
        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig, HttpCompliance.RFC7230_LEGACY));
        connector.setHost(properties.getProperty("jetty.http.host"));
        connector.setPort(Integer.parseInt(properties.getProperty("jetty.http.port", "8181")));
        connector.setIdleTimeout(Long.parseLong(properties.getProperty("jetty.http.idleTimeout", "30000")));
        return connector;
    }

    /**
     * The keystore defaults are those of Jetty's {@code jetty-ssl-context.xml}; the image generates its self-signed
     * {@code etc/keystore} with the passwords they stand for.
     */
    private static ServerConnector httpsConnector(Server server, HttpConfiguration httpConfig, Properties properties) {
        SslContextFactory.Server ssl = new SslContextFactory.Server();
        ssl.setKeyStorePath(Paths.get(properties.getProperty("jetty.sslContext.keyStorePath", "etc/keystore")).toAbsolutePath().toString());
        ssl.setKeyStorePassword(properties.getProperty("jetty.sslContext.keyStorePassword", "OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4"));
        ssl.setKeyManagerPassword(properties.getProperty("jetty.sslContext.keyManagerPassword", "OBF:1u2u1wml1z7s1z7a1wnl1u2g"));
        ssl.setKeyStoreType(properties.getProperty("jetty.sslContext.keyStoreType", "JKS"));
        ssl.setTrustStorePath(Paths.get(properties.getProperty("jetty.sslContext.trustStorePath", "etc/keystore")).toAbsolutePath().toString());
        ssl.setTrustStorePassword(properties.getProperty("jetty.sslContext.trustStorePassword"));
        ssl.setTrustStoreType(properties.getProperty("jetty.sslContext.trustStoreType"));
        ssl.setNeedClientAuth(Boolean.parseBoolean(properties.getProperty("jetty.sslContext.needClientAuth", "false")));

        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());
        ServerConnector connector = new ServerConnector(server, new SslConnectionFactory(ssl, "http/1.1"),
            new HttpConnectionFactory(httpsConfig, HttpCompliance.RFC7230));
        connector.setHost(properties.getProperty("jetty.ssl.host"));
        connector.setPort(Integer.parseInt(properties.getProperty("jetty.ssl.port", "9443")));
        connector.setIdleTimeout(Long.parseLong(properties.getProperty("jetty.ssl.idleTimeout", "30000")));
        return connector;
    }

    static ServletContextHandler catalogContext() {
        ServletContextHandler context = webAppContext("/sdc1", "sdc1");
        context.addEventListener(new FEAppContextListener());
        context.addEventListener(new LogbackServletContextListener());

        addFilter(context, "contentSecurityPolicyHeaderFilter", ContentSecurityPolicyHeaderFilter.class, "/*");
        addFilter(context, "defaultUserIdFilter", DefaultUserIdFilter.class, "/*");
        addFilter(context, "AuditLogServletFilter", AuditLogServletFilter.class, "/*");
        addFilter(context, "gzipFilter", GzipFilter.class, "*.jsgz");
        addFilter(context, "cacheControlFilter", CacheControlFilter.class, "*.jsgz", "*.js", "*.css", "*.png", "*.woff", "*.woff2");

        ServletHolder jersey = servlet("jersey", ServletContainer.class);
        jersey.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "org.openecomp.sdc.fe.servlets");
        jersey.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, MultiPartFeature.class.getName());
        jersey.setInitOrder(1);
        context.addServlet(jersey, "/rest/*");
        context.addServlet(servlet("ViewStatusMessages", ViewStatusMessagesServlet.class), "/lbClassicStatus");
        ServletHolder feProxy = servlet("FeProxy", FeProxyServlet.class);
        feProxy.setInitOrder(1);
        context.addServlet(feProxy, "/feProxy/*");
        context.addServlet(servlet("Portal", PortalServlet.class), "/portal");

        ServletHolder defaultServlet = new ServletHolder("default", DefaultServlet.class);
        defaultServlet.setInitParameters(WEBDEFAULT_DEFAULT_SERVLET);
        defaultServlet.setInitOrder(0);
        defaultServlet.setAsyncSupported(false);
        context.addServlet(defaultServlet, "/");
        return context;
    }

    static ServletContextHandler onboardingContext() {
        ServletContextHandler context = webAppContext("/onboarding", "onboarding");
        ServletHolder scripts = new ServletHolder("js", DefaultServlet.class);
        scripts.setInitParameter("cacheControl", "public, no-cache");
        scripts.setInitParameter("etags", "true");
        scripts.setAsyncSupported(false);
        context.addServlet(scripts, "*.js");
        ServletHolder resources = new ServletHolder("resources", DefaultServlet.class);
        resources.setInitParameter("cacheControl", "public, max-age=31536000");
        resources.setInitParameter("etags", "true");
        resources.setAsyncSupported(false);
        context.addServlet(resources, "/");
        return context;
    }

    /**
     * A context with what a deployed WAR got from Jetty's {@code webdefault.xml} besides the default servlet: sessions
     * that time out after 30 minutes and TRACE refused with 403. The static files are read from {@code webapp/} in the
     * working directory: from inside a jar, Jetty would no longer resolve a path such as {@code //index.html}.
     */
    private static ServletContextHandler webAppContext(String contextPath, String staticRoot) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);
        context.setContextPath(contextPath);
        context.setBaseResource(Resource.newResource(Paths.get("webapp", staticRoot).toAbsolutePath().toFile()));
        context.setProtectedTargets(new String[]{"/WEB-INF", "/META-INF"});
        context.setWelcomeFiles(new String[]{"index.html"});
        context.setInitParameter(DIR_ALLOWED, "false");
        context.getSessionHandler().setMaxInactiveInterval(30 * 60);
        context.setErrorHandler(new ErrorPageErrorHandler());
        context.setSecurityHandler(traceDisabled());
        return context;
    }

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

    private static void addFilter(ServletContextHandler context, String name, Class<? extends Filter> filterClass, String... pathSpecs) {
        FilterHolder holder = new FilterHolder(filterClass);
        holder.setName(name);
        holder.setAsyncSupported(true);
        for (String pathSpec : pathSpecs) {
            context.addFilter(holder, pathSpec, REQUEST);
        }
    }

    private static ServletHolder servlet(String name, Class<? extends Servlet> servletClass) {
        ServletHolder holder = new ServletHolder(name, servletClass);
        holder.setAsyncSupported(true);
        return holder;
    }
}
