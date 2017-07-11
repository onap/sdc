/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.openecomp.sdc.be.dao.DAOTitanStrategy;
import org.openecomp.sdc.be.dao.TitanClientStrategy;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DAOSpringConfig {

    @Bean(name = "titan-generic-dao")
    @Primary
    public TitanGenericDao  titanGenericDao(@Qualifier("titan-client") TitanGraphClient titanGraphClient) {
        return new TitanGenericDao(titanGraphClient);
    }

    @Bean(name = "titan-client", initMethod = "createGraph")
    @Primary
    public TitanGraphClient titanGraphClient(@Qualifier("dao-client-strategy") TitanClientStrategy titanClientStrategy) {
        return new TitanGraphClient(titanClientStrategy);
    }

    @Bean(name = "dao-client-strategy")
    public TitanClientStrategy titanClientStrategy() {
        return new DAOTitanStrategy();
    }



}
