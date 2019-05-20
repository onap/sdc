package org.openecomp.sdc.asdctool.configuration;

import org.openecomp.sdc.asdctool.impl.VrfObjectFixHandler;
import org.openecomp.sdc.be.dao.DAOJanusGraphStrategy;
import org.openecomp.sdc.be.dao.JanusGraphClientStrategy;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class VrfObjectFixConfiguration {

    @Bean(name = "janusgraph-dao")
    public JanusGraphDao janusGraphDao(@Qualifier("janusgraph-client") JanusGraphClient janusGraphClient){
        return new JanusGraphDao(janusGraphClient);
    }

    @Bean(name = "janusgraph-client")
    @Primary
    public JanusGraphClient janusGraphClient(@Qualifier("dao-client-strategy")
                                            JanusGraphClientStrategy janusGraphClientStrategy) {
        return new JanusGraphClient(janusGraphClientStrategy);
    }

    @Bean(name ="dao-client-strategy")
    public JanusGraphClientStrategy janusGraphClientStrategy() {
        return new DAOJanusGraphStrategy();
    }

    @Bean
    public VrfObjectFixHandler vrfObjectFixHandler(@Qualifier("janusgraph-dao")
                                                       JanusGraphDao janusGraphDao){
        return new VrfObjectFixHandler(janusGraphDao);
    }
}
