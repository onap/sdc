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

package org.openecomp.sdc.asdctool.impl;

import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactUuidFixTest {
	@InjectMocks
	private ArtifactUuidFix test;

	@Mock
	private Component component;

	@Mock
	private JanusGraphDao janusGraphDao;

	@Mock
	private JanusGraphVertex vertex;

	@Mock
	ToscaOperationFacade toscaOperationFacade;

	@Mock
	ArtifactCassandraDao artifactCassandraDao;

	@Mock
	Service service;

	@Test
	public void testDoFixVf() {
		String fixComponent = "";
		String runMode = "";
		String uniqueId = "uniqueId";
		boolean result;
		fixComponent = "vf_only";
		Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
		hasProps1.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
		hasProps1.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		List<GraphVertex> list = new ArrayList<>();
		GraphVertex graphVertex = new GraphVertex();
		graphVertex.setVertex(vertex);
		graphVertex.setUniqueId(uniqueId);
		graphVertex.setMetadataProperties(hasProps1);
		list.add(graphVertex);
		when(janusGraphDao.getByCriteria(VertexTypeEnum.NODE_TYPE, hasProps1)).thenReturn(Either.left(list));

		Map<GraphPropertyEnum, Object> hasProps2 = new HashMap<>();
		hasProps2.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
		hasProps2.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VF);
		hasProps2.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		when(janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps2)).thenReturn(Either.left(list));

		Map<GraphPropertyEnum, Object> hasProps3 = new HashMap<>();
		hasProps3.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
		hasProps3.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		when(janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps3)).thenReturn(Either.left(list));

		Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
		hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
		hasProps.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VF.name());
		Map<GraphPropertyEnum, Object> hasNotProps = new HashMap<>();
		hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
		when(janusGraphDao
        .getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps, hasNotProps, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(list));
		Resource resource = new Resource();
		resource.setName(uniqueId);
		Map<String, ArtifactDefinition> deployArtifact = new HashMap<>();
		ArtifactDefinition artifactDefinition = new ArtifactDefinition();
		artifactDefinition.setArtifactType(ArtifactTypeEnum.VF_MODULES_METADATA.getType());
		artifactDefinition.setUniqueId("one.two");
		artifactDefinition.setArtifactUUID("one.two");
		deployArtifact.put("two", artifactDefinition);
		resource.setDeploymentArtifacts(deployArtifact);
		List<GroupDefinition> groups = new ArrayList<>();
		GroupDefinition groupDefinition = new GroupDefinition();
		groupDefinition.setType(Constants.DEFAULT_GROUP_VF_MODULE);
		List<String> artifacts = new ArrayList<>();
		artifacts.add("one.two");
		groupDefinition.setArtifacts(artifacts);
		groupDefinition.setArtifactsUuid(artifacts);
		groups.add(groupDefinition);
		resource.setGroups(groups);
		when(toscaOperationFacade.getToscaElement(graphVertex.getUniqueId())).thenReturn(Either.left(resource));
		List<Service> serviceList = new ArrayList<>();
		serviceList.add(service);

		result = test.doFix(fixComponent, runMode);
		assertEquals(true, result);
	}

	@Test
	public void testDoFixServiceVf()
	{
		String fixComponent = "distributed_only";
		String runMode = "service_vf";
		String uniqueId = "uniqueId";
		boolean result;
		Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
		hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
		hasProps.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		hasProps.put(GraphPropertyEnum.DISTRIBUTION_STATUS, DistributionStatusEnum.DISTRIBUTED.name());
		Map<GraphPropertyEnum, Object> hasNotProps = new HashMap<>();
		hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
		Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
		hasProps1.put(GraphPropertyEnum.NAME, "name");
		List<GraphVertex> list = new ArrayList<>();
		GraphVertex graphVertex = new GraphVertex();
		graphVertex.setUniqueId(uniqueId);
		graphVertex.setMetadataProperties(hasProps1);
		list.add(graphVertex);

		Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
		ArtifactDefinition artifactDefinition = new ArtifactDefinition();
		artifactDefinition.setArtifactType(ArtifactTypeEnum.VF_MODULES_METADATA.name());
		artifactDefinition.setEsId("esID");
		deploymentArtifacts.put("1",artifactDefinition);
		List<GroupInstance> groupInstances = new ArrayList<>();
		GroupInstance groupInstance = new GroupInstance();
		groupInstances.add(groupInstance);

		ComponentInstance componentInstance = new ComponentInstance();
		componentInstance.setDeploymentArtifacts(deploymentArtifacts);
		componentInstance.setGroupInstances(groupInstances);

		Service service = new Service();
		service.setUniqueId(uniqueId);
		List<ComponentInstance> componentInstances = new ArrayList<>();
		componentInstances.add(componentInstance);
		service.setComponentInstances(componentInstances);

		when(janusGraphDao
        .getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps, hasNotProps, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(list));
		when(toscaOperationFacade.getToscaElement(ArgumentMatchers.eq(graphVertex.getUniqueId()),any(ComponentParametersView.class)))
				.thenReturn(Either.left(service));

		DAOArtifactData artifactData = new DAOArtifactData();
		byte[] data = "value".getBytes();
		ByteBuffer bufferData = ByteBuffer.wrap(data);
		artifactData.setData(bufferData);

		Either<DAOArtifactData, CassandraOperationStatus> artifactfromESres = Either.left(artifactData);
		when(artifactCassandraDao.getArtifact(anyString())).thenReturn(artifactfromESres);
		result = test.doFix(fixComponent, runMode);
		assertEquals(false, result);
	}

	@Test
	public void testDoFixOnly()
	{
		String runMode = "fix_only_services";
		String fixComponent = "";
		boolean result;
		Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
		hasProps1.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
		hasProps1.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		List<GraphVertex> list = new ArrayList<>();
		GraphVertex graphVertex = new GraphVertex();
		graphVertex.setVertex(vertex);
		graphVertex.setUniqueId("uniqueId");
		graphVertex.setMetadataProperties(hasProps1);
		graphVertex.addMetadataProperty(GraphPropertyEnum.NAME, "name");
		list.add(graphVertex);
		Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
		hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
		Map<GraphPropertyEnum, Object> hasNotProps = new HashMap<>();
		hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
		when(janusGraphDao
        .getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps, hasNotProps, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(list));
		result = test.doFix(fixComponent, runMode);
		assertEquals(false,result);
	}


	@Test
	public void testDoFixVfWithFixMode() {
		String fixComponent = "";
		String runMode = "fix";
		String uniqueId = "uniqueId";
		boolean result;
		fixComponent = "vf_only";
		Map<GraphPropertyEnum, Object> hasProps1 = new HashMap<>();
		hasProps1.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
		hasProps1.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		List<GraphVertex> list = new ArrayList<>();
		GraphVertex graphVertex = new GraphVertex();
		graphVertex.setVertex(vertex);
		graphVertex.setUniqueId(uniqueId);
		graphVertex.setMetadataProperties(hasProps1);
		list.add(graphVertex);
		when(janusGraphDao.getByCriteria(VertexTypeEnum.NODE_TYPE, hasProps1)).thenReturn(Either.left(list));

		Map<GraphPropertyEnum, Object> hasProps2 = new HashMap<>();
		hasProps2.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
		hasProps2.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VF);
		hasProps2.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		when(janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps2)).thenReturn(Either.left(list));

		Map<GraphPropertyEnum, Object> hasProps3 = new HashMap<>();
		hasProps3.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
		hasProps3.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		when(janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps3)).thenReturn(Either.left(list));

		Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
		hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
		hasProps.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VF.name());
		Map<GraphPropertyEnum, Object> hasNotProps = new HashMap<>();
		hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
		when(janusGraphDao
			.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps, hasNotProps, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(list));
		Resource resource = new Resource();
		resource.setName(uniqueId);
		Map<String, ArtifactDefinition> deployArtifact = new HashMap<>();
		ArtifactDefinition artifactDefinition = new ArtifactDefinition();
		artifactDefinition.setArtifactType(ArtifactTypeEnum.VF_MODULES_METADATA.getType());
		artifactDefinition.setUniqueId("one.two");
		artifactDefinition.setArtifactUUID("one.two");
		deployArtifact.put("two", artifactDefinition);
		resource.setDeploymentArtifacts(deployArtifact);
		List<GroupDefinition> groups = new ArrayList<>();
		GroupDefinition groupDefinition = new GroupDefinition();
		groupDefinition.setType(Constants.DEFAULT_GROUP_VF_MODULE);
		List<String> artifacts = new ArrayList<>();
		artifacts.add("one.two");
		groupDefinition.setArtifacts(artifacts);
		groupDefinition.setArtifactsUuid(artifacts);
		groups.add(groupDefinition);
		resource.setGroups(groups);
		resource.setUniqueId(uniqueId);
		resource.setComponentType(ComponentTypeEnum.SERVICE_INSTANCE);

		when(toscaOperationFacade.getToscaElement(graphVertex.getUniqueId())).thenReturn(Either.left(resource));
		when(toscaOperationFacade.getToscaFullElement(Mockito.anyString())).thenReturn(Either.left(component));
		when(component.getUniqueId()).thenReturn(uniqueId);
		when(toscaOperationFacade.getToscaElement(Mockito.anyString(), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(component));
		when(janusGraphDao.getVertexById(uniqueId, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(graphVertex));
		when(janusGraphDao.getChildVertex(graphVertex, EdgeLabelEnum.GROUPS, JsonParseFlagEnum.ParseJson)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
		when(janusGraphDao.getChildVertex(graphVertex, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS, JsonParseFlagEnum.ParseJson)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
		when(janusGraphDao.getChildVertex(graphVertex, EdgeLabelEnum.TOSCA_ARTIFACTS, JsonParseFlagEnum.ParseJson)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));

		List<Service> serviceList = new ArrayList<>();
		serviceList.add(service);

		result = test.doFix(fixComponent, runMode);
		assertEquals(true, result);
	}

}
