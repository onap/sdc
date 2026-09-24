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

import com.nimbusds.jwt.JWTClaimsSet;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.ServletRequest;

/**
 * The tenants a validated bearer token entitles its caller to: the token's realm roles.
 */
public final class TenantContext {

    public static final String ATTRIBUTE = TenantContext.class.getName();

    private final Set<String> roles;

    public TenantContext(Collection<String> roles) {
        this.roles = Collections.unmodifiableSet(new HashSet<>(roles));
    }

    static TenantContext fromClaims(JWTClaimsSet claims) throws ParseException {
        Map<String, Object> realmAccess = claims.getJSONObjectClaim("realm_access");
        Object roles = realmAccess == null ? null : realmAccess.get("roles");
        if (!(roles instanceof Collection)) {
            return new TenantContext(Collections.emptySet());
        }
        return new TenantContext(((Collection<?>) roles).stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .collect(Collectors.toSet()));
    }

    public static TenantContext required(ServletRequest request) {
        Object context = request.getAttribute(ATTRIBUTE);
        if (context instanceof TenantContext) {
            return (TenantContext) context;
        }
        throw new IllegalStateException("Multitenancy is enabled but no bearer token was validated for this request; "
            + "the tenant token filter is not mapped to this path");
    }

    public Set<String> roles() {
        return roles;
    }

    public boolean permits(String tenant) {
        return tenant != null && !tenant.isBlank() && roles.contains(tenant);
    }
}
