package org.openecomp.sdc.asdctool.impl;

import com.thinkaurelius.titan.core.TitanGraph;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class UpdatePropertyOnVertexTest {

	private UpdatePropertyOnVertex createTestSubject() {
		return new UpdatePropertyOnVertex();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testOpenGraph() throws Exception {
		UpdatePropertyOnVertex testSubject;
		String titanFileLocation = "";
		TitanGraph result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.openGraph("");
	}

	@Test(expected=NullPointerException.class)
	public void testUpdatePropertyOnServiceAtLeastCertified() throws Exception {
		UpdatePropertyOnVertex testSubject;
		String titanFile = "";
		Map<String, Object> keyValueToSet = null;
		List<Map<String, Object>> orCriteria = null;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.updatePropertyOnServiceAtLeastCertified(titanFile, keyValueToSet, orCriteria);
	}
}