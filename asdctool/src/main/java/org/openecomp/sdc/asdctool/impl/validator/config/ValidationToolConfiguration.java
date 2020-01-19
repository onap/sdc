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

package org.openecomp.sdc.asdctool.impl.validator.config;

import org.openecomp.sdc.asdctool.impl.VrfObjectFixHandler;
import org.openecomp.sdc.asdctool.impl.validator.ArtifactToolBL;
import org.openecomp.sdc.asdctool.impl.validator.ValidationToolBL;
import org.openecomp.sdc.asdctool.impl.validator.executers.IArtifactValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.executers.NodeToscaArtifactsValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.executers.ServiceToscaArtifactsValidatorExecutor;
import org.openecomp.sdc.asdctool.impl.validator.executers.ServiceValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.executers.VFToscaArtifactValidatorExecutor;
import org.openecomp.sdc.asdctool.impl.validator.executers.ValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.executers.VfValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.tasks.VfValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.ArtifactValidationUtils;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.ServiceArtifactValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.VfArtifactValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.moduleJson.ModuleJsonTask;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportManager;
import org.openecomp.sdc.asdctool.migration.config.mocks.DistributionEngineMock;
import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.config.CatalogModelSpringConfig;
import org.openecomp.sdc.be.dao.DAOJanusGraphStrategy;
import org.openecomp.sdc.be.dao.JanusGraphClientStrategy;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.config.DAOSpringConfig;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.jsongraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArchiveOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ByToscaNameDerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.config.CatalogBESpringConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

/**
 * Created by chaya on 7/3/2017.
 */
@Configuration
@Import({DAOSpringConfig.class, CatalogBESpringConfig.class, CatalogModelSpringConfig.class})
public class ValidationToolConfiguration {

    @Bean
    public ServiceValidatorExecuter basicServiceValidator(JanusGraphDao janusGraphDao) {
        return new ServiceValidatorExecuter(janusGraphDao);
    }

    @Bean
    public NodeToscaArtifactsValidatorExecuter NodeToscaArtifactsValidatorValidator(JanusGraphDao janusGraphDao,
        ToscaOperationFacade toscaOperationFacade) {
        return new NodeToscaArtifactsValidatorExecuter(janusGraphDao, toscaOperationFacade);
    }

    @Bean
    public ServiceToscaArtifactsValidatorExecutor ServiceToscaArtifactsValidator(JanusGraphDao janusGraphDao,
        ToscaOperationFacade toscaOperationFacade) {
        return new ServiceToscaArtifactsValidatorExecutor(janusGraphDao, toscaOperationFacade);
    }

    @Bean
    public VFToscaArtifactValidatorExecutor VFToscaArtifactValidator(JanusGraphDao janusGraphDao,
        ToscaOperationFacade toscaOperationFacade) {
        return new VFToscaArtifactValidatorExecutor(janusGraphDao, toscaOperationFacade);
    }

    @Bean
    public VfArtifactValidationTask vfArtifactValidationTask(ArtifactValidationUtils artifactValidationUtils) {
        return new VfArtifactValidationTask(artifactValidationUtils);
    }

    @Bean
    public ServiceArtifactValidationTask serviceArtifactValidationTask(ArtifactValidationUtils artifactValidationUtils) {
        return new ServiceArtifactValidationTask(artifactValidationUtils);
    }

    @Bean
    public ModuleJsonTask moduleJsonTask(TopologyTemplateOperation topologyTemplateOperation) {
        return new ModuleJsonTask(topologyTemplateOperation);
    }

    @Bean
    public ValidationToolBL validationToolBL(List<ValidatorExecuter> validators) {
        return new ValidationToolBL(validators);
    }

    @Bean
    public ArtifactToolBL artifactToolBL(List<IArtifactValidatorExecuter> validators) {
        return new ArtifactToolBL(validators);
    }

