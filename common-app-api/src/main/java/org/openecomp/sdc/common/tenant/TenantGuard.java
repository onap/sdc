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

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;

/**
 * Tenant decisions for REST call sites. When multitenancy is disabled every check passes and lists come back untouched.
 */
public final class TenantGuard {

    public static final String TENANT_NOT_PERMITTED = "Tenant not permitted";

    private final Supplier<MultitenancyConfig> config;

    public TenantGuard(Supplier<MultitenancyConfig> config) {
        this.config = config;
    }

    public static TenantGuard fromCatalogConfiguration() {
        return new TenantGuard(() -> {
            ConfigurationManager manager = ConfigurationManager.getConfigurationManager();
            Configuration configuration = manager == null ? null : manager.getConfiguration();
            return configuration == null ? null : configuration.getMultitenancy();
        });
    }

    public static TenantGuard fromConfigurationFileProperty() {
        return new TenantGuard(MultitenancyConfigLoader::fromConfigurationFileProperty);
    }

    public MultitenancyConfig config() {
        return config.get();
    }

    public boolean isEnabled() {
        MultitenancyConfig current = config.get();
        return current != null && current.isEnabled();
    }

    public boolean permits(HttpServletRequest request, String tenant) {
        return !isEnabled() || TenantContext.required(request).permits(tenant);
    }

    public <T> List<T> visible(HttpServletRequest request, List<T> items, Function<? super T, String> tenantOf) {
        if (!isEnabled()) {
            return items;
        }
        TenantContext context = TenantContext.required(request);
        return items.stream().filter(item -> context.permits(tenantOf.apply(item))).collect(Collectors.toList());
    }
}
