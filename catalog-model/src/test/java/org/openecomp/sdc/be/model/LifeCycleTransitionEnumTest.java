package org.openecomp.sdc.be.model;

import javax.annotation.Generated;

import org.junit.Test;

public class LifeCycleTransitionEnumTest {

	private LifeCycleTransitionEnum createTestSubject() {
		return LifeCycleTransitionEnum.CERTIFY;
	}

	
	@Test
	public void testGetDisplayName() throws Exception {
		LifeCycleTransitionEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDisplayName();
	}

	
	@Test
	public void testGetFromDisplayName() throws Exception {
		String name = LifeCycleTransitionEnum.CHECKIN.getDisplayName() ;
		LifeCycleTransitionEnum result;

		// default test
		result = LifeCycleTransitionEnum.getFromDisplayName(name);
	}

	
	@Test
	public void testValuesAsString() throws Exception {
		String result;

		// default test
		result = LifeCycleTransitionEnum.valuesAsString();
	}
}