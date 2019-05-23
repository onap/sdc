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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.utils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommonBeUtilsTest {

	@Test
	public void testCompareAsdcComponentVersions() {
		assertTrue(CommonBeUtils.compareAsdcComponentVersions("1.1", "0.15"));
		assertFalse(CommonBeUtils.compareAsdcComponentVersions("0.5", "0.5"));
		assertFalse(CommonBeUtils.compareAsdcComponentVersions("0.5", "0.6"));
		assertFalse(CommonBeUtils.compareAsdcComponentVersions("1.5", "2.6"));
		assertTrue(CommonBeUtils.compareAsdcComponentVersions("0.10", "0.1"));
		assertFalse(CommonBeUtils.compareAsdcComponentVersions("1", "1.0"));
		assertTrue(CommonBeUtils.compareAsdcComponentVersions("2", "1.15"));
	}
	
	@Test
	public void testConformanceLevelCompare() {
		assertTrue(CommonBeUtils.conformanceLevelCompare("1.1", "0.15") > 0);
		assertEquals(0, CommonBeUtils.conformanceLevelCompare("0.5", "0.5"));
		assertTrue(CommonBeUtils.conformanceLevelCompare("0.5", "0.6") < 0);
		assertTrue(CommonBeUtils.conformanceLevelCompare("1.5", "2.6") < 0);
		assertTrue(CommonBeUtils.conformanceLevelCompare("1.5", "1.5.3") < 0);
		assertTrue(CommonBeUtils.conformanceLevelCompare("2.5", "1.5.300") > 0);
		assertTrue(CommonBeUtils.conformanceLevelCompare("0.10", "0.1") > 0);
		assertTrue(CommonBeUtils.conformanceLevelCompare("2", "1.15") > 0);
	}

	@Test
	public void testGenerateToscaResourceName(){
		assertEquals(CommonBeUtils.generateToscaResourceName("ANY_RESOURCE", "ANY_RESOURCE_SYSTEM_NAME"),
			"org.openecomp.resource.any_resource.ANY_RESOURCE_SYSTEM_NAME");
	}
}
