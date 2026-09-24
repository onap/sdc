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


package org.openecomp.sdc.be.test.util;

import com.nimbusds.jose.util.JSONObjectUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import javax.servlet.ServletException;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;
import org.openecomp.sdc.common.tenant.TenantContext;
import org.openecomp.sdc.common.tenant.TenantTokenFilter;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * One Keycloak per test JVM with the {@code sdc-it} and {@code other} realms imported.
 */
public final class KeycloakTestRealm {

    public static final String REALM = "sdc-it";
    public static final String OTHER_REALM = "other";
    public static final String AUDIENCE = "sdc-backend";
    private static final String CLIENT_ID = "sdc-it-client";
    private static final String PASSWORD = "password";
    private static final int PORT = 8080;
    private static final DockerImageName IMAGE = DockerImageName.parse("quay.io/keycloak/keycloak:26.4.7");
    private static KeycloakTestRealm instance;

    private final GenericContainer<?> container;

    private KeycloakTestRealm() {
        container = new GenericContainer<>(IMAGE)
            .withExposedPorts(PORT)
            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
            .withCopyFileToContainer(MountableFile.forClasspathResource("keycloak/sdc-it-realm.json"),
                "/opt/keycloak/data/import/sdc-it-realm.json")
            .withCopyFileToContainer(MountableFile.forClasspathResource("keycloak/other-realm.json"),
                "/opt/keycloak/data/import/other-realm.json")
            .withCommand("start-dev", "--import-realm")
            .waitingFor(Wait.forHttp("/realms/" + OTHER_REALM + "/.well-known/openid-configuration").forPort(PORT).forStatusCode(200))
            .withStartupTimeout(Duration.ofMinutes(3));
        container.start();
    }

    public static synchronized KeycloakTestRealm get() {
        if (instance == null) {
            instance = new KeycloakTestRealm();
        }
        return instance;
    }

    public static MultitenancyConfig config(String issuer, String audience) {
        MultitenancyConfig config = new MultitenancyConfig();
        config.setEnabled(true);
        config.setIssuer(issuer);
        config.setAudience(audience);
        return config;
    }

    public String issuer() {
        return issuer(REALM);
    }

    public String issuer(String realm) {
        return "http://" + container.getHost() + ":" + container.getMappedPort(PORT) + "/realms/" + realm;
    }

    public String token(String realm, String username) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(issuer(realm) + "/protocol/openid-connect/token").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String form = "grant_type=password&scope=openid&client_id=" + CLIENT_ID
            + "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8.name())
            + "&password=" + PASSWORD;
        try (OutputStream out = connection.getOutputStream()) {
            out.write(form.getBytes(StandardCharsets.UTF_8));
        }
        if (connection.getResponseCode() != 200) {
            throw new IOException("Token request for " + username + "@" + realm + " failed with HTTP " + connection.getResponseCode()
                + ": " + read(connection.getErrorStream()));
        }
        try {
            return JSONObjectUtils.getString(JSONObjectUtils.parse(read(connection.getInputStream())), "access_token");
        } catch (ParseException e) {
            throw new IOException("Unreadable token response for " + username, e);
        }
    }

    public TenantContext contextFor(String username) throws IOException, ServletException {
        MultitenancyConfig config = config(issuer(), null);
        TenantTokenFilter filter = new TenantTokenFilter() {
            @Override
            protected MultitenancyConfig loadConfig() {
                return config;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token(REALM, username));
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        if (chain.getRequest() == null) {
            throw new IllegalStateException("Tenant filter rejected " + username + " with HTTP " + response.getStatus());
        }
        return TenantContext.required(chain.getRequest());
    }

    private static String read(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        try (InputStream stream = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            stream.transferTo(out);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
