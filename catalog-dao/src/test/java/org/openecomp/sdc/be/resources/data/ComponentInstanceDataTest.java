package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;


public class ComponentInstanceDataTest {

	private ComponentInstanceData createTestSubject() {
		return new ComponentInstanceData();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		ComponentInstanceData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		ComponentInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetName() throws Exception {
		ComponentInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testGetUniqueIdKey() throws Exception {
		ComponentInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	
	@Test
	public void testGetComponentInstDataDefinition() throws Exception {
		ComponentInstanceData testSubject;
		ComponentInstanceDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstDataDefinition();
	}

	
	@Test
	public void testSetComponentInstDataDefinition() throws Exception {
		ComponentInstanceData testSubject;
		ComponentInstanceDataDefinition componentInstDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstDataDefinition(componentInstDataDefinition);
	}

	
	@Test
	public void testToString() throws Exception {
		ComponentInstanceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetGroupInstanceCounter() throws Exception {
		ComponentInstanceData testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupInstanceCounter();
	}

	
	@Test
	public void testSetGroupInstanceCounter() throws Exception {
		ComponentInstanceData testSubject;
		Integer componentInstanceCounter = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupInstanceCounter(componentInstanceCounter);
	}

	
	@Test
	public void testIncreaseAndGetGroupInstanceCounter() throws Exception {
		ComponentInstanceData testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.increaseAndGetGroupInstanceCounter();
	}
}