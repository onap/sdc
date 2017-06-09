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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {

	private static Logger log = LoggerFactory.getLogger(ZipUtil.class.getName());

	private ZipUtil() {
	}

	public static Map<String, byte[]> readZip(byte[] zipAsBytes) {

		ZipInputStream zis = null;
		zis = new ZipInputStream(new ByteArrayInputStream(zipAsBytes));

		return readZip(zis);
	}

	public static Map<String, byte[]> readZip(ZipInputStream zis) {

		Map<String, byte[]> fileNameToByteArray = new HashMap<String, byte[]>();

		byte[] buffer = new byte[1024];
		try {
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

	public static void main(String[] args) {

		String zipFileName = "/src/test/resources/config/config.zip";

		zipFileName = "C:\\Git_work\\D2-SDnC\\catalog-be\\src\\test\\resources\\config\\config.zip";

		Path path = Paths.get(zipFileName);

		try {
			byte[] zipAsBytes = Files.readAllBytes(path);
			// encode to base

			ZipUtil.readZip(zipAsBytes);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static byte[] zipBytes(byte[] input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		ZipEntry entry = new ZipEntry("zip");
		entry.setSize(input.length);
		zos.putNextEntry(entry);
		zos.write(input);
		zos.closeEntry();
		zos.close();
		return baos.toByteArray();
	}

	public static byte[] unzip(byte[] zipped) {
		ZipInputStream zipinputstream = null;
		ByteArrayOutputStream outputStream = null;
		try {
			byte[] buf = new byte[1024];

			zipinputstream = new ZipInputStream(new ByteArrayInputStream(zipped));
			ZipEntry zipentry = zipinputstream.getNextEntry();
			outputStream = new ByteArrayOutputStream();
			int n;
			while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
				outputStream.write(buf, 0, n);
			}

			return outputStream.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("Can't unzip input stream", e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					log.debug("Failed to close output stream", e);
				}
			}
			if (zipinputstream != null) {
				try {
					zipinputstream.closeEntry();
					zipinputstream.close();
				} catch (IOException e) {
					log.debug("Failed to close zip input stream", e);
				}
			}
		}
	}

}
