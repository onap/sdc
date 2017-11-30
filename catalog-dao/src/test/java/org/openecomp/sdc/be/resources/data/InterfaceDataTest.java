package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;


public class InterfaceDataTest {

	private InterfaceData createTestSubject() {
		return new InterfaceData();
	}

	
	@Test
	public void testGetInterfaceDataDefinition() throws Exception {
		InterfaceData testSubject;
		InterfaceDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInterfaceDataDefinition();
	}

	
	@Test
	public void testSetInterfaceDataDefinition() throws Exception {
		InterfaceData testSubject;
		InterfaceDataDefinition interfaceDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInterfaceDataDefinition(interfaceDataDefinition);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		InterfaceData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		InterfaceData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}