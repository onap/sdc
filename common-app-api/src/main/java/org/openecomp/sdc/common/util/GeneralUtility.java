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

package org.openecomp.sdc.common.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.common.api.Constants;

public class GeneralUtility {

	public static boolean generateTextFile(String fileName, String fileData) {
		boolean isSuccessfull = true;
		try {
			FileUtils.writeStringToFile(new File(fileName), fileData);
		} catch (IOException e) {
			isSuccessfull = false;
		}
		return isSuccessfull;
	}
	
	/**
	 * Use with care, usage is not advised!!!
	 * The method only checks if String does not contain special characters + length divided by 4 with no remainder.
	 * The methods contained in other common libraries do the same.
	 */
	public static boolean isBase64Encoded(byte[] data) {
		return Base64.isBase64(data);
	}
	
	/**
	 *Use with care, usage is not advised!!!
	 * The method only checks if String does not contain special characters + length divided by 4 with no remainder.
	 * The methods contained in other common libraries do the same.
	 */
	public static boolean isBase64Encoded(String str) {
		boolean isEncoded = false;
		try {
			// checks if the string was properly padded to the
			isEncoded = ((str.length() % 4 == 0) && (Pattern.matches("\\A[a-zA-Z0-9/+]+={0,2}\\z", str)));
			if (isEncoded) {
				// If no exception is caught, then it is possibly a base64
				// encoded string
				byte[] data = Base64.decodeBase64(str);
			}

		} catch (Exception e) {
			// If exception is caught, then it is not a base64 encoded string
			isEncoded = false;
		}
		return isEncoded;
	}

	/**
	 * Checks whether the passed string exceeds a limit of number of characters.
	 * 
	 * @param str
	 * @param limit
	 * @return the result of comparison, or false if str is null.
	 */
	public static boolean isExceedingLimit(String str, int limit) {
		if (str == null) {
			return false;
		}
		return str.length() > limit;
	}

	/**
	 * Checks the passed string list whether the cumulative length of strings and delimiters between them exceeds a limit of number of characters. For example for list ("one","two","three") with delimiter "," the length of list is calculated
	 * 3+1+3+1+5=13
	 *
	 * @param strList
	 * @param limit
	 * @param delimiterLength
	 *            - 0 if there is no delimeter.
	 * @return the result of comparison, or false if strList is null.
	 */
	public static boolean isExceedingLimit(List<String> strList, int limit, int delimiterLength) {
		if (strList == null || strList.isEmpty()) {
			return false;
		}
		int sum = 0;
		int size = strList.size();
		for (int i = 0; i < size - 1; i++) {
			String str = strList.get(i);
			if (str != null) {
				sum += str.length();
			}
			sum += delimiterLength;
		}
		String str = strList.get(size - 1);
		if (str != null) {
			sum += str.length();
		}
		return sum > limit;
	}

	/**
	 * Return the extension as the substring from the last dot. For input "kuku.txt", "txt" will be returned. If no dot is found or input is null, empty string is returned.
	 * 
	 * @param fileName
	 * @return extension
	 */
	public static String getFilenameExtension(String fileName) {
		String res = Constants.EMPTY_STRING;
		if (fileName != null) {
			int indexOf = fileName.lastIndexOf('.');
			if (indexOf != -1 && indexOf < (fileName.length() - 1)) {
				res = fileName.substring(indexOf + 1);
			}
		}
		return res;
	}

	public static String calculateMD5ByByteArray(byte[] payload) {
		String decodedMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(payload);
		byte[] encodeMd5 = Base64.encodeBase64(decodedMd5.getBytes());
		return new String(encodeMd5);

	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	public static String calculateMD5ByString(String data) {
		String calculatedMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(data);

		// encode base-64 result
		byte[] encodeBase64 = Base64.encodeBase64(calculatedMd5.getBytes());
		return new String(encodeBase64);
	}
}
