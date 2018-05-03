package org.openecomp.sdc.asdctool.impl;

import org.junit.Test;

public class ProductLogicTest {

	private ProductLogic createTestSubject() {
		return new ProductLogic();
	}

	@Test(expected=NullPointerException.class)
	public void testDeleteAllProducts() throws Exception {
		ProductLogic testSubject;
		String titanFile = "";
		String beHost = "";
		String bePort = "";
		String adminUser = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteAllProducts(titanFile, beHost, bePort, adminUser);
	}
}