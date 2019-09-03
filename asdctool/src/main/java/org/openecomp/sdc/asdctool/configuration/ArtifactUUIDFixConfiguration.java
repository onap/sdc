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

package org.openecomp.sdc.asdctool.configuration;


import org.openecomp.sdc.asdctool.impl.ArtifactUuidFix;
import org.openecomp.sdc.be.config.CatalogModelSpringConfig;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.config.DAOSpringConfig;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.config.CatalogBESpringConfig;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;

@Configuration
@Import({DAOSpringConfig.class, CatalogBESpringConfig.class, CatalogModelSpringConfig.class})
@ComponentScan({"org.openecomp.sdc.asdctool.migration.tasks",//migration tasks
        "org.openecomp.sdc.asdctool.migration.config.mocks"
                })
public class ArtifactUUIDFixConfiguration {

    @Bean
    public ArtifactUuidFix artifactUuidFix(JanusGraphDao janusGraphDao,
        ToscaOperationFacade toscaOperationFacade, ToscaExportHandler toscaExportHandler,
        ArtifactCassandraDao artifactCassandraDao, CsarUtils csarUtils) {
        return new ArtifactUuidFix(janusGraphDao, toscaOperationFacade, toscaExportHandler, artifactCassandraDao, csarUtils);
    }
    
    @Bean(name = "elasticsearchConfig")
    public PropertiesFactoryBean mapper() {
        String configHome = System.getProperty("config.home");
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new FileSystemResource(configHome + "/elasticsearch.yml"));
        return bean;
    }


}
