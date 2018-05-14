package org.openecomp.sdc.be.components.lifecycle;

import org.junit.Test;

public class LifecycleChangeInfoBaseTest {

	private LifecycleChangeInfoBase createTestSubject() {
		return new LifecycleChangeInfoBase();
	}

	@Test
	public void testGetUserRemarks() throws Exception {
		LifecycleChangeInfoBase testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserRemarks();
	}

	@Test
	public void testSetUserRemarks() throws Exception {
		LifecycleChangeInfoBase testSubject;
		String userRemarks = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUserRemarks(userRemarks);
	}
}