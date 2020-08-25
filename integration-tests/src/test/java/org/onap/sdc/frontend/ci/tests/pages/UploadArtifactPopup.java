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

package org.onap.sdc.frontend.ci.tests.pages;

import com.aventstack.extentreports.Status;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.io.File;

public class UploadArtifactPopup {

    private static final int WAITING_FOR_ELEMENT_TIME_OUT = 10;
    private boolean isCompositionPage;

    public UploadArtifactPopup(boolean isCompositionPage) {
        super();
        this.isCompositionPage = isCompositionPage;
    }

    public UploadArtifactPopup() {
        super();
    }

    private WebElement getArtifactDescriptionWebElement() {
        return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPopup.ARTIFACT_DESCRIPTION.getValue());
    }

    public void loadFile(String path, String filename) {
        final WebElement browseWebElement = GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPopup.BROWSE.getValue());
        browseWebElement.sendKeys(path + File.separator + filename);
        GeneralUIUtils.ultimateWait();
    }

    public void loadUndefinedFile(String path, String filename) {
        final WebElement browseWebElement = GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPopup.FILE_UNDEFINED.getValue());
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Uploading file: %s", path + File.separator + filename));
        browseWebElement.sendKeys(path + File.separator + filename);
        GeneralUIUtils.ultimateWait();
    }


    public void insertDescription(String artifactDescriptoin) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Changing artifact description to: %s", artifactDescriptoin));
        WebElement artifactDescriptionTextbox = getArtifactDescriptionWebElement();
        artifactDescriptionTextbox.clear();
        artifactDescriptionTextbox.sendKeys(artifactDescriptoin);

        GeneralUIUtils.ultimateWait();
    }

    public Select defineArtifactLabel(String requiredArtifactLabel) {
        Select selectList = null;
        WebElement artifactLabelWebElement;

        artifactLabelWebElement = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPopup.ARTIFACT_LABEL.getValue());


        artifactLabelWebElement.clear();
        artifactLabelWebElement.sendKeys(requiredArtifactLabel);
        return selectList;
    }

    public void selectArtifactType(String artifactType) {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPopup.ARTIFACT_TYPE_ICON.getValue());
        GeneralUIUtils.clickOnElementByTestId(artifactType);
    }

    public void clickDoneButton() throws Exception {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPopup.DONE_BUTTON.getValue());
        GeneralUIUtils.waitForLoader();
        GeneralUIUtils.waitForElementInVisibilityBy(By.className("sdc-add-artifact"), WAITING_FOR_ELEMENT_TIME_OUT);
    }

    public void clickUpgradeArtifactsButton() throws Exception {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.UPGRADE_ARTIFACT_BUTTON.getValue());
        GeneralUIUtils.waitForLoader();
        GeneralUIUtils.waitForElementInVisibilityBy(By.className("sdc-add-artifact"), WAITING_FOR_ELEMENT_TIME_OUT);
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
        artifactDescriptionTextbox.sendKeys(artifactDescriptoin);
    }

    public WebElement getArtifactURLWebElement() {
        return GeneralUIUtils.getWebElementBy(By.cssSelector((DataTestIdEnum.ArtifactPopup.URL.getValue())));
    }


}
