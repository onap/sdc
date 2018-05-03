package org.openecomp.sdc.asdctool.impl;

import org.junit.Test;

public class RestUtilsTest {

	private RestUtils createTestSubject() {
		return new RestUtils();
	}

	@Test
	public void testDeleteProduct() throws Exception {
		RestUtils testSubject;
		String productUid = "";
		String beHost = "";
		String bePort = "";
		String adminUser = "";
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteProduct(productUid, beHost, bePort, adminUser);
	}
}