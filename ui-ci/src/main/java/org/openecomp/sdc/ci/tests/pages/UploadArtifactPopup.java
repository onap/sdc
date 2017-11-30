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

import java.io.File;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.aventstack.extentreports.Status;

public class UploadArtifactPopup {

	boolean isCompositionPage;
	
	public UploadArtifactPopup(boolean isCompositionPage) {
		super();
		this.isCompositionPage = isCompositionPage;
	}
	
	public UploadArtifactPopup() {
		super();
	}
	
	public WebElement getArtifactDescriptionWebElement(){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPopup.ARTIFACT_DESCRIPTION.getValue());
	}

	public void loadFile(String path, String filename) {
		final WebElement browseWebElement = GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPopup.BROWSE.getValue());
//		browseWebElement.sendKeys(path + filename);
		browseWebElement.sendKeys(path + File.separator + filename);
		GeneralUIUtils.ultimateWait();
		
//		if (!browseWebElement.getAttribute("value").equals(filename))
//		{
//			throw new TestException("File named " + filename + " does not presented");
//		}
	}
	


	public void insertDescription(String artifactDescriptoin) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Changing artifact description to: %s", artifactDescriptoin));  
		WebElement artifactDescriptionTextbox = getArtifactDescriptionWebElement();
		artifactDescriptionTextbox.clear();
		artifactDescriptionTextbox.sendKeys(artifactDescriptoin);
        
		GeneralUIUtils.ultimateWait();;
	}

	public Select defineArtifactLabel(String requiredArtifactLabel) {
		Select selectList = null;
		WebElement artifactLabelWebElement = null;
		
//		if (isCompositionPage){
			artifactLabelWebElement = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPopup.ARTIFACT_LABEL.getValue());
//		}
//		else{
//			selectList = GeneralUIUtils.getSelectList("Create New Artifact", DataTestIdEnum.ArtifactPopup.ARTIFACT_LABEL.getValue());
//			artifactLabelWebElement = GeneralUIUtils.getDriver().findElement(By.name(DataTestIdEnum.ArtifactPopup.ARTIFACT_LABEL.getValue()));	
//		}
		
		artifactLabelWebElement.clear();
		artifactLabelWebElement.sendKeys(requiredArtifactLabel);
		return selectList;
	}

	public Select selectArtifactType(String artifactType) {
		return GeneralUIUtils.getSelectList(artifactType, DataTestIdEnum.ArtifactPopup.ARTIFACT_TYPE.getValue());
	}

	public void clickDoneButton() throws Exception {
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPopup.DONE_BUTTON.getValue());
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.waitForElementInVisibilityBy(By.className("sdc-add-artifact"), 10);
	}

	public void clickCancelButton() throws Exception {
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPopup.CANCEL_BUTTON.getValue()).click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.waitForElementInVisibilityByTestId("sdc-add-artifact");
	}

//	public void clickUpdateButton() throws Exception {
//		clickAddButton();
//		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPopup.UPDATE_BUTTON.getValue()).click();
//		GeneralUIUtils.waitForLoader();
//		GeneralUIUtils.waitForElementInVisibilityByTestId(By.className("sdc-add-artifact"), 50);
//	}
	
	public void insertURL(String artifactDescriptoin) throws Exception {
		WebElement artifactDescriptionTextbox = getArtifactURLWebElement();
		artifactDescriptionTextbox.clear();
		artifactDescriptionTextbox. sendKeys(artifactDescriptoin);		
	}
	
	public WebElement getArtifactURLWebElement(){
		return GeneralUIUtils.getWebElementBy(By.cssSelector((DataTestIdEnum.ArtifactPopup.URL.getValue())));
	}
	

}
