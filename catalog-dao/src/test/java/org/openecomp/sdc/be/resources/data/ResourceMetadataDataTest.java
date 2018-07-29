package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;

import java.util.HashMap;
import java.util.Map;

public class ResourceMetadataDataTest {

	private ResourceMetadataData createTestSubject() {
		return new ResourceMetadataData();
	}

	@Test
	public void testCtor() throws Exception {
		new ResourceMetadataData(new ResourceMetadataDataDefinition());
		new ResourceMetadataData(new HashMap<>());
	}
	
	@Test
	public void testToGraphMap() throws Exception {
		ResourceMetadataData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}