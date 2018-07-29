package org.openecomp.sdc.be.components.distribution.engine;

import mockit.Deencapsulation;
import org.junit.Test;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class ExecutorFactoryTest {

	private ExecutorFactory createTestSubject() {
		return new ExecutorFactory();
	}

	@Test
	public void testCreate() throws Exception {
		ExecutorFactory testSubject;
		String name = "mock";
		UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandlerMock();
		ExecutorService result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.create(name, exceptionHandler);
	}

	@Test
	public void testCreateScheduled() throws Exception {
		ExecutorFactory testSubject;
		String name = "";
		ScheduledExecutorService result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createScheduled(name);
	}

	@Test
	public void testCreateThreadFactory() throws Exception {
		ExecutorFactory testSubject;
		String name = "mock";
		ThreadFactory result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "createThreadFactory",
				 name, new UncaughtExceptionHandlerMock());
	}
	
	private class UncaughtExceptionHandlerMock implements UncaughtExceptionHandler {

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}