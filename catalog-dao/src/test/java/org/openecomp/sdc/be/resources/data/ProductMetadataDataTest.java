package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class ProductMetadataDataTest {

	private ProductMetadataData createTestSubject() {
		return new ProductMetadataData();
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