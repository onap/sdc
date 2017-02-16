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

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openqa.selenium.WebElement;

public class DeploymentArtifactPage extends GeneralPageElements {

	public DeploymentArtifactPage() {
		super();
	}

	public static ResourceLeftMenu getLeftPanel() {
		return new ResourceLeftMenu();
	}

	public static UploadArtifactPopup artifactPopup() {
		return new UploadArtifactPopup();
	}

	protected static void addNewArtifact(ArtifactGroupTypeEnum artifactGroupType) {
		switch (artifactGroupType) {
		case DEPLOYMENT:
			GeneralUIUtils.getWebButton(DataTestIdEnum.ArtifactPageEnum.ADD_DEPLOYMENT_ARTIFACT.getValue()).click();
			break;
		case INFORMATIONAL:
			GeneralUIUtils.getWebButton(DataTestIdEnum.ArtifactPageEnum.ADD_INFORMATIONAL_ARTIFACT.getValue()).click();
			break;
		default:
			break;
		}
	}

	public static void clickAddNewArtifact() {
		addNewArtifact(ArtifactGroupTypeEnum.DEPLOYMENT);
	}

	public static void clickAddAnotherArtifact() {
		GeneralUIUtils.getWebButton(DataTestIdEnum.ArtifactPageEnum.ADD_ANOTHER_ARTIFACT.getValue()).click();
	}

	public static void clickEditArtifact(String artifactLabel) {
		GeneralUIUtils.getWebButton(DataTestIdEnum.ArtifactPageEnum.EDIT_ARTIFACT.getValue() + artifactLabel).click();
	}

	public static void clickDeleteArtifact(String artifactLabel) {
		GeneralUIUtils.getWebButton(DataTestIdEnum.ArtifactPageEnum.DELETE_ARTIFACT.getValue() + artifactLabel).click();
	}

	public static void clickDownloadArtifact(String artifactLabel) {
		GeneralUIUtils.getWebButton(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT.getValue() + artifactLabel)
				.click();
	}

	public static String getArtifactDescription(String artifactLabel) throws Exception {
		clickOnArtifact(artifactLabel); // open artifact
		WebElement artifactDescriptionElement = GeneralUIUtils.getWebElementWaitForVisible(
				DataTestIdEnum.ArtifactPageEnum.GET_DEPLOYMENT_ARTIFACT_DESCRIPTION.getValue());
		String artifactDesc = artifactDescriptionElement.getText();
		clickOnArtifact(artifactLabel); // close artifact

		return artifactDesc;
	}

	public static void clickOnArtifact(String artifactLabel) throws Exception {
		GeneralUIUtils.getWebButton(artifactLabel).click();
		GeneralUIUtils.waitForLoader();
	}

}
