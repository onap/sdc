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

package org.openecomp.sdc.ci.tests.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

public class Decoder {

	public static String encode(byte[] byteArrayToEncode) {

		byte[] bytesEncoded = Base64.encodeBase64(byteArrayToEncode);
		String strEncoded = new String(bytesEncoded);
		return strEncoded;
	}

	public static String decode(String strEncoded) throws IOException {

		byte[] byteDecoded = Base64.decodeBase64(strEncoded);
		String decoded = new String(byteDecoded);

		return decoded;

	}

	public static String readFileToString(String file) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		reader.close();
		return stringBuilder.toString();
	}

}
