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

import org.openecomp.sdc.be.auditing.impl.ConfigurationProvider;
import org.openecomp.sdc.be.components.impl.ComponentLocker;
import org.openecomp.sdc.be.components.impl.lock.ComponentLockAspect;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.ecomp.converters.AssetMetadataConverter;
import org.openecomp.sdc.common.transaction.mngr.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@ComponentScan({"org.openecomp.sdc.be.user",
        "org.openecomp.sdc.be.impl",
        "org.openecomp.sdc.be.auditing.impl",
        "org.openecomp.sdc.be.distribution",
        "org.openecomp.sdc.be.switchover.detector",
        "org.openecomp.sdc.be.tosca",
        "org.openecomp.sdc.be.components.validation",
        "org.openecomp.sdc.be.components.impl",
        "org.openecomp.sdc.be.components.path",
        "org.openecomp.sdc.be.components.merge",
        "org.openecomp.sdc.be.components.csar",
        "org.openecomp.sdc.be.components.property",
        "org.openecomp.sdc.be.datamodel.utils",
        "org.openecomp.sdc.be.components.upgrade"})
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

    @Bean(name = "transactionManager")
    public TransactionManager transactionManager() {
        return new TransactionManager();
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


}
