package org.openecomp.sdc.asdctool.configuration;

import org.openecomp.sdc.asdctool.impl.VrfObjectFixHandler;
import org.openecomp.sdc.be.dao.DAOTitanStrategy;
import org.openecomp.sdc.be.dao.TitanClientStrategy;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class VrfObjectFixConfiguration {

    @Bean(name = "titan-dao")
    public TitanDao titanDao(@Qualifier("titan-client") TitanGraphClient titanClient){
        return new TitanDao(titanClient);
    }

    @Bean(name = "titan-client")
    @Primary
    public TitanGraphClient titanClient(@Qualifier("dao-client-strategy") TitanClientStrategy titanClientStrategy) {
        return new TitanGraphClient(titanClientStrategy);
    }

    @Bean(name ="dao-client-strategy")
    public TitanClientStrategy titanClientStrategy() {
        return new DAOTitanStrategy();
    }

    @Bean
    public VrfObjectFixHandler vrfObjectFixHandler(@Qualifier("titan-dao") TitanDao titanDao){
        return new VrfObjectFixHandler(titanDao);
    }
}
