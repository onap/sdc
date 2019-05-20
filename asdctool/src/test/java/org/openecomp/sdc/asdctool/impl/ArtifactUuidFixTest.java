package org.openecomp.sdc.asdctool.impl;

import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;

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
		byte[] payload = "value".getBytes();

		ESArtifactData esArtifactData =new ESArtifactData();
		esArtifactData.setDataAsArray(payload);
		Either<ESArtifactData, CassandraOperationStatus> artifactfromESres = Either.left(esArtifactData);
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
}