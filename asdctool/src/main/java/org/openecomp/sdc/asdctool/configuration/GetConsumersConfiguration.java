package org.openecomp.sdc.asdctool.configuration;

import org.openecomp.sdc.be.dao.config.JanusGraphSpringConfig;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.model.operations.impl.ConsumerOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({JanusGraphSpringConfig.class})
public class GetConsumersConfiguration {


    @Bean("consumer-operation")
    public ConsumerOperation consumerOperation(JanusGraphGenericDao janusGraphGenericDao) {
        return new ConsumerOperation(janusGraphGenericDao);
    }

}
