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

package org.openecomp.sdc.ci.tests.pages;

import java.util.List;

import javax.lang.model.util.Elements;

import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class InformationalArtifactPage extends DeploymentArtifactPage {

	public InformationalArtifactPage() {
		super();
	}

	public static void clickAddNewArtifact() {
		addNewArtifact(ArtifactGroupTypeEnum.INFORMATIONAL);
	}

	public static String getArtifactDescription(String artifactName) throws Exception {
		clickOnArtifact(artifactName);
		String artifactDesc = GeneralUIUtils.getWebElementWaitForVisible(
				artifactName + DataTestIdEnum.ArtifactPageEnum.GET_INFORMATIONAL_ARTIFACT_DESCRIPTION.getValue())
				.getText();
		clickOnArtifact(artifactName); // close artifact
		return artifactDesc;
	}

	public static List<WebElement> getElemenetsFromTable() {
		return GeneralUIUtils.getWebElementsListByDataTestId("InformationalArtifactRow");
	}

}
