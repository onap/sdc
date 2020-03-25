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

import java.util.LinkedList;
import java.util.List;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ArtifactTemplateInfoTest {

	private static final String TYPE = "TYPE";
	private static final String FILENAME = "FILENAME";
	private static final String ENV = "ENV";

	@Test
	public void shouldHaveValidGettersAndSetters() {
		assertThat(ArtifactTemplateInfo.class, hasValidGettersAndSetters());
	}

	@Test
	public void testDefaultCtor() {
		assertThat(ArtifactTemplateInfo.class, hasValidBeanConstructor());
	}

	@Test
	public void testCtor() {
		List<ArtifactTemplateInfo> artifactsInfo = new LinkedList<>();
		ArtifactTemplateInfo artifactTemplateInfo = new ArtifactTemplateInfo(TYPE, FILENAME, ENV, artifactsInfo);
		assertThat(artifactTemplateInfo.getType(), is(TYPE));
		assertThat(artifactTemplateInfo.getFileName(), is(FILENAME));
		assertThat(artifactTemplateInfo.getEnv(), is(ENV));
		assertThat(artifactTemplateInfo.getRelatedArtifactsInfo(), is(artifactsInfo));
	}
}