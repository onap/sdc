/*

 * Copyright (c) 2018 AT&T Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import com.google.common.collect.Lists;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NodeTemplateOperationTest extends ModelTestBase {

    private final static String COMPONENT_ID = "componentId";
    private final static String TO_INSTANCE_ID = "toInstanceId";
    private final static String FROM_INSTANCE_ID = "fromInstanceId";
    private final static String RELATION_ID = "relationId";
    private final static String CAPABILITY_OWNER_ID = "capabilityOwnerId";
    private final static String CAPABILITY_UID = "capabilityUid";
    private final static String CAPABILITY_NAME = "capabilityName";
    private final static String REQUIREMENT_OWNER_ID = "requirementOwnerId";
    private final static String REQUIREMENT_UID = "requirementUid";
    private final static String REQUIREMENT_NAME = "requirementName";
    private final static String RELATIONSHIP_TYPE = "relationshipType";

    private static Map<String, MapListCapabilityDataDefinition> fulfilledCapability;
    private static Map<String, MapListRequirementDataDefinition> fulfilledRequirement;
    private static CapabilityDataDefinition capability;
    private static RequirementDataDefinition requirement;
    private static RequirementCapabilityRelDef relation;

    @InjectMocks
    private static NodeTemplateOperation operation;

    @Mock
    private static JanusGraphDao janusGraphDao;

    @Mock
    private static TopologyTemplateOperation topologyTemplateOperation;

    @BeforeClass
    public static void setup() {
        init();
        janusGraphDao = Mockito.mock(JanusGraphDao.class);
        operation = new NodeTemplateOperation();
        operation.setJanusGraphDao(janusGraphDao);
        buildDataDefinition();
    }

    private static void buildDataDefinition() {
        buildCapabiltyDataDefinition();
        buildRequirementDataDefinition();
        buildRelation();
    }

    @Test
    public void testGetFulfilledCapabilityByRelationSuccess(){
        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        Either<GraphVertex, JanusGraphOperationStatus> vertexRes = Either.left(vertex);
        when(janusGraphDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);

        GraphVertex dataVertex = new GraphVertex();
        dataVertex.setJson(fulfilledCapability);
        Either<GraphVertex, JanusGraphOperationStatus> childVertexRes = Either.left(dataVertex);
        when(janusGraphDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_CAPABILITIES), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
        Either<CapabilityDataDefinition, StorageOperationStatus> result = operation.getFulfilledCapabilityByRelation(COMPONENT_ID, TO_INSTANCE_ID, relation, this::isBelongingCapability);
        assertTrue(result.isLeft());
        assertEquals(result.left().value(), capability);
    }

    @Test
    public void testGetFulfilledRequirementByRelationSuccess(){
        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        Either<GraphVertex, JanusGraphOperationStatus> vertexRes = Either.left(vertex);
        when(janusGraphDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);

        GraphVertex dataVertex = new GraphVertex();
        dataVertex.setJson(fulfilledRequirement);
        Either<GraphVertex, JanusGraphOperationStatus> childVertexRes = Either.left(dataVertex);
        when(janusGraphDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_REQUIREMENTS), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
        Either<RequirementDataDefinition, StorageOperationStatus> result = operation.getFulfilledRequirementByRelation(COMPONENT_ID, FROM_INSTANCE_ID, relation, this::isBelongingRequirement);
        assertTrue(result.isLeft());
        assertEquals(result.left().value(), requirement);
    }

    @Test
    public void testGetFulfilledCapabilityByRelationNotFoundFailure(){
        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        Either<GraphVertex, JanusGraphOperationStatus> vertexRes = Either.left(vertex);
        when(janusGraphDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);

        Either<GraphVertex, JanusGraphOperationStatus> childVertexRes = Either.right(
            JanusGraphOperationStatus.NOT_FOUND);
        when(janusGraphDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_CAPABILITIES), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
        Either<CapabilityDataDefinition, StorageOperationStatus> result = operation.getFulfilledCapabilityByRelation(COMPONENT_ID, TO_INSTANCE_ID, relation, this::isBelongingCapability);
        assertTrue(result.isRight());
        assertSame(result.right().value(), StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void testGetFulfilledRequirementByRelationNotFoundFailure(){
        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        Either<GraphVertex, JanusGraphOperationStatus> vertexRes = Either.left(vertex);
        when(janusGraphDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);

        Either<GraphVertex, JanusGraphOperationStatus> childVertexRes = Either.right(
            JanusGraphOperationStatus.NOT_FOUND);
        when(janusGraphDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_REQUIREMENTS), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
        Either<RequirementDataDefinition, StorageOperationStatus> result = operation.getFulfilledRequirementByRelation(COMPONENT_ID, FROM_INSTANCE_ID, relation, this::isBelongingRequirement);
        assertTrue(result.isRight());
        assertSame(result.right().value(), StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void testUpdateCIMetadataOfTopologyTemplate() {
        Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> result;
        String id = "id";
        TopologyTemplate container = new TopologyTemplate();
        ToscaElement toscaElement = new TopologyTemplate();
        toscaElement.setResourceType(ResourceTypeEnum.VF);
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setName(id);
        componentInstance.setComponentUid(id);
        container.setUniqueId(id);
        GraphVertex graphVertex = new GraphVertex();
        when(janusGraphDao.getVertexById(container.getUniqueId(), JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(graphVertex));
        when(janusGraphDao.updateVertex(graphVertex)).thenReturn(Either.left(graphVertex));
        when(topologyTemplateOperation.getToscaElement(anyString())).thenReturn(Either.left(toscaElement));

        result = operation.updateComponentInstanceMetadataOfTopologyTemplate(container, toscaElement, componentInstance);
        assertTrue(result.isLeft());
    }

	@Test
	public void testGetDefaultHeatTimeout() {
		Integer result;

		// default test
		result = NodeTemplateOperation.getDefaultHeatTimeout();
    }

	@Test
    public void testPrepareInstDeploymentArtifactPerInstance() {
        Map<String, Object> deploymentResourceArtifacts = new HashMap<>();
        Map<String, ArtifactDataDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
        artifactDataDefinition.setArtifactType("HEAT");
        artifactDataDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        deploymentArtifacts.put("1", artifactDataDefinition);
        deploymentResourceArtifacts.put("1", artifactDataDefinition);
        String componentInstanceId = "componentInstanceId";
        User user = new User();
        user.setUserId("userId");
        user.setFirstName("first");
        user.setLastName("last");
        String envType = "VfHeatEnv";
        MapArtifactDataDefinition result;

        result = operation.prepareInstDeploymentArtifactPerInstance(deploymentArtifacts, componentInstanceId, user,
                envType);
        Assert.assertEquals(2, result.getMapToscaDataDefinition().size());
    }

	@Test
	public void testCreateCapPropertyKey() throws Exception {
		String key = "";
		String instanceId = "";
		String result;

		// default test
		result = NodeTemplateOperation.createCapPropertyKey(key, instanceId);
	}

	@Test
	public void testPrepareCalculatedCapabiltyForNodeType() {
        Map<String, ListCapabilityDataDefinition> capabilities = new HashMap<>();
        ListCapabilityDataDefinition listCapDataDefinition = new ListCapabilityDataDefinition();
        List<CapabilityDataDefinition> listToscaDataDefinition = new ArrayList<>();
        CapabilityDataDefinition capabilityDataDefinition = new CapabilityDefinition();
        capabilityDataDefinition.setMaxOccurrences("1");
        listToscaDataDefinition.add(capabilityDataDefinition);
        listCapDataDefinition.setListToscaDataDefinition(listToscaDataDefinition);
        capabilities.put("1", listCapDataDefinition);
        ComponentInstance componentInstance = createCompInstance();
        MapListCapabilityDataDefinition result;

        result = operation.prepareCalculatedCapabiltyForNodeType(capabilities, componentInstance);
        Assert.assertEquals(1, result.getMapToscaDataDefinition().size());
	}

    @Test
    public void testPrepareCalculatedReqForNodeType() {
        Map<String, ListRequirementDataDefinition> requirements = new HashMap<>();
        ListRequirementDataDefinition listReqDataDef = new ListRequirementDataDefinition();
        List<RequirementDataDefinition> listToscaDataDefinition = new ArrayList<>();
        RequirementDataDefinition reqDataDefinition = new RequirementDataDefinition();
        reqDataDefinition.setMaxOccurrences("1");
        listToscaDataDefinition.add(reqDataDefinition);
        listReqDataDef.setListToscaDataDefinition(listToscaDataDefinition);
        requirements.put("1", listReqDataDef);
        ComponentInstance componentInstance = createCompInstance();
        MapListRequirementDataDefinition result;

        result = operation.prepareCalculatedRequirementForNodeType(requirements, componentInstance);
        Assert.assertEquals(1, result.getMapToscaDataDefinition().size());
    }

	@Test
	public void testAddGroupInstancesToComponentInstance() throws Exception {
		Component containerComponent = null;
		ComponentInstanceDataDefinition componentInstance = null;
		List<GroupDefinition> groups = null;
		Map<String, List<ArtifactDefinition>> groupInstancesArtifacts = null;
		StorageOperationStatus result;

		result = operation.addGroupInstancesToComponentInstance(containerComponent, componentInstance, groups,
				groupInstancesArtifacts);
		Assert.assertEquals(StorageOperationStatus.OK, result);
	}

	@Test
	public void testGenerateCustomizationUUIDOnInstanceGroup() throws Exception {
		String componentId = "";
		String instanceId = "";
		List<String> groupInstances = null;
		StorageOperationStatus result;

		result = operation.generateCustomizationUUIDOnInstanceGroup(componentId, instanceId, groupInstances);
		Assert.assertEquals(StorageOperationStatus.OK, result);
	}

    private ComponentInstance createCompInstance() {
        ComponentInstance componentInstance = new ComponentInstance();
        String id = "id";
        componentInstance.setComponentUid(id);
        componentInstance.setUniqueId(id);
        componentInstance.setName(id);
        return componentInstance;
    }
    private static void buildRequirementDataDefinition() {
        buildRequirement();
        fulfilledRequirement = new HashMap<>();
        MapListRequirementDataDefinition mapListRequirementDataDefinition = new MapListRequirementDataDefinition();
        mapListRequirementDataDefinition.add(requirement.getCapability(), requirement);
        fulfilledRequirement.put(FROM_INSTANCE_ID, mapListRequirementDataDefinition);

    }

    private static void buildRequirement() {
        requirement = new RequirementDataDefinition();
        requirement.setOwnerId(REQUIREMENT_OWNER_ID);
        requirement.setUniqueId(REQUIREMENT_UID);
        requirement.setName(REQUIREMENT_NAME);
        requirement.setRelationship(RELATIONSHIP_TYPE);
    }

    private static void buildCapabiltyDataDefinition() {
        buildCapability();
        fulfilledCapability = new HashMap<>();
        MapListCapabilityDataDefinition mapListCapabiltyDataDefinition = new MapListCapabilityDataDefinition();
        mapListCapabiltyDataDefinition.add(capability.getType(), capability);
        fulfilledCapability.put(TO_INSTANCE_ID, mapListCapabiltyDataDefinition);
    }

    private static void buildCapability() {
        capability = new CapabilityDataDefinition();
        capability.setOwnerId(CAPABILITY_OWNER_ID);
        capability.setUniqueId(CAPABILITY_UID);
        capability.setName(CAPABILITY_NAME);
    }

    private static void buildRelation() {

        relation = new RequirementCapabilityRelDef();
        CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        RelationshipInfo relationInfo = new RelationshipInfo();
        relationInfo.setId(RELATION_ID);
        relationship.setRelation(relationInfo);

        relation.setRelationships(Lists.newArrayList(relationship));
        relation.setToNode(TO_INSTANCE_ID);
        relation.setFromNode(FROM_INSTANCE_ID);

        relationInfo.setCapabilityOwnerId(CAPABILITY_OWNER_ID);
        relationInfo.setCapabilityUid(CAPABILITY_UID);
        relationInfo.setCapability(CAPABILITY_NAME);
        relationInfo.setRequirementOwnerId(REQUIREMENT_OWNER_ID);
        relationInfo.setRequirementUid(REQUIREMENT_UID);
        relationInfo.setRequirement(REQUIREMENT_NAME);
        RelationshipImpl relationshipImpl  = new RelationshipImpl();
        relationshipImpl.setType(RELATIONSHIP_TYPE);
        relationInfo.setRelationships(relationshipImpl);
    }

    private boolean isBelongingRequirement(RelationshipInfo relationshipInfo, RequirementDataDefinition req) {
        return  req.getRelationship().equals(relationshipInfo.getRelationship().getType()) &&
                req.getName().equals(relationshipInfo.getRequirement()) &&
                req.getUniqueId().equals(relationshipInfo.getRequirementUid()) &&
                req.getOwnerId().equals(relationshipInfo.getRequirementOwnerId());
    }

    private boolean isBelongingCapability(RelationshipInfo relationshipInfo, CapabilityDataDefinition cap) {
        return     cap.getName().equals(relationshipInfo.getCapability()) &&
                cap.getUniqueId().equals(relationshipInfo.getCapabilityUid()) &&
                cap.getOwnerId().equals(relationshipInfo.getCapabilityOwnerId());
    }

}
