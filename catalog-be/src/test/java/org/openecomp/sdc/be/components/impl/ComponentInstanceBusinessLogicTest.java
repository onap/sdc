package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.info.CreateAndAssotiateInfo;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ForwardingPathOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ArtifactOperation;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.common.datastructure.Wrapper;

import fj.data.Either;
import javassist.CodeConverter.ArrayAccessReplacementMethodNames;
import mockit.Deencapsulation;

import java.util.*;

/**
 * The test suite designed for test functionality of
 * ComponentInstanceBusinessLogic class
 */
public class ComponentInstanceBusinessLogicTest {

	private final static String USER_ID = "jh0003";
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

	private static ComponentsUtils componentsUtils;
	private static ServletUtils servletUtils;
	private static ResponseFormat responseFormat;
	private static ToscaOperationFacade toscaOperationFacade;
	private static IComponentInstanceOperation componentInstanceOperation;
	private static UserBusinessLogic userAdmin;

	private static ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
	private static ForwardingPathOperation forwardingPathOperation;
	private static User user;
	private static UserValidations userValidations;
	private static Component service;
	private static Component resource;
	private static ComponentInstance toInstance;
	private static ComponentInstance fromInstance;
	private static CapabilityDataDefinition capability;
	private static RequirementDataDefinition requirement;
	private static RequirementCapabilityRelDef relation;
	private static BaseBusinessLogic baseBusinessLogic;
	private static ArtifactsBusinessLogic artifactsBusinessLogic;
	private static ToscaDataDefinition toscaDataDefinition;

	@Before
	public void init() {
		createMocks();
		setMocks();
		stubMethods();
		createComponents();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetRelationByIdSuccess() {
		getServiceRelationByIdSuccess(service);
		getServiceRelationByIdSuccess(resource);
	}

	@Test
	public void testGetRelationByIdUserValidationFailure() {
		getServiceRelationByIdUserValidationFailure(service);
		getServiceRelationByIdUserValidationFailure(resource);
	}

	@Test
	public void testGetRelationByIdComponentNotFoundFailure() {
		getRelationByIdComponentNotFoundFailure(service);
		getRelationByIdComponentNotFoundFailure(resource);
	}

	
	@Test
	public void testForwardingPathOnVersionChange() {
		getforwardingPathOnVersionChange();
	}

	private void getforwardingPathOnVersionChange() {
		String containerComponentParam = "services";
		String containerComponentID = "121-cont";
		String componentInstanceID = "121-cont-1-comp";
		Service component = new Service();
		Map<String, ForwardingPathDataDefinition> forwardingPaths = generateForwardingPath(componentInstanceID);

		// Add existing componentInstance to component
		List<ComponentInstance> componentInstanceList = new ArrayList<>();
		ComponentInstance oldComponentInstance = new ComponentInstance();
		oldComponentInstance.setName("OLD_COMP_INSTANCE");
		oldComponentInstance.setUniqueId(componentInstanceID);
		oldComponentInstance.setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_UID, "1-comp");
		componentInstanceList.add(oldComponentInstance);
		component.setComponentInstances(componentInstanceList);
		component.setForwardingPaths(forwardingPaths);

		List<ComponentInstance> componentInstanceListNew = new ArrayList<>();
		ComponentInstance newComponentInstance = new ComponentInstance();
		String new_Comp_UID = "2-comp";
		newComponentInstance.setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_UID, new_Comp_UID);
		newComponentInstance.setUniqueId(new_Comp_UID);
		componentInstanceListNew.add(newComponentInstance);
		Component component2 = new Service();
		component2.setComponentInstances(componentInstanceListNew);

		// Mock for getting component
		when(toscaOperationFacade.getToscaElement(eq(containerComponentID), any(ComponentParametersView.class)))
				.thenReturn(Either.left(component));
		when(toscaOperationFacade.validateComponentExists(any(String.class))).thenReturn(Either.left(Boolean.TRUE));
		// Mock for getting component for componentInstance
		when(toscaOperationFacade.getToscaFullElement(eq("1-comp"))).thenReturn(Either.left(component));
		when(toscaOperationFacade.getToscaFullElement(eq(new_Comp_UID))).thenReturn(Either.left(component2));

