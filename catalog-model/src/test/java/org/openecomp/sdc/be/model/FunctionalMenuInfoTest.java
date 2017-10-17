package org.openecomp.sdc.be.model;

import javax.annotation.Generated;

import org.junit.Test;


public class FunctionalMenuInfoTest {

	private FunctionalMenuInfo createTestSubject() {
		return new FunctionalMenuInfo();
	}

	
	@Test
	public void testGetFunctionalMenu() throws Exception {
		FunctionalMenuInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFunctionalMenu();
	}

	
	@Test
	public void testSetFunctionalMenu() throws Exception {
		FunctionalMenuInfo testSubject;
		String functionalMenu = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFunctionalMenu(functionalMenu);
	}

	
	@Test
	public void testToString() throws Exception {
		FunctionalMenuInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}