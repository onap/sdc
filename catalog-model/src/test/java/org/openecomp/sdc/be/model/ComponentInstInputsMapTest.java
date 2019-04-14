package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ComponentInstInputsMapTest {

	private ComponentInstInputsMap createTestSubject() {
		return new ComponentInstInputsMap();
	}

	@Test
	public void testGetComponentInstanceInputsMap() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstanceInputsMap();
	}

	@Test
	public void testSetComponentInstanceInputsMap() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstanceInputsMap(componentInstanceInputsMap);
	}

	@Test
	public void testGetComponentInstanceProperties() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstanceProperties();
	}

	@Test
	public void testSetComponentInstancePropInput() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = new HashMap<>();

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstanceProperties(componentInstanceProperties);
	}

	@Test
	public void testResolvePropertiesToDeclareEmpty() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = null;

		// default test
		testSubject = createTestSubject();
		try {
			testSubject.resolvePropertiesToDeclare();
		} catch (Exception e) {
            Assert.assertTrue(e.getClass() == IllegalStateException.class);
		}
	}

	@Test
	public void testResolvePropertiesToDeclare() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = null;

		Map<String, List<ComponentInstancePropInput>> inputs = new HashMap<>();
		inputs.put("mock", new LinkedList<>());
		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstanceInputsMap(inputs);
		testSubject.resolvePropertiesToDeclare();
		testSubject = createTestSubject();
		testSubject.setComponentInstanceProperties(inputs);
		testSubject.resolvePropertiesToDeclare();
		testSubject = createTestSubject();
		testSubject.setPolicyProperties(inputs);
		testSubject.resolvePropertiesToDeclare();
	}
	
	@Test
	public void testGetPolicyProperties() throws Exception {
		ComponentInstInputsMap testSubject;
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.getPolicyProperties();
	}
}