		Either<Set<String>, ResponseFormat> resultOp = componentInstanceBusinessLogic.forwardingPathOnVersionChange(
				containerComponentParam, containerComponentID, componentInstanceID, newComponentInstance);
		Assert.assertEquals(1, resultOp.left().value().size());
		Assert.assertEquals("FP-ID-1", resultOp.left().value().iterator().next());

	}

	@Test
	public void testDeleteForwardingPathsWhenComponentinstanceDeleted(){

		ComponentTypeEnum containerComponentType = ComponentTypeEnum.findByParamName("services");
		String containerComponentID = "Service-comp";
		String componentInstanceID = "NodeA1";
		Service component = new Service();
		component.setComponentInstances(Arrays.asList(createComponentIstance("NodeA2"),createComponentIstance("NodeB2"),
				createComponentIstance(componentInstanceID)));

		component.addForwardingPath(createPath("path1", componentInstanceID, "NodeB1",  "1"));
		component.addForwardingPath(createPath("Path2", "NodeA2","NodeB2", "2"));
		when(toscaOperationFacade.getToscaElement(eq(containerComponentID),any(ComponentParametersView.class))).thenReturn(Either.left(component));
		when(toscaOperationFacade.getToscaElement(eq(containerComponentID))).thenReturn(Either.left(component));
		when(forwardingPathOperation.deleteForwardingPath(any(Service.class), anySet())).thenReturn(Either.left(new HashSet<>()));
		final ComponentInstance ci = new ComponentInstance();
		ci.setName(componentInstanceID);
		Either<ComponentInstance, ResponseFormat> responseFormatEither = componentInstanceBusinessLogic.deleteForwardingPathsRelatedTobeDeletedComponentInstance(
				containerComponentID, containerComponentType, Either.left(ci));
		Assert.assertTrue(responseFormatEither.isLeft());

	}

	private ComponentInstance createComponentIstance(String path1) {
		ComponentInstance componentInstance = new ComponentInstance();
		componentInstance.setName(path1);
		return componentInstance;
	}

	@Test
	public void testDeleteForwardingPathsWhenErrorInComponentinstanceDelete() {

		ComponentTypeEnum containerComponentType = ComponentTypeEnum.findByParamName("services");
		String containerComponentID = "Service-comp";
		String componentInstanceID = "NodeA1";
		Service component = new Service();

		component.addForwardingPath(createPath("path1", "NodeA1", "NodeB1", "1"));
		component.addForwardingPath(createPath("Path2", "NodeA2", "NodeB2", "2"));
		when(toscaOperationFacade.getToscaElement(eq(containerComponentID), any(ComponentParametersView.class)))
				.thenReturn(Either.left(component));
		when(toscaOperationFacade.getToscaElement(eq(containerComponentID))).thenReturn(Either.left(component));
		when(forwardingPathOperation.deleteForwardingPath(any(Service.class), anySet()))
				.thenReturn(Either.left(new HashSet<>()));
		Either<ComponentInstance, ResponseFormat> responseFormatEither = componentInstanceBusinessLogic
				.deleteForwardingPathsRelatedTobeDeletedComponentInstance(containerComponentID,
						containerComponentType, Either.right(new ResponseFormat()));
		Assert.assertTrue(responseFormatEither.isRight());

	}

	private ForwardingPathDataDefinition createPath(String pathName, String fromNode, String toNode, String uniqueId) {
		ForwardingPathDataDefinition forwardingPath = new ForwardingPathDataDefinition(pathName);
		forwardingPath.setProtocol("protocol");
		forwardingPath.setDestinationPortNumber("port");
		forwardingPath.setUniqueId(uniqueId);
		ListDataDefinition<ForwardingPathElementDataDefinition> forwardingPathElementListDataDefinition = new ListDataDefinition<>();
		forwardingPathElementListDataDefinition.add(new ForwardingPathElementDataDefinition(fromNode, toNode,
				"nodeAcpType", "nodeBcpType", "nodeDcpName", "nodeBcpName"));
		forwardingPath.setPathElements(forwardingPathElementListDataDefinition);

		return forwardingPath;
	}

	private Map<String, ForwardingPathDataDefinition> generateForwardingPath(String componentInstanceID) {
		ForwardingPathDataDefinition forwardingPath = new ForwardingPathDataDefinition("fpName");
		String protocol = "protocol";
		forwardingPath.setProtocol(protocol);
		forwardingPath.setDestinationPortNumber("DestinationPortNumber");
		forwardingPath.setUniqueId("FP-ID-1");
		ListDataDefinition<ForwardingPathElementDataDefinition> forwardingPathElementListDataDefinition = new ListDataDefinition<>();
		forwardingPathElementListDataDefinition.add(new ForwardingPathElementDataDefinition(componentInstanceID,
				"nodeB", "nodeA_FORWARDER_CAPABILITY", "nodeBcpType", "nodeDcpName", "nodeBcpName"));
		forwardingPath.setPathElements(forwardingPathElementListDataDefinition);
		Map<String, ForwardingPathDataDefinition> forwardingPaths = new HashMap<>();
		forwardingPaths.put("1122", forwardingPath);
		return forwardingPaths;
	}

	@SuppressWarnings("unchecked")
	private void getServiceRelationByIdSuccess(Component component) {
		Either<User, ActionStatus> eitherCreator = Either.left(user);
		when(userAdmin.getUser(eq(USER_ID), eq(false))).thenReturn(eitherCreator);
		Either<Component, StorageOperationStatus> getComponentRes = Either.left(component);
		when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
				.thenReturn(getComponentRes);

		Either<RequirementDataDefinition, StorageOperationStatus> getfulfilledRequirementRes = Either.left(requirement);
		when(toscaOperationFacade.getFulfilledRequirementByRelation(eq(COMPONENT_ID), eq(FROM_INSTANCE_ID),
				eq(relation), any(BiPredicate.class))).thenReturn(getfulfilledRequirementRes);

		Either<CapabilityDataDefinition, StorageOperationStatus> getfulfilledCapabilityRes = Either.left(capability);
		when(toscaOperationFacade.getFulfilledCapabilityByRelation(eq(COMPONENT_ID), eq(FROM_INSTANCE_ID), eq(relation),
				any(BiPredicate.class))).thenReturn(getfulfilledCapabilityRes);

		Either<RequirementCapabilityRelDef, ResponseFormat> response = componentInstanceBusinessLogic
				.getRelationById(COMPONENT_ID, RELATION_ID, USER_ID, component.getComponentType());
		assertTrue(response.isLeft());
	}

	private void getServiceRelationByIdUserValidationFailure(Component component) {
		// Either<User, ActionStatus> eitherCreator =
		// Either.right(ActionStatus.USER_NOT_FOUND);
		// when(userAdmin.getUser(eq(USER_ID),
		// eq(false))).thenReturn(eitherCreator);
		when(userValidations.validateUserExists(eq(USER_ID), eq("get relation by Id"), eq(false)))
				.thenReturn(Either.right(new ResponseFormat(404)));
		Either<RequirementCapabilityRelDef, ResponseFormat> response = componentInstanceBusinessLogic
				.getRelationById(COMPONENT_ID, RELATION_ID, USER_ID, component.getComponentType());
		assertTrue(response.isRight());
	}

	private void getRelationByIdComponentNotFoundFailure(Component component) {
		Either<User, ActionStatus> eitherCreator = Either.left(user);
		when(userAdmin.getUser(eq(USER_ID), eq(false))).thenReturn(eitherCreator);
		Either<Component, StorageOperationStatus> getComponentRes = Either.right(StorageOperationStatus.NOT_FOUND);
		when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
				.thenReturn(getComponentRes);

		Either<RequirementCapabilityRelDef, ResponseFormat> response = componentInstanceBusinessLogic
				.getRelationById(COMPONENT_ID, RELATION_ID, USER_ID, component.getComponentType());
		assertTrue(response.isRight());
	}

	private static void createMocks() {
		componentsUtils = Mockito.mock(ComponentsUtils.class);
		servletUtils = Mockito.mock(ServletUtils.class);
		responseFormat = Mockito.mock(ResponseFormat.class);
		toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
		userAdmin = Mockito.mock(UserBusinessLogic.class);
		user = Mockito.mock(User.class);
		baseBusinessLogic = Mockito.mock(BaseBusinessLogic.class);
		userValidations = Mockito.mock(UserValidations.class);
		forwardingPathOperation = Mockito.mock(ForwardingPathOperation.class);
		componentInstanceOperation = Mockito.mock(IComponentInstanceOperation.class);
		artifactsBusinessLogic = Mockito.mock(ArtifactsBusinessLogic.class);
		toscaDataDefinition = Mockito.mock(ToscaDataDefinition.class);
	}

	private static void setMocks() {
		componentInstanceBusinessLogic = new ComponentInstanceBusinessLogic();
		componentInstanceBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
		componentInstanceBusinessLogic.setUserAdmin(userAdmin);
		componentInstanceBusinessLogic.setComponentsUtils(componentsUtils);
		componentInstanceBusinessLogic.setUserValidations(userValidations);
		componentInstanceBusinessLogic.setForwardingPathOperation(forwardingPathOperation);
	}

	private static void stubMethods() {
		when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
		when(userValidations.validateUserExists(eq(USER_ID), eq("get relation by Id"), eq(false)))
				.thenReturn(Either.left(user));
		when(componentsUtils.getResponseFormat(eq(ActionStatus.RELATION_NOT_FOUND), eq(RELATION_ID), eq(COMPONENT_ID)))
				.thenReturn(responseFormat);
		Either<User, ActionStatus> eitherGetUser = Either.left(user);
		when(userAdmin.getUser("jh0003", false)).thenReturn(eitherGetUser);
		when(userValidations.validateUserExists(eq(user.getUserId()), anyString(), eq(false)))
				.thenReturn(Either.left(user));
	}

	private static void createComponents() {
		createRelation();
		createInstances();
		createService();
		createResource();
	}

	private static Component createResource() {
		resource = new Resource();
		resource.setUniqueId(COMPONENT_ID);
		resource.setComponentInstancesRelations(Lists.newArrayList(relation));
		resource.setComponentInstances(Lists.newArrayList(toInstance, fromInstance));
		resource.setCapabilities(toInstance.getCapabilities());
		resource.setRequirements(fromInstance.getRequirements());
		resource.setComponentType(ComponentTypeEnum.RESOURCE);
		return resource;
	}

	private static Component createService() {
		service = new Service();
		service.setUniqueId(COMPONENT_ID);
		service.setComponentInstancesRelations(Lists.newArrayList(relation));
		service.setComponentInstances(Lists.newArrayList(toInstance, fromInstance));
		service.setCapabilities(toInstance.getCapabilities());
		service.setRequirements(fromInstance.getRequirements());
		service.setComponentType(ComponentTypeEnum.SERVICE);
		return service;
	}

	private static ComponentInstance createInstances() {
		toInstance = new ComponentInstance();
		toInstance.setUniqueId(TO_INSTANCE_ID);
		toInstance.setComponentUid("uuuiiid");
		toInstance.setName("tests");

		fromInstance = new ComponentInstance();
		fromInstance.setUniqueId(FROM_INSTANCE_ID);

		capability = new CapabilityDataDefinition();
		capability.setOwnerId(CAPABILITY_OWNER_ID);
		capability.setUniqueId(CAPABILITY_UID);
		capability.setName(CAPABILITY_NAME);

		Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
		capabilities.put(capability.getName(), Lists.newArrayList(new CapabilityDefinition(capability)));

		requirement = new RequirementDataDefinition();
		requirement.setOwnerId(REQUIREMENT_OWNER_ID);
		requirement.setUniqueId(REQUIREMENT_UID);
		requirement.setName(REQUIREMENT_NAME);
		requirement.setRelationship(RELATIONSHIP_TYPE);

		Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
		requirements.put(requirement.getCapability(), Lists.newArrayList(new RequirementDefinition(requirement)));

		toInstance.setCapabilities(capabilities);
		fromInstance.setRequirements(requirements);
		return toInstance;
	}

	private static void createRelation() {

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
		RelationshipImpl relationshipImpl = new RelationshipImpl();
		relationshipImpl.setType(RELATIONSHIP_TYPE);
		relationInfo.setRelationships(relationshipImpl);
	}

	///////////////////////////////////////////////////////////////////////////////
	/////////////////////////////new test//////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	
	
	private ComponentInstanceBusinessLogic createTestSubject() {
			return componentInstanceBusinessLogic;
	}

	


	
	@Test
	public void testChangeServiceProxyVersion() throws Exception {
		ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
		String containerComponentType = "";
		String containerComponentId = "";
		String serviceProxyId = "";
		String userId = user.getUserId();
		Either<ComponentInstance, ResponseFormat> result;

		// default test
		componentInstanceBusinessLogic = createTestSubject();
		result = componentInstanceBusinessLogic.changeServiceProxyVersion(containerComponentType, containerComponentId, serviceProxyId,
				userId);
	}




	

	
	@Test
	public void testCreateServiceProxy() throws Exception {
		ComponentInstanceBusinessLogic testSubject;
		String containerComponentType = "";
		String containerComponentId = "";
		String userId = user.getUserId();
		ComponentInstance componentInstance = createInstances();
		Either<ComponentInstance, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createServiceProxy(containerComponentType, containerComponentId, userId,
				componentInstance);
	}



	
	
	@Test
	public void testDeleteForwardingPathsRelatedTobeDeletedComponentInstance() throws Exception {
		ComponentInstanceBusinessLogic testSubject;
		String containerComponentId = "";
		String componentInstanceId = "";
		ComponentTypeEnum containerComponentType = ComponentTypeEnum.RESOURCE;
		Either<ComponentInstance, ResponseFormat> resultOp = null;
		Either<ComponentInstance, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteForwardingPathsRelatedTobeDeletedComponentInstance(containerComponentId,
				 containerComponentType, resultOp);
	}

	
	@Test
	public void testDeleteServiceProxy() throws Exception {
		ComponentInstanceBusinessLogic testSubject;
		String containerComponentType = "";
		String containerComponentId = "";
		String serviceProxyId = "";
		String userId = user.getUserId();
		Either<ComponentInstance, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteServiceProxy(containerComponentType, containerComponentId, serviceProxyId, userId);
	}


	


	
	@Test
	public void testGetComponentInstanceInputsByInputId() throws Exception {
		ComponentInstanceBusinessLogic testSubject;
		Component component = new Service();
		String inputId = "";
		List<ComponentInstanceInput> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstanceInputsByInputId(component, inputId);
	}


	
	@Test
	public void testGetComponentInstancePropertiesByInputId() throws Exception {
		ComponentInstanceBusinessLogic testSubject;
		Component component = new Service();
		String inputId = "";
		List<ComponentInstanceProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstancePropertiesByInputId(component, inputId);
	}

	
	@Test
	public void testGetRelationById() throws Exception {
		ComponentInstanceBusinessLogic testSubject;
		String componentId = "";
		String relationId = "";
		String userId = user.getUserId();
		ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE_INSTANCE;
		Either<RequirementCapabilityRelDef, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationById(componentId, relationId, userId, componentTypeEnum);
	}

	


	
	@Test
	public void testCreateComponentInstance_1() throws Exception {
	ComponentInstanceBusinessLogic testSubject;String containerComponentParam = "";
	String containerComponentId = "";
	String userId = user.getUserId();
	ComponentInstance resourceInstance = null;
	boolean inTransaction = false;
	boolean needLock = false;
	Either<ComponentInstance,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.createComponentInstance(containerComponentParam, containerComponentId, userId, resourceInstance, inTransaction, needLock);
	}

	


	
	@Test
	public void testCreateAndAssociateRIToRI() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	
	String containerComponentParam = "";
	String containerComponentId = "";
	String userId = user.getUserId();
	CreateAndAssotiateInfo createAndAssotiateInfo = new CreateAndAssotiateInfo(null, null);
	Either<CreateAndAssotiateInfo,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.createAndAssociateRIToRI(containerComponentParam, containerComponentId, userId, createAndAssotiateInfo);
	}
	
	@Test
	public void testGetOriginComponentFromComponentInstance_1() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	Component compoent = createResource();
	String componentInstanceName = "";
	String origComponetId = compoent.getUniqueId();
	Either<Component, StorageOperationStatus> oldResourceRes = Either.left(compoent);
	when(toscaOperationFacade.getToscaFullElement(compoent.getUniqueId())).thenReturn(oldResourceRes);
	Either<Component,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getOriginComponentFromComponentInstance", new Object[]{componentInstanceName, origComponetId});
	}

	
	@Test
	public void testCreateComponentInstanceOnGraph() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	Component containerComponent = createResource();
	Component originComponent = null;
	ComponentInstance componentInstance = createInstances();
	Either<ComponentInstance,ResponseFormat> result;
	
	Either<ImmutablePair<Component, String>, StorageOperationStatus> result2 = Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND);
	when(toscaOperationFacade.addComponentInstanceToTopologyTemplate(containerComponent, containerComponent,componentInstance, false, user)).thenReturn(result2);
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "createComponentInstanceOnGraph", new Object[]{containerComponent, containerComponent, componentInstance, user});
	}
	
	/*@Test
	public void testCreateComponentInstanceOnGraph2() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	Component containerComponent = createResource();
	containerComponent.setName("name");
	ComponentInstance componentInstance = createInstances();
	Either<ComponentInstance,ResponseFormat> result;
	ImmutablePair<Component, String> pair =  new ImmutablePair<>(containerComponent,"");
	
	
	
	
	Either<ImmutablePair<Component, String>, StorageOperationStatus> result2 = Either.left(pair);
	when(toscaOperationFacade.addComponentInstanceToTopologyTemplate(containerComponent, containerComponent,componentInstance, false, user)).thenReturn(result2);
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "createComponentInstanceOnGraph", new Object[]{containerComponent, containerComponent, componentInstance, user});
	}*/
	
	@Test
	public void testUpdateComponentInstanceMetadata() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	String containerComponentParam = "";
	String containerComponentId = "";
	String componentInstanceId = "";
	String userId = user.getUserId();
	ComponentInstance componentInstance = createInstances();
	Either<ComponentInstance,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.updateComponentInstanceMetadata(containerComponentParam, containerComponentId, componentInstanceId, userId, componentInstance);
	}

	
	@Test
	public void testUpdateComponentInstanceMetadata_1() throws Exception {
	ComponentInstanceBusinessLogic testSubject;String containerComponentParam = "";
	String containerComponentId = "";
	String componentInstanceId = "";
	String userId = user.getUserId();
	ComponentInstance componentInstance = createInstances();
	boolean inTransaction = false;
	boolean needLock = false;
	boolean createNewTransaction = false;
	Either<ComponentInstance,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.updateComponentInstanceMetadata(containerComponentParam, containerComponentId, componentInstanceId, userId, componentInstance, inTransaction, needLock, createNewTransaction);
	}

	


	
	@Test
	public void testValidateParent() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	Component containerComponent = createResource();
	String nodeTemplateId = "";
	boolean result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateParent", new Object[]{containerComponent, nodeTemplateId});
	}

	
	@Test
	public void testGetComponentType() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentTypeEnum containerComponentType = ComponentTypeEnum.RESOURCE;
	ComponentTypeEnum result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getComponentType", new Object[]{ComponentTypeEnum.class});
	}

	
	
	@Test
	public void testGetNewGroupName() throws Exception {
	ComponentInstanceBusinessLogic testSubject;String oldPrefix = "";
	String newNormailzedPrefix = "";
	String qualifiedGroupInstanceName = "";
	String result;
	
	// test 1
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "getNewGroupName", new Object[]{oldPrefix, newNormailzedPrefix, qualifiedGroupInstanceName});
	}

	
	@Test
	public void testUpdateComponentInstanceMetadata_3() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentInstance oldComponentInstance = createInstances();
	ComponentInstance newComponentInstance = null;
	ComponentInstance result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "updateComponentInstanceMetadata", new Object[]{oldComponentInstance, oldComponentInstance});
	}

	
	@Test
	public void testDeleteComponentInstance() throws Exception {
	ComponentInstanceBusinessLogic testSubject;String containerComponentParam = "";
	String containerComponentId = "";
	String componentInstanceId = "";
	String userId = user.getUserId();
	Either<ComponentInstance,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.deleteComponentInstance(containerComponentParam, containerComponentId, componentInstanceId, userId);
	}

	
	@Test
	public void testDeleteForwardingPaths() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	Component service = createService();
	String serviceId = service.getUniqueId();
	List<String> pathIdsToDelete = new ArrayList<>();
	Either<Set<String>,ResponseFormat> result;
	
