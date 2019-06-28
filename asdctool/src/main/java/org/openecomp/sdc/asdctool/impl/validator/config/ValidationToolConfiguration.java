package org.openecomp.sdc.asdctool.impl.validator.config;

import org.openecomp.sdc.asdctool.impl.validator.ArtifactToolBL;
import org.openecomp.sdc.asdctool.impl.validator.ValidationToolBL;
import org.openecomp.sdc.asdctool.impl.validator.executers.*;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.ArtifactValidationUtils;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.ServiceArtifactValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.VfArtifactValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.moduleJson.ModuleJsonTask;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportManager;
import org.openecomp.sdc.be.dao.DAOJanusGraphStrategy;
import org.openecomp.sdc.be.dao.JanusGraphClientStrategy;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.jsongraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.*;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by chaya on 7/3/2017.
 */
@Configuration
public class ValidationToolConfiguration {

    @Bean
    public ServiceValidatorExecuter basicServiceValidator() { return new ServiceValidatorExecuter();}
    
    @Bean
    public NodeToscaArtifactsValidatorExecuter NodeToscaArtifactsValidatorValidator() { return new NodeToscaArtifactsValidatorExecuter();}
    
    @Bean
    public ServiceToscaArtifactsValidatorExecutor ServiceToscaArtifactsValidator() { return new ServiceToscaArtifactsValidatorExecutor();}
    
    @Bean
    public VFToscaArtifactValidatorExecutor VFToscaArtifactValidator() { return new VFToscaArtifactValidatorExecutor();}

    @Bean
    public VfArtifactValidationTask vfArtifactValidationTask() { return new VfArtifactValidationTask(); }

    @Bean
    public ServiceArtifactValidationTask serviceArtifactValidationTask() { return new ServiceArtifactValidationTask();}

    @Bean
    public ModuleJsonTask moduleJsonTask() { return new ModuleJsonTask();}

    @Bean
    public ValidationToolBL validationToolBL() {
        return new ValidationToolBL();
    }
    
    @Bean
    public ArtifactToolBL artifactToolBL() {
        return new ArtifactToolBL();
    }

    @Bean
    public VfValidatorExecuter basicVfValidator() { return new VfValidatorExecuter();}

    @Bean
    public ReportManager reportManager() { return new ReportManager();}

    @Bean(name = "artifact-cassandra-dao")
    public ArtifactCassandraDao artifactCassandraDao() {
        return new ArtifactCassandraDao();
    }

    @Bean
    public ArtifactValidationUtils artifactValidationUtils() { return new ArtifactValidationUtils();}

    @Bean(name = "groups-operation")
    public GroupsOperation jsonGroupsOperation(@Qualifier("janusgraph-dao") JanusGraphDao janusGraphDao) {
        return new GroupsOperation(janusGraphDao);
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
    public ToscaOperationFacade toscaOperationFacade(NodeTypeOperation nodeTypeOperation, TopologyTemplateOperation topologyTemplateOperation, NodeTemplateOperation nodeTemplateOperation, GroupsOperation operation, HealingJanusGraphDao janusGraphDao) {
        return new ToscaOperationFacade(nodeTypeOperation,topologyTemplateOperation,nodeTemplateOperation,operation, janusGraphDao);
    }

    @Bean(name = "node-type-operation")
    public NodeTypeOperation nodeTypeOperation(@Qualifier("mig-derived-resolver") DerivedNodeTypeResolver migrationDerivedNodeTypeResolver, @Qualifier("janusgraph-dao") JanusGraphDao janusGraphDao,CategoryOperation categoryOperation) {
        return new NodeTypeOperation(migrationDerivedNodeTypeResolver, janusGraphDao,categoryOperation);
    }

    @Bean(name = "topology-template-operation")
    public TopologyTemplateOperation topologyTemplateOperation(ArchiveOperation archiveOperation,CategoryOperation categoryOperation, @Qualifier("janusgraph-dao") JanusGraphDao janusGraphDao) {
        return new TopologyTemplateOperation(archiveOperation,categoryOperation,janusGraphDao);
    }

    @Bean
    public ArchiveOperation archiveOperation(@Qualifier("janusgraph-dao") JanusGraphDao janusGraphDao, IGraphLockOperation iGraphLockOperation){
        return new ArchiveOperation(janusGraphDao, iGraphLockOperation);
    }

    @Bean(name="healing-janus-graph-dao")
    public HealingJanusGraphDao healingJanusGraphDao(JanusGraphClient client){
        return new HealingJanusGraphDao(client);
    }
    @Bean
    public IGraphLockOperation graphLockOperation(){
        return new GraphLockOperation();
    }
    @Bean(name = "node-template-operation")
    public NodeTemplateOperation nodeTemplateOperation(@Qualifier("janusgraph-dao") JanusGraphDao janusGraphDao) {
        return new NodeTemplateOperation(janusGraphDao);
    }

    @Bean(name = "mig-derived-resolver")
    public DerivedNodeTypeResolver migrationDerivedNodeTypeResolver(@Qualifier("janusgraph-dao") JanusGraphDao janusGraphDao) {
        return new ByToscaNameDerivedNodeTypeResolver(janusGraphDao);
    }

    @Bean(name = "janusgraph-dao")
    public JanusGraphDao janusGraphDao(@Qualifier("migration-janusgraph-client") JanusGraphClient janusGraphClient) {
        return new JanusGraphDao(janusGraphClient);
    }

    @Bean(name = "category-operation")
    public CategoryOperation categoryOperation(@Qualifier("janusgraph-dao") JanusGraphDao janusGraphDao) {
        return new CategoryOperation(janusGraphDao);
    }

    @Bean(name = "artifacts-operation")
    public ArtifactsOperations artifactsOperation(@Qualifier("janusgraph-dao") JanusGraphDao janusGraphDao) {
        return new ArtifactsOperations(janusGraphDao);
    }

    @Bean(name = "tosca-data-operation")
    public ToscaDataOperation toscaDataOperation(@Qualifier("janusgraph-dao") JanusGraphDao janusGraphDao) {
        return new ToscaDataOperation(janusGraphDao);
    }

    @Bean(name = "tosca-element-lifecycle-operation")
    public ToscaElementLifecycleOperation toscaElementLifecycleOperation(@Qualifier("janusgraph-dao") JanusGraphDao janusGraphDao) {
        return new ToscaElementLifecycleOperation(janusGraphDao);
    }
}
