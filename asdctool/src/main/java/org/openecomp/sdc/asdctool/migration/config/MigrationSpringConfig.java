package org.openecomp.sdc.asdctool.migration.config;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.asdctool.migration.core.SdcMigrationTool;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.PostMigration;
import org.openecomp.sdc.asdctool.migration.dao.MigrationTasksDao;
import org.openecomp.sdc.asdctool.migration.resolver.MigrationResolver;
import org.openecomp.sdc.asdctool.migration.resolver.SpringBeansMigrationResolver;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.ArtifactsResolver;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngine;
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.components.impl.AdditionalInformationBusinessLogic;
import org.openecomp.sdc.be.components.impl.ArtifactResolverImpl;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.CompositionBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.InputsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ProductBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.VFComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.merge.heat.HeatEnvArtifactsMergeBusinessLogic;
import org.openecomp.sdc.be.components.merge.input.InputsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.components.merge.instance.ComponentInstanceMergeDataBusinessLogic;
import org.openecomp.sdc.be.components.merge.property.DataDefinitionsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.dao.DAOTitanStrategy;
import org.openecomp.sdc.be.dao.TitanClientStrategy;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.config.DAOSpringConfig;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.cache.ComponentCache;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.ComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.model.operations.impl.ElementOperation;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;

@Configuration
@Import(DAOSpringConfig.class)
@ComponentScan({"org.openecomp.sdc.asdctool.migration.tasks",//migration tasks
                "org.openecomp.sdc.be.model.operations.impl",
                "org.openecomp.sdc.be.model.cache",
                "org.openecomp.sdc.be.dao.titan",
                "org.openecomp.sdc.be.components.validation",
                "org.openecomp.sdc.be.dao.cassandra",
                "org.openecomp.sdc.be.model.jsontitan.operations",
                "org.openecomp.sdc.be.dao.jsongraph",
                "org.openecomp.sdc.be.components.merge",
                "org.openecomp.sdc.be.impl"})
public class MigrationSpringConfig {

    @Autowired(required=false)
    private List<Migration> migrations = new ArrayList<>();
    
    @Autowired(required=false)
    private List<PostMigration> postMigrations = new ArrayList<>();

    @Bean(name = "sdc-migration-tool")
    public SdcMigrationTool sdcMigrationTool(MigrationResolver migrationResolver, SdcRepoService sdcRepoService) {
        return new SdcMigrationTool(migrationResolver, sdcRepoService);
    }

    @Bean(name = "spring-migrations-resolver")
    public SpringBeansMigrationResolver migrationResolver(SdcRepoService sdcRepoService) {
        return new SpringBeansMigrationResolver(migrations, postMigrations, sdcRepoService);
    }

    @Bean(name = "sdc-repo-service")
    public SdcRepoService sdcRepoService(MigrationTasksDao migrationTasksDao) {
        return new SdcRepoService(migrationTasksDao);
    }

    @Bean(name = "sdc-migration-tasks-cassandra-dao")
    public MigrationTasksDao migrationTasksDao() {
        return new MigrationTasksDao();
    }

    @Bean(name = "cassandra-client")
    public CassandraClient cassandraClient() {
        return new CassandraClient();
    }


    @Bean(name = "dao-titan-strategy")
    public TitanClientStrategy daoStrategy() {
        return new DAOTitanStrategy();
    }
    
    @Bean(name = "titan-dao")
    public TitanDao titanDao(@Qualifier("titan-client") TitanGraphClient titanGraphClient) {
        return new TitanDao(titanGraphClient);
    }
    
    @Bean(name = "titan-client", initMethod = "createGraph")
    public TitanGraphClient titanClient(@Qualifier("dao-titan-strategy") TitanClientStrategy titanClientStrategy) {
        return new TitanGraphClient(titanClientStrategy);
    }
    
    @Bean(name = "resource-business-logic")
    public ResourceBusinessLogic resourceBusinessLogic() {
        return new ResourceBusinessLogic();
    }

//    @Bean(name = "healthCheckBusinessLogic")
//    public HealthCheckBusinessLogic healthCheckBusinessLogic() {
//        return new HealthCheckBusinessLogic();
//    }
//
//    @Bean(name = "distribution-engine-cluster-health")
//    public DistributionEngineClusterHealth distributionEngineClusterHealth() {
//        return new DistributionEngineClusterHealth();
//    }
//
//    @Bean(name = "cassandra-health-check")
//    public CassandraHealthCheck cassandraHealthCheck() {
//        return new CassandraHealthCheck();
//    }

//    @Bean(name = "switchover-detector")
//    public SwitchoverDetector switchoverDetector() {
//        return new SwitchoverDetector();
//    }

    @Bean(name = "service-business-logic")
    public ServiceBusinessLogic serviceBusinessLogic() {
        return new ServiceBusinessLogic();
    }

    @Bean(name = "capability-type-operation")
    public CapabilityTypeOperation CapabilityTypeOperation() {
        return new CapabilityTypeOperation();
    }
    
    @Bean(name = "lifecycle-business-logic")
    public LifecycleBusinessLogic lifecycleBusinessLogic() {
        return new LifecycleBusinessLogic();
    }

