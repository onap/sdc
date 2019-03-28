package org.openecomp.sdc.config;

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
