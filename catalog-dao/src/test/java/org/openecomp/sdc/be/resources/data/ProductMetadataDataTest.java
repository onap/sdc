package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ProductMetadataDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class ProductMetadataDataTest {

	private ProductMetadataData createTestSubject() {
		return new ProductMetadataData();
	}

	@Test
	public void testCtor() throws Exception {
		new ProductMetadataData(new ProductMetadataDataDefinition());
		new ProductMetadataData(new HashMap<>());
	}
	
	@Test
	public void testGetUniqueIdKey() throws Exception {
		ProductMetadataData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		ProductMetadataData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testToString() throws Exception {
		ProductMetadataData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}