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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;


public class DriverFactory {

	private static ThreadLocal<WebDriverThread> driverThread;
	private static List<WebDriverThread> webDriverThreadPool = Collections.synchronizedList(new ArrayList<WebDriverThread>());
	private static Config config;
	
	public DriverFactory() {
		try {
			config = Utils.getConfig();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@BeforeSuite(alwaysRun = true)
	public static void instantiateDriverObject() {

		// Selenium 3.4.0 change, location of gecko driver, set system property
//        System.setProperty("webdriver.gecko.driver","C:\\Gekko18\\geckodriver-v0.18.0-win64\\geckodriver.exe"); //change for 3.4.0, gecko driver location
		// End of Selenium 3.4.0 change 
		
		File basePath = new File(FileHandling.getBasePath());
		File[] listFiles = basePath.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File basePath, String name) {
				return name.startsWith(WebDriverThread.AUTOMATION_DOWNLOAD_DIR);
			}
		});
		Arrays.asList(listFiles).forEach(e -> FileHandling.deleteDirectory(e.getAbsolutePath()));
		
		
		
		driverThread = new ThreadLocal<WebDriverThread>() {
			@Override
			protected WebDriverThread initialValue() {
				WebDriverThread webDriverThread = new WebDriverThread(config);
				webDriverThreadPool.add(webDriverThread);
				return webDriverThread;
			}
		};
	}
	
	public static WebDriver getDriver() throws Exception {
		return driverThread.get().getDriver();
	}
	
	public static FirefoxProfile getDriverFirefoxProfile() throws Exception {
		return driverThread.get().getFirefoxProfile();
	}
	
	@AfterSuite(alwaysRun = true)
	public static void quitDriverAfterSuite() throws Exception {
		for (WebDriverThread webDriverThread : webDriverThreadPool) {
			if (webDriverThread.getDriver() != null)
				webDriverThread.quitDriver();
		}
		
		MobProxy.removeAllProxyServers();
		
		deleteDownloadDirs();
	}

	private static void deleteDownloadDirs() throws IOException {
//		System.gc();
		HashMap<Long,WindowTest> windowMap = WindowTestManager.getWholeMap();
		for (WindowTest win : windowMap.values()){
			String downloadDirectory = win.getDownloadDirectory();
			FileUtils.deleteDirectory(new File(downloadDirectory));
		}
	}
	
	public static void quitDriver() throws Exception{
		driverThread.get().quitDriver();
		driverThread.remove();
		WindowTestManager.removeWindowTest();
		MobProxy.removePoxyServer();
	}

	public static Config getConfig() {
		return config;
	}

	public static void setConfig(Config config) {
		DriverFactory.config = config;
	}
	

}
