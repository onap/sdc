package org.openecomp.sdc.be.components.scheduledtasks;

import mockit.Deencapsulation;
import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;

import java.util.concurrent.ExecutorService;

public class AsdcComponentsCleanerTaskTest extends BeConfDependentTest{

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
		Deencapsulation.invoke(testSubject, "stopTask");
		testSubject.init();
		testSubject.startTask();
		Deencapsulation.invoke(testSubject, "stopTask");
	}

	@Test
	public void testRun() throws Exception {
		AsdcComponentsCleanerTask testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.run();
	}

	@Test
	public void testGetExecutorService() throws Exception {
		AsdcComponentsCleanerTask testSubject;
		ExecutorService result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getExecutorService();
	}
}