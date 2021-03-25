/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.tosca;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.tosca.version.ApplicationVersionException;
import org.openecomp.sdc.be.model.tosca.version.Version;

import static org.junit.jupiter.api.Assertions.*;


public class VersionUtilTest {

	@Test
	public void testIsSnapshot() {
		assertTrue(VersionUtil.isSnapshot("test_snapshot"));
		assertTrue(VersionUtil.isSnapshot("test_SNAPSHOT"));
		assertFalse(VersionUtil.isSnapshot("test_SNAP"));
	}

	@Test
	public void testIsValid() {
		assertTrue(VersionUtil.isValid("1.0.2"));
		assertTrue(VersionUtil.isValid("1.0-2"));
		assertFalse(VersionUtil.isValid("1!2"));
	}

	@Test
	public void testParseVersion() {
		Version ver1 = VersionUtil.parseVersion("1.0.2");
		assertEquals(1, ver1.getMajorVersion());
		assertEquals(0, ver1.getMinorVersion());
		assertEquals(2, ver1.getIncrementalVersion());

		Version ver2 = VersionUtil.parseVersion("1.0-2");
		assertEquals(1, ver2.getMajorVersion());
		assertEquals(0, ver2.getMinorVersion());
		assertEquals(2, ver2.getBuildNumber());

		assertThrows(
				ApplicationVersionException.class,
				() -> VersionUtil.parseVersion("1!2")
		);
	}

	@Test
	public void testCompare() {
		assertEquals(-1, VersionUtil.compare("1.0.2", "1.0.3"));
		assertEquals(0, VersionUtil.compare("1.0.2", "1.0.2"));
		assertEquals(1, VersionUtil.compare("1.0.2", "0.0.5"));
	}
}
