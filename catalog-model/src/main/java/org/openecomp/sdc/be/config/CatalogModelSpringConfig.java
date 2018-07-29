package org.openecomp.sdc.be.config;

import org.openecomp.sdc.be.model.tosca.validators.DataTypeValidatorConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.openecomp.sdc.be.model.operations.impl",
                "org.openecomp.sdc.be.model.cache",
                "org.openecomp.sdc.be.model.jsontitan.utils",
                "org.openecomp.sdc.be.model.jsontitan.operations",
})
public class CatalogModelSpringConfig {

    @Bean
    public DataTypeValidatorConverter dataTypeValidatorConverter() {
        return DataTypeValidatorConverter.getInstance();
    }

}
