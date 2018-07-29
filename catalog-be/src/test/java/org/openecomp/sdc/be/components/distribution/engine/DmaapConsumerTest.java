package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.function.Consumer;

public class DmaapConsumerTest  extends BeConfDependentTest{

	private DmaapConsumer createTestSubject() {
		return new DmaapConsumer(new ExecutorFactory(), new DmaapClientFactory());
	}

	@Test
	public void testConsumeDmaapTopic() throws Exception {
		DmaapConsumer testSubject;
		Consumer<String> notificationReceived = null;
		UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandlerMock();

		// default test
		testSubject = createTestSubject();
		testSubject.consumeDmaapTopic(notificationReceived, exceptionHandler);
	}
	
	private class UncaughtExceptionHandlerMock implements UncaughtExceptionHandler{

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}