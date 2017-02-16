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

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.thinkaurelius.titan.core.TitanTransaction;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.ICapabilityOperation;
import org.openecomp.sdc.be.model.operations.api.IRequirementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.CapabilityInstData;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.RequirementData;
import org.openecomp.sdc.be.unittests.utils.FactoryUtils;

import fj.data.Either;

public class ComponentOperationTest {
	@InjectMocks
	ComponentOperation compOperation = getAnnonimusImpl();

	ComponentInstanceOperation componentInstanceOperation = Mockito.mock(ComponentInstanceOperation.class);
	TitanGenericDao titanGenericDao = Mockito.mock(TitanGenericDao.class);
	ICapabilityOperation capabilityOperation = Mockito.mock(ICapabilityOperation.class);
	IRequirementOperation requirementOperation = Mockito.mock(IRequirementOperation.class);

	@Before
	public void beforeTest() {
		Mockito.reset(componentInstanceOperation, requirementOperation, capabilityOperation);
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetCapabilities() {
		Resource vf = FactoryUtils.createVF();
		ComponentInstance ri = FactoryUtils.createResourceInstance();
		CapabilityData capData = FactoryUtils.createCapabilityData();

		FactoryUtils.addComponentInstanceToVF(vf, ri);
		Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> capDataList = prepareCompOperationReturnValue(
				ri, capData);

		prepareMocksForCapabilitiesMethods(ri, capDataList);

		Map<String, List<CapabilityDefinition>> capabilities = compOperation
				.getCapabilities(vf, NodeTypeEnum.Resource, false).left().value();
		assertTrue(capabilities.size() == 1);
		Entry<String, List<CapabilityDefinition>> entry = capabilities.entrySet().iterator().next();
		assertTrue(entry.getKey().equals(capData.getType()));
		assertTrue(entry.getValue().size() == 1);
		assertTrue(entry.getValue().get(0).getUniqueId().equals(capData.getUniqueId()));
	}

	@Test
	public void testGetRequirments() {
		Resource vf = FactoryUtils.createVF();
		ComponentInstance ri = FactoryUtils.createResourceInstance();

		RequirementData reqData = FactoryUtils.createRequirementData();

		FactoryUtils.addComponentInstanceToVF(vf, ri);

		Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> reqDataEdgeList = prepareCompOperationReturnValue(
				ri, reqData);

		prepareMocksForRequirmenetsMethods(ri, reqDataEdgeList);

		Map<String, List<RequirementDefinition>> requirements = compOperation
				.getRequirements(vf, NodeTypeEnum.Resource, false).left().value();
		assertTrue(requirements.size() == 1);
		Entry<String, List<RequirementDefinition>> entry = requirements.entrySet().iterator().next();
		assertTrue(entry.getKey().equals(FactoryUtils.Constants.DEFAULT_CAPABILITY_TYPE));
		assertTrue(entry.getValue().size() == 1);
		assertTrue(entry.getValue().get(0).getUniqueId().equals(reqData.getUniqueId()));
	}

	private void prepareMocksForRequirmenetsMethods(ComponentInstance ri,
			Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> reqDataEdgeList) {

		when(componentInstanceOperation.getRequirements(ri, NodeTypeEnum.Resource)).thenReturn(reqDataEdgeList);
		when(requirementOperation.getRequirement(Mockito.anyString())).then(createReqDefAnswer());
	}

	private void prepareMocksForCapabilitiesMethods(ComponentInstance ri,
			Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> capDataList) {
		when(componentInstanceOperation.getCapabilities(ri, NodeTypeEnum.Resource)).thenReturn(capDataList);
		when(capabilityOperation.getCapabilityByCapabilityData(Mockito.any(CapabilityData.class)))
				.then(createCapDefByDataAnswer());
		List<ImmutablePair<CapabilityInstData, GraphEdge>> capInstList = new ArrayList<>();
		CapabilityInstData curCapabilityInst = FactoryUtils.createCapabilityInstData();
		GraphEdge edge = new GraphEdge();
		Map<String, Object> properties = new HashMap<>();
		properties.put(GraphPropertiesDictionary.CAPABILITY_ID.getProperty(),
				capDataList.left().value().get(0).getLeft().getUniqueId());
		edge.setProperties(properties);
		ImmutablePair<CapabilityInstData, GraphEdge> pair = new ImmutablePair<CapabilityInstData, GraphEdge>(
				curCapabilityInst, edge);
		capInstList.add(pair);
		when(titanGenericDao.getChildrenNodes(
				UniqueIdBuilder.getKeyByNodeType(
						NodeTypeEnum.getByNameIgnoreCase(ri.getOriginType().getInstanceType().trim())),
				ri.getUniqueId(), GraphEdgeLabels.CAPABILITY_INST, NodeTypeEnum.CapabilityInst,
				CapabilityInstData.class)).thenReturn(Either.left(capInstList));

		when(titanGenericDao.getChild(
				UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.getByName(curCapabilityInst.getLabel())),
				curCapabilityInst.getUniqueId(), GraphEdgeLabels.INSTANCE_OF, NodeTypeEnum.Capability,
				CapabilityData.class)).thenReturn(Either.left(capDataList.left().value().get(0)));

		PropertyValueData propertyValueData = FactoryUtils.createPropertyData();
		ImmutablePair<PropertyValueData, GraphEdge> propPair = new ImmutablePair<PropertyValueData, GraphEdge>(
				propertyValueData, null);
		List<ImmutablePair<PropertyValueData, GraphEdge>> propPairList = new ArrayList<>();
		propPairList.add(propPair);
		when(titanGenericDao.getChildrenNodes(
				UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.getByName(curCapabilityInst.getLabel())),
				curCapabilityInst.getUniqueId(), GraphEdgeLabels.PROPERTY_VALUE, NodeTypeEnum.PropertyValue,
				PropertyValueData.class)).thenReturn(Either.left(propPairList));

