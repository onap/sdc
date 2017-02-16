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

package org.openecomp.sdc.ci.tests.utilities;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;

public class CaptureFailedTests extends TestListenerAdapter {

	@Override
	public void onTestFailure(ITestResult tr) {

		String testName = tr.getName();
		String parameter = tr.getParameters().length == 1 ? tr.getParameters()[0].toString() : "";
		File folder = new File(String.format("test-output\\failure_screenshots\\%s_%s.png", testName, parameter));

		File scrFile = ((TakesScreenshot) GeneralUIUtils.getDriver()).getScreenshotAs(OutputType.FILE);

		try {
			FileUtils.copyFile(scrFile, folder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Reporter.setEscapeHtml(false);
		String screenPath = String.format("<img src=%s />", folder.getAbsolutePath());
		Reporter.log(screenPath);
	}

}