    @Bean(name = "property-operation")
    public PropertyOperation propertyOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao) {
        return new PropertyOperation(titanGenericDao);
    }
    
    @Bean(name = "csar-operation")
    public CsarOperation csarOperation() {
        return new CsarOperation();
    }
    
    @Bean(name = "vf-component-instance-business-logic")
    public VFComponentInstanceBusinessLogic vFComponentInstanceBusinessLogic() {
        return new VFComponentInstanceBusinessLogic();
    }
    
    @Bean(name = "resource-import-manager")
    public ResourceImportManager resourceImportManager() {
        return new ResourceImportManager();
    }

    @Bean(name = "group-business-logic")
    public GroupBusinessLogic groupBusinessLogic() {
        return new GroupBusinessLogic();
    }

    @Bean(name = "inputs-business-logic")
    public InputsBusinessLogic inputsBusinessLogic() {
        return new InputsBusinessLogic();
    }

    @Bean(name = "composition-business-logic")
    public CompositionBusinessLogic compositionBusinessLogic() {
        return new CompositionBusinessLogic();
    }

    @Bean(name = "artifacts-business-logic")
    public ArtifactsBusinessLogic artifactsBusinessLogic() {
        return new ArtifactsBusinessLogic();
    }
    
    @Bean(name = "component-cache")
    public ComponentCache componentCache() {
        return new ComponentCache();
    }
    
    @Bean(name = "componentUtils")
    public ComponentsUtils componentsUtils() {
        return new ComponentsUtils();
    }
    
    @Bean(name = "user-business-logic")
    public UserBusinessLogic userBusinessLogic() {
        return new UserBusinessLogic();
    }
    
    @Bean(name = "graph-lock-operation")
    public GraphLockOperation graphLockOperation() {
        return new GraphLockOperation();
    }
    
    @Bean(name = "titan-generic-dao")
    public TitanGenericDao titanGenericDao(@Qualifier("titan-client") TitanGraphClient titanGraphClient) {
        return new TitanGenericDao(titanGraphClient);
    }
    
    @Bean(name = "element-operation")
    public ElementOperation elementOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao) {
        return new ElementOperation(titanGenericDao);
    }
    
    @Bean(name = "group-operation")
    public GroupOperation groupOperation() {
        return new GroupOperation();
    }
    
    @Bean(name = "group-instance-operation")
    public GroupInstanceOperation groupInstanceOperation() {
        return new GroupInstanceOperation();
    }
    
    @Bean(name = "group-type-operation")
    public GroupTypeOperation groupTypeOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao, @Qualifier("property-operation") PropertyOperation propertyOperation) {
        return new GroupTypeOperation(titanGenericDao, propertyOperation);
    }
    
    @Bean(name = "tosca-operation-facade")
    public ToscaOperationFacade toscaOperationFacade() {
        return new ToscaOperationFacade();
    }
    
    @Bean(name = "distribution-engine")
    public DistributionEngine distributionEngine() {
        return null;
    }
    
    @Bean(name = "audit-cassandra-dao")
    public AuditCassandraDao auditCassandraDao() {
        return new AuditCassandraDao();
    }

    @Bean(name = "service-component-instance-business-logic")
    public ServiceComponentInstanceBusinessLogic serviceComponentInstanceBusinessLogic() {
        return new ServiceComponentInstanceBusinessLogic();
    }
    
    @Bean("tosca-export-handler")
    public ToscaExportHandler toscaExportHandler() {
        return new ToscaExportHandler();
    }
    
    @Bean(name = "component-instance-operation")
    public ComponentInstanceOperation componentInstanceOperation() {
        return new ComponentInstanceOperation();
    }
    
    @Bean(name = "additional-information-business-logic")
    public AdditionalInformationBusinessLogic additionalInformationBusinessLogic() {
        return new AdditionalInformationBusinessLogic();
    }

    @Bean(name = "auditing-manager")
    public AuditingManager auditingManager() {
        return new AuditingManager();
    }
    
    @Bean(name = "auditing-dao")
    public AuditingDao auditingDao() {
        return new AuditingDao();
    }
    
    @Bean(name = "elasticsearch-client", initMethod = "initialize")
    public ElasticSearchClient elasticSearchClient() {
        return new ElasticSearchClient();
    }
    
    @Bean(name = "csar-utils")
    public CsarUtils csarUtils() {
        return new CsarUtils();
    }

    @Bean(name = "service-distribution-artifacts-builder")
    public ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder() {
        return new ServiceDistributionArtifactsBuilder();
    }
    
    @Bean(name = "product-business-logic")
    public ProductBusinessLogic productBusinessLogic() {
        return null;
    }

    @Bean(name = "dataDefinitionsValuesMergingBusinessLogic")
    public DataDefinitionsValuesMergingBusinessLogic dataDefinitionsValuesMergingBusinessLogic() {
        return new DataDefinitionsValuesMergingBusinessLogic();
    }

    @Bean(name = "artifacts-resolver")
    public ArtifactsResolver artifactsResolver() {
        return new ArtifactResolverImpl();
    }
    
    @Bean(name = "InputsValuesMergingBusinessLogic")
    public InputsValuesMergingBusinessLogic InputsValuesMergingBusinessLogic(){
    	return new InputsValuesMergingBusinessLogic();
    }

    @Bean(name = "GenericTypeBusinessLogic")
    public GenericTypeBusinessLogic genericTypeBusinessLogic(){
    	return new GenericTypeBusinessLogic();
    }

    @Bean(name ="componentInstanceMergeDataBusinessLogic")
    public ComponentInstanceMergeDataBusinessLogic componentInstanceMergeDataBusinessLogic(){
    	return new ComponentInstanceMergeDataBusinessLogic();
    }
    
    @Bean(name ="heatEnvArtifactsMergeBusinessLogic")
    public HeatEnvArtifactsMergeBusinessLogic heatEnvArtifactsMergeBusinessLogic(){
    	return new HeatEnvArtifactsMergeBusinessLogic();
    }

    @Bean(name = "elasticsearchConfig")
    public PropertiesFactoryBean mapper() {
        String configHome = System.getProperty("config.home");
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new FileSystemResource(configHome + "/elasticsearch.yml"));
        return bean;
    }

}