		CapabilityDefinition capDef = FactoryUtils
				.convertCapabilityDataToCapabilityDefinitionAddProperties(capDataList.left().value().get(0).getLeft());
		List<PropertyDefinition> propDefList = capDef.getProperties().stream().filter(p -> p.getName().equals("host"))
				.collect(Collectors.toList());
		PropertyDefinition propDef = propDefList.get(0);
		PropertyData propData = FactoryUtils.convertCapabilityDefinitionToCapabilityData(propDef);

		ImmutablePair<PropertyData, GraphEdge> defPropPair = new ImmutablePair<PropertyData, GraphEdge>(propData, edge);

		when(titanGenericDao.getChild(
				UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.getByName(propertyValueData.getLabel())),
				propertyValueData.getUniqueId(), GraphEdgeLabels.PROPERTY_IMPL, NodeTypeEnum.Property,
				PropertyData.class)).thenReturn(Either.left(defPropPair));
		List<CapabilityDefinition> capDefList = new ArrayList<>();
		capDefList.add(capDef);
		when(componentInstanceOperation.updateCapDefPropertyValues(Mockito.any(ComponentInstance.class),
				Mockito.any(List.class))).thenReturn(Either.left(capDefList));
	}

	private <Data> Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus> prepareCompOperationReturnValue(
			ComponentInstance ri, Data data) {
		ImmutablePair<Data, GraphEdge> dataEdgePair = new ImmutablePair<>(data, new GraphEdge());
		List<ImmutablePair<Data, GraphEdge>> dataEdgeList = new ArrayList<>();
		dataEdgeList.add(dataEdgePair);
		return Either.left(dataEdgeList);
	}

	private Answer<Either<RequirementDefinition, TitanOperationStatus>> createReqDefAnswer() {
		return new Answer<Either<RequirementDefinition, TitanOperationStatus>>() {

			@Override
			public Either<RequirementDefinition, TitanOperationStatus> answer(InvocationOnMock invocation)
					throws Throwable {
				String reqDataId = (String) invocation.getArguments()[0];
				return Either.left(FactoryUtils.convertRequirementDataIDToRequirementDefinition(reqDataId));
			}
		};
	}

	private Answer<Either<CapabilityDefinition, TitanOperationStatus>> createCapDefByDataAnswer() {
		return new Answer<Either<CapabilityDefinition, TitanOperationStatus>>() {

			@Override
			public Either<CapabilityDefinition, TitanOperationStatus> answer(InvocationOnMock invocation)
					throws Throwable {
				CapabilityData capData = (CapabilityData) invocation.getArguments()[0];
				return Either.left(FactoryUtils.convertCapabilityDataToCapabilityDefinitionAddProperties(capData));
			}
		};
	}

	private ComponentOperation getAnnonimusImpl() {
		return new ComponentOperation() {

			@Override
			protected StorageOperationStatus validateCategories(Component currentComponent, Component component,
					ComponentMetadataData componentData, NodeTypeEnum type) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected <T extends Component> StorageOperationStatus updateDerived(Component component,
					Component currentComponent, ComponentMetadataData updatedResourceData, Class<T> clazz) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected <T> Either<T, StorageOperationStatus> updateComponent(T component, boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Either<Integer, StorageOperationStatus> increaseAndGetComponentInstanceCounter(String componentId,
					boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected ComponentMetadataData getMetaDataFromComponent(Component component) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> Either<T, StorageOperationStatus> getLightComponent(String id, boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected <T> Either<T, StorageOperationStatus> getComponentByNameAndVersion(String name, String version,
					Map<String, Object> additionalParams, boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> Either<T, StorageOperationStatus> getComponent(String id, boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			// @Override
			// public <T> Either<T, StorageOperationStatus>
			// getComponent_tx(String id, boolean inTransaction) {
			// // TODO Auto-generated method stub
			// return null;
			// }

			@Override
			public Either<List<ArtifactDefinition>, StorageOperationStatus> getAdditionalArtifacts(String resourceId,
					boolean recursively, boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> Either<T, StorageOperationStatus> cloneComponent(T other, String version,
					boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Component getDefaultComponent() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isComponentExist(String componentId) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Either<Component, StorageOperationStatus> getMetadataComponent(String id, boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			Component convertComponentMetadataDataToComponent(ComponentMetadataData componentMetadataData) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			TitanOperationStatus setComponentCategoriesFromGraph(Component component) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Either<Boolean, StorageOperationStatus> validateComponentNameExists(String componentName) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Either<Component, StorageOperationStatus> markComponentToDelete(Component componentToDelete,
					boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Either<Component, StorageOperationStatus> deleteComponent(String id, boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Either<Boolean, StorageOperationStatus> isComponentInUse(String componentId) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Either<List<String>, StorageOperationStatus> getAllComponentsMarkedForDeletion() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> Either<T, StorageOperationStatus> cloneComponent(T other, String version,
					LifecycleStateEnum targetLifecycle, boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> Either<T, StorageOperationStatus> getComponent(String id,
					ComponentParametersView componentParametersView, boolean inTrasnaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> Either<List<T>, StorageOperationStatus> getFilteredComponents(Map<FilterKeyEnum, String> filters,
					boolean inTransaction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected <T> Either<T, StorageOperationStatus> updateComponentFilterResult(T component,
					boolean inTransaction, ComponentParametersView filterParametersView) {
				// TODO Auto-generated method stub
				return null;
			}

		};
	}

}
