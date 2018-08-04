package org.openecomp.sdc.be.switchover.detector;

import org.junit.Test;


public class SwitchoverDetectorTest {

	private SwitchoverDetector createTestSubject() {
		return new SwitchoverDetector();
	}

	
	@Test
	public void testGetSiteMode() throws Exception {
		SwitchoverDetector testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSiteMode();
	}

	
	@Test
	public void testSetSiteMode() throws Exception {
		SwitchoverDetector testSubject;
		String mode = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSiteMode(mode);
	}

	

}