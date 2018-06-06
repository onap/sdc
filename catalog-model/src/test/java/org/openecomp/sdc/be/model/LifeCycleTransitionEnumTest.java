package org.openecomp.sdc.be.model;

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
		String name = LifeCycleTransitionEnum.CHECKIN.getDisplayName();
		LifeCycleTransitionEnum result;

		// default test
		for (LifeCycleTransitionEnum iterable_element : LifeCycleTransitionEnum.values()) {
			result = LifeCycleTransitionEnum.getFromDisplayName(iterable_element.getDisplayName());
		}
	}

	@Test
	public void testGetFromDisplayNameException() throws Exception {
		String name = LifeCycleTransitionEnum.CHECKIN.getDisplayName();
		LifeCycleTransitionEnum result;

		// default test
		try {
			result = LifeCycleTransitionEnum.getFromDisplayName("mock");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testValuesAsString() throws Exception {
		String result;

		// default test
		result = LifeCycleTransitionEnum.valuesAsString();
	}
}