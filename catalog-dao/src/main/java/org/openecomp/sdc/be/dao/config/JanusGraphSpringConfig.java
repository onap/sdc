/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 */
package org.openecomp.sdc.be.dao.config;

import org.openecomp.sdc.be.dao.DAOJanusGraphStrategy;
import org.openecomp.sdc.be.dao.JanusGraphClientStrategy;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan({"org.openecomp.sdc.be.dao.janusgraph"})
@EnableTransactionManagement
public class JanusGraphSpringConfig {

    @Bean(name = "janusgraph-generic-dao")
    @Primary
    public HealingJanusGraphGenericDao janusGraphGenericDao(@Qualifier("janusgraph-client") JanusGraphClient janusGraphClient) {
        return new HealingJanusGraphGenericDao(janusGraphClient);
    }

    @Bean(name = "janusgraph-client", initMethod = "createGraph")
    @Primary
    public JanusGraphClient janusGraphClient(@Qualifier("dao-client-strategy") JanusGraphClientStrategy janusGraphClientStrategy) {
        return new JanusGraphClient(janusGraphClientStrategy);
    }

    @Bean(name = "dao-client-strategy")
    public JanusGraphClientStrategy janusGraphClientStrategy() {
        return new DAOJanusGraphStrategy();
    }

    @Bean(name = "healingPipelineDao")
    public HealingPipelineDao healingPipeline() {
        HealingPipelineDao healingPipelineDao = new HealingPipelineDao();
        healingPipelineDao.setHealVersion(1);
        healingPipelineDao.initHealVersion();
        return healingPipelineDao;
    }
}
