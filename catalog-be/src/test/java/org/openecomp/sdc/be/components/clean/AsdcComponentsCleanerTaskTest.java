package org.openecomp.sdc.be.components.clean;

import org.junit.Test;


public class AsdcComponentsCleanerTaskTest {

	private AsdcComponentsCleanerTask createTestSubject() {
		return new AsdcComponentsCleanerTask();
	}

	
	@Test
	public void testInit() throws Exception {
		AsdcComponentsCleanerTask testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.init();
	}

	
	@Test
	public void testDestroy() throws Exception {
		AsdcComponentsCleanerTask testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.destroy();
	}

	
	@Test
	public void testStartTask() throws Exception {
		AsdcComponentsCleanerTask testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.startTask();
	}

	
	@Test
	public void testStopTask() throws Exception {
		AsdcComponentsCleanerTask testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.stopTask();
	}

	


	
	@Test
	public void testRun() throws Exception {
		AsdcComponentsCleanerTask testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.run();
	}
}