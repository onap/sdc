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

package org.openecomp.sdc.common.api;

import java.util.List;

import org.junit.Test;


public class ArtifactTypeEnumTest {

	private ArtifactTypeEnum createTestSubject() {
		return ArtifactTypeEnum.AAI_SERVICE_MODEL;
	}

	
	@Test
	public void testGetType() throws Exception {
		ArtifactTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}
	
	@Test
	public void testFindType() throws Exception {
		String type = "";
		ArtifactTypeEnum result;

		// default test
		result = ArtifactTypeEnum.findType(type);
	}

	
	@Test
	public void testGetAllTypes() throws Exception {
		List<String> result;

		// default test
		result = ArtifactTypeEnum.getAllTypes();
	}
}
