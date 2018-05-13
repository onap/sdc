package org.openecomp.sdc.be.servlets;

import static org.mockito.Mockito.mock;

import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.springframework.context.annotation.Bean;

public class BaseTestConfig {

    @Bean
    ComponentsUtils componentsUtils() {return new ComponentsUtils(mock(AuditingManager.class));}

    @Bean
    DefaultExceptionMapper defaultExceptionMapper() {return new DefaultExceptionMapper();}

    @Bean
    ComponentExceptionMapper componentExceptionMapper() {
        return new ComponentExceptionMapper(componentsUtils());
    }

    @Bean
    StorageExceptionMapper storageExceptionMapper() {
        return new StorageExceptionMapper(componentsUtils());
    }

}
