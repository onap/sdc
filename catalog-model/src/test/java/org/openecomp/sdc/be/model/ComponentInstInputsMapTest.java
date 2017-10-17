package org.openecomp.sdc.be.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;

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
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstancePropInput(componentInstanceProperties);
	}
}