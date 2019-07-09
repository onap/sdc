/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.junit.Test;

import java.util.Map;

public class ZipUtilTest {

	// private ZipUtil createTestSubject() {
	// return new ZipUtil();
	// }

	@Test
	public void testReadZip() throws Exception {
		byte[] zipAsBytes = new byte[] { ' ' };
		Map<String, byte[]> result;

		// default test
		result = ZipUtil.readZip(zipAsBytes);
	}

	@Test
	public void testMain() throws Exception {
		String[] args = new String[] { "" };

		// default test
		ZipUtil.main(args);
	}

	@Test
	public void testZipBytes() throws Exception {
		byte[] input = new byte[] { ' ' };
		byte[] result;

		// default test
		result = ZipUtil.zipBytes(input);
	}

	@Test
	public void testUnzip() throws Exception {
		byte[] zipped = new byte[] { ' ' };
		byte[] result;

		// default test
		result = ZipUtil.unzip(zipped);
	}
}
