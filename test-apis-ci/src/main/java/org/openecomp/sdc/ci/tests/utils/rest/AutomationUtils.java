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

package org.openecomp.sdc.ci.tests.utils.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;

public class AutomationUtils extends BaseRestUtils {
	
	
	public static String getOnboardVersion()  {
		String onboardVersionStr = null;
		try {
			
			RestResponse onboardVersion = CatalogRestUtils.getOnboardVersion();
			onboardVersionStr = ResponseParser.getValueFromJsonResponse(onboardVersion.getResponse() , "Version");
						
		} catch (Exception e) {
			System.out.println("UnknownOnboardVersion");
		}
		return onboardVersionStr != null ? onboardVersionStr : "UnknownOnboardVersion";
		
	}

	public static String getOSVersion()  {
		String osVersionStr = null;
		try {
			RestResponse osVersion = CatalogRestUtils.getOsVersion();
			osVersionStr = ResponseParser.getVersionFromResponse(osVersion);
			
		} catch (Exception e) {
			System.out.println("UnknownOSversion");
		}
	
		return osVersionStr != null  ? osVersionStr : "UnknownOSversion" ;
	}
	
	
	
	public static void createVersionsInfoFile(String filepath, String onboardVersion, String osVersion, String envData, String suiteName)
			throws FileNotFoundException, IOException {
		File myFoo = new File(filepath);
		FileOutputStream fooStream = new FileOutputStream(myFoo, false); // true to append
		String versions =  ("onboardVesrion=\""+ onboardVersion+ "\"\n" + "osVersion=\"" + osVersion + "\"\n" + "env=\""+ envData + "\"\n" + "suiteName=\""+ suiteName+ "\"\n");
		byte[] myBytes = versions.getBytes();
		fooStream.write(myBytes);
		fooStream.close();
	}
	
	public static void createVersionsInfoFile(String filepath, String onboardVersion, String osVersion, String envData)
			throws FileNotFoundException, IOException {
		createVersionsInfoFile(filepath, onboardVersion, osVersion, envData, null);
	}
	
	public static void addEnvDetailsToReport() throws FileNotFoundException{
	
		Config config = Utils.getConfig();
		config.getUrl();
	}
	
	public static File getConfigFile(String configFileName) throws Exception {
		File configFile = new File(getBasePath() + File.separator + "conf" + File.separator + configFileName);
		if (!configFile.exists()) {
			configFile = new File(getConfFilesPath() + configFileName);
		}
		return configFile;
	}
	
	public static String getCiFilesPath() {
		return getBasePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources"
				+ File.separator + "ci";
	}

	public static String getConfFilesPath() {
		return getCiFilesPath() + File.separator + "conf" + File.separator;
	}

	public static String getTestSuitesFilesPath() {
		return getCiFilesPath() + File.separator + "testSuites" + File.separator;
	}
	public static String getBasePath() {
		return System.getProperty("user.dir");
	}
	

}
