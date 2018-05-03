package org.openecomp.sdc.asdctool.impl.validator.config;

import org.openecomp.sdc.asdctool.impl.validator.ArtifactToolBL;
import org.openecomp.sdc.asdctool.impl.validator.ValidationToolBL;
import org.openecomp.sdc.asdctool.impl.validator.executers.NodeToscaArtifactsValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.executers.ServiceToscaArtifactsValidatorExecutor;
import org.openecomp.sdc.asdctool.impl.validator.executers.ServiceValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.executers.VFToscaArtifactValidatorExecutor;
import org.openecomp.sdc.asdctool.impl.validator.executers.VfValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.ArtifactValidationUtils;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.ServiceArtifactValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.VfArtifactValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.moduleJson.ModuleJsonTask;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportManager;
import org.openecomp.sdc.be.dao.DAOTitanStrategy;
import org.openecomp.sdc.be.dao.TitanClientStrategy;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.jsontitan.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsontitan.operations.ByToscaNameDerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.jsontitan.operations.CategoryOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.GroupsOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaDataOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
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

    @Bean(name = "dao-titan-strategy")
    public TitanClientStrategy daoStrategy() {
        return new DAOTitanStrategy();
    }

    @Bean(name = "migration-titan-client", initMethod = "createGraph")
    public TitanGraphClient titanMigrationClient(@Qualifier("dao-titan-strategy") TitanClientStrategy titanClientStrategy) {
        return new TitanGraphClient(titanClientStrategy);
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

    @Bean(name = "titan-dao")
    public TitanDao titanDao(@Qualifier("migration-titan-client") TitanGraphClient titanGraphClient) {
        return new TitanDao(titanGraphClient);
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
