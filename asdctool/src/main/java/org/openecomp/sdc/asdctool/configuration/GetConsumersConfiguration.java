package org.openecomp.sdc.asdctool.configuration;

import org.openecomp.sdc.be.dao.config.TitanSpringConfig;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.model.operations.impl.ConsumerOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TitanSpringConfig.class})
public class GetConsumersConfiguration {


    @Bean("consumer-operation")
    public ConsumerOperation consumerOperation(TitanGenericDao titanGenericDao) {
        return new ConsumerOperation(titanGenericDao);
    }

}
