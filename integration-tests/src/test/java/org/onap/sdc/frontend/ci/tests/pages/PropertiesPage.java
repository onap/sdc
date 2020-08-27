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

import java.util.List;

public class PropertiesPage extends GeneralPageElements {

    private static final int WAIT_FOR_ELEMENT_TIME_OUT = 10;

    private PropertiesPage() {
        super();
    }

    public static List<WebElement> getElemenetsFromTable() {
        return GeneralUIUtils.getInputElements(DataTestIdEnum.PropertiesPageEnum.PROPERTY_ROW.getValue());
    }

    public static void clickAddPropertyArtifact() {
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPageEnum.ADD_NEW_PROPERTY.getValue()).click();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPageEnum.POPUP_FORM.getValue());
    }

    public static void clickEditPropertyArtifact(String propertyName) {
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPageEnum.EDIT_PROPERTY.getValue() + propertyName).click();
    }

    public static void clickDeletePropertyArtifact(String propertyName) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Delete property %s", propertyName));
        GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.PropertiesPageEnum.PROPERTY_NAME.getValue() + propertyName);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesPageEnum.DELETE_PROPERTY.getValue() + propertyName);
        GeneralPageElements.clickOKButton();
        GeneralUIUtils.waitForElementInVisibilityBy(By.className("w-sdc-modal-confirmation"), WAIT_FOR_ELEMENT_TIME_OUT);
    }

    public static void clickDeletePropertyFromPopup(String propertyName) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Delete property %s", propertyName));
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesPageEnum.PROPERTY_NAME.getValue() + propertyName);
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.PropertiesPageEnum.DELETE_PROPERTY_POPUP.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
    }

    public static void clickOnProperty(String propertyName) {
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPageEnum.PROPERTY_NAME.getValue() + propertyName).click();
    }

    public static PropertyPopup getPropertyPopup() {
        return new PropertyPopup();
    }

    public static boolean verifyTotalProperitesField(int count) {
        String totalPropertiesCount = GeneralUIUtils.getWebElementBy(By.id("properties-count")).getText();
        return ("Total Properties: " + count).equals(totalPropertiesCount);
    }


}