//	Either<Service, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(serviceId);
	when(toscaOperationFacade.getToscaElement(serviceId)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "deleteForwardingPaths", new Object[]{serviceId, pathIdsToDelete});
	}

	
	@Test
	public void testAssociateRIToRIOnGraph() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	Component containerComponent = createResource();
	RequirementCapabilityRelDef requirementDef = new RequirementCapabilityRelDef();
	ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
	boolean inTransaction = false;
	Either<RequirementCapabilityRelDef,ResponseFormat> result;
	
	

	Either<RequirementCapabilityRelDef, StorageOperationStatus> getResourceResult = Either.left(requirementDef);
	when(toscaOperationFacade.associateResourceInstances(containerComponent.getUniqueId(), requirementDef)).thenReturn(getResourceResult);
	
	// default test
	testSubject=createTestSubject();result=testSubject.associateRIToRIOnGraph(containerComponent, requirementDef, componentTypeEnum, inTransaction);
	}


	
	@Test
	public void testFindRelation() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	String relationId = "";
	List<RequirementCapabilityRelDef> requirementCapabilityRelations = new ArrayList<>();
	RequirementCapabilityRelDef result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "findRelation", new Object[]{relationId, requirementCapabilityRelations});
	}

		
	@Test
	public void testIsNetworkRoleServiceProperty() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentInstanceProperty property = new ComponentInstanceProperty();
	ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
	boolean result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "isNetworkRoleServiceProperty", new Object[]{property, componentTypeEnum});
	}

	
	@Test
	public void testConcatServiceNameToVLINetworkRolePropertiesValues() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ToscaOperationFacade toscaOperationFacade = new ToscaOperationFacade();
	ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
	String componentId = "";
	String resourceInstanceId = "";
	List<ComponentInstanceProperty> properties = new ArrayList<>();
	StorageOperationStatus result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "concatServiceNameToVLINetworkRolePropertiesValues", new Object[]{toscaOperationFacade, componentTypeEnum, componentId, resourceInstanceId, properties});
	}

	
	@Test
	public void testCreateOrUpdatePropertiesValues() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
	Component component = createResource();
	String componentId = component.getUniqueId();
	String resourceInstanceId = "";
	List<ComponentInstanceProperty> properties = new ArrayList<>();
	String userId = user.getUserId();
	Either<List<ComponentInstanceProperty>,ResponseFormat> result;
	
