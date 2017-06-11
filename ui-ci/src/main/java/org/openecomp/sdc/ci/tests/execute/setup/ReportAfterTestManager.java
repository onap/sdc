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

package org.openecomp.sdc.ci.tests.execute.setup;

import java.io.IOException;

import org.openecomp.sdc.ci.tests.execute.setup.ExtentManager.suiteNameXml;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.aventstack.extentreports.Status;

public class ReportAfterTestManager extends ExtentTestActions  {
	
	private static String testName;
	private static Throwable throwable;
	private static int status;
	
	private static void logSuccessAfterTest(){
		final Status logStatus = Status.PASS;
		addTag(logStatus, "Success");
		try{
			String message = "Finished the test with the following screenshot : ";
			addScreenshotToReport(logStatus, testName, message);
		}catch(Exception e){
			log(logStatus, "SUCCESS - The following exepction occured : " + e.getMessage());
		}
	}
	
	private static void logFailAfterTest(){
		addTag(Status.FAIL, "Failure");
		try{
			log(Status.ERROR, "ERROR - The following exepction occured : ");
			log(Status.ERROR, throwable);
			String message = "Failure is described in the following screenshot : ";
			addScreenshotToReport(Status.FAIL, testName, message);
		}catch(Exception e){
			log(Status.ERROR, "ERROR - The following exepction occured : " + e.getMessage());
		}
	}
	
	private static void logSkipAfterTest(){
		final Status logStatus = Status.SKIP;
		addTag(logStatus, "Skipped");
		try{
			log(logStatus, "SKIP - The following exepction occured : ");
			log(logStatus, throwable);
			String message = "Skip is described in the following screenshot : ";
			addScreenshotToReport(logStatus, testName, message);
		}catch(Exception e){
			log(logStatus, "SKIP - The following exepction occured : " + e.getMessage());
		}
	}
	private static void logFatalAfterTest(){
		final Status logStatus = Status.FATAL;
		addTag(logStatus, "Fatal");
		try{
			log(logStatus, "FATAL - The following exepction occured : ");
			log(logStatus, throwable);
			String message = "Fatal is described in the following screenshot : ";
			addScreenshotToReport(logStatus, testName, message);
		}catch(Exception e){
			log(logStatus, "FATAL - The following exepction occured : " + e.getMessage());
		}
	}
	
	private static String addScreenshotToReport(Status logStatus, String testName, String message) throws IOException{
		
		String addedValueFromDataProvider = WindowTestManager.getWindowMap().getAddedValueFromDataProvider();
		if (addedValueFromDataProvider != null){
			addedValueFromDataProvider = addedValueFromDataProvider.replace(":", "-");
			testName = testName + "...." + addedValueFromDataProvider;
		}
		
		return addScreenshot(logStatus, testName, message);
	}
	
	public static void report(ITestResult result, ITestContext context){
		
		testName = result.getName();
		throwable = result.getThrowable();
		status = result.getStatus();
		
		String suiteName = ExtentManager.getSuiteName(context);

		switch(status){
		case ITestResult.SUCCESS:				
			logSuccessAfterTest();
			break;
				
		case ITestResult.FAILURE:
			
			if (suiteName.equals(suiteNameXml.TESTNG_FAILED_XML_NAME.getValue())) {
				logFatalAfterTest();
			}else{
				logFailAfterTest();
			}
			break;
			
		case ITestResult.SKIP:
			logSkipAfterTest();
			break;
				
		default:
			break;
		}
				
	}
		
}

