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

package org.openecomp.sdc.asdctool.impl.migration.v1604;

import org.openecomp.sdc.asdctool.impl.PopulateComponentCache;
import org.openecomp.sdc.asdctool.impl.migration.MigrationOperationUtils;
import org.openecomp.sdc.asdctool.impl.migration.v1607.CsarMigration;
import org.openecomp.sdc.asdctool.impl.migration.v1610.TitanFixUtils;
import org.openecomp.sdc.asdctool.impl.migration.v1610.ToscaArtifactsAlignment;
import org.openecomp.sdc.asdctool.impl.migration.v1702.DataTypesUpdate;
import org.openecomp.sdc.asdctool.impl.migration.v1702.Migration1702;
import org.openecomp.sdc.asdctool.impl.migration.v1707.VfModulesPropertiesAdding;
import org.openecomp.sdc.be.auditing.api.IAuditingManager;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.components.impl.*;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.cassandra.ComponentCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.SdcSchemaFilesCassandraDao;
import org.openecomp.sdc.be.dao.config.DAOSpringConfig;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.cache.ComponentCache;
import org.openecomp.sdc.be.model.jsontitan.operations.GroupsOperation;
import org.openecomp.sdc.be.model.operations.api.IAdditionalInformationOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.impl.*;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import(DAOSpringConfig.class)
public class AppConfig {

	@Bean(name = "sdc-schema-files-cassandra-dao")
	public SdcSchemaFilesCassandraDao sdcSchemaFilesCassandraDao() {
		return new SdcSchemaFilesCassandraDao();
	}
	@Bean(name = "componentsUtils")
	public ComponentsUtils componentsUtils() {
		return new ComponentsUtils();
	}
	@Bean(name = "updateDataTypes")
	public DataTypesUpdate dataTypesUpdate() {
		return new DataTypesUpdate();
	}
	@Bean(name = "serviceMigrationBean")
	public ServiceMigration serviceMigration() {
		return new ServiceMigration();
	}

	@Bean(name = "vfcNamingAlignmentBean")
	public VfcNamingAlignment vfcNamingAlignment() {
		return new VfcNamingAlignment();
	}

	@Bean(name = "derivedFromAlignment")
	public DerivedFromAlignment derivedFromAlignment() {
		return new DerivedFromAlignment();
	}

	@Bean(name = "groupsAlignment")
	public GroupsAlignment groupsAlignment() {
		return new GroupsAlignment();
	}

	@Bean(name = "csarMigration")
	public CsarMigration csarMigration() {
		return new CsarMigration();
	}

	@Bean(name = "resource-operation")
	public ResourceOperation resourceOperation() {
		return new ResourceOperation();
	}

	@Bean(name = "service-operation")
	public ServiceOperation serviceOperation() {
		return new ServiceOperation();
	}

	@Bean(name = "component-instance-operation")
	public ComponentInstanceOperation componentInstanceOperation() {
		return new ComponentInstanceOperation();
	}

	@Bean(name = "capability-instanceOperation")
	public CapabilityInstanceOperation capabilityInstanceOperation() {
		return new CapabilityInstanceOperation();
	}

