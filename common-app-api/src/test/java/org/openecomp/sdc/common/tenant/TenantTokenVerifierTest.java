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


package org.openecomp.sdc.common.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RateLimitReachedException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;

class TenantTokenVerifierTest {

    private LocalIssuer issuer;
    private HttpServer server;

    @BeforeEach
    void setUp() throws Exception {
        issuer = new LocalIssuer();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static Date secondsFromNow(long seconds) {
        return new Date(System.currentTimeMillis() + seconds * 1000);
    }

    @Test
    void acceptsValidTokenAndReadsRealmRoles() throws Exception {
        TenantContext context = issuer.verifier(null).verify(issuer.token(issuer.claims("tenant-a", "tenant-b").build()));
        assertEquals(new HashSet<>(Arrays.asList("tenant-a", "tenant-b")), context.roles());
    }

    @Test
    void rejectsExpiredTokenBeyondClockSkew() throws Exception {
        String token = issuer.token(issuer.claims("tenant-a").expirationTime(secondsFromNow(-120)).build());
        assertThrows(InvalidTenantTokenException.class, () -> issuer.verifier(null).verify(token));
    }

    @Test
    void acceptsExpiredTokenWithinClockSkew() throws Exception {
        String token = issuer.token(issuer.claims("tenant-a").expirationTime(secondsFromNow(-30)).build());
        assertTrue(issuer.verifier(null).verify(token).permits("tenant-a"));
    }

    @Test
    void rejectsTokenNotYetValid() throws Exception {
        String token = issuer.token(issuer.claims("tenant-a").notBeforeTime(secondsFromNow(120)).build());
        assertThrows(InvalidTenantTokenException.class, () -> issuer.verifier(null).verify(token));
    }

    @Test
    void rejectsTokenWithoutExpiry() throws Exception {
        String token = issuer.token(issuer.claims("tenant-a").expirationTime(null).build());
        assertThrows(InvalidTenantTokenException.class, () -> issuer.verifier(null).verify(token));
    }

    @Test
    void rejectsWrongIssuer() throws Exception {
        String token = issuer.token(issuer.claims("tenant-a").issuer("https://other.test/realms/sdc").build());
        assertThrows(InvalidTenantTokenException.class, () -> issuer.verifier(null).verify(token));
    }

    @Test
    void rejectsUnsignedToken() throws Exception {
        String token = new PlainJWT(issuer.claims("tenant-a").build()).serialize();
        assertThrows(InvalidTenantTokenException.class, () -> issuer.verifier(null).verify(token));
    }

    @Test
    void rejectsHmacSignedToken() throws Exception {
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), issuer.claims("tenant-a").build());
        jwt.sign(new MACSigner(new byte[32]));
        assertThrows(InvalidTenantTokenException.class, () -> issuer.verifier(null).verify(jwt.serialize()));
    }

    @Test
    void rejectsGarbage() {
        assertThrows(InvalidTenantTokenException.class, () -> issuer.verifier(null).verify("not-a-jwt"));
    }

    @Test
    void enforcesAudienceOnlyWhenConfigured() throws Exception {
        String withAudience = issuer.token(issuer.claims("tenant-a").audience("sdc-backend").build());
        String withoutAudience = issuer.token(issuer.claims("tenant-a").build());
        assertTrue(issuer.verifier("sdc-backend").verify(withAudience).permits("tenant-a"));
        assertTrue(issuer.verifier(null).verify(withoutAudience).permits("tenant-a"));
        assertThrows(InvalidTenantTokenException.class, () -> issuer.verifier("sdc-backend").verify(withoutAudience));
        assertThrows(InvalidTenantTokenException.class, () -> issuer.verifier("someone-else").verify(withAudience));
    }

    @Test
    void missingRealmAccessMeansNoRoles() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer(LocalIssuer.ISSUER).expirationTime(secondsFromNow(300)).build();
        assertEquals(Collections.emptySet(), issuer.verifier(null).verify(issuer.token(claims)).roles());
    }

    @Test
    void normalizesTrailingSlashOnIssuer() {
        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer("https://issuer.test/realms/sdc/");
        assertEquals(LocalIssuer.ISSUER, TenantTokenVerifier.normalizedIssuer(config));
    }

    @Test
    void requiresIssuer() {
        MultitenancyConfig config = new MultitenancyConfig();
        config.setEnabled(true);
        config.setIssuer("  ");
        assertThrows(IllegalArgumentException.class, () -> TenantTokenVerifier.normalizedIssuer(config));
    }

    @Test
    void requiresAnAbsoluteIssuerUri() {
        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer("issuer.test/realms/sdc");
        assertThrows(IllegalArgumentException.class, () -> TenantTokenVerifier.normalizedIssuer(config));
    }

    @Test
    void requiresAnHttpOrHttpsIssuerScheme() {
        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer("ftp://issuer.test/realms/sdc");
        assertThrows(IllegalArgumentException.class, () -> TenantTokenVerifier.normalizedIssuer(config));
    }

    @Test
    void discoverMapsAnInvalidIssuerToKeysUnavailable() {
        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer("ftp://issuer.test/realms/sdc");
        assertThrows(TenantKeysUnavailableException.class, () -> TenantTokenVerifier.discover(config));
    }

    @Test
    void discoverRejectsANonHttpJwksUriScheme() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String issuerUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        String discoveryBody = "{\"issuer\":\"" + issuerUrl + "\",\"jwks_uri\":\"ftp://127.0.0.1/jwks\"}";
        server.createContext("/.well-known/openid-configuration", exchange -> respond(exchange, discoveryBody));
        server.start();

        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer(issuerUrl);
        assertThrows(TenantKeysUnavailableException.class, () -> TenantTokenVerifier.discover(config));
    }

    @Test
    void rejectsRs512SignedWithTheRealKey() throws Exception {
        String token = issuer.token(issuer.claims("tenant-a").build(), JWSAlgorithm.RS512, new RSASSASigner(issuer.key), issuer.key.getKeyID());
        assertThrows(InvalidTenantTokenException.class, () -> issuer.verifier(null).verify(token));
    }

    @Test
    void acceptsEs256() throws Exception {
        ECKey ecKey = new ECKeyGenerator(Curve.P_256).keyID("local-ec-1").generate();
        TenantTokenVerifier verifier = issuer.verifier(null, ecKey.toPublicJWK());
        String token = issuer.token(issuer.claims("tenant-a").build(), JWSAlgorithm.ES256, new ECDSASigner(ecKey), ecKey.getKeyID());
        assertTrue(verifier.verify(token).permits("tenant-a"));
    }

    @Test
    void classifiesRemoteKeySourceFailureAsKeysUnavailable() throws Exception {
        JWKSource<SecurityContext> broken = (selector, context) -> {
            throw new RemoteKeySourceException("jwks endpoint unreachable", new IOException());
        };
        TenantTokenVerifier verifier = new TenantTokenVerifier(LocalIssuer.ISSUER, null, broken);
        String token = issuer.token(issuer.claims("tenant-a").build());
        assertThrows(TenantKeysUnavailableException.class, () -> verifier.verify(token));
    }

    private static JWKSource<SecurityContext> alwaysRateLimited() {
        return (selector, context) -> {
            throw new RateLimitReachedException();
        };
    }

    @Test
    void classifiesRateLimitAsKeysUnavailableWhenNoFetchHasSucceededYet() throws Exception {
        TenantTokenVerifier.KeyFetchStatus keyFetchStatus = new TenantTokenVerifier.KeyFetchStatus();
        TenantTokenVerifier verifier = new TenantTokenVerifier(LocalIssuer.ISSUER, null, alwaysRateLimited(), keyFetchStatus);
        String token = issuer.token(issuer.claims("tenant-a").build());
        assertThrows(TenantKeysUnavailableException.class, () -> verifier.verify(token));
    }

    @Test
    void classifiesRateLimitAsInvalidTokenWhenLastFetchSucceeded() throws Exception {
        TenantTokenVerifier.KeyFetchStatus keyFetchStatus = new TenantTokenVerifier.KeyFetchStatus();
        keyFetchStatus.recordSuccess();
        TenantTokenVerifier verifier = new TenantTokenVerifier(LocalIssuer.ISSUER, null, alwaysRateLimited(), keyFetchStatus);
        String token = issuer.token(issuer.claims("tenant-a").build());
        assertThrows(InvalidTenantTokenException.class, () -> verifier.verify(token));
    }

    @Test
    void classifiesRateLimitAsKeysUnavailableAfterALaterFetchFails() throws Exception {
        TenantTokenVerifier.KeyFetchStatus keyFetchStatus = new TenantTokenVerifier.KeyFetchStatus();
        keyFetchStatus.recordSuccess();
        keyFetchStatus.recordFailure();
        TenantTokenVerifier verifier = new TenantTokenVerifier(LocalIssuer.ISSUER, null, alwaysRateLimited(), keyFetchStatus);
        String token = issuer.token(issuer.claims("tenant-a").build());
        assertThrows(TenantKeysUnavailableException.class, () -> verifier.verify(token));
    }

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static int closedLocalPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"))) {
            return socket.getLocalPort();
        }
    }

    private static void respondWithStatus(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    private String tokenWithKid(String issuerUrl, String kid) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(issuerUrl)
            .expirationTime(secondsFromNow(300))
            .claim("realm_access", Collections.singletonMap("roles", Collections.singletonList("tenant-a")))
            .build();
        return issuer.token(claims, JWSAlgorithm.RS256, new RSASSASigner(issuer.key), kid);
    }

    @Test
    void discoverTracksRateLimitAsInvalidTokenWhenTheLastRealFetchSucceeded() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String issuerUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        String discoveryBody = "{\"issuer\":\"" + issuerUrl + "\",\"jwks_uri\":\"" + issuerUrl + "/jwks\"}";
        String jwksBody = new JWKSet(issuer.key.toPublicJWK()).toString();
        server.createContext("/.well-known/openid-configuration", exchange -> respond(exchange, discoveryBody));
        server.createContext("/jwks", exchange -> respond(exchange, jwksBody));
        server.start();

        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer(issuerUrl);
        TenantTokenVerifier verifier = TenantTokenVerifier.discover(config);

        // Real fetch #1: populates the cache from the live JWKS.
        assertTrue(verifier.verify(tokenWithKid(issuerUrl, issuer.key.getKeyID())).permits("tenant-a"));
        // Real fetch #2: an unknown kid forces one more live refresh; the key set still doesn't contain it.
        InvalidTenantTokenException second = assertThrows(InvalidTenantTokenException.class,
            () -> verifier.verify(tokenWithKid(issuerUrl, "unknown-kid-1")));
        assertFalse(second.getCause() instanceof RateLimitReachedException);
        // The JWKSource's rate-limit budget is now spent: this is RateLimitReachedException, not a real fetch.
        // Since fetch #2 succeeded, it is judged as an unknown key, not an outage.
        InvalidTenantTokenException third = assertThrows(InvalidTenantTokenException.class,
            () -> verifier.verify(tokenWithKid(issuerUrl, "unknown-kid-2")));
        assertInstanceOf(RateLimitReachedException.class, third.getCause());
    }

    @Test
    void discoverTracksRateLimitAsKeysUnavailableWhenTheLastRealFetchFailed() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String issuerUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        String discoveryBody = "{\"issuer\":\"" + issuerUrl + "\",\"jwks_uri\":\"" + issuerUrl + "/jwks\"}";
        String jwksBody = new JWKSet(issuer.key.toPublicJWK()).toString();
        AtomicBoolean jwksDown = new AtomicBoolean(false);
        server.createContext("/.well-known/openid-configuration", exchange -> respond(exchange, discoveryBody));
        server.createContext("/jwks", exchange -> {
            if (jwksDown.get()) {
                respondWithStatus(exchange, 503);
            } else {
                respond(exchange, jwksBody);
            }
        });
        server.start();

        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer(issuerUrl);
        TenantTokenVerifier verifier = TenantTokenVerifier.discover(config);

        // Real fetch #1: populates the cache while the JWKS endpoint is still healthy.
        assertTrue(verifier.verify(tokenWithKid(issuerUrl, issuer.key.getKeyID())).permits("tenant-a"));

        jwksDown.set(true);

        // Real fetch #2: an unknown kid forces a live refresh, which now fails (503) -> reported as an outage.
        TenantKeysUnavailableException second = assertThrows(TenantKeysUnavailableException.class,
            () -> verifier.verify(tokenWithKid(issuerUrl, "unknown-kid-1")));
        assertFalse(second.getCause() instanceof RateLimitReachedException);
        // The rate-limit budget is spent: RateLimitReachedException, judged as an outage because fetch #2 failed.
        TenantKeysUnavailableException third = assertThrows(TenantKeysUnavailableException.class,
            () -> verifier.verify(tokenWithKid(issuerUrl, "unknown-kid-2")));
        assertInstanceOf(RateLimitReachedException.class, third.getCause());
    }

    @Test
    void discoverTreatsA200WithANonJwksBodyAsAFailedFetch() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String issuerUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        String discoveryBody = "{\"issuer\":\"" + issuerUrl + "\",\"jwks_uri\":\"" + issuerUrl + "/jwks\"}";
        server.createContext("/.well-known/openid-configuration", exchange -> respond(exchange, discoveryBody));
        server.createContext("/jwks", exchange -> respond(exchange, "<html><body>maintenance</body></html>"));
        server.start();

        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer(issuerUrl);
        TenantTokenVerifier verifier = TenantTokenVerifier.discover(config);

        // Real fetch #1: the endpoint answers 200, but the body isn't a JWK set -> treated as a failed fetch.
        assertThrows(TenantKeysUnavailableException.class,
            () -> verifier.verify(tokenWithKid(issuerUrl, issuer.key.getKeyID())));
        // Real fetch #2: same as above, still not a JWK set.
        TenantKeysUnavailableException second = assertThrows(TenantKeysUnavailableException.class,
            () -> verifier.verify(tokenWithKid(issuerUrl, "unknown-kid-1")));
        assertFalse(second.getCause() instanceof RateLimitReachedException);
        // The rate-limit budget is spent: RateLimitReachedException, still judged as an outage.
        TenantKeysUnavailableException third = assertThrows(TenantKeysUnavailableException.class,
            () -> verifier.verify(tokenWithKid(issuerUrl, "unknown-kid-2")));
        assertInstanceOf(RateLimitReachedException.class, third.getCause());
    }

    @Test
    void discoversAndVerifiesAgainstAnInProcessIssuer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String issuerUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        String jwksUrl = issuerUrl + "/jwks";
        String discoveryBody = "{\"issuer\":\"" + issuerUrl + "\",\"jwks_uri\":\"" + jwksUrl + "\"}";
        String jwksBody = new JWKSet(issuer.key.toPublicJWK()).toString();
        server.createContext("/.well-known/openid-configuration", exchange -> respond(exchange, discoveryBody));
        server.createContext("/jwks", exchange -> respond(exchange, jwksBody));
        server.start();

        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer(issuerUrl);
        TenantTokenVerifier verifier = TenantTokenVerifier.discover(config);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(issuerUrl)
            .expirationTime(secondsFromNow(300))
            .claim("realm_access", Collections.singletonMap("roles", Collections.singletonList("tenant-a")))
            .build();
        assertTrue(verifier.verify(issuer.token(claims)).permits("tenant-a"));
    }

    @Test
    void discoverRejectsMismatchedAdvertisedIssuer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String issuerUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        String discoveryBody = "{\"issuer\":\"" + issuerUrl + "/wrong\",\"jwks_uri\":\"" + issuerUrl + "/jwks\"}";
        server.createContext("/.well-known/openid-configuration", exchange -> respond(exchange, discoveryBody));
        server.start();

        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer(issuerUrl);
        assertThrows(TenantKeysUnavailableException.class, () -> TenantTokenVerifier.discover(config));
    }

    @Test
    void discoverRejectsMissingJwksUri() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String issuerUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        String discoveryBody = "{\"issuer\":\"" + issuerUrl + "\"}";
        server.createContext("/.well-known/openid-configuration", exchange -> respond(exchange, discoveryBody));
        server.start();

        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer(issuerUrl);
        assertThrows(TenantKeysUnavailableException.class, () -> TenantTokenVerifier.discover(config));
    }

    @Test
    void discoverRejectsNonJsonDiscoveryDocument() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String issuerUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        server.createContext("/.well-known/openid-configuration", exchange -> respond(exchange, "this is not json"));
        server.start();

        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer(issuerUrl);
        assertThrows(TenantKeysUnavailableException.class, () -> TenantTokenVerifier.discover(config));
    }

    @Test
    void discoverRejectsRelativeJwksUri() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String issuerUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        String discoveryBody = "{\"issuer\":\"" + issuerUrl + "\",\"jwks_uri\":\"jwks\"}";
        server.createContext("/.well-known/openid-configuration", exchange -> respond(exchange, discoveryBody));
        server.start();

        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer(issuerUrl);
        assertThrows(TenantKeysUnavailableException.class, () -> TenantTokenVerifier.discover(config));
    }

    @Test
    void discoverRejectsWhenNothingIsListening() throws Exception {
        String issuerUrl = "http://127.0.0.1:" + closedLocalPort();
        MultitenancyConfig config = new MultitenancyConfig();
        config.setIssuer(issuerUrl);
        assertThrows(TenantKeysUnavailableException.class, () -> TenantTokenVerifier.discover(config));
    }
}
