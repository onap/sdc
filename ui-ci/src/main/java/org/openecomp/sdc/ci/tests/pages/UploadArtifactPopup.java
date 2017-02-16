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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class UploadArtifactPopup {

	public UploadArtifactPopup() {
		super();
	}

	public void loadFile(String path, String filename) {
		final WebElement browseWebElement = GeneralUIUtils
				.getWebElementByDataTestId(DataTestIdEnum.ArtifactPopup.BROWSE.getValue());
		browseWebElement.sendKeys(path + "\\" + filename);
	}

	public void insertDescription(String artifactDescriptoin) {
		WebElement artifactDescriptionTextbox = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.ArtifactPopup.ARTIFACT_DESCRIPTION.getValue());
		artifactDescriptionTextbox.clear();
		artifactDescriptionTextbox.sendKeys(artifactDescriptoin);
	}

	public Select defineArtifactLabel(String requiredArtifactLabel) {
		Select selectList = GeneralUIUtils.getSelectList("Create New Artifact",
				DataTestIdEnum.ArtifactPopup.ARTIFACT_LABEL.getValue());
		WebElement artifactLabelWebElement = GeneralUIUtils.getDriver().findElement(By.name("artifactLabel"));
		artifactLabelWebElement.clear();
		artifactLabelWebElement.sendKeys(requiredArtifactLabel);
		return selectList;
	}

	public Select selectArtifactType(String artifactType) {
		return GeneralUIUtils.getSelectList(artifactType, DataTestIdEnum.ArtifactPopup.ARTIFACT_TYPE.getValue());
	}

	public void clickAddButton() throws Exception {
		GeneralUIUtils.getWebButton(DataTestIdEnum.ArtifactPopup.ADD_BUTTON.getValue()).click();
		GeneralUIUtils.waitForLoader();
	}

	public void clickCancelButton() throws Exception {
		GeneralUIUtils.getWebButton(DataTestIdEnum.ArtifactPopup.CANCEL_BUTTON.getValue()).click();
		GeneralUIUtils.waitForLoader();
	}

	public void clickUpdateButton() throws Exception {
		GeneralUIUtils.getWebButton(DataTestIdEnum.ArtifactPopup.UPDATE_BUTTON.getValue()).click();
		GeneralUIUtils.waitForLoader();
	}

}
