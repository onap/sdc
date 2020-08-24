/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.sdc.backend.ci.tests.execute.devCI;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.utils.validation.CsarValidationUtils;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.BaseRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResourceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResponseParser;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;

public class ImportCsarValidateArtifacts extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();

	@DataProvider(name = "happyArts")
	public Object[][] getHappyArtifacts() {

		return new Object[][] { { "happy_VF_RI2_G2_two_different_artifacts_under_heatBaseheatVolheatNet2" },
				{ "happy_VF_RI2_G2_two_different_artifacts_under_heatBaseheatVolheatNet" },
				{ "happy_VF_RI2_G2_two_identical_artifacts_under_heatBaseheatVolheatNet" },
				{ "happy_VF_RI2_G2_two_different_artifacts_under_nested" },
				{ "happy_VF_RI2_G2_two_indentical_nested_under_different_groups" },
				{ "happy_VF_RI2_G2_two_different_nested_under_different_groups" },
				{ "happy_VF_RI2_G2_two_different_nested_under_same_group" },

		};
	}

	@DataProvider(name = "negativeArts")
	public Object[][] getNegativeArtifacts() {

		return new Object[][] {

				{ "negative_VF_RI2_G2_same_heatVol_different_groups" },
				{ "negative_VF_RI2_G2_same_heatBase_different_envs" },
				{ "negative_VF_RI2_G2_heatBaseHeatVolHeatNet_under_nested" },
				{ "negative_VF_RI2_G2_two_indentical_artifacts_under_nested" },
				{ "negative_VF_RI2_G2_nested_under_nested" }, { "negative_VF_RI2_G2_same_heatVol_different_groups" }, };
	}

	@Test(dataProvider = "happyArts")
	public void createResourceFromCsarArtsHappy(String artifactName) throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(artifactName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));

		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		CsarValidationUtils.validateCsarVfArtifact(artifactName, resource);

	}

	@Test(dataProvider = "negativeArts")
	public void createResourceFromCsarArtsNegative(String artifactName) throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(artifactName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		assertTrue(createResource.getErrorCode() != 201 && createResource.getErrorCode() != 500);

	}

}
