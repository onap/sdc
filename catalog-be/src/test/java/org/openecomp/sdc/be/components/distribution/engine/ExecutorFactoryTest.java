/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.distribution.engine;

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
	
	private class UncaughtExceptionHandlerMock implements UncaughtExceptionHandler {

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
