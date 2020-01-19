/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.info;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NodeTypeInfoToUpdateArtifactsTest {

	private static final String NAME = "NAME";
	private static final Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> NODE_TYPES_ARTIFACTS_TO_HANDLE = Collections
		.emptyMap();

	@Test
	public void shouldHaveValidGettersAndSetters() {
		assertThat(NodeTypeInfoToUpdateArtifacts.class, hasValidGettersAndSetters());
	}

	@Test
	public void shouldConstructCorrectObject() {
		NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(NAME,
			NODE_TYPES_ARTIFACTS_TO_HANDLE);
		assertThat(nodeTypeInfoToUpdateArtifacts.getNodeName(), is(NAME));
		assertThat(nodeTypeInfoToUpdateArtifacts.getNodeTypesArtifactsToHandle(), is(NODE_TYPES_ARTIFACTS_TO_HANDLE));
	}
}