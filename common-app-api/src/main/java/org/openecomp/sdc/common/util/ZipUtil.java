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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	private static Logger log = Logger.getLogger(ZipUtil.class.getName());

	private ZipUtil() {
	}

	public static Map<String, byte[]> readZip(ZipInputStream input) throws IOException {
		Map<String, byte[]> fileNameToByteArray = new HashMap<>();

		try (ZipInputStream stream = input) {
			ZipEntry entry;
			while ((entry = stream.getNextEntry()) != null) {
				assertEntryNotVulnerable(entry);
				if (!isDirectory(entry)) {
					fileNameToByteArray.put(entry.getName(), IOUtils.toByteArray(stream));
				}
			}
		}
		return fileNameToByteArray;
	}

	public static Map<String, byte[]> readZip(byte[] zipAsBytes) throws IOException {
		return readZip(new ZipInputStream(new ByteArrayInputStream(zipAsBytes)));
	}

	public static byte[] unzip(byte[] zipped) throws IOException {
		return readZip(zipped)
				.values()
				.stream()
				.collect(
						ByteArrayOutputStream::new,
						ZipUtil::appendStream,
						(a, b) -> {})
				.toByteArray();
	}

	public static byte[] zipBytes(byte[] input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(baos)) {
			ZipEntry entry = new ZipEntry("zip");
			entry.setSize(input.length);
			zos.putNextEntry(entry);
			zos.write(input);
			zos.closeEntry();
		}
		return baos.toByteArray();
	}

	private static boolean isDirectory(ZipEntry entry) {
		return entry.isDirectory();
	}

	private static void assertEntryNotVulnerable(ZipEntry entry) throws ZipException {
		if (entry.getName().contains("../")) {
			throw new ZipException("Path traversal attempt discovered.");
		}
	}

	private static void appendStream(ByteArrayOutputStream result, byte[] x) {
		try {
			result.write(x);
		} catch (IOException e) {
			log.info("Appending stream failed - {}" , e);
			// TODO exception
		}
	}
}
