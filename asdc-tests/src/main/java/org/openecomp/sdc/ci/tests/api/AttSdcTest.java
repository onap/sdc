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

package org.openecomp.sdc.ci.tests.api;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.execute.artifacts.CrudArt;
import org.openecomp.sdc.ci.tests.rules.MyTestWatcher;
import org.openecomp.sdc.ci.tests.run.StartTest;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class AttSdcTest {

	public static StringBuilder doc = new StringBuilder();
	public static String file = null;
	public static Config config = null;
	// protected Gson gson = new Gson();
	protected Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

	protected TestName testName = null;

	protected static boolean displayException = false;
	static Logger logger = LoggerFactory.getLogger(CrudArt.class.getName());
	
	
	public AttSdcTest(TestName testName, String className) {
		super();

		StartTest.enableLogger();

		this.testName = testName;

		String displayEx = System.getProperty("displayException");
		if (displayEx != null && Boolean.valueOf(displayEx).booleanValue()) {
			displayException = true;
		}

	}

	@Rule
	public TestWatcher tw = new MyTestWatcher(this);

	@BeforeClass
	public static void beforeClass() {
		doc = new StringBuilder();
		doc.append(
				"<Html><head><style>th{background-color: gray;color: white;height: 30px;}td {color: black;height: 30px;}.fail {background-color: #FF5555;width: 100px;text-align: center;}.success {background-color: #00FF00;width: 100px;text-align: center;}.name {width: 200px;background-color: #F0F0F0;}.message {width: 300px;background-color: #F0F0F0;}</style>");

		doc.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
		doc.append(
				"<link rel=\"stylesheet\" href=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css\">");

		doc.append("</head><body>");

		doc.append("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script>");
		doc.append("<script src=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js\"></script>");

		doc.append("<table>");

		doc.append("<tr>");
		doc.append("<th>").append("Test Name").append("</th>");
		doc.append("<th>").append("Status").append("</th>");
		doc.append("<th>").append("Message").append("</th>");

		if (displayException) {
			doc.append("<th>").append("Exception").append("</th>");
		}
		doc.append("</tr>");
	}

	@AfterClass
	public static void afterClass() {
		doc.append("<table>");
		// writeToFile("./" + ConfigAttOdlIt.REPORT_FILE , doc.toString());
		FileUtils.writeToFile(
				Config.instance().getOutputFolder() + File.separator + file + StartTest.timeOfTest + ".html",
				doc.toString());

	}

	@Before
	public void beforeTest() throws FileNotFoundException {
		file = FileUtils.getFileName(this.getClass().getName());
		config = Utils.getConfig();
		assertTrue(config != null);

		logger.info("Start running test {}", testName.getMethodName());
	}

	@After
	public void afterTest() throws FileNotFoundException {

		logger.info("Finish running test {}", testName.getMethodName());
	}

	public void addTestSummary(String testName, boolean isSuccess) {
		addTestSummary(testName, isSuccess, null);
	}

	public void addTestSummary(String testName, boolean isSuccess, Throwable exception) {

		String message = exception == null ? "" : exception.getMessage();

		String result = (isSuccess) ? "success" : "fail";
		doc.append("<tr>");
		doc.append("<td class=\"name\">").append(testName).append("</td>");
		doc.append("<td class=\"" + result + "\">").append(result).append("</td>");
		doc.append("<td class=\"message\">").append(message).append("</td>");

		if (displayException) {
			// doc.append("<td
			// class=\"message\">").append(convertExceptionToString(exception)).append("</td>");
			doc.append("<td class=\"message\">");

			doc.append("<button type=\"button\" class=\"btn btn-info\" data-toggle=\"collapse\" data-target=\"#demo"
					+ testName + "\">Simple collapsible</button>");
			doc.append("<div id=\"demo" + testName + "\" class=\"collapse out\">");

			doc.append(convertExceptionToString(exception));

			doc.append("</div>");
			doc.append("</td>");
		}

		doc.append("</tr>");

		if (isSuccess) {
			logger.debug("Test {} {}", testName, (isSuccess ? " SUCCEEDED " : " FAILED with error " + message));
		} else {
			logger.error("Test {} {}", testName, (isSuccess ? " SUCCEEDED " : " FAILED with error " + message));
		}
	}

	private String convertExceptionToString(Throwable exception) {

		if (exception == null) {
			return "";
		}

		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();

		return exceptionAsString;
	}

	public Logger getLogger() {
		return logger;
	}

	protected boolean ignoreDueToBug(String bug) {

		List<String> bugs = config.getBugs();

		if (bugs != null && bugs.size() > 0) {
			for (String bugNumber : bugs) {
				if (bugNumber.startsWith(bug)) {
					return true;
				}
			}
		}

		return false;
	}

}
