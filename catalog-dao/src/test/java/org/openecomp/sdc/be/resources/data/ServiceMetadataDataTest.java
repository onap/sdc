package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import org.junit.Test;


public class ServiceMetadataDataTest {

	private ServiceMetadataData createTestSubject() {
		return new ServiceMetadataData();
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