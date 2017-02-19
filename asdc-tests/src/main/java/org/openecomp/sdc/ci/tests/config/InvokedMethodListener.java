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

package org.openecomp.sdc.ci.tests.config;

import java.util.HashMap;
import java.util.Map;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.internal.TestResult;

public class InvokedMethodListener implements IInvokedMethodListener {

	static Map<String, Integer> methodFailCount = new HashMap<String, Integer>();

	@Override

	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {

		if (methodFailCount.get(method.getTestMethod().getMethodName()) != null
				&& methodFailCount.get(method.getTestMethod().getMethodName()) > 1)
			throw new SkipException("Skipped due to failure count > 1");
		;

	}

	@Override

	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {

		if (testResult.getStatus() == TestResult.FAILURE) {
			if (methodFailCount.get(method.getTestMethod().getMethodName()) == null)
				methodFailCount.put(method.getTestMethod().getMethodName(), 1);
			else {
				methodFailCount.put(method.getTestMethod().getMethodName(),
						methodFailCount.get(method.getTestMethod().getMethodName()) + 1);
			}

		}

	}

}
