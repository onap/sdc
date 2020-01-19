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

package org.openecomp.sdc.be.datamodel.utils;


import org.junit.Test;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.operations.impl.ArtifactOperation;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArtifactUtilsTest {

	private static final String ARTIFACT_NAME = "myHeatArtifact";


	@javax.annotation.Resource
	private ArtifactOperation artifactOperation;


	private static String RESOURCE_ID = "resourceId";

	private static String USER_ID = "muUserId";
	private static String CATEGORY_NAME = "category/mycategory";


	private ArtifactUtils createTestSubject() {
		return new ArtifactUtils();
	}

	@Test
	public void testFindMasterArtifact() throws Exception {
		Map<String, ArtifactDefinition> deploymentArtifact = new HashMap<>();
		List<ArtifactDefinition> artifacts = new LinkedList<>();
		List<String> artifactsList = new LinkedList<>();
		ArtifactDefinition result;

		// default test
		result = ArtifactUtils.findMasterArtifact(deploymentArtifact, artifacts, artifactsList);
	}

	@Test
	public void testFindMasterArtifactWithArtifactDef() throws Exception {
		ArtifactDefinition artifactWithHeat = createResourceWithHeat();
		Map<String, ArtifactDefinition> deploymentArtifact = new HashMap<>();
		deploymentArtifact.put("artifactId",artifactWithHeat);
		List<ArtifactDefinition> artifacts = new LinkedList<>();
		artifacts.add(artifactWithHeat);
		List<String> artifactsList = new LinkedList<>();
		artifactsList.add("artifactId");
		ArtifactDefinition result;

		// default test
		result = ArtifactUtils.findMasterArtifact(deploymentArtifact, artifacts, artifactsList);
	}

	@Test
	public void testBuildJsonForUpdateArtifact() throws Exception {
		String artifactId = "";
		String artifactName = "";
		String artifactType = "";
		ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.DEPLOYMENT;
		String label = "";
		String displayName = "";
		String description = "";
		byte[] artifactContentent = new byte[] { ' ' };
		List<ArtifactTemplateInfo> updatedRequiredArtifacts = null;
		boolean isFromCsar = false;
		Map<String, Object> result;

		// test 1
		artifactId = null;
		result = ArtifactUtils.buildJsonForUpdateArtifact(artifactId, artifactName, artifactType, artifactGroupType,
				label, displayName, description, artifactContentent, updatedRequiredArtifacts, isFromCsar);

		// test 2
		/*artifactId = "";
		result = ArtifactUtils.buildJsonForUpdateArtifact(artifactId, artifactName, artifactType, artifactGroupType,
				label, displayName, description, artifactContentent, updatedRequiredArtifacts, isFromCsar);
		Assert.assertEquals(null, result);*/
	}

	@Test
	public void testBuildJsonForArtifact() throws Exception {
		ArtifactTemplateInfo artifactTemplateInfo = new ArtifactTemplateInfo();
		artifactTemplateInfo.setFileName("mock.mock.heat");
		byte[] artifactContentent = new byte[] { ' ' };
		int atrifactLabelCounter = 0;
		Map<String, Object> result;

		// default test
		result = ArtifactUtils.buildJsonForArtifact(artifactTemplateInfo, artifactContentent, atrifactLabelCounter,false);
	}

	@Test
	public void testFindArtifactInList() throws Exception {
		List<ArtifactDefinition> createdArtifacts = new LinkedList<>();
		String artifactId = "mock";
		ArtifactDefinition result;

		// default test
		result = ArtifactUtils.findArtifactInList(createdArtifacts, artifactId);
	}

    @Test
    public void testFindArtifactInListwithArtifactList() throws Exception {
        ArtifactDefinition artifactWithHeat = createResourceWithHeat();
        List<ArtifactDefinition> createdArtifacts = new LinkedList<>();
        createdArtifacts.add(artifactWithHeat);
        String artifactId = "artifactId";
        ArtifactDefinition result;

        // default test
        result = ArtifactUtils.findArtifactInList(createdArtifacts, artifactId);
    }

	public ArtifactDefinition createResourceWithHeat() {
		ArtifactDefinition artifactDefinition = createArtifactDefinition(USER_ID, RESOURCE_ID, ARTIFACT_NAME);
		artifactDefinition.setArtifactType("HEAT");
		artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);

		List<HeatParameterDefinition> heatParams = new ArrayList<>();
		HeatParameterDefinition heatParam = new HeatParameterDefinition();
		heatParam.setCurrentValue("11");
		heatParam.setDefaultValue("22");
		heatParam.setDescription("desc");
		heatParam.setName("myParam");
		heatParam.setType("number");
		heatParams.add(heatParam);
		artifactDefinition.setListHeatParameters(heatParams);
		return artifactDefinition;

	}


	private ArtifactDefinition createArtifactDefinition(String userId, String serviceId, String artifactName) {
		ArtifactDefinition artifactInfo = new ArtifactDefinition();

		artifactInfo.setArtifactName(artifactName + ".sh");
		artifactInfo.setArtifactType("SHELL");
		artifactInfo.setDescription("hdkfhskdfgh");
		artifactInfo.setArtifactChecksum("UEsDBAoAAAAIAAeLb0bDQz");

		artifactInfo.setUserIdCreator(userId);
		String fullName = "Jim H";
		artifactInfo.setUpdaterFullName(fullName);
		long time = System.currentTimeMillis();
		artifactInfo.setCreatorFullName(fullName);
		artifactInfo.setCreationDate(time);
		artifactInfo.setLastUpdateDate(time);
		artifactInfo.setUserIdLastUpdater(userId);
		artifactInfo.setArtifactLabel(artifactName);
		artifactInfo.setUniqueId("artifactId");
		return artifactInfo;
	}

}
