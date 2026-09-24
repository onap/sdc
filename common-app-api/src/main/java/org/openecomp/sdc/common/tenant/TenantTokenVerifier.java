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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.jwk.source.RateLimitReachedException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jose.util.Resource;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;

/**
 * Validates OIDC bearer tokens against one issuer's published signing keys.
 */
public final class TenantTokenVerifier {

    private static final int MAX_CLOCK_SKEW_SECONDS = 60;
    private static final int HTTP_TIMEOUT_MILLIS = 5000;

    private final ConfigurableJWTProcessor<SecurityContext> processor;
    private final KeyFetchStatus keyFetchStatus;

    TenantTokenVerifier(String issuer, String audience, JWKSource<SecurityContext> keys) {
        this(issuer, audience, keys, alwaysHealthy());
    }

    TenantTokenVerifier(String issuer, String audience, JWKSource<SecurityContext> keys, KeyFetchStatus keyFetchStatus) {
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(
            new JWSVerificationKeySelector<>(new HashSet<>(Arrays.asList(JWSAlgorithm.RS256, JWSAlgorithm.ES256)), keys));
        DefaultJWTClaimsVerifier<SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>(
            audience == null || audience.isBlank() ? null : Collections.singleton(audience),
            new JWTClaimsSet.Builder().issuer(issuer).build(),
            new HashSet<>(Arrays.asList("iss", "exp")),
            null);
        claimsVerifier.setMaxClockSkew(MAX_CLOCK_SKEW_SECONDS);
        jwtProcessor.setJWTClaimsSetVerifier(claimsVerifier);
        this.processor = jwtProcessor;
        this.keyFetchStatus = keyFetchStatus;
    }

    private static KeyFetchStatus alwaysHealthy() {
        KeyFetchStatus status = new KeyFetchStatus();
        status.recordSuccess();
        return status;
    }

    public static TenantTokenVerifier discover(MultitenancyConfig config) throws IOException {
        String issuer;
        try {
            issuer = normalizedIssuer(config);
        } catch (IllegalArgumentException e) {
            throw new TenantKeysUnavailableException("Invalid multitenancy issuer configuration: " + e.getMessage(), e);
        }
        String discoveryUrl = issuer + "/.well-known/openid-configuration";
        ResourceRetriever retriever = new DefaultResourceRetriever(
            HTTP_TIMEOUT_MILLIS, HTTP_TIMEOUT_MILLIS, JWKSourceBuilder.DEFAULT_HTTP_SIZE_LIMIT);
        try {
            URL discovery = new URL(discoveryUrl);
            Map<String, Object> metadata = JSONObjectUtils.parse(retriever.retrieveResource(discovery).getContent());
            String advertisedIssuer = JSONObjectUtils.getString(metadata, "issuer");
            if (!issuer.equals(advertisedIssuer)) {
                throw new TenantKeysUnavailableException(
                    discoveryUrl + " advertises issuer " + advertisedIssuer + ", expected " + issuer, null);
            }
            URI jwksUri = JSONObjectUtils.getURI(metadata, "jwks_uri");
            if (jwksUri == null) {
                throw new TenantKeysUnavailableException(discoveryUrl + " has no jwks_uri", null);
            }
            String jwksScheme = jwksUri.getScheme();
            if (!"http".equalsIgnoreCase(jwksScheme) && !"https".equalsIgnoreCase(jwksScheme)) {
                // JWKSourceBuilder's HTTP fetch casts the URLConnection to HttpURLConnection; a non-http(s)
                // jwks_uri would fail with a ClassCastException deep inside the key fetch, not this IOException.
                throw new TenantKeysUnavailableException("jwks_uri advertised at " + discoveryUrl + " is not http(s): " + jwksUri, null);
            }
            URL jwks;
            try {
                jwks = jwksUri.toURL();
            } catch (IllegalArgumentException e) {
                // URI.toURL() throws this (not an IOException) for a jwks_uri that isn't an absolute URI.
                throw new TenantKeysUnavailableException("Malformed jwks_uri advertised at " + discoveryUrl, e);
            }
            KeyFetchStatus keyFetchStatus = new KeyFetchStatus();
            JWKSource<SecurityContext> keys = JWKSourceBuilder.create(jwks, trackingRetriever(retriever, keyFetchStatus))
                .retrying(true).build();
            return new TenantTokenVerifier(issuer, config.getAudience(), keys, keyFetchStatus);
        } catch (ParseException e) {
            throw new TenantKeysUnavailableException("Unreadable discovery document at " + discoveryUrl, e);
        } catch (IOException e) {
            throw e instanceof TenantKeysUnavailableException ? e
                : new TenantKeysUnavailableException("Cannot fetch " + discoveryUrl, e);
        }
    }

    private static ResourceRetriever trackingRetriever(ResourceRetriever delegate, KeyFetchStatus keyFetchStatus) {
        return url -> {
            Resource resource;
            try {
                resource = delegate.retrieveResource(url);
            } catch (IOException e) {
                keyFetchStatus.recordFailure();
                throw e;
            }
            try {
                // A 200 response whose body isn't a JWK set (e.g. a proxy error page) is not a healthy fetch.
                JWKSet.parse(resource.getContent());
            } catch (ParseException e) {
                keyFetchStatus.recordFailure();
                throw new IOException("Response from " + url + " is not a valid JWK set", e);
            }
            keyFetchStatus.recordSuccess();
            return resource;
        };
    }

    public static String normalizedIssuer(MultitenancyConfig config) {
        String issuer = config.getIssuer();
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("multitenancy.issuer must be set when multitenancy.enabled is true");
        }
        String trimmed = issuer.trim();
        String normalized = trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        URI uri;
        try {
            uri = new URI(normalized);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("multitenancy.issuer must be an absolute http(s) URI: " + issuer, e);
        }
        String scheme = uri.getScheme();
        if (!uri.isAbsolute() || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
            throw new IllegalArgumentException("multitenancy.issuer must be an absolute http(s) URI: " + issuer);
        }
        return normalized;
    }

    public TenantContext verify(String token) throws InvalidTenantTokenException, TenantKeysUnavailableException {
        try {
            return TenantContext.fromClaims(processor.process(token, null));
        } catch (RateLimitReachedException e) {
            // Judged by the last real JWKS fetch: after a failed fetch this is an outage (503), otherwise an unknown key (401).
            if (keyFetchStatus.isHealthy()) {
                throw new InvalidTenantTokenException("Unknown signing key", e);
            }
            throw new TenantKeysUnavailableException("Signing keys unavailable", e);
        } catch (KeySourceException e) {
            throw new TenantKeysUnavailableException("Signing keys unavailable", e);
        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new InvalidTenantTokenException(e);
        }
    }

    /**
     * Whether the most recent JWKS fetch attempt returned a parseable key set.
     */
    static final class KeyFetchStatus {

        private volatile boolean healthy;

        void recordSuccess() {
            healthy = true;
        }

        void recordFailure() {
            healthy = false;
        }

        boolean isHealthy() {
            return healthy;
        }
    }
}
