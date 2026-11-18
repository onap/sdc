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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Sends requests through FeServer's HTTP connector the way a TLS-terminating gateway does.
 */
class FeServerProxyTest {

    private Server server;
    private int port;

    @BeforeEach
    void start() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("jetty.http.port", "0");
        properties.setProperty("jetty.ssl.enabled", "false");
        server = FeServer.create(properties);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
                baseRequest.setHandled(true);
                response.setHeader("X-Secure", String.valueOf(request.isSecure()));
                response.setHeader("X-Remote-Addr", request.getRemoteAddr());
                response.sendRedirect("/sdc1/");
            }
        });
        server.start();
        port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    @AfterEach
    void stop() throws Exception {
        server.stop();
    }

    @Test
    void redirectsStayOnTheSchemeTheBrowserUsed() throws IOException {
        HttpURLConnection connection = get(Map.of("X-Forwarded-Proto", "https"));

        assertEquals(302, connection.getResponseCode());
        assertEquals("/sdc1/", connection.getHeaderField("Location"));
    }

    @Test
    void treatsARequestAsSecureWhenTheGatewayTerminatedTls() throws IOException {
        assertEquals("true", get(Map.of("X-Forwarded-Proto", "https")).getHeaderField("X-Secure"));
    }

    @Test
    void readsTheBrowserSchemeFromTheFrontOfAForwardedProtoChain() throws IOException {
        assertEquals("true", get(Map.of("X-Forwarded-Proto", "https, http")).getHeaderField("X-Secure"));
    }

    @Test
    void treatsARequestAsPlainWhenTheGatewayReportsPlainHttp() throws IOException {
        assertEquals("false", get(Map.of("X-Forwarded-Proto", "http")).getHeaderField("X-Secure"));
    }

    @Test
    void reportsTheBrowserAddressRatherThanTheGateway() throws IOException {
        assertEquals("203.0.113.7", get(Map.of("X-Forwarded-For", "203.0.113.7, 10.0.0.1")).getHeaderField("X-Remote-Addr"));
    }

    private HttpURLConnection get(Map<String, String> headers) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:" + port + "/").openConnection();
        connection.setInstanceFollowRedirects(false);
        headers.forEach(connection::setRequestProperty);
        connection.getResponseCode();
        return connection;
    }
}
