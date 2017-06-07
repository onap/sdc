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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.operations.impl.ComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.CapabilityInstData;
import org.openecomp.sdc.be.resources.data.RequirementData;
import org.openecomp.sdc.be.unittests.utils.FactoryUtils;

import fj.data.Either;

public class ComponentInstanceOperationTest {

	@InjectMocks
	ComponentInstanceOperation componentInstanceOperation = new ComponentInstanceOperation();
	@InjectMocks
	private TitanGenericDao titanGenericDao = Mockito.mock(TitanGenericDao.class);

	@Before
	public void beforeTest() {
		Mockito.reset(titanGenericDao);
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetCapabilities() {

		ComponentInstance ri = FactoryUtils.createResourceInstance();
		CapabilityData capData = FactoryUtils.createCapabilityData();
		Either<List<ImmutablePair<GraphNode, GraphEdge>>, TitanOperationStatus> childNodesReturned = prepareChildNodeRetValue(capData);

		Mockito.when(titanGenericDao.getChildrenNodes(Mockito.anyString(), Mockito.anyString(), Mockito.any(GraphEdgeLabels.class), Mockito.any(NodeTypeEnum.class), Mockito.any())).thenReturn(childNodesReturned);

		// ImmutablePair<ComponentInstance, List<ImmutablePair<CapabilityData,
		// GraphEdge>>> instanceAndCapabilities =
		// componentInstanceOperation.getCapabilities(ri,
		// NodeTypeEnum.Resource);

		Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> instanceAndCapabilities = componentInstanceOperation.getCapabilities(ri, NodeTypeEnum.Resource);

		// assertTrue(instanceAndCapabilities.left.getUniqueId().equals(ri.getUniqueId()));
		assertTrue(instanceAndCapabilities.left().value().size() == 1);
		assertTrue(instanceAndCapabilities.left().value().get(0).left.getUniqueId().equals(capData.getUniqueId()));

	}

	@Test
	public void testGetRequirements() {
		ComponentInstance ri = FactoryUtils.createResourceInstance();
		RequirementData reqData = FactoryUtils.createRequirementData();
		Either<List<ImmutablePair<GraphNode, GraphEdge>>, TitanOperationStatus> childNodesReturned = prepareChildNodeRetValue(reqData);

		Mockito.when(titanGenericDao.getChildrenNodes(Mockito.anyString(), Mockito.anyString(), Mockito.any(GraphEdgeLabels.class), Mockito.any(NodeTypeEnum.class), Mockito.any())).thenReturn(childNodesReturned);

		// ImmutablePair<ComponentInstance, List<ImmutablePair<RequirementData,
		// GraphEdge>>> instanceAndCapabilities =
		// componentInstanceOperation.getRequirements(ri,
		// NodeTypeEnum.Resource);
		Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> instanceAndCapabilities = componentInstanceOperation.getRequirements(ri, NodeTypeEnum.Resource);

		// assertTrue(instanceAndCapabilities.left.getUniqueId().equals(ri.getUniqueId()));
		// assertTrue(instanceAndCapabilities.right.size() == 1);
		// assertTrue(instanceAndCapabilities.right.get(0).left.getUniqueId().equals(reqData.getUniqueId()));

		assertTrue(instanceAndCapabilities.left().value().size() == 1);
		assertTrue(instanceAndCapabilities.left().value().get(0).left.getUniqueId().equals(reqData.getUniqueId()));

	}

	private CapabilityInstData buildCapabilityInstanceData(String resourceInstanceId, CapabilityDefinition capability) {
		CapabilityInstData capabilityInstance = new CapabilityInstData();
		Long creationTime = System.currentTimeMillis();
		String uniqueId = UniqueIdBuilder.buildCapabilityInstanceUid(resourceInstanceId, capability.getName());

		capabilityInstance.setCreationTime(creationTime);
		capabilityInstance.setModificationTime(creationTime);
		capabilityInstance.setUniqueId(uniqueId);

		return capabilityInstance;
	}

	private Either<List<ImmutablePair<GraphNode, GraphEdge>>, TitanOperationStatus> prepareChildNodeRetValue(GraphNode data) {
		ImmutablePair<GraphNode, GraphEdge> pair = new ImmutablePair<>(data, FactoryUtils.createGraphEdge());
		List<ImmutablePair<GraphNode, GraphEdge>> retList = new ArrayList<>();
		retList.add(pair);
		return Either.left(retList);
	}

}
