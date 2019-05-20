package org.openecomp.sdc.be.dao.config;

import org.openecomp.sdc.be.dao.DAOJanusGraphStrategy;
import org.openecomp.sdc.be.dao.JanusGraphClientStrategy;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.janusgraph.transactions.SimpleJanusGraphTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan({
        "org.openecomp.sdc.be.dao.jsongraph",
})
@EnableTransactionManagement
public class JanusGraphSpringConfig {

    @Bean(name = "janusgraph-generic-dao")
    @Primary
    public HealingJanusGraphGenericDao janusGraphGenericDao(@Qualifier("janusgraph-client") JanusGraphClient janusGraphClient) {
        return new HealingJanusGraphGenericDao(janusGraphClient);
    }

    @Bean(name = "janusgraph-client", initMethod = "createGraph")
    @Primary
    public JanusGraphClient janusGraphClient(@Qualifier("dao-client-strategy")
                                                 JanusGraphClientStrategy janusGraphClientStrategy) {
        return new JanusGraphClient(janusGraphClientStrategy);
    }

    @Bean(name = "dao-client-strategy")
    public JanusGraphClientStrategy janusGraphClientStrategy() {
        return new DAOJanusGraphStrategy();
    }

    @Bean
    public PlatformTransactionManager txManager() {
        return new SimpleJanusGraphTransactionManager(janusGraphClient(janusGraphClientStrategy()));
    }

    @Bean(name = "healingPipelineDao")
    public HealingPipelineDao  healingPipeline(){
        HealingPipelineDao healingPipelineDao = new HealingPipelineDao();
        healingPipelineDao.setHealVersion(1);
        healingPipelineDao.initHealVersion();
        return healingPipelineDao;
    }
}
