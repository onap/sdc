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

package org.onap.sdc.frontend.ci.tests.utilities;

import com.aventstack.extentreports.Status;
import org.onap.sdc.frontend.ci.tests.datatypes.CreateAndImportButtonsEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.List;

public final class HomeUtils {

    public static WebElement createAndImportButtons(CreateAndImportButtonsEnum type, WebDriver driver)
            throws InterruptedException {
        switch (type) {
            case IMPORT_CP:
            case IMPORT_VFC:
            case IMPORT_VL:
            case IMPORT_VF:
                GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.Dashboard.IMPORT_AREA.getValue());
                return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.IMPORT_VFC.getValue());
            case CREATE_SERVICE:
                GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.Dashboard.ADD_AREA.getValue());
                GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue()).click();
                break;

            case CREATE_PRODUCT:
                GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue()).click();
                GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue()).click();
                break;

            default:
                GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.Dashboard.ADD_AREA.getValue());
                driver.findElement(By.xpath("//*[@data-tests-id='createResourceButton']")).click();
                break;
        }
        return null;

    }

    public static void findComponentAndClick(String componentName) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "finding component " + componentName);
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue()).sendKeys(componentName);
        WebElement foundComp = null;
        try {
            foundComp = GeneralUIUtils.getWebElementByTestID(componentName);
            foundComp.click();
            GeneralUIUtils.waitForLoader();
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue());
        } catch (Exception e) {
            String msg = String.format("DID NOT FIND A COMPONENT NAMED %s", componentName);
            SetupCDTest.getExtendTest().log(Status.FAIL, msg);
            System.out.println(msg);
            Assert.fail(msg);
        }
    }

    public static void findComponentAndClickByVersion(String componentName, String version) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("finding component %s v%s", componentName, version));
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue()).sendKeys(componentName);
        List<WebElement> foundComp = null;
        try {
            foundComp = GeneralUIUtils.getWebElementsListByTestID(componentName + "Version");

            for (WebElement webElement : foundComp) {
                if (webElement.getText().contains(version)) {
                    webElement.click();
                    GeneralUIUtils.ultimateWait();
                    break;
                }
            }
        } catch (Exception e) {
            String msg = String.format("DID NOT FIND A COMPONENT NAMED %s", componentName);
            SetupCDTest.getExtendTest().log(Status.FAIL, msg);
            System.out.println(msg);
            Assert.fail(msg);
        }
    }
}