	@Bean(name = "property-operation")
	@Primary
	public PropertyOperation propertyOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao) {
		return new PropertyOperation(titanGenericDao);
	}

	@Bean(name = "attribute-operation")
	public AttributeOperation attribueOperation() {
		return new AttributeOperation();
	}

	@Bean(name = "application-datatype-cache")
	public ApplicationDataTypeCache applicationDataTypeCache() {
		return new ApplicationDataTypeCache();
	}

	@Bean(name = "requirement-operation")
	public RequirementOperation requirementOperation() {
		return new RequirementOperation();
	}

	@Bean(name = "capability-operation")
	public CapabilityOperation capabilityOperation() {
		return new CapabilityOperation();
	}

	@Bean(name = "interface-operation")
	public InterfaceLifecycleOperation interfaceLifecycleOperation() {
		return new InterfaceLifecycleOperation();
	}

	@Bean(name = "element-operation")
	@Primary
	public IElementOperation elementOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao) {
		return new ElementOperation(titanGenericDao);
	}

	@Bean(name = "additional-information-operation")
	public IAdditionalInformationOperation addioAdditionalInformationOperation() {
		return new AdditionalInformationOperation();
	}

	@Bean(name = "capability-type-operation")
	public CapabilityTypeOperation capabilityTypeOperation() {
		return new CapabilityTypeOperation();
	}

	@Bean(name = "artifact-operation")
	public ArtifactOperation artifactOperation() {
		return new ArtifactOperation();
	}

	@Bean(name = "heat-parameter-operation")
	public HeatParametersOperation heatParametersOperation() {
		return new HeatParametersOperation();
	}

	@Bean(name = "product-operation")
	public ProductOperation productOperation() {
		return new ProductOperation();
	}

	@Bean(name = "lifecycle-operation")
	public LifecycleOperation lifecycleOperation() {
		return new LifecycleOperation();
	}

	@Bean(name = "group-operation")
	public GroupOperation groupOperation() {
		return new GroupOperation();
	}

	@Bean(name = "groups-operation")
	public GroupsOperation jsonGroupsOperation() {
		return new GroupsOperation();
	}
	
	@Bean(name = "group-instance-operation")
	public GroupInstanceOperation groupInstanceOperation() {
		return new GroupInstanceOperation();
	}
	
	@Bean(name = "group-type-operation")
	@Primary
	public GroupTypeOperation groupTypeOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenricDao, @Qualifier("property-operation")PropertyOperation propertyOperation) {
		return new GroupTypeOperation(titanGenricDao, propertyOperation);
	}

	@Bean(name = "attribute-operation")
	public AttributeOperation attributeOperation() {
		return new AttributeOperation();
	}

	@Bean(name = "titanFixUtils")
	public TitanFixUtils titanFixUtils() {
		return new TitanFixUtils();
	}

	@Bean(name = "populateComponentCache")
	public PopulateComponentCache populateComponentCache() {
		return new PopulateComponentCache();
	}

	@Bean(name = "artifact-cassandra-dao")
	public ArtifactCassandraDao artifactCassandraDao() {
		return new ArtifactCassandraDao();
	}

	@Bean(name = "component-cassandra-dao")
	public ComponentCassandraDao componentCassandraDao() {
		return new ComponentCassandraDao();
	}

	@Bean(name = "cassandra-client")
	public CassandraClient cassandraClient() {
		return new CassandraClient();
	}

	@Bean(name = "cacheManger-operation")
	public CacheMangerOperation cacheMangerOperation() {
		return new CacheMangerOperation();
	}

	@Bean(name = "component-cache")
	public ComponentCache componentCache() {
		return new ComponentCache();
	}

	@Bean(name = "input-operation")
	public InputsOperation inputsOperation() {
		return new InputsOperation();
	}

	/**
	 * Returns new instance of AuditCassandraDao
	 * 
	 * @return
	 */
	@Bean(name = "audit-cassandra-dao")
	public AuditCassandraDao auditCassandraDao() {
		return new AuditCassandraDao();
	}

	/**
	 * Returns new instance of UserBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "userBusinessLogic")
	public IUserBusinessLogic userBusinessLogic() {
		return new UserBusinessLogic();
	}

	/**
	 * Returns new instance of UserAdminOperation
	 * 
	 * @return
	 */
	@Bean(name = "user-operation")
	@Primary
	public IUserAdminOperation userOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao) {
		return new UserAdminOperation(titanGenericDao);
	}

	/**
	 * Returns new instance of GraphLockOperation
	 * 
	 * @return
	 */
	@Bean(name = "graph-lock-operation")
	public IGraphLockOperation graphLockOperation() {
		return new GraphLockOperation();
	}

	/**
	 * Returns new instance of AuditingDao
	 * 
	 * @return
	 */
	@Bean(name = "auditingDao")
	public AuditingDao auditingDao() {
		return new AuditingDao();
	}

	/**
	 * Returns new instance of AuditingManager
	 * 
	 * @return
	 */
	@Bean(name = "auditingManager")
	public IAuditingManager auditingManager() {
		return new AuditingManager();
	}

	/**
	 * Returns new instance of ServiceBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "serviceBusinessLogic")
	public ServiceBusinessLogic serviceBusinessLogic() {
		return new ServiceBusinessLogic();
	}

	/**
	 * Returns new instance of ComponentsUtils
	 * 
	 * @return
	 */
	@Bean(name = "componentUtils")
	public ComponentsUtils componentUtils() {
		return new ComponentsUtils();
	}

	/**
	 * Returns new instance of ToscaArtifactsAlignment
	 * 
	 * @return
	 */
	@Bean(name = "toscaArtifactsAlignment")
	public ToscaArtifactsAlignment toscaArtifactsAlignment() {
		return new ToscaArtifactsAlignment();
	}

	/**
	 * Returns new instance of ArtifactsBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "artifactBusinessLogic")
	public ArtifactsBusinessLogic artifactBusinessLogic() {
		return new ArtifactsBusinessLogic();
	}

	/**
	 * Returns new instance of ResourceBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "resourceBusinessLogic")
	public ResourceBusinessLogic resourceBusinessLogic() {
		return new ResourceBusinessLogic();
	}

	/**
	 * Returns new instance of LifecycleBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "lifecycleBusinessLogic")
	public LifecycleBusinessLogic lifecycleBusinessLogic() {
		return new LifecycleBusinessLogic();
	}

	/**
	 * Returns new instance of ServiceDistributionArtifactsBuilder
	 * 
	 * @return
	 */
	@Bean(name = "serviceDistributionArtifactsBuilder")
	public ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder() {
		return new ServiceDistributionArtifactsBuilder();
	}

	/**
	 * Returns new instance of DistributionEngine
	 * 
	 * @return
	 */
	@Bean(name = "distributionEngine")
	public IDistributionEngine distributionEngine() {
		// This dependency is needed for initializing context but is not used
		return null;
	}

	/**
	 * Returns new instance of ElasticSearchClient
	 * 
	 * @return
	 */
	@Bean(name = "elasticsearch-client")
	public ElasticSearchClient elasticsearchClient() {
		// This dependency is needed for initializing context but is not used
		return null;
	}

	/**
	 * Returns new instance of ProductBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "productBusinessLogic")
	public ProductBusinessLogic productBusinessLogic() {
		return new ProductBusinessLogic();
	}

	/**
	 * Returns new instance of ProductComponentInstanceBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "productComponentInstanceBusinessLogic")
	public ProductComponentInstanceBusinessLogic productComponentInstanceBusinessLogic() {
		return new ProductComponentInstanceBusinessLogic();
	}

	/**
	 * Returns new instance of ToscaExportHandler
	 * 
	 * @return
	 */
	@Bean(name = "tosca-export-handler")
	public ToscaExportHandler toscaExportHandler() {
		return new ToscaExportHandler();
	}

	/**
	 * Returns new instance of CsarOperation
	 * 
	 * @return
	 */
	@Bean(name = "csar-operation")
	public CsarOperation csarOperation() {
		return new CsarOperation();
	}

	/**
	 * Returns new instance of OnboardingClient
	 * 
	 * @return
	 */
	@Bean(name = "onboarding-client")
	public OnboardingClient onboardingClient() {
		return new OnboardingClient();
	}

	/**
	 * Returns new instance of VFComponentInstanceBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "vfComponentInstanceBusinessLogic")
	public VFComponentInstanceBusinessLogic vfComponentInstanceBusinessLogic() {
		return new VFComponentInstanceBusinessLogic();
	}

	/**
	 * Returns new instance of ResourceImportManager
	 * 
	 * @return
	 */
	@Bean(name = "resourceImportManager")
	public ResourceImportManager resourceImportManager() {
		return new ResourceImportManager();
	}

	/**
	 * Returns new instance of GroupBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "groupBusinessLogic")
	public GroupBusinessLogic groupBusinessLogic() {
		return new GroupBusinessLogic();
	}

	/**
	 * Returns new instance of InputsBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "inputsBusinessLogic")
	public InputsBusinessLogic inputsBusinessLogic() {
		return new InputsBusinessLogic();
	}

	/**
	 * Returns new instance of CompositionBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "compositionBusinessLogic")
	public CompositionBusinessLogic compositionBusinessLogic() {
		return new CompositionBusinessLogic();
	}

	/**
	 * Returns new instance of CsarUtils
	 * 
	 * @return
	 */
	@Bean(name = "csar-utils")
	public CsarUtils csarUtils() {
		return new CsarUtils();
	}

	/**
	 * Returns new instance of ServiceComponentInstanceBusinessLogic
	 * 
	 * @return
	 */
	@Bean(name = "serviceComponentInstanceBusinessLogic")
	public ServiceComponentInstanceBusinessLogic serviceComponentInstanceBusinessLogic() {
		return new ServiceComponentInstanceBusinessLogic();
	}
	/** 
	 * 
	 * @return new instance of migration1702
	 */
	@Bean(name = "migration1702")
	public Migration1702 migration1702() {
		return new Migration1702();
	}


	@Bean(name = "migrationUtils")
	public MigrationOperationUtils migrationUtils() {
		return new MigrationOperationUtils();
	}

    @Bean(name = "vfModulesPropertiesAdding")
    public VfModulesPropertiesAdding vfModulesPropertiesAdding() {
        return new VfModulesPropertiesAdding();
    }

}
