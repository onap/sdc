package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import org.junit.Test;


public class UserFunctionalMenuDataTest {

	private UserFunctionalMenuData createTestSubject() {
		return new UserFunctionalMenuData("", "");
	}

	
	@Test
	public void testGetFunctionalMenu() throws Exception {
		UserFunctionalMenuData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFunctionalMenu();
	}

	
	@Test
	public void testSetFunctionalMenu() throws Exception {
		UserFunctionalMenuData testSubject;
		String functionalMenu = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFunctionalMenu(functionalMenu);
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		UserFunctionalMenuData testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testToString() throws Exception {
		UserFunctionalMenuData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testToJson() throws Exception {
		UserFunctionalMenuData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toJson();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		UserFunctionalMenuData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetUniqueIdKey() throws Exception {
		UserFunctionalMenuData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		UserFunctionalMenuData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}
}