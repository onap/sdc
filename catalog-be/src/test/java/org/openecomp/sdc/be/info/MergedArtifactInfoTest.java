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

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.ArtifactDefinition;

public class MergedArtifactInfoTest {

	private static final String NAME = "NAME";
	private static final String FILE = "FILE";
	private static final String ENV = "ENV";
	private static final String URL = "URL";
	private static final String CHECKSUM = "CHECKSUM";
	private static final String CREATOR = "CREATOR";
	private static final String DISPLAY_NAME = "DISPLAY_NAME";
	private static final String LABEL = "LABEL";
	private static final String REF = "REF";
	private static final String REPOSITORY = "REPOSITORY";
	private static final String TYPE = "TYPE";
	private static final String UUID = "UUID";
	private static final String ES_ID = "ES_ID";
	private static final String UNIQUE_ID = "UNIQUE_ID";
	private static final String GENERATED_FROM = "GENERATED_FROM";

	@Test
	public void shouldHaveValidDefaultConstructor() {
		assertThat(MergedArtifactInfo.class, hasValidBeanConstructor());
	}

	@Test
	public void shouldCorrectlySetCreatedArtifact() {
		MergedArtifactInfo mergedArtifactInfo = createMergedArtifactInfo();
		assertJsonArtifactTemplate(mergedArtifactInfo.getJsonArtifactTemplate());
		assertCreatedArtifact(mergedArtifactInfo.getCreatedArtifact());
	}

	@Test
	public void shouldReturnListToDisassociateArtifactFromGroup() {
		MergedArtifactInfo mergedArtifactInfo = createMergedArtifactInfo();
		mergedArtifactInfo.getCreatedArtifact().get(0).setToscaPresentationValue(JsonPresentationFields.GENERATED_FROM_ID,
			GENERATED_FROM);
		List<ArtifactDefinition> listToDissotiateArtifactFromGroup = mergedArtifactInfo
			.getListToDissotiateArtifactFromGroup(Collections.emptyList());
		assertThat(listToDissotiateArtifactFromGroup.size(), is(1));
		assertThat(listToDissotiateArtifactFromGroup.get(0).getUniqueId(), is(UNIQUE_ID));
	}

	@Test
	public void shouldReturnEmptyListToDisassociateArtifactFromGroupWhenGeneratedFormIdIsEmpty() {
		MergedArtifactInfo mergedArtifactInfo = createMergedArtifactInfo();
		List<ArtifactDefinition> artifactDefinitions = new ArrayList<>();
		artifactDefinitions.add(new ArtifactDefinition(createArtifactDataDefinition()));
		List<ArtifactDefinition> listToDissotiateArtifactFromGroup = mergedArtifactInfo
			.getListToDissotiateArtifactFromGroup(artifactDefinitions);
		assertThat(listToDissotiateArtifactFromGroup.isEmpty(), is(true));
	}

	private MergedArtifactInfo createMergedArtifactInfo() {
		MergedArtifactInfo mergedArtifactInfo = new MergedArtifactInfo();
		List<ArtifactTemplateInfo> relatedArtifactsInfo = new ArrayList<>();
		relatedArtifactsInfo.add(new ArtifactTemplateInfo(TYPE, FILE, ENV, Collections.emptyList()));
		mergedArtifactInfo.setJsonArtifactTemplate(new ArtifactTemplateInfo(NAME, FILE, ENV, relatedArtifactsInfo));
		List<ArtifactDefinition> createdArtifact = new ArrayList<>();
		createdArtifact.add(new ArtifactDefinition(createArtifactDataDefinition()));
		mergedArtifactInfo.setCreatedArtifact(createdArtifact);
		return mergedArtifactInfo;
	}

	private ArtifactDataDefinition createArtifactDataDefinition() {
		ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
		artifactDataDefinition.setApiUrl(URL);
		artifactDataDefinition.setUniqueId(UNIQUE_ID);
		artifactDataDefinition.setArtifactChecksum(CHECKSUM);
		artifactDataDefinition.setArtifactCreator(CREATOR);
		artifactDataDefinition.setArtifactDisplayName(DISPLAY_NAME);
		artifactDataDefinition.setArtifactLabel(LABEL);
		artifactDataDefinition.setArtifactName(NAME);
		artifactDataDefinition.setArtifactRef(REF);
		artifactDataDefinition.setArtifactRepository(REPOSITORY);
		artifactDataDefinition.setArtifactType(TYPE);
		artifactDataDefinition.setArtifactUUID(UUID);
		artifactDataDefinition.setEsId(ES_ID);
		return artifactDataDefinition;
	}

	private void assertCreatedArtifact(List<ArtifactDefinition> artifactDefinitions){
		assertThat(artifactDefinitions.size(), is(1));
		ArtifactDefinition artifactDefinition = artifactDefinitions.get(0);
		assertThat(artifactDefinition.getArtifactType(), is(TYPE));
		assertThat(artifactDefinition.getArtifactRef(), is(REF));
		assertThat(artifactDefinition.getArtifactName(), is(NAME));
		assertThat(artifactDefinition.getArtifactRepository(), is(REPOSITORY));
		assertThat(artifactDefinition.getArtifactChecksum(), is(CHECKSUM));
		assertThat(artifactDefinition.getEsId(), is(ES_ID));
		assertThat(artifactDefinition.getArtifactLabel(), is(LABEL));
		assertThat(artifactDefinition.getArtifactCreator(), is(CREATOR));
		assertThat(artifactDefinition.getArtifactDisplayName(), is(DISPLAY_NAME));
		assertThat(artifactDefinition.getApiUrl(), is(URL));
		assertThat(artifactDefinition.getServiceApi(), is(false));
		assertThat(artifactDefinition.getArtifactUUID(), is(UUID));
	}

	private void assertJsonArtifactTemplate(ArtifactTemplateInfo artifactTemplateInfo){
		assertThat(artifactTemplateInfo.getType(), is(NAME));
		assertThat(artifactTemplateInfo.getFileName(), is(FILE));
		assertThat(artifactTemplateInfo.getEnv(), is(ENV));
	}
}