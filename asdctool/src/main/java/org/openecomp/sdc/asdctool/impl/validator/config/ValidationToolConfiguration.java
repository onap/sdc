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
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.*;
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

    @Bean(name = "janusgraph-dao")
    public JanusGraphDao janusGraphDao(@Qualifier("migration-janusgraph-client") JanusGraphClient janusGraphClient) {
        return new JanusGraphDao(janusGraphClient);
    }

    @Bean(name = "category-operation")
    public CategoryOperation categoryOperation() {
        return new CategoryOperation();
    }

    @Bean(name = "artifacts-operation")
    public ArtifactsOperations artifactsOperation() {
        return new ArtifactsOperations();
    }

    @Bean(name = "tosca-data-operation")
    public ToscaDataOperation toscaDataOperation() {
        return new ToscaDataOperation();
    }

    @Bean(name = "tosca-element-lifecycle-operation")
    public ToscaElementLifecycleOperation toscaElementLifecycleOperation() {
        return new ToscaElementLifecycleOperation();
    }
}