    @Bean
    public VfValidatorExecuter basicVfValidator(List<VfValidationTask> tasks, JanusGraphDao janusGraphDao) {
        return new VfValidatorExecuter(tasks, janusGraphDao);
    }

    @Bean
    public ReportManager reportManager() { return new ReportManager();}

    @Bean(name = "artifact-cassandra-dao")
    public ArtifactCassandraDao artifactCassandraDao(CassandraClient cassandraClient) {
        return new ArtifactCassandraDao(cassandraClient);
    }

    @Bean
    public ArtifactValidationUtils artifactValidationUtils(ArtifactCassandraDao artifactCassandraDao,
        TopologyTemplateOperation topologyTemplateOperation) {
        return new ArtifactValidationUtils(artifactCassandraDao, topologyTemplateOperation);
    }

    @Bean(name = "groups-operation")
    public GroupsOperation jsonGroupsOperation() {
        return new GroupsOperation();
    }

    @Bean(name = "cassandra-client")
    public CassandraClient cassandraClient() {
        return new CassandraClient();
    }

    @Bean(name = "dao-janusgraph-strategy")
    public JanusGraphClientStrategy daoStrategy() {
        return new DAOJanusGraphStrategy();
    }

    @Bean(name = "migration-janusgraph-client", initMethod = "createGraph")
    public JanusGraphClient janusGraphMigrationClient(@Qualifier("dao-janusgraph-strategy")
                                                     JanusGraphClientStrategy janusGraphClientStrategy) {
        return new JanusGraphClient(janusGraphClientStrategy);
    }

    @Bean(name = "tosca-operation-facade")
    public ToscaOperationFacade toscaOperationFacade() {
        return new ToscaOperationFacade();
    }

    @Bean(name = "node-type-operation")
    public NodeTypeOperation nodeTypeOperation(@Qualifier("mig-derived-resolver") DerivedNodeTypeResolver migrationDerivedNodeTypeResolver) {
        return new NodeTypeOperation(migrationDerivedNodeTypeResolver);
    }

    @Bean(name = "topology-template-operation")
    public TopologyTemplateOperation topologyTemplateOperation() {
        return new TopologyTemplateOperation();
    }

    @Bean(name = "node-template-operation")
    public NodeTemplateOperation nodeTemplateOperation() {
        return new NodeTemplateOperation();
    }


    @Bean(name = "mig-derived-resolver")
    public DerivedNodeTypeResolver migrationDerivedNodeTypeResolver() {
        return new ByToscaNameDerivedNodeTypeResolver();
    }

    @Bean
    public JanusGraphGenericDao janusGraphGenericDao(@Qualifier("migration-janusgraph-client") JanusGraphClient janusGraphClient) {
        return new JanusGraphGenericDao(janusGraphClient);
    }

    @Bean(name = "elasticsearchConfig")
    public PropertiesFactoryBean mapper() {
        String configHome = System.getProperty("config.home");
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new FileSystemResource(configHome + "/elasticsearch.yml"));
        return bean;
    }

    @Bean
    public ArchiveOperation archiveOperation(JanusGraphDao janusGraphDao, IGraphLockOperation graphLockOperation) {
        return new ArchiveOperation(janusGraphDao, graphLockOperation);
    }

    @Bean
    public IGraphLockOperation graphLockOperation() {
        return new GraphLockOperation();
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

    @Bean(name = "healingPipelineDao")
    public HealingPipelineDao healingPipelineDao(){
        return new HealingPipelineDao();
    }

    @Bean(name = "janusgraph-dao")
    public HealingJanusGraphDao healingJanusGraphDao(HealingPipelineDao healingPipelineDao, JanusGraphClient janusGraphClient) {
        return new HealingJanusGraphDao(healingPipelineDao, janusGraphClient);
    }

    @Bean
    public IDistributionEngine iDistributionEngine() {
        return new DistributionEngineMock();
    }

    @Bean
    public ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder(ToscaOperationFacade toscaOperationFacade) {
        return new ServiceDistributionArtifactsBuilder(toscaOperationFacade);
    }
}
