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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;


public class ArtifactTypeEnumTest {
	
	@Test
	public void testGetType() {
		assertThat("The artifact type should match", ArtifactTypeEnum.WORKFLOW.getType(), is("WORKFLOW"));
		assertThat("The artifact type should match", ArtifactTypeEnum.OTHER.getType(), is("OTHER"));
		assertThat("The artifact type should match", ArtifactTypeEnum.HEAT.getType(), is("HEAT"));
	}
	
	@Test
	public void testParse() {
		ArtifactTypeEnum actual = ArtifactTypeEnum.parse("HEAT");
		assertThat("The artifact type should not be null", actual, notNullValue());
		assertThat("The artifact type should match", actual, is(ArtifactTypeEnum.HEAT));
		actual = ArtifactTypeEnum.parse("OTHER");
		assertThat("The artifact type should not be null", actual, notNullValue());
		assertThat("The artifact type should match", actual, is(ArtifactTypeEnum.OTHER));
		actual = ArtifactTypeEnum.parse("WORKFLOW");
		assertThat("The artifact type should not be null", actual, notNullValue());
		assertThat("The artifact type should match", actual, is(ArtifactTypeEnum.WORKFLOW));
		actual = ArtifactTypeEnum.parse("anyNotKnownType");
		assertThat("The artifact type should be null", actual, nullValue());
	}

}
