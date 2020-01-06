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

package org.openecomp.sdc.ci.tests.utilities;

import com.aventstack.extentreports.Status;
import java.io.File;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.Dashboard;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public final class ResourceUIUtils {

    private static final int BASIC_TIMEOUT = 10 * 60;

    private ResourceUIUtils() {
    }

    public static void fillResourceGeneralInformationPage(ResourceReqDetails resource, boolean isNewResource) {
        try {
            ResourceGeneralPage.defineName(resource.getName());
            ResourceGeneralPage.defineDescription(resource.getDescription());
            ResourceGeneralPage.defineCategory(resource.getCategories().get(0).getSubcategories().get(0).getName());
            ResourceGeneralPage.defineVendorName(resource.getVendorName());
            ResourceGeneralPage.defineVendorRelease(resource.getVendorRelease());
            if (isNewResource) {
                ResourceGeneralPage.defineTagsList(resource, new String[]{"This-is-tag", "another-tag", "Test-automation-tag"});
            } else {
                ResourceGeneralPage.defineTagsList(resource, new String[]{"one-more-tag"});
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void createVF(ResourceReqDetails resource, User user) {
        ExtentTestActions.log(Status.INFO, "Going to create a new VF.");
        createResource(resource, user, Dashboard.BUTTON_ADD_VF);
    }

    private static void createResource(ResourceReqDetails resource, User user, Dashboard button) {
        WebElement addVFButton;
        try {
            GeneralUIUtils.ultimateWait();
            try {
                GeneralUIUtils.hoverOnAreaByClassName("w-sdc-dashboard-card-new");
                addVFButton = GeneralUIUtils.getWebElementByTestID(button.getValue());
            } catch (Exception e) {
                File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Warning_" + resource.getName());
                final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
                SetupCDTest.getExtendTest().log(Status.WARNING, "Add button is not visible after hover on import area of Home page, moving on ..." + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath));
                showButtonsAdd();
                addVFButton = GeneralUIUtils.getWebElementByTestID(button.getValue());
            }
            addVFButton.click();
            GeneralUIUtils.ultimateWait();
        } catch (Exception e) {
            SetupCDTest.getExtendTest().log(Status.WARNING, "Exeption catched on ADD button, retrying ... ");
            GeneralUIUtils.hoverOnAreaByClassName("w-sdc-dashboard-card-new");
            GeneralUIUtils.ultimateWait();
            GeneralUIUtils.getWebElementByTestID(button.getValue()).click();
            GeneralUIUtils.ultimateWait();
        }
        fillResourceGeneralInformationPage(resource, true);
        resource.setVersion("0.1");
        GeneralPageElements.clickCreateButton();
    }

    private static void showButtonsAdd() {
        try {
            GeneralUIUtils.ultimateWait();
            String parentElementClassAttribute = "sdc-dashboard-create-element-container";
            WebElement fileInputElementWithVisible = GeneralUIUtils.getDriver().findElement(By.className(parentElementClassAttribute));
            GeneralUIUtils.unhideElement(fileInputElementWithVisible, parentElementClassAttribute);
            GeneralUIUtils.ultimateWait();
        } catch (Exception e) {
            GeneralUIUtils.ultimateWait();
            String parentElementClassAttribute = "sdc-dashboard-create-element-container";
            WebElement fileInputElementWithVisible = GeneralUIUtils.getDriver().findElement(By.className(parentElementClassAttribute));
            GeneralUIUtils.unhideElement(fileInputElementWithVisible, parentElementClassAttribute);
            GeneralUIUtils.ultimateWait();
        }
        SetupCDTest.getExtendTest().log(Status.WARNING, "Input buttons now visible...");
    }

}
