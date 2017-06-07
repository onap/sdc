package org.openecomp.sdc.asdctool.impl.migration.v1707;


import java.util.List;

import org.openecomp.sdc.asdctool.impl.migration.Migration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.MigrationByIdDerivedNodeTypeResolver;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.NormativesMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.NormativesResolver;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.ResourceVersionMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.ResourcesCategoriesMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.ServiceCategoriesMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.ServiceVersionMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.ServicesMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.UserStatesMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.UsersMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.VFResourcesMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.VersionMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.relations.FulfilledCapabilitiesMigrationService;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.relations.FulfilledRequirementsMigrationService;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.relations.RequirementsCapabilitiesMigrationService;
import org.openecomp.sdc.be.dao.TitanClientStrategy;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsontitan.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsontitan.operations.CategoryOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaDataOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.ToscaDefinitionPathCalculator;
import org.openecomp.sdc.be.model.operations.impl.ElementOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.ToscaDefinitionPathCalculatorImpl;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class Migration1707Config {


    @Bean(name = "migration1707")
    public Migration1707 migration1707(List<Migration> migrations) {
        return new Migration1707(migrations);
    }

    @Bean(name = "renameGraphPropertyKeysMigration")
    @Order(1)
    public Migration renameGraphPropertyKeysMigration() {
        return new RenameGraphPropertyKeys();
    }

    @Bean(name = "toscaNamesUpdate")
    @Order(2)
    public Migration toscaNamesUpdate() {
        return new ToscaNamesUpdate();
    }

    @Bean(name = "users-migration")
    @Order(3)
    public Migration usersMigration() {
        return new UsersMigration();
    }

    @Bean(name = "resource-category-migration")
    @Order(4)
    public Migration resourceCategoriesMigration() {
        return new ResourcesCategoriesMigration();
    }

    @Bean(name = "service-category-migration")
    @Order(5)
    public Migration serviceCategoriesMigration() {
        return new ServiceCategoriesMigration();
    }

    @Bean(name = "normatives-migration")
    @Order(6)
    public Migration normativesMigration() {
        return new NormativesMigration();
    }

    @Bean(name = "vf-migration")
    @Order(7)
    public Migration vfMigration() {
        return new VFResourcesMigration();
    }

    @Bean(name = "service-migration")
    @Order(8)
    public Migration serviceMigration() {
        return new ServicesMigration();
    }

    @Bean(name = "user-states-migration")
    @Order(9)
    public Migration userStatesMigration() {
        return new UserStatesMigration();
    }
    
//    @Bean(name = "tosca-template-regeneration")
//    @Order(10)
//    public Migration ToscaTemplateRegeneration() {
//        return new ToscaTemplateRegeneration();
//    }

    @Bean("resource-version-migration")
    public VersionMigration<Resource> resourceVersionMigration() {
        return new ResourceVersionMigration();
    }

    @Bean("service-version-migration")
    public VersionMigration<Service> serviceVersionMigration() {
        return new ServiceVersionMigration();
    }

    @Bean(name = "normatives-resolver")
    public NormativesResolver normativesResolver() {
        return new NormativesResolver();
    }
    
	@Bean(name = "property-operation-mig")
	public PropertyOperation propertyOperation(@Qualifier("titan-generic-dao-migration") TitanGenericDao titanGenericDao) {
		return new PropertyOperation(titanGenericDao);
	} 
	
    @Bean(name = "group-type-operation-mig")
    public GroupTypeOperation groupTypeOperation(@Qualifier("titan-generic-dao-migration") TitanGenericDao titanGenericDao, @Qualifier("property-operation-mig") PropertyOperation propertyOperation) {
    	return new GroupTypeOperation(titanGenericDao, propertyOperation);
    }

    @Bean(name = "titan-generic-dao-migration")
    public TitanGenericDao titanGenericDaoMigration(@Qualifier("migration-titan-client") TitanGraphClient titanGraphClient) {
        return new TitanGenericDao(titanGraphClient);
    }

    @Bean(name = "migration-titan-strategy")
    public TitanClientStrategy migrationStrategy() {
        return new MigrationTitanStrategy();
    }

    @Bean(name = "migration-titan-client", initMethod = "createGraph")
    public TitanGraphClient titanMigrationClient(@Qualifier("migration-titan-strategy") TitanClientStrategy titanClientStrategy) {
        return new TitanGraphClient(titanClientStrategy);
    }

    @Bean(name = "user-operation-migration")
    public IUserAdminOperation userOperationNewKeySpace(@Qualifier("titan-generic-dao-migration") TitanGenericDao titanGenericDao) {
        return new UserAdminOperation(titanGenericDao);
    }

    @Bean(name = "element-operation-migration")
    public IElementOperation elementOperationNewKeyspace(@Qualifier("titan-generic-dao-migration") TitanGenericDao titanGenericDao) {
        return new ElementOperation(titanGenericDao);
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

    @Bean(name = "tosca-path-calculator")
    public ToscaDefinitionPathCalculator pathCalculator() {
        return new ToscaDefinitionPathCalculatorImpl();
    }

    @Bean(name = "fulfilled-capabilities-mig-service")
    public FulfilledCapabilitiesMigrationService fulfilledCapabilitiesMigService() {
        return new FulfilledCapabilitiesMigrationService();
    }

    @Bean(name = "fulfilled-requirements-mig-service")
    public FulfilledRequirementsMigrationService requirementsMigService() {
        return new FulfilledRequirementsMigrationService();
    }

    @Bean(name ="req-cap-mig-service")
    public RequirementsCapabilitiesMigrationService reqCapMigService() {
        return new RequirementsCapabilitiesMigrationService();
    }

    @Bean(name = "mig-derived-resolver")
    public DerivedNodeTypeResolver migrationDerivedNodeTypeResolver() {
        return new MigrationByIdDerivedNodeTypeResolver();
    }


}
