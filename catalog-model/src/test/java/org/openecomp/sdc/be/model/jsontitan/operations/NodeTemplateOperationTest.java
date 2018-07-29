package org.openecomp.sdc.be.model.jsontitan.operations;

import com.google.common.collect.Lists;
import fj.data.Either;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

    private static TitanDao titanDao;
    private static NodeTemplateOperation operation;

    @BeforeClass
    public static void setup() {
        init();
        titanDao = Mockito.mock(TitanDao.class);
        operation = new NodeTemplateOperation();
        operation.setTitanDao(titanDao);
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
        Either<GraphVertex, TitanOperationStatus> vertexRes = Either.left(vertex);
        when(titanDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);

        GraphVertex dataVertex = new GraphVertex();
        dataVertex.setJson(fulfilledCapability);
        Either<GraphVertex, TitanOperationStatus> childVertexRes = Either.left(dataVertex);
        when(titanDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_CAPABILITIES), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
        Either<CapabilityDataDefinition, StorageOperationStatus> result = operation.getFulfilledCapabilityByRelation(COMPONENT_ID, TO_INSTANCE_ID, relation, this::isBelongingCapability);
        assertTrue(result.isLeft());
        assertEquals(result.left().value(), capability);
    }

    @Test
    public void testGetFulfilledRequirementByRelationSuccess(){
        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        Either<GraphVertex, TitanOperationStatus> vertexRes = Either.left(vertex);
        when(titanDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);

        GraphVertex dataVertex = new GraphVertex();
        dataVertex.setJson(fulfilledRequirement);
        Either<GraphVertex, TitanOperationStatus> childVertexRes = Either.left(dataVertex);
        when(titanDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_REQUIREMENTS), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
        Either<RequirementDataDefinition, StorageOperationStatus> result = operation.getFulfilledRequirementByRelation(COMPONENT_ID, FROM_INSTANCE_ID, relation, this::isBelongingRequirement);
        assertTrue(result.isLeft());
        assertEquals(result.left().value(), requirement);
    }

    @Test
    public void testGetFulfilledCapabilityByRelationNotFoundFailure(){
        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        Either<GraphVertex, TitanOperationStatus> vertexRes = Either.left(vertex);
        when(titanDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);

        Either<GraphVertex, TitanOperationStatus> childVertexRes = Either.right(TitanOperationStatus.NOT_FOUND);
        when(titanDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_CAPABILITIES), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
        Either<CapabilityDataDefinition, StorageOperationStatus> result = operation.getFulfilledCapabilityByRelation(COMPONENT_ID, TO_INSTANCE_ID, relation, this::isBelongingCapability);
        assertTrue(result.isRight());
        assertSame(result.right().value(), StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void testGetFulfilledRequirementByRelationNotFoundFailure(){
        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        Either<GraphVertex, TitanOperationStatus> vertexRes = Either.left(vertex);
        when(titanDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);

        Either<GraphVertex, TitanOperationStatus> childVertexRes = Either.right(TitanOperationStatus.NOT_FOUND);
        when(titanDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_REQUIREMENTS), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
        Either<RequirementDataDefinition, StorageOperationStatus> result = operation.getFulfilledRequirementByRelation(COMPONENT_ID, FROM_INSTANCE_ID, relation, this::isBelongingRequirement);
        assertTrue(result.isRight());
        assertSame(result.right().value(), StorageOperationStatus.NOT_FOUND);
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

	private NodeTemplateOperation createTestSubject() {
		return operation;
	}

	
	@Test
	public void testGetDefaultHeatTimeout() throws Exception {
		Integer result;

		// default test
		result = NodeTemplateOperation.getDefaultHeatTimeout();
    }

	

	

	

	

	

	
	@Test
	public void testPrepareInstDeploymentArtifactPerInstance() throws Exception {
		NodeTemplateOperation testSubject;
		Map<String, ArtifactDataDefinition> deploymentArtifacts = null;
		String componentInstanceId = "";
		User user = null;
		String envType = "";
		MapArtifactDataDefinition result;

		// test 1
		testSubject = createTestSubject();
		deploymentArtifacts = null;
		result = testSubject.prepareInstDeploymentArtifactPerInstance(deploymentArtifacts, componentInstanceId, user,
				envType);
		Assert.assertEquals(null, result);
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
	public void testPrepareCalculatedCapabiltyForNodeType() throws Exception {
		NodeTemplateOperation testSubject;
		Map<String, ListCapabilityDataDefinition> capabilities = null;
		ComponentInstanceDataDefinition componentInstance = null;
		MapListCapabilityDataDefinition result;

		// test 1
		testSubject = createTestSubject();
		capabilities = null;
		result = testSubject.prepareCalculatedCapabiltyForNodeType(capabilities, componentInstance);
		Assert.assertEquals(null, result);
	}

	
	@Test
	public void testPrepareCalculatedRequirementForNodeType() throws Exception {
		NodeTemplateOperation testSubject;
		Map<String, ListRequirementDataDefinition> requirements = null;
		ComponentInstanceDataDefinition componentInstance = null;
		MapListRequirementDataDefinition result;

		// test 1
		testSubject = createTestSubject();
		requirements = null;
		result = testSubject.prepareCalculatedRequirementForNodeType(requirements, componentInstance);
		Assert.assertEquals(null, result);
	}

	
	@Test
	public void testAddGroupInstancesToComponentInstance() throws Exception {
		NodeTemplateOperation testSubject;
		Component containerComponent = null;
		ComponentInstanceDataDefinition componentInstance = null;
		List<GroupDefinition> groups = null;
		Map<String, List<ArtifactDefinition>> groupInstancesArtifacts = null;
		StorageOperationStatus result;

		// test 1
		testSubject = createTestSubject();
		groupInstancesArtifacts = null;
		result = testSubject.addGroupInstancesToComponentInstance(containerComponent, componentInstance, groups,
				groupInstancesArtifacts);
		Assert.assertEquals(StorageOperationStatus.OK, result);
	}

	@Test
	public void testGenerateCustomizationUUIDOnInstanceGroup() throws Exception {
		NodeTemplateOperation testSubject;
		String componentId = "";
		String instanceId = "";
		List<String> groupInstances = null;
		StorageOperationStatus result;

		// test 1
		testSubject = createTestSubject();
		groupInstances = null;
		result = testSubject.generateCustomizationUUIDOnInstanceGroup(componentId, instanceId, groupInstances);
		Assert.assertEquals(StorageOperationStatus.OK, result);
	}

}
