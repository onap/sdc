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
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

final class LocalIssuer {

    static final String ISSUER = "https://issuer.test/realms/sdc";
    final RSAKey key;

    LocalIssuer() throws JOSEException {
        key = new RSAKeyGenerator(2048).keyID("local-1").generate();
    }

    JWTClaimsSet.Builder claims(String... roles) {
        return new JWTClaimsSet.Builder()
            .issuer(ISSUER)
            .subject("user")
            .expirationTime(new Date(System.currentTimeMillis() + 300_000))
            .claim("realm_access", Collections.singletonMap("roles", Arrays.asList(roles)));
    }

    String token(JWTClaimsSet claims) throws JOSEException {
        return token(claims, JWSAlgorithm.RS256, new RSASSASigner(key), key.getKeyID());
    }

    String token(JWTClaimsSet claims, JWSAlgorithm algorithm, JWSSigner signer, String keyId) throws JOSEException {
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(algorithm).keyID(keyId).build(), claims);
        jwt.sign(signer);
        return jwt.serialize();
    }

    TenantTokenVerifier verifier(String audience) {
        return verifier(audience, key.toPublicJWK());
    }

    TenantTokenVerifier verifier(String audience, JWK verificationKey) {
        return new TenantTokenVerifier(ISSUER, audience, new ImmutableJWKSet<>(new JWKSet(verificationKey)));
    }
}
