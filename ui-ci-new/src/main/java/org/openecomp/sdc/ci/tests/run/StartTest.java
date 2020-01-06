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

package org.openecomp.sdc.ci.tests.run;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.PropertyConfigurator;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;

public class StartTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(StartTest.class);


	public static boolean debug = false;

	public static AtomicBoolean loggerInitialized = new AtomicBoolean(false);

	public static void main(String[] args) {

		String debugEnabled = System.getProperty("debug");
		if (debugEnabled != null && debugEnabled.equalsIgnoreCase("true")) {
			debug = true;
		}
		System.out.println("Debug mode is " + (debug ? "enabled" : "disabled"));

		enableLogger();

		Config config = null;
        config = Utils.getConfig();

        if (config == null) {
			LOGGER.error("Failed to configuration file of ci tests.");
			System.exit(1);
		}

		TestNG testng = new TestNG();

		final List<String> suites = new ArrayList<>();
		suites.add("testSuites/" + args[0]);
		testng.setTestSuites(suites);
		testng.setUseDefaultListeners(true);
		testng.setOutputDirectory("target/");

		testng.run();

	}



	public static void enableLogger() {

		if (!loggerInitialized.get()) {

			loggerInitialized.set(true);

			String log4jPropsFile = System.getProperty("log4j.configuration");
			if (System.getProperty("os.name").contains("Windows")) {
				String logProps = "src/main/resources/ci/conf/log4j.properties";
				if (log4jPropsFile == null) {
					System.setProperty("targetlog", "target/");
					log4jPropsFile = logProps;
				}

			}
			PropertyConfigurator.configureAndWatch(log4jPropsFile);

		}
	}



}
