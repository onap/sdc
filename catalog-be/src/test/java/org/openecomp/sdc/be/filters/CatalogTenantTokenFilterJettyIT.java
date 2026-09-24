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

package org.openecomp.sdc.be.filters;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.test.util.KeycloakTestRealm;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.tenant.TenantContext;

/**
 * Proves that {@code WWW-Authenticate} survives Jetty 9.4's {@code HttpServletResponse#sendError}, which
 * {@link org.openecomp.sdc.common.tenant.TenantTokenFilter} relies on and neither {@link TenantTokenFilterIT}
 * (Spring {@code MockHttpServletResponse}) nor {@link CatalogTenantTokenFilterWiringTest} (web.xml only) exercises.
 */
class CatalogTenantTokenFilterJettyIT {

    private static KeycloakTestRealm keycloak;
    private static Server server;
    private static int port;

    @BeforeAll
    static void startAll() throws Exception {
        keycloak = KeycloakTestRealm.get();

        ConfigurationManager configurationManager = new ConfigurationManager(
            new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
        Configuration configuration = new Configuration();
        configuration.setMultitenancy(KeycloakTestRealm.config(keycloak.issuer(), null));
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");

        // ServletHandler, not ServletContextHandler: catalog-be's pom deliberately excludes jetty-security from
        // jetty-servlet, and ServletContextHandler's constructor loads ConstraintSecurityHandler unconditionally.
        ServletHandler handler = new ServletHandler();
        handler.addFilterWithMapping(CatalogTenantTokenFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addServletWithMapping(new ServletHolder(new TenantsServlet()), "/*");

        server = new Server(0);
        server.setHandler(handler);
        server.start();
        port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    @AfterAll
    static void stopAll() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    private static HttpURLConnection request(String authorization) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + port + "/tenants").openConnection();
        if (authorization != null) {
            connection.setRequestProperty("Authorization", authorization);
        }
        return connection;
    }

    @Test
    void missingTokenIsRejectedWithWwwAuthenticateHeader() throws Exception {
        HttpURLConnection connection = request(null);
        assertThat(connection.getResponseCode()).isEqualTo(401);
        assertThat(connection.getHeaderField("WWW-Authenticate")).isEqualTo("Bearer error=\"invalid_token\"");
    }

    @Test
    void garbageTokenIsRejectedWithWwwAuthenticateHeader() throws Exception {
        HttpURLConnection connection = request("Bearer garbage");
        assertThat(connection.getResponseCode()).isEqualTo(401);
        assertThat(connection.getHeaderField("WWW-Authenticate")).isEqualTo("Bearer error=\"invalid_token\"");
    }

    @Test
    void validTokenReachesTheServletWithTheCallersTenants() throws Exception {
        HttpURLConnection connection = request("Bearer " + keycloak.token(KeycloakTestRealm.REALM, "alice"));
        assertThat(connection.getResponseCode()).isEqualTo(200);
        assertThat(read(connection.getInputStream())).contains("tenant-a");
    }

    private static String read(InputStream in) throws IOException {
        try (InputStream stream = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            stream.transferTo(out);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static class TenantsServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            TenantContext tenantContext = TenantContext.required(request);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(String.join(",", tenantContext.roles()));
        }
    }
}
