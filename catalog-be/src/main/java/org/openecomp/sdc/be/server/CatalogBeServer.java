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

import java.nio.file.Paths;
import java.util.Properties;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.rewrite.handler.RedirectPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.MimeMappings;

/**
 * The Jetty server around catalog-be, configured the way the Jetty distribution configured it from {@code jetty.xml}
 * and the http, https, ssl and rewrite modules. It reads the same {@code jetty.*} properties the image's and the
 * deployments' {@code start.d} files set, so a deployment that overrode them sets them as system properties.
 */
final class CatalogBeServer {

    private CatalogBeServer() {
    }

    static void customize(JettyServletWebServerFactory factory) {
        factory.setThreadPool(new QueuedThreadPool(200, 10, 60000));
        // The WAR had no display name, so the context was named after its path, which the Togglz console shows.
        factory.setDisplayName(null);
        // Jetty's own MIME types, as in the distribution, rather than Boot's list on top of them.
        factory.setMimeMappings(new MimeMappings());
        factory.setDocumentRoot(Paths.get("webapp").toAbsolutePath().toFile());
        factory.addConfigurations(new CatalogBeWebApp());
        factory.addServerCustomizers(server -> configure(server, System.getProperties()));
    }

    /**
     * Replaces the connector Boot created and wraps the web application in the rewrite and default handlers, in the
     * order the distribution's modules inserted them.
     */
    static void configure(Server server, Properties properties) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecurePort(8443);
        httpConfig.setSendServerVersion(false);
        httpConfig.setSendDateHeader(false);
        httpConfig.setHeaderCacheSize(1024);
        server.setConnectors(new Connector[]{httpConnector(server, httpConfig, properties), httpsConnector(server, httpConfig, properties)});

        DefaultHandler defaultHandler = new DefaultHandler();
        defaultHandler.setShowContexts(false);
        RewriteHandler rewrite = new RewriteHandler();
        rewrite.setRewriteRequestURI(true);
        rewrite.setRewritePathInfo(false);
        rewrite.setOriginalPathAttribute("requestedPath");
        rewrite.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        rewrite.addRule(new RedirectPatternRule("", "/swagger-ui/index.html"));
        rewrite.setHandler(new HandlerCollection(server.getHandler(), defaultHandler));
        server.setHandler(rewrite);
    }

    private static ServerConnector httpConnector(Server server, HttpConfiguration httpConfig, Properties properties) {
        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig, HttpCompliance.RFC7230_LEGACY));
        connector.setHost(properties.getProperty("jetty.http.host"));
        connector.setPort(Integer.parseInt(properties.getProperty("jetty.http.port", "8080")));
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
        connector.setPort(Integer.parseInt(properties.getProperty("jetty.ssl.port", "8443")));
        connector.setIdleTimeout(Long.parseLong(properties.getProperty("jetty.ssl.idleTimeout", "30000")));
        return connector;
    }
}
