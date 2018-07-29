package org.openecomp.sdc.be.servlets;

import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.DefaultExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.StorageExceptionMapper;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

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
