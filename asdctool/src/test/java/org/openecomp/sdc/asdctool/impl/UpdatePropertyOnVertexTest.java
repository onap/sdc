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

package org.openecomp.sdc.asdctool.impl;

import org.janusgraph.core.JanusGraph;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class UpdatePropertyOnVertexTest {

	private UpdatePropertyOnVertex createTestSubject() {
		return new UpdatePropertyOnVertex();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testOpenGraph() throws Exception {
		UpdatePropertyOnVertex testSubject;
		String janusGraphFileLocation = "";
		JanusGraph result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.openGraph("");
	}

	@Test(expected=NullPointerException.class)
	public void testUpdatePropertyOnServiceAtLeastCertified() throws Exception {
		UpdatePropertyOnVertex testSubject;
		String janusGraphFile = "";
		Map<String, Object> keyValueToSet = null;
		List<Map<String, Object>> orCriteria = null;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.updatePropertyOnServiceAtLeastCertified(janusGraphFile, keyValueToSet, orCriteria);
	}
}
