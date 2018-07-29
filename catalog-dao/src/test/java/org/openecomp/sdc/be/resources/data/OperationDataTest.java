package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class OperationDataTest {

	private OperationData createTestSubject() {
		return new OperationData();
	}

	@Test
	public void testCtor() throws Exception {
		new OperationData(new HashMap<>());
		new OperationData(new OperationData());
		new OperationData(new OperationDataDefinition());
		
	}
	
	@Test
	public void testGetOperationDataDefinition() throws Exception {
		OperationData testSubject;
		OperationDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationDataDefinition();
	}

	
	@Test
	public void testSetOperationDataDefinition() throws Exception {
		OperationData testSubject;
		OperationDataDefinition operationDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOperationDataDefinition(operationDataDefinition);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		OperationData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		OperationData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}