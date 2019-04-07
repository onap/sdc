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

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.model.ArtifactDefinition;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ArtifactDefinitionInfoTest {

	@Mock
	private ArtifactDefinition artifactDefinition;

	private static final String VERSION = "VERSION";
	private static final String DISPLAY_NAME = "DISPLAY NAME";
	private static final String UUID = "1";
	private static final byte[] PAYLOAD_DATA = "Test".getBytes();
	private static final String NAME = "Name";

	@Test
	public void shouldHaveValidGettersAndSetters() {
		assertThat(ArtifactDefinitionInfo.class, hasValidGettersAndSetters());
	}

	@Test
	public void testCtor() {
		Mockito.when(artifactDefinition.getPayloadData()).thenReturn(PAYLOAD_DATA);
		Mockito.when(artifactDefinition.getArtifactName()).thenReturn(NAME);
		Mockito.when(artifactDefinition.getArtifactDisplayName()).thenReturn(DISPLAY_NAME);
		Mockito.when(artifactDefinition.getArtifactVersion()).thenReturn(VERSION);
		Mockito.when(artifactDefinition.getArtifactUUID()).thenReturn(UUID);

		ArtifactDefinitionInfo artifactDefinitionInfo = new ArtifactDefinitionInfo(artifactDefinition);
		assertThat(artifactDefinitionInfo.getArtifactDisplayName(), is(DISPLAY_NAME));
		assertThat(artifactDefinitionInfo.getArtifactName(), is(NAME));
		assertThat(artifactDefinitionInfo.getArtifactVersion(), is(VERSION));
		assertThat(artifactDefinitionInfo.getArtifactUUID(), is(UUID));

	}


}