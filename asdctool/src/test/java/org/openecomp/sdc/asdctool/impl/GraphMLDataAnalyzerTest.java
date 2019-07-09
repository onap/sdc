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

import org.junit.Test;

public class GraphMLDataAnalyzerTest {

	private GraphMLDataAnalyzer createTestSubject() {
		return new GraphMLDataAnalyzer();
	}

	@Test
	public void testAnalyzeGraphMLData() throws Exception {
		GraphMLDataAnalyzer testSubject;
		String[] args = new String[] { "export", "src/main/resources/config/janusgraph.properties", "./" };
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.analyzeGraphMLData(args);
	}
}
