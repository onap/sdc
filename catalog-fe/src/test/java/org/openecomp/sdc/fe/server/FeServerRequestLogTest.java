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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FeServerRequestLogTest {

    @Test
    void logsNoRequestsUnlessAFileIsConfigured() {
        assertNull(FeServer.create(new Properties()).getRequestLog());
    }

    @Test
    void writesTheConfiguredFormatToTheConfiguredFile(@TempDir Path logDir) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("jetty.http.port", "0");
        properties.setProperty("jetty.ssl.enabled", "false");
        properties.setProperty("jetty.requestlog.filePath", logDir.resolve("yyyy_mm_dd.request.log").toString());
        properties.setProperty("jetty.requestlog.formatString", "%{client}a %{USER_ID}i \"%r\" %s");
        Server server = FeServer.create(properties);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
                baseRequest.setHandled(true);
                response.setStatus(204);
            }
        });
        server.start();
        try {
            int port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
            HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:" + port + "/sdc1/rest/version").openConnection();
            connection.setRequestProperty("USER_ID", "cs0008");
            connection.setRequestProperty("X-Forwarded-For", "203.0.113.7");
            assertEquals(204, connection.getResponseCode());
        } finally {
            server.stop();
        }

        assertEquals(List.of("203.0.113.7 cs0008 \"GET /sdc1/rest/version HTTP/1.1\" 204"), logLines(logDir));
    }

    private static List<String> logLines(Path logDir) throws IOException {
        try (Stream<Path> files = Files.list(logDir)) {
            List<Path> logs = files.collect(Collectors.toList());
            assertEquals(1, logs.size(), "log files: " + logs);
            return Files.readAllLines(logs.get(0));
        }
    }
}
