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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class ZipUtil {

	private static Logger log = Logger.getLogger(ZipUtil.class.getName());

	private ZipUtil() {
	}

	public static Map<String, byte[]> readZip(File file) {
        try(InputStream fileInputStream = new FileInputStream(file)){
	        return readZip(IOUtils.toByteArray(fileInputStream));
        } catch (IOException e) {
            log.info("close File stream failed - {}" , e);
            return null;
        }
    }

	public static Map<String, byte[]> readZip(byte[] zipAsBytes) {
		Map<String, byte[]> fileNameToByteArray = new HashMap<>();
		byte[] buffer = new byte[1024];
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipAsBytes);
			ZipInputStream zis = new ZipInputStream(byteArrayInputStream)) {
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();

				if (!ze.isDirectory()) {

					try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
						int len;
						while ((len = zis.read(buffer)) > 0) {
							os.write(buffer, 0, len);
						}

						fileNameToByteArray.put(fileName, os.toByteArray());

					}
				}
				ze = zis.getNextEntry();
			}
		} catch (IOException ex) {
			log.info("close Byte stream failed" , ex);
			return null;
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
			log.info("close Byte stream failed" , e);
		}
	}

	public static byte[] zipBytes(byte[] input) throws IOException {
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zos = new ZipOutputStream(baos)){
			ZipEntry entry = new ZipEntry("zip");
			entry.setSize(input.length);
			zos.putNextEntry(entry);
			zos.write(input);
			zos.closeEntry();
			return baos.toByteArray();
		}
	}

	public static byte[] unzip(byte[] zipped) {
		try(ZipInputStream zipinputstream = new ZipInputStream(new ByteArrayInputStream(zipped));
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			byte[] buf = new byte[1024];
			ZipEntry zipentry = zipinputstream.getNextEntry();
			int n;
			while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
				outputStream.write(buf, 0, n);
			}
			return outputStream.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("Can't unzip input stream", e);
		}
	}
}
