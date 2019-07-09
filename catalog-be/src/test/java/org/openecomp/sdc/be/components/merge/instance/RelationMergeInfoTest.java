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

package org.openecomp.sdc.be.components.merge.instance;

import org.junit.Test;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;

public class RelationMergeInfoTest {

	private RelationMergeInfo createTestSubject() {
		return new RelationMergeInfo("", "", "", new RequirementCapabilityRelDef());
	}

	@Test
	public void testGetCapReqType() throws Exception {
		RelationMergeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapReqType();
	}


	@Test
	public void testGetRelDef() throws Exception {
		RelationMergeInfo testSubject;
		RequirementCapabilityRelDef result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelDef();
	}



	@Test
	public void testGetCapReqName() throws Exception {
		RelationMergeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapReqName();
	}

}
