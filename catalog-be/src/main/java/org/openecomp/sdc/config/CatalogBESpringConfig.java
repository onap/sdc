/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.onap.portalsdk.core.onboarding.exception.CipherUtilException;
import org.onap.sdc.security.PortalClient;
import org.openecomp.sdc.be.auditing.impl.ConfigurationProvider;
import org.openecomp.sdc.be.components.impl.ComponentLocker;
import org.openecomp.sdc.be.components.impl.aaf.RoleAuthorizationHandler;
import org.openecomp.sdc.be.components.impl.lock.ComponentLockAspect;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.ecomp.converters.AssetMetadataConverter;
import org.openecomp.sdc.be.filters.FilterConfiguration;
import org.openecomp.sdc.be.filters.PortalConfiguration;
import org.openecomp.sdc.be.filters.ThreadLocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@ComponentScan({"org.openecomp.sdc.be.user",
        "org.openecomp.sdc.be.facade.operations",
        "org.openecomp.sdc.be.impl",
        "org.openecomp.sdc.be.auditing.impl",
        "org.openecomp.sdc.be.distribution",
        "org.openecomp.sdc.be.switchover.detector",
        "org.openecomp.sdc.be.tosca",
        "org.openecomp.sdc.be.components.validation",
        "org.openecomp.sdc.be.catalog.impl",
        "org.openecomp.sdc.be.components.impl",
        "org.openecomp.sdc.be.components.path",
        "org.openecomp.sdc.be.components.merge",
        "org.openecomp.sdc.be.components.csar",
        "org.openecomp.sdc.be.components.property",
        "org.openecomp.sdc.be.datamodel.utils",
        "org.openecomp.sdc.be.components.upgrade",
        "org.openecomp.sdc.be.externalapi.servlet",
        "org.openecomp.sdc.be.servlets",
        "org.openecomp.sdc.be.filters",
        "org.openecomp.sdc.be.togglz"
})
public class CatalogBESpringConfig {

    private static final int BEFORE_TRANSACTION_MANAGER = 0;
    private final ComponentLocker componentLocker;

    @Autowired
    public CatalogBESpringConfig(ComponentLocker componentLocker) {
        this.componentLocker = componentLocker;
    }

    @Bean(name = "lifecycleBusinessLogic")
    public LifecycleBusinessLogic lifecycleBusinessLogic() {
        return new LifecycleBusinessLogic();
    }

    @Bean(name = "configurationProvider")
    public ConfigurationProvider configurationProvider() {
        return new ConfigurationProvider();
    }

    @Bean(name = "asset-metadata-utils")
    public AssetMetadataConverter assetMetadataConverter() {
        return new AssetMetadataConverter();
    }

    @Bean(name = "componentLockAspect")
    @Order(BEFORE_TRANSACTION_MANAGER)
    public ComponentLockAspect componentLockAspect() {
        return new ComponentLockAspect(componentLocker);
    }

    @Bean
    public RoleAuthorizationHandler roleAuthorizationHandler() {return new RoleAuthorizationHandler();}

    @Bean
    public CloseableHttpClient httpClientConnectionManager() {
        HttpClientFactory httpClientFactory = new HttpClientFactory();
        return httpClientFactory.createHttpClient();
    }

    @Bean
    public PortalConfiguration portalConfiguration() throws CipherUtilException {return new PortalConfiguration();}

    @Bean
    public FilterConfiguration filterConfiguration() {return new FilterConfiguration(configuration());}

    @Bean
    public ThreadLocalUtils threadLocalUtils() {return new ThreadLocalUtils();}

    @Bean
    public PortalClient portalClient() throws CipherUtilException {
        return new PortalClient(httpClientConnectionManager(), portalConfiguration());
    }

    @Bean
    public org.openecomp.sdc.be.config.Configuration configuration(){
        return ConfigurationManager.getConfigurationManager().getConfiguration();
    }

}
