package org.openecomp.sdc.be.components.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import fj.data.Either;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.property.propertytopolicydeclarators.ComponentInstancePropertyToPolicyDeclarator;
import org.openecomp.sdc.be.components.property.propertytopolicydeclarators.ComponentPropertyToPolicyDeclarator;
import org.openecomp.sdc.be.components.utils.PolicyTypeBuilder;
import org.openecomp.sdc.be.components.utils.ServiceBuilder;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.*;

public class PropertyDeclarationOrchestratorTest {

	@InjectMocks
	PropertyDeclarationOrchestrator testSubject;

	@Mock
	List<PropertyDeclarator> propertyDeceleratorsMock;

	@Mock
	private ComponentInstancePropertyToPolicyDeclarator componentInstancePropertyToPolicyDeclarator;

	@Mock
	private ComponentPropertyToPolicyDeclarator componentPropertyToPolicyDeclarator;

	@Mock
	private ComponentInstanceInputPropertyDeclarator componentInstanceInputPropertyDecelerator;

	@Mock
	private ComponentInstancePropertyDeclarator componentInstancePropertyDecelerator;

	@Mock
	private ComponentPropertyDeclarator servicePropertyDeclarator;

	@Mock
	private PolicyPropertyDeclarator policyPropertyDecelerator;

	private static final String PROP_UID = "propertyUid";
	private static final String SERVICE_UID = "serviceUid";

	private Service service;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);

		service = new ServiceBuilder().setUniqueId(SERVICE_UID).build();
	}

	@Test(expected = IllegalStateException.class)
	public void testDeclarePropertiesToInputs() throws Exception {
		Component component = new Resource();
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Either<List<InputDefinition>, StorageOperationStatus> result;

		// default test
		result = testSubject.declarePropertiesToInputs(component, componentInstInputsMap);
	}

	@Test
	public void testUnDeclarePropertiesAsInputs() throws Exception {
		Component component = new Resource();
		InputDefinition inputToDelete = new InputDefinition();
		StorageOperationStatus result;

		Iterator<PropertyDeclarator> mockIter = Mockito.mock(Iterator.class);
		when(propertyDeceleratorsMock.iterator()).thenReturn(mockIter);
		when(mockIter.hasNext()).thenReturn(false);
		
		// default test
		result = testSubject.unDeclarePropertiesAsInputs(component, inputToDelete);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetPropertyDecelerator() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithInputsMap() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = getPropertiesMapToDeclare();
		componentInstInputsMap.setComponentInstanceInputsMap(componentInstanceInputsMap);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithCIProperties() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		componentInstanceProperties.put("mock", value);
		componentInstInputsMap.setComponentInstanceProperties(componentInstanceProperties);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithCIPolicy() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> policyProperties = getPropertiesMapToDeclare();
		componentInstInputsMap.setPolicyProperties(policyProperties);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}

	@Test
	public void testDeclarePropertiesToPolicies() {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> policyProperties = getPropertiesMapToDeclare();
		componentInstInputsMap.setComponentInstancePropertiesToPolicies(policyProperties);

		PolicyDefinition declaredPolicy = getDeclaredPolicy();

		when(componentInstancePropertyToPolicyDeclarator.declarePropertiesAsPolicies(any(), anyString(), anyList())).thenReturn(
				Either.left(Collections.singletonList(declaredPolicy)));

		Either<List<PolicyDefinition>, StorageOperationStatus> declareEither =
				testSubject.declarePropertiesToPolicies(service, componentInstInputsMap);

		validatePolicyDeclaration(declaredPolicy, declareEither);
	}

	@Test
	public void testUndeclarePropertiesAsPolicies() {
		when(componentInstancePropertyToPolicyDeclarator.unDeclarePropertiesAsPolicies(any(), any(PolicyDefinition.class))).thenReturn(StorageOperationStatus.OK);
		when(componentPropertyToPolicyDeclarator.unDeclarePropertiesAsPolicies(any(), any(PolicyDefinition.class))).thenReturn(StorageOperationStatus.OK);

		StorageOperationStatus undeclareStatus =
				testSubject.unDeclarePropertiesAsPolicies(service, getDeclaredPolicy());

		assertEquals(StorageOperationStatus.OK, undeclareStatus);
	}

	@Test
	public void testDeclareServiceProperties() {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> serviceProperties = getPropertiesMapToDeclare();
		componentInstInputsMap.setServiceProperties(serviceProperties);

		PolicyDefinition declaredPolicy = getDeclaredPolicy();

		when(servicePropertyDeclarator.declarePropertiesAsPolicies(any(), anyString(), anyList())).thenReturn(
				Either.left(Collections.singletonList(declaredPolicy)));

		Either<List<PolicyDefinition>, StorageOperationStatus> declareEither =
				testSubject.declarePropertiesToPolicies(service, componentInstInputsMap);

		validatePolicyDeclaration(declaredPolicy, declareEither);
	}

	private PolicyDefinition getDeclaredPolicy() {
		PolicyDefinition declaredPolicy = new PolicyDefinition();
		declaredPolicy.setUniqueId(PROP_UID);
		return declaredPolicy;
	}

	private Map<String, List<ComponentInstancePropInput>> getPropertiesMapToDeclare() {
		Map<String, List<ComponentInstancePropInput>> policyProperties = new HashMap<>();

		ComponentInstancePropInput propertyToDeclare = new ComponentInstancePropInput();
		propertyToDeclare.setUniqueId(PROP_UID);
		propertyToDeclare.setPropertiesName(PROP_UID);

		policyProperties.put("mock", Collections.singletonList(propertyToDeclare));
		return policyProperties;
	}

	private void validatePolicyDeclaration(PolicyDefinition declaredPolicy,
			Either<List<PolicyDefinition>, StorageOperationStatus> declareEither) {
		assertTrue(declareEither.isLeft());

		List<PolicyDefinition> declaredPolicies = declareEither.left().value();
		assertEquals(1, declaredPolicies.size());
		assertEquals(declaredPolicy.getUniqueId(), declaredPolicies.get(0).getUniqueId());
	}
}
