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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class FileHandling {

	public static Map<?, ?> parseYamlFile(String filePath) throws Exception {
		Yaml yaml = new Yaml();
		File file = new File(filePath);
		InputStream inputStream = new FileInputStream(file);
		Map<?, ?> map = (Map<?, ?>) yaml.load(inputStream);
		return map;
	}

	public static String getBasePath() {
		return System.getProperty("user.dir");
	}

	public static String getResourcesFilesPath() {
		return getBasePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources"
				+ File.separator + "Files" + File.separator;
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

	public static Object[] getFileNamesFromFolder(String filepath, String extension) {
		try {
			File dir = new File(filepath);
			List<String> filenames = new ArrayList<String>();
			if (dir.isDirectory()) {
				for (File file : dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(extension);
					}
				})) {

					filenames.add(file.getName());
				}
				return filenames.toArray();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
