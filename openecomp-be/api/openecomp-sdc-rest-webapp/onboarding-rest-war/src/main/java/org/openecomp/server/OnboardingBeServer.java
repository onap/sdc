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

import java.nio.file.Paths;
import java.util.Properties;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.MimeMappings;

/**
 * The Jetty server around onboarding-be, configured the way the Jetty distribution configured it from
 * {@code jetty.xml} and the http, https and ssl modules. It reads the same {@code jetty.*} properties as the
 * {@code start.d} files the deployments mounted, with their values as defaults, so a deployment that overrode them
 * sets them as system properties.
 */
final class OnboardingBeServer {

    private OnboardingBeServer() {
    }

    static void customize(JettyServletWebServerFactory factory) {
        factory.setThreadPool(new QueuedThreadPool(200, 10, 60000));
        factory.setContextPath("/onboarding-api");
        // Rather than Boot's "application": the WAR's context was named after the WAR file.
        factory.setDisplayName("onboarding-be");
        // Jetty's own MIME types, as in the distribution, rather than Boot's list on top of them.
        factory.setMimeMappings(new MimeMappings());
        factory.addConfigurations(new OnboardingBeWebApp());
        factory.addServerCustomizers(server -> configure(server, System.getProperties()));
    }

    /**
     * Replaces the connector Boot created, and puts the application next to the swagger UI's {@code api-docs.war}
     * and in front of the default handler, as the distribution's deployer did.
     */
    static void configure(Server server, Properties properties) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecurePort(8443);
        httpConfig.setSendServerVersion(false);
        httpConfig.setSendDateHeader(false);
        httpConfig.setHeaderCacheSize(1024);
        server.setConnectors(new Connector[]{httpConnector(server, httpConfig, properties), httpsConnector(server, httpConfig, properties)});

        WebAppContext apiDocs = new WebAppContext();
        apiDocs.setContextPath("/api-docs");
        apiDocs.setDisplayName("api-docs");
        apiDocs.setWar(Paths.get("webapps/api-docs.war").toAbsolutePath().toString());
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{server.getHandler(), apiDocs});
        server.setHandler(new HandlerCollection(contexts, new DefaultHandler()));
    }

    private static ServerConnector httpConnector(Server server, HttpConfiguration httpConfig, Properties properties) {
        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig, HttpCompliance.RFC7230_LEGACY));
        connector.setHost(properties.getProperty("jetty.http.host"));
        connector.setPort(Integer.parseInt(properties.getProperty("jetty.http.port", "8081")));
        connector.setIdleTimeout(Long.parseLong(properties.getProperty("jetty.http.idleTimeout", "30000")));
        return connector;
    }

    /**
     * The image's {@code etc/org.onap.sdc.p12} serves the HTTPS connector unless a deployment configures its own;
     * {@code startup.sh} sets its passwords.
     */
    private static ServerConnector httpsConnector(Server server, HttpConfiguration httpConfig, Properties properties) {
        SslContextFactory.Server ssl = new SslContextFactory.Server();
        ssl.setKeyStorePath(Paths.get(properties.getProperty("jetty.sslContext.keyStorePath", "etc/org.onap.sdc.p12")).toAbsolutePath().toString());
        ssl.setKeyStorePassword(properties.getProperty("jetty.sslContext.keyStorePassword"));
        ssl.setKeyManagerPassword(properties.getProperty("jetty.sslContext.keyManagerPassword"));
        ssl.setKeyStoreType(properties.getProperty("jetty.sslContext.keyStoreType", "JKS"));
        ssl.setTrustStorePath(Paths.get(properties.getProperty("jetty.sslContext.trustStorePath", "etc/org.onap.sdc.trust.jks")).toAbsolutePath().toString());
        ssl.setTrustStorePassword(properties.getProperty("jetty.sslContext.trustStorePassword"));
        ssl.setTrustStoreType(properties.getProperty("jetty.sslContext.trustStoreType"));
        ssl.setNeedClientAuth(Boolean.parseBoolean(properties.getProperty("jetty.sslContext.needClientAuth", "false")));

        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());
        ServerConnector connector = new ServerConnector(server, new SslConnectionFactory(ssl, "http/1.1"),
            new HttpConnectionFactory(httpsConfig, HttpCompliance.RFC7230));
        connector.setHost(properties.getProperty("jetty.ssl.host"));
        connector.setPort(Integer.parseInt(properties.getProperty("jetty.ssl.port", "8445")));
        connector.setIdleTimeout(Long.parseLong(properties.getProperty("jetty.ssl.idleTimeout", "30000")));
        return connector;
    }
}
