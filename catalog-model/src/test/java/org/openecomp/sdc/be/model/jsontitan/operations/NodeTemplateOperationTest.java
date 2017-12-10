package org.openecomp.sdc.be.model.jsontitan.operations;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabiltyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import com.google.common.collect.Lists;

import fj.data.Either;

public class NodeTemplateOperationTest extends ModelTestBase{

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
	
	private static Map<String, MapListCapabiltyDataDefinition> fulfilledCapability;
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
		Either<CapabilityDataDefinition, StorageOperationStatus> result = operation.getFulfilledCapabilityByRelation(COMPONENT_ID, TO_INSTANCE_ID, relation, (rel, cap)->isBelongingCapability(rel, cap));
		assertTrue(result.isLeft());
		assertTrue(result.left().value().equals(capability));
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
		Either<RequirementDataDefinition, StorageOperationStatus> result = operation.getFulfilledRequirementByRelation(COMPONENT_ID, FROM_INSTANCE_ID, relation, (rel, req)->isBelongingRequirement(rel, req));
		assertTrue(result.isLeft());
		assertTrue(result.left().value().equals(requirement));
	}
	
	@Test
	public void testGetFulfilledCapabilityByRelationNotFoundFailure(){
		GraphVertex vertex = Mockito.mock(GraphVertex.class);
		Either<GraphVertex, TitanOperationStatus> vertexRes = Either.left(vertex);
		when(titanDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);
		
		Either<GraphVertex, TitanOperationStatus> childVertexRes = Either.right(TitanOperationStatus.NOT_FOUND);
		when(titanDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_CAPABILITIES), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
		Either<CapabilityDataDefinition, StorageOperationStatus> result = operation.getFulfilledCapabilityByRelation(COMPONENT_ID, TO_INSTANCE_ID, relation, (rel, cap)->isBelongingCapability(rel, cap));
		assertTrue(result.isRight());
		assertTrue(result.right().value() == StorageOperationStatus.NOT_FOUND);
	}
	
	@Test
	public void testGetFulfilledRequirementByRelationNotFoundFailure(){
		GraphVertex vertex = Mockito.mock(GraphVertex.class);
		Either<GraphVertex, TitanOperationStatus> vertexRes = Either.left(vertex);
		when(titanDao.getVertexById(eq(COMPONENT_ID), eq(JsonParseFlagEnum.ParseAll))).thenReturn(vertexRes);
		
		Either<GraphVertex, TitanOperationStatus> childVertexRes = Either.right(TitanOperationStatus.NOT_FOUND);
		when(titanDao.getChildVertex(eq(vertex), eq(EdgeLabelEnum.FULLFILLED_REQUIREMENTS), eq(JsonParseFlagEnum.ParseJson))).thenReturn(childVertexRes);
		Either<RequirementDataDefinition, StorageOperationStatus> result = operation.getFulfilledRequirementByRelation(COMPONENT_ID, FROM_INSTANCE_ID, relation,(rel, req)->isBelongingRequirement(rel, req));
		assertTrue(result.isRight());
		assertTrue(result.right().value() == StorageOperationStatus.NOT_FOUND);
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
		MapListCapabiltyDataDefinition mapListCapabiltyDataDefinition = new MapListCapabiltyDataDefinition();
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
		return 	cap.getName().equals(relationshipInfo.getCapability()) &&
				cap.getUniqueId().equals(relationshipInfo.getCapabilityUid()) &&
				cap.getOwnerId().equals(relationshipInfo.getCapabilityOwnerId());
	}
}
