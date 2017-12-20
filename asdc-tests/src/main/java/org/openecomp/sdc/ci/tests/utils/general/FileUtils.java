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

package org.openecomp.sdc.ci.tests.utils.general;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openecomp.sdc.ci.tests.utils.Decoder;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public class FileUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class.getName());

	public static void writeToFile(String filePath, String content) {
		try {
			Files.write(Paths.get(filePath), content.getBytes());
		} catch (IOException e) {
			LOGGER.debug(String.format("Failed to write to file '%s'", filePath), e);
		}
	}

	public static String getFileName(String fullyQualified) {
		String fileName = fullyQualified;

		int i = fullyQualified.lastIndexOf('.');
		if (i > 0) {
			fileName = fullyQualified.substring(i + 1);
		}
		return fileName;

	}

	public static Either<String, Exception> getFileContentUTF8(String filePath) {
		Either<String, Exception> eitherResult;
		try {
			String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
			eitherResult = Either.left(content);
		} catch (Exception e) {
			eitherResult = Either.right(e);
		}
		return eitherResult;
	}

	public static List<String> getFileListFromBaseDirectoryByTestName(String testResourcesPath) {

		File file = new File(testResourcesPath);
		File[] listFiles = file.listFiles();
		if (listFiles != null) {
			List<String> listFileName = new ArrayList<>();
			for (File newFile : listFiles) {
				if (newFile.isFile()) {
					listFileName.add(newFile.getPath());
				}
			}
			return listFileName;
		}
		assertTrue("directory " + testResourcesPath + " is empty", false);
		return null;
	}

	public static String getFilePathFromListByPattern(List<String> fileList, String pattern) {

		for (String filePath : fileList) {
			if (filePath.contains(pattern)) {
				return filePath;
			}
		}
		return null;
	}

	public static String loadPayloadFileFromListUsingPosition(List<String> listFileName, String pattern,
			Boolean isBase64, int positionInList) throws IOException {
		List<String> newList = new ArrayList<>(Arrays.asList(listFileName.get(positionInList)));
		return loadPayloadFile(newList, pattern, isBase64);
	}

	public static String loadPayloadFile(List<String> listFileName, String pattern, Boolean isBase64)
			throws IOException {
		String fileName;
		String payload = null;
		fileName = FileUtils.getFilePathFromListByPattern(listFileName, pattern);
		LOGGER.debug("fileName: {}",fileName);

		if (fileName != null) {
			payload = Decoder.readFileToString(fileName);
			if (isBase64) {
				payload = Decoder.encode(payload.getBytes());
			}
		} else {
			assertTrue("file to upload not found", false);
		}
		return payload;
	}

	public static String getFileNameFromPath(String testResourcesPath) {

		File file = new File(testResourcesPath);
		String fileName = null;
		if (file.exists()) {
			return file.getName();
		} else {
			assertTrue("file to upload not found", false);
		}
		return fileName;

	}
}
