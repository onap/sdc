package org.openecomp.sdc.be.impl;

import com.google.gson.Gson;
import org.junit.Test;
import org.openecomp.sdc.be.user.IUserBusinessLogic;

public class ServletUtilsTest {

	private ServletUtils createTestSubject() {
		return new ServletUtils();
	}

	@Test
	public void testGetComponentsUtils() throws Exception {
		ServletUtils testSubject;
		ComponentsUtils result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentsUtils();
	}

	@Test
	public void testGetGson() throws Exception {
		ServletUtils testSubject;
		Gson result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGson();
	}

	@Test
	public void testGetUserAdmin() throws Exception {
		ServletUtils testSubject;
		IUserBusinessLogic result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserAdmin();
	}
}