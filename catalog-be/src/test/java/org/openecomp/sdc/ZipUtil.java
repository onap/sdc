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

package org.openecomp.sdc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class ZipUtil {

	public static void main(String[] args) {

		String zipFileName = "/src/test/resources/config/config.zip";

		zipFileName = "C:\\Git_work\\D2-SDnC\\catalog-be\\src\\test\\resources\\config\\config.zip";
		zipFileName = "src/test/resources/config/config.zip";

		Path path = Paths.get(zipFileName);

		try {
			byte[] zipAsBytes = Files.readAllBytes(path);
			// encode to base

			byte[] decodedMd5 = Base64.encodeBase64(zipAsBytes);
			String decodedStr = new String(decodedMd5);

			zipAsBytes = Base64.decodeBase64(decodedStr.getBytes());

			// String str = new String(zipAsBytes);

			// readZip(str.getBytes());
			readZip(zipAsBytes);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static Map<String, byte[]> readZip(byte[] zipAsBytes) {

		Map<String, byte[]> fileNameToByteArray = new HashMap<String, byte[]>();

		byte[] buffer = new byte[1024];
		ZipInputStream zis = null;
		try {

			zis = new ZipInputStream(new ByteArrayInputStream(zipAsBytes));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();

				if (false == ze.isDirectory()) {

					ByteArrayOutputStream os = new ByteArrayOutputStream();
					try {
						int len;
						while ((len = zis.read(buffer)) > 0) {
							os.write(buffer, 0, len);
						}

						// aClass.outputStreamMethod(os);
						String aString = new String(os.toByteArray(), "UTF-8");

						fileNameToByteArray.put(fileName, os.toByteArray());

					} finally {
						if (os != null) {
							os.close();
						}
					}
				}
				ze = zis.getNextEntry();

			}

			zis.closeEntry();
			zis.close();

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} finally {
			if (zis != null) {
				try {
					zis.closeEntry();
					zis.close();
				} catch (IOException e) {
					// TODO: add log
				}

			}
		}

		return fileNameToByteArray;

	}

}
