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
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraphVertex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class NodeTemplateOperationTest extends ModelTestBase {

    private static final String COMPONENT_ID = "componentId";
    private static final String TO_INSTANCE_ID = "toInstanceId";
    private static final String FROM_INSTANCE_ID = "fromInstanceId";
    private static final String RELATION_ID = "relationId";
    private static final String CAPABILITY_OWNER_ID = "capabilityOwnerId";
    private static final String CAPABILITY_UID = "capabilityUid";
    private static final String CAPABILITY_NAME = "capabilityName";
    private static final String REQUIREMENT_OWNER_ID = "requirementOwnerId";
    private static final String REQUIREMENT_UID = "requirementUid";
    private static final String REQUIREMENT_NAME = "requirementName";
    private static final String RELATIONSHIP_TYPE = "relationshipType";

    private static Map<String, MapListCapabilityDataDefinition> fulfilledCapability;
    private static Map<String, MapListRequirementDataDefinition> fulfilledRequirement;
    private static CapabilityDataDefinition capability;
    private static RequirementDataDefinition requirement;
    private static RequirementCapabilityRelDef relation;

    private final JanusGraphDao janusGraphDao = Mockito.mock(JanusGraphDao.class);
    private final TopologyTemplateOperation topologyTemplateOperation = Mockito.mock(TopologyTemplateOperation.class);

    @InjectMocks
    private NodeTemplateOperation operation;

    @BeforeAll
    public void setup() {
        init();
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
    void testGetFulfilledCapabilityByRelationSuccess() {
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
    void testGetFulfilledRequirementByRelationSuccess() {
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
    void testGetFulfilledCapabilityByRelationNotFoundFailure() {
        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        Either<GraphVertex, JanusGraphOperationStatus> vertexRes = Either.left(vertex);
        when(janusGraphDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);

        Either<GraphVertex, JanusGraphOperationStatus> childVertexRes = Either.right(
            JanusGraphOperationStatus.NOT_FOUND);
        when(janusGraphDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_CAPABILITIES), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
        Either<CapabilityDataDefinition, StorageOperationStatus> result = operation.getFulfilledCapabilityByRelation(COMPONENT_ID, TO_INSTANCE_ID, relation, this::isBelongingCapability);
        assertTrue(result.isRight());
        assertSame(StorageOperationStatus.NOT_FOUND, result.right().value());
    }

    @Test
    void testGetFulfilledRequirementByRelationNotFoundFailure() {
        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        Either<GraphVertex, JanusGraphOperationStatus> vertexRes = Either.left(vertex);
        when(janusGraphDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);

        Either<GraphVertex, JanusGraphOperationStatus> childVertexRes = Either.right(
            JanusGraphOperationStatus.NOT_FOUND);
        when(janusGraphDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_REQUIREMENTS), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
        Either<RequirementDataDefinition, StorageOperationStatus> result = operation.getFulfilledRequirementByRelation(COMPONENT_ID, FROM_INSTANCE_ID, relation, this::isBelongingRequirement);
        assertTrue(result.isRight());
        assertSame(StorageOperationStatus.NOT_FOUND, result.right().value());
    }

    @Test
    void testUpdateCIMetadataOfTopologyTemplate() {
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
    void testGetDefaultHeatTimeout() {
        Integer result;

        // default test
        result = NodeTemplateOperation.getDefaultHeatTimeout();
    }

    @Test
    void testPrepareInstDeploymentArtifactPerInstance() {
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
        assertEquals(2, result.getMapToscaDataDefinition().size());
    }

    @Test
    void testCreateCapPropertyKey() throws Exception {
        String key = "";
        String instanceId = "";
        String result;

        // default test
        result = NodeTemplateOperation.createCapPropertyKey(key, instanceId);
    }

    @Test
    void testPrepareCalculatedCapabiltyForNodeType() {
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
        assertEquals(1, result.getMapToscaDataDefinition().size());
	}

    @Test
    void testPrepareCalculatedReqForNodeType() {
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
        assertEquals(1, result.getMapToscaDataDefinition().size());
    }

    @Test
    void testAddGroupInstancesToComponentInstance() throws Exception {
        Component containerComponent = null;
        ComponentInstanceDataDefinition componentInstance = null;
        List<GroupDefinition> groups = null;
        Map<String, List<ArtifactDefinition>> groupInstancesArtifacts = null;
        StorageOperationStatus result;

        result = operation.addGroupInstancesToComponentInstance(containerComponent, componentInstance, groups,
            groupInstancesArtifacts);
        assertEquals(StorageOperationStatus.OK, result);
    }

    @Test
    void testGenerateCustomizationUUIDOnInstanceGroup() throws Exception {
        String componentId = "";
        String instanceId = "";
        List<String> groupInstances = null;
        StorageOperationStatus result;

        result = operation.generateCustomizationUUIDOnInstanceGroup(componentId, instanceId, groupInstances);
        assertEquals(StorageOperationStatus.OK, result);
    }

    @Test
    void testUpdateComponentInstanceRequirement() {
        String componentId = "";
        String componentInstanceId = "requirementOwnerId";

        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setUniqueId("uniqueId");
        when(janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(graphVertex));
        when(janusGraphDao.updateVertex(graphVertex)).thenReturn(Either.left(graphVertex));

        MapListRequirementDataDefinition mapListRequirementDataDefinition = new MapListRequirementDataDefinition();
        mapListRequirementDataDefinition.add(requirement.getCapability(), requirement);
        Map<String, MapListRequirementDataDefinition> mapOfRequirements = new HashMap<>();
        mapOfRequirements.put(requirement.getOwnerId(), mapListRequirementDataDefinition);
        GraphVertex childVertex = new GraphVertex();
        childVertex.setJson(mapOfRequirements);
        when(janusGraphDao.getChildVertex(graphVertex, EdgeLabelEnum.CALCULATED_REQUIREMENTS, JsonParseFlagEnum.ParseJson)).thenReturn(Either.left(childVertex));
        
        JanusGraphVertex outVertex = Mockito.mock(JanusGraphVertex.class);
        Edge edge = Mockito.mock(Edge.class);
        when(edge.outVertex()).thenReturn(outVertex);
        Iterator<Edge> edgeIterator = new Iterator<Edge>() {
            private int counter = 0;
            @Override
            public boolean hasNext() {
                return counter++ < 1;
            }

            @Override
            public Edge next() {
                return edge;
            }
        };
        String outId = (String) janusGraphDao
            .getProperty((JanusGraphVertex) outVertex, GraphPropertyEnum.UNIQUE_ID.getProperty());
        when(janusGraphDao.getProperty(outVertex, GraphPropertyEnum.UNIQUE_ID.getProperty())).thenReturn("uniqueId");
        when(janusGraphDao.updateVertex(childVertex)).thenReturn(Either.left(childVertex));
        JanusGraphVertex janusGraphVertex = Mockito.mock(JanusGraphVertex.class);
        childVertex.setVertex(janusGraphVertex);
        when(janusGraphVertex.edges(Direction.IN, EdgeLabelEnum.CALCULATED_REQUIREMENTS.name())).thenReturn(edgeIterator);

        StorageOperationStatus result = operation.updateComponentInstanceRequirement(componentId, componentInstanceId, requirement);
        assertEquals(StorageOperationStatus.OK, result);
    }

    @Test
    void test_addComponentInstanceOutput() {
        final NodeTemplateOperation testInstance = Mockito.spy(new NodeTemplateOperation());
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);

        final String componentInstanceId = "requirementOwnerId";

        final ComponentInstanceOutput instanceOutput = new ComponentInstanceOutput();

        doReturn(StorageOperationStatus.OK).when((BaseOperation) testInstance)
            .addToscaDataDeepElementToToscaElement(eq(COMPONENT_ID), eq(EdgeLabelEnum.INST_OUTPUTS), eq(VertexTypeEnum.INST_OUTPUTS),
                ArgumentMatchers.any(ComponentInstanceOutput.class), ArgumentMatchers.anyList(), eq(JsonPresentationFields.NAME));

        final StorageOperationStatus result = testInstance.addComponentInstanceOutput(component, componentInstanceId, instanceOutput);
        assertNotNull(result);
        assertEquals(StorageOperationStatus.OK, result);
        verify((BaseOperation) testInstance, times(1))
            .addToscaDataDeepElementToToscaElement(eq(COMPONENT_ID), eq(EdgeLabelEnum.INST_OUTPUTS), eq(VertexTypeEnum.INST_OUTPUTS),
                ArgumentMatchers.any(ComponentInstanceOutput.class), ArgumentMatchers.anyList(), eq(JsonPresentationFields.NAME));
    }

    @Test
    void test_updateComponentInstanceAttributes() {
        final NodeTemplateOperation testInstance = Mockito.spy(new NodeTemplateOperation());
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);

        final String componentInstanceId = "requirementOwnerId";

        final ComponentInstanceAttribute instanceAttribute = new ComponentInstanceAttribute();
        final List<ComponentInstanceAttribute> attributes = new ArrayList<>();
        attributes.add(instanceAttribute);

        doReturn(StorageOperationStatus.OK).when((BaseOperation) testInstance)
            .updateToscaDataDeepElementsOfToscaElement(eq(COMPONENT_ID), eq(EdgeLabelEnum.INST_ATTRIBUTES), eq(VertexTypeEnum.INST_ATTRIBUTES),
                eq(attributes), ArgumentMatchers.anyList(), eq(JsonPresentationFields.NAME));

        final StorageOperationStatus result = testInstance.updateComponentInstanceAttributes(component, componentInstanceId, attributes);
        assertNotNull(result);
        assertEquals(StorageOperationStatus.OK, result);
        verify((BaseOperation) testInstance, times(1))
            .updateToscaDataDeepElementsOfToscaElement(eq(COMPONENT_ID), eq(EdgeLabelEnum.INST_ATTRIBUTES), eq(VertexTypeEnum.INST_ATTRIBUTES),
                eq(attributes), ArgumentMatchers.anyList(), eq(JsonPresentationFields.NAME));
    }

    @Test
    void test_updateComponentInstanceAttribute() {
        final NodeTemplateOperation testInstance = Mockito.spy(new NodeTemplateOperation());
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);

        final String componentInstanceId = "requirementOwnerId";

        final ComponentInstanceAttribute instanceAttribute = new ComponentInstanceAttribute();

        doReturn(StorageOperationStatus.OK).when((BaseOperation) testInstance)
            .updateToscaDataDeepElementOfToscaElement(eq(COMPONENT_ID), eq(EdgeLabelEnum.INST_ATTRIBUTES), eq(VertexTypeEnum.INST_ATTRIBUTES),
                eq(instanceAttribute), ArgumentMatchers.anyList(), eq(JsonPresentationFields.NAME));

        final StorageOperationStatus result = testInstance.updateComponentInstanceAttribute(component, componentInstanceId, instanceAttribute);
        assertNotNull(result);
        assertEquals(StorageOperationStatus.OK, result);
        verify((BaseOperation) testInstance, times(1))
            .updateToscaDataDeepElementOfToscaElement(eq(COMPONENT_ID), eq(EdgeLabelEnum.INST_ATTRIBUTES), eq(VertexTypeEnum.INST_ATTRIBUTES),
                eq(instanceAttribute), ArgumentMatchers.anyList(), eq(JsonPresentationFields.NAME));
    }

    @Test
    void test_updateComponentInstanceOutputs() {
        final NodeTemplateOperation testInstance = Mockito.spy(new NodeTemplateOperation());
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);

        final String componentInstanceId = "requirementOwnerId";

        List<ComponentInstanceOutput> componentInstanceOutputList = new ArrayList<>();
        ComponentInstanceOutput instanceOutput = new ComponentInstanceOutput();
        componentInstanceOutputList.add(instanceOutput);

        doReturn(StorageOperationStatus.OK).when((BaseOperation) testInstance)
            .updateToscaDataDeepElementsOfToscaElement(eq(COMPONENT_ID), eq(EdgeLabelEnum.INST_OUTPUTS), eq(VertexTypeEnum.INST_OUTPUTS),
                eq(componentInstanceOutputList), ArgumentMatchers.anyList(), eq(JsonPresentationFields.NAME));

        final StorageOperationStatus result = testInstance.updateComponentInstanceOutputs(component, componentInstanceId, componentInstanceOutputList);
        assertNotNull(result);
        assertEquals(StorageOperationStatus.OK, result);
        verify((BaseOperation) testInstance, times(1))
            .updateToscaDataDeepElementsOfToscaElement(eq(COMPONENT_ID), eq(EdgeLabelEnum.INST_OUTPUTS), eq(VertexTypeEnum.INST_OUTPUTS),
                eq(componentInstanceOutputList), ArgumentMatchers.anyList(), eq(JsonPresentationFields.NAME));
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
