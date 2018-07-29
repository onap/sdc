package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class ServiceMetadataDataTest {

	private ServiceMetadataData createTestSubject() {
		return new ServiceMetadataData();
	}

	@Test
	public void testCtor() throws Exception {
		new ServiceMetadataData(new HashMap());
		new ServiceMetadataData(new ServiceMetadataDataDefinition());
	}
	
	@Test
	public void testGetUniqueIdKey() throws Exception {
		ServiceMetadataData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		ServiceMetadataData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}