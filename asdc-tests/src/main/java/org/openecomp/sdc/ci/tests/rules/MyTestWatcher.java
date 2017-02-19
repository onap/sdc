/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.ci.tests.rules;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openecomp.sdc.ci.tests.api.SdcTest;

public class MyTestWatcher extends TestWatcher {

	SdcTest odlTest;

	public MyTestWatcher(SdcTest odlTest) {
		this.odlTest = odlTest;
	}

	/**
	 * Invoked when a test succeeds
	 * 
	 * @param description
	 */
	@Override
	protected void succeeded(Description description) {
		String testName = description.getMethodName();
		odlTest.addTestSummary(testName, true);

	}

	/**
	 * Invoked when a test fails
	 * 
	 * @param e
	 * @param description
	 */
	@Override
	protected void failed(Throwable e, Description description) {
		String testName = description.getMethodName();
		odlTest.addTestSummary(testName, false, e);
	}

	/**
	 * Invoked when a test is about to start
	 * 
	 * @param description
	 */
	@Override
	protected void starting(Description description) {
		// System.out.println("protected void starting(Description description)
		// {");
		this.odlTest.getLogger().debug("Start running test {}", description.getMethodName());
	}

	/**
	 * Invoked when a test method finishes (whether passing or failing)
	 * 
	 * @param description
	 */
	@Override
	protected void finished(Description description) {
		// System.out.println("protected void finished(Description description)
		// {");
		this.odlTest.getLogger().debug("Finish running test {}", description.getMethodName());
	}
}