//	Either<Component, StorageOperationStatus> getResourceResult = toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll);
	when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(component));
	
	// test 1
	testSubject=createTestSubject();
	result=testSubject.createOrUpdatePropertiesValues(componentTypeEnum, componentId, resourceInstanceId, properties, userId);
	
	componentTypeEnum =null;
	result=testSubject.createOrUpdatePropertiesValues(componentTypeEnum, componentId, resourceInstanceId, properties, userId);
	
	when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
	result=testSubject.createOrUpdatePropertiesValues(componentTypeEnum, componentId, resourceInstanceId, properties, userId);
	
	}

	
	@Test
	public void testUpdateCapabilityPropertyOnContainerComponent() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentInstanceProperty property = new ComponentInstanceProperty();
	String newValue = "";
	Component containerComponent = createResource();
	ComponentInstance foundResourceInstance = createInstances();
	String capabilityType = "";
	String capabilityName = "";
	ResponseFormat result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "updateCapabilityPropertyOnContainerComponent", new Object[]{property, newValue, containerComponent, foundResourceInstance, capabilityType, capabilityName});
	}

	
	
	@Test
	public void testCreateOrUpdateInstanceInputValues() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
	Component resource = createResource();
	String componentId = resource.getUniqueId();
	String resourceInstanceId = "";
	List<ComponentInstanceInput> inputs = new ArrayList<>();
	String userId = user.getUserId();
	Either<List<ComponentInstanceInput>,ResponseFormat> result;
	
	 when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(resource));
	
	// test 1
	testSubject=createTestSubject();
	result=testSubject.createOrUpdateInstanceInputValues(componentTypeEnum, componentId, resourceInstanceId, inputs, userId);
	componentTypeEnum =null;
	result=testSubject.createOrUpdateInstanceInputValues(componentTypeEnum, componentId, resourceInstanceId, inputs, userId);
	
	
	 when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
	 result=testSubject.createOrUpdateInstanceInputValues(componentTypeEnum, componentId, resourceInstanceId, inputs, userId);
	
	}

	
	@Test
	public void testCreateOrUpdateGroupInstancePropertyValue() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
	Component resource = createResource();
	String componentId = resource.getUniqueId();
	String resourceInstanceId = "";
	String groupInstanceId = "";
	ComponentInstanceProperty property = new ComponentInstanceProperty();
	String userId = user.getUserId();
	Either<ComponentInstanceProperty,ResponseFormat> result;
	
	
	 when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(resource));
	
	// test 1
	testSubject=createTestSubject();
	result=testSubject.createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, resourceInstanceId, groupInstanceId, property, userId);
	componentTypeEnum = null;
	result=testSubject.createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, resourceInstanceId, groupInstanceId, property, userId);
	
	 when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
	 result=testSubject.createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, resourceInstanceId, groupInstanceId, property, userId);
	}

	
	@Test
	public void testCreateOrUpdateInputValue() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	Component component = createResource();
	ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
	String componentId = component.getUniqueId();
	String resourceInstanceId = component.getUniqueId();
	ComponentInstanceInput inputProperty = new ComponentInstanceInput();
	String userId = user.getUserId();
	Either<ComponentInstanceInput,ResponseFormat> result;
	

	Either<Component, StorageOperationStatus> getResourceResult = Either.left(component);
	when(toscaOperationFacade.getToscaElement(component.getUniqueId(), JsonParseFlagEnum.ParseMetadata)).thenReturn(getResourceResult);
	
	// test 1
	testSubject=createTestSubject();
	result=testSubject.createOrUpdateInputValue(componentTypeEnum, componentId, resourceInstanceId, inputProperty, userId);
	
	componentTypeEnum = null;
	result=testSubject.createOrUpdateInputValue(componentTypeEnum, componentId, resourceInstanceId, inputProperty, userId);
	
	when(toscaOperationFacade.getToscaElement(component.getUniqueId(), JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
	result=testSubject.createOrUpdateInputValue(componentTypeEnum, componentId, resourceInstanceId, inputProperty, userId);			
	}

	
	@Test
	public void testDeletePropertyValue() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
	Component service = createService();
	String serviceId = service.getUniqueId();
	String resourceInstanceId = "";
	String propertyValueId = "";
	String userId = user.getUserId();
	Either<ComponentInstanceProperty,ResponseFormat> result;
	
	 when(toscaOperationFacade.getToscaElement(serviceId, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(service));
	
	// test 1
	testSubject=createTestSubject();
	result=testSubject.deletePropertyValue(componentTypeEnum, serviceId, resourceInstanceId, propertyValueId, userId);
	componentTypeEnum= null;
	result=testSubject.deletePropertyValue(componentTypeEnum, serviceId, resourceInstanceId, propertyValueId, userId);
	
	 when(toscaOperationFacade.getToscaElement(serviceId, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
	result=testSubject.deletePropertyValue(componentTypeEnum, serviceId, resourceInstanceId, propertyValueId, userId);
	}

	
	@Test
	public void testGetAndValidateOriginComponentOfComponentInstance() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentTypeEnum containerComponentType = ComponentTypeEnum.RESOURCE;
	Component resource = createResource();
	ComponentInstance componentInstance = createInstances();
	Either<Component,ResponseFormat> result;
	
	 when(toscaOperationFacade.getToscaFullElement(componentInstance.getComponentUid())).thenReturn(Either.left(resource));
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getAndValidateOriginComponentOfComponentInstance", new Object[]{containerComponentType, componentInstance});
	}

	


	
	@Test
	public void testGetComponentParametersViewForForwardingPath() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentParametersView result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getComponentParametersViewForForwardingPath");
	}

	
	@Test
	public void testChangeComponentInstanceVersion() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	String containerComponentParam = "";
	String containerComponentId = "";
	String componentInstanceId = "";
	String userId = user.getUserId();
	ComponentInstance newComponentInstance = createInstances();
	Either<ComponentInstance,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.changeComponentInstanceVersion(containerComponentParam, containerComponentId, componentInstanceId, userId, newComponentInstance);
	newComponentInstance = null;
	testSubject=createTestSubject();result=testSubject.changeComponentInstanceVersion(containerComponentParam, containerComponentId, componentInstanceId, userId, newComponentInstance);
	
	}
	
	@Test
	public void testValidateInstanceNameUniquenessUponUpdate() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	Component containerComponent = createResource();
	ComponentInstance oldComponentInstance = createInstances();
	String newInstanceName = oldComponentInstance.getName();
	Boolean result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateInstanceNameUniquenessUponUpdate", new Object[]{containerComponent, oldComponentInstance, newInstanceName});
	}

	
	@Test
	public void testGetResourceInstanceById() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	Component containerComponent = createResource();
	String instanceId = "";
	Either<ComponentInstance,StorageOperationStatus> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getResourceInstanceById", new Object[]{containerComponent, instanceId});
	}

	
	@Test
	public void testBuildComponentInstance() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentInstance resourceInstanceForUpdate = createInstances();
	ComponentInstance origInstanceForUpdate = null;
	ComponentInstance result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "buildComponentInstance", new Object[]{resourceInstanceForUpdate, resourceInstanceForUpdate});
	}

	


	
	@Test
	public void testFindCapabilityOfInstance() throws Exception {
	ComponentInstanceBusinessLogic testSubject;String componentId = "";
	String instanceId = "";
	String capabilityType = "";
	String capabilityName = "";
	String ownerId = "";
	Map<String,List<CapabilityDefinition>> instanceCapabilities = new HashMap<>();
	Either<List<ComponentInstanceProperty>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "findCapabilityOfInstance", new Object[]{componentId, instanceId, capabilityType, capabilityName, ownerId, instanceCapabilities});
	}

	
	@Test
	public void testFetchComponentInstanceCapabilityProperties() throws Exception {
	ComponentInstanceBusinessLogic testSubject;String componentId = "";
	String instanceId = "";
	String capabilityType = "";
	String capabilityName = "";
	String ownerId = "";
	Either<List<ComponentInstanceProperty>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "fetchComponentInstanceCapabilityProperties", new Object[]{componentId, instanceId, capabilityType, capabilityName, ownerId});
	}

	
	@Test
	public void testUpdateCapabilityPropertyOnContainerComponent_1() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentInstanceProperty property = new ComponentInstanceProperty();
	String newValue = "";
	Component containerComponent = createResource();
	ComponentInstance foundResourceInstance = createInstances();
	String capabilityType = "";
	String capabilityName = "";
	String ownerId = "";
	ResponseFormat result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "updateCapabilityPropertyOnContainerComponent", new Object[]{property, newValue, containerComponent, foundResourceInstance, capabilityType, capabilityName, ownerId});
	}

	
	@Test
	public void testUpdateInstanceCapabilityProperties() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
	Component resource = createResource();
	String containerComponentId = resource.getUniqueId();
	String componentInstanceUniqueId = "";
	String capabilityType = "";
	String capabilityName = "";
	String ownerId = "";
	List<ComponentInstanceProperty> properties = new ArrayList<>();
	String userId = user.getUserId();
	Either<List<ComponentInstanceProperty>,ResponseFormat> result;
	
	
	 when(toscaOperationFacade.getToscaFullElement(containerComponentId)).thenReturn(Either.left(resource));
	
	
	
	// test 1
	testSubject=createTestSubject();
	result=testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId, componentInstanceUniqueId, capabilityType, capabilityName, ownerId, properties, userId);
	when(toscaOperationFacade.getToscaFullElement(containerComponentId)).thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
	result=testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId, componentInstanceUniqueId, capabilityType, capabilityName, ownerId, properties, userId);
	componentTypeEnum = null;
	result=testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId, componentInstanceUniqueId, capabilityType, capabilityName, ownerId, properties, userId);
	
	
	}

	
	@Test
	public void testUpdateInstanceCapabilityProperties_1() throws Exception {
	ComponentInstanceBusinessLogic testSubject;
	ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
	Component component = createResource();
	String containerComponentId = component.getUniqueId();
	String componentInstanceUniqueId = "";
	String capabilityType = "";
	String capabilityName = "";
	List<ComponentInstanceProperty> properties = new ArrayList<>();
	String userId = user.getUserId();
	Either<List<ComponentInstanceProperty>,ResponseFormat> result;
	
	 
	 when(toscaOperationFacade.getToscaFullElement(containerComponentId)).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
	// test 1
	testSubject=createTestSubject();
	result=testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId, componentInstanceUniqueId, capabilityType, capabilityName, properties, userId);
	 when(toscaOperationFacade.getToscaFullElement(containerComponentId)).thenReturn(Either.left(component));
	 result=testSubject.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId, componentInstanceUniqueId, capabilityType, capabilityName, properties, userId);
	}


}

	

