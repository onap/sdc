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
import java.util.ArrayList;
import java.util.List;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.GeneralPageElements;
import org.onap.sdc.frontend.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class ServiceUIUtils {

    private ServiceUIUtils() {

    }

    private static void defineTagsList2(List<String> serviceTags) {
        WebElement serviceTagsTextbox = GeneralUIUtils.getWebElementByTestID("i-sdc-tag-input");
        for (String tag : serviceTags) {
            serviceTagsTextbox.clear();
            serviceTagsTextbox.sendKeys(tag);
            GeneralUIUtils.waitForAngular();
            serviceTagsTextbox.sendKeys(Keys.ENTER);
        }
    }

    public static void fillServiceGeneralPage(final ServiceReqDetails service) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Fill in metadata values in general page");
        ServiceGeneralPage.defineName(service.getName());
        ServiceGeneralPage.defineDescription(service.getDescription());
        ServiceGeneralPage.defineCategory(service.getCategories().get(0).getName());
        ServiceGeneralPage.defineServiceFunction(service.getServiceFunction());
        ServiceGeneralPage.defineNamingPolicy(service.getNamingPolicy());
        defineTagsList2(service.getTags());
        ServiceGeneralPage.defineContactId(service.getContactId());
        GeneralUIUtils.waitForElementInVisibilityBy(By.className("notification-container"), 10000);
        GeneralUIUtils.clickSomewhereOnPage();
    }

    public static void createService(ServiceReqDetails service) {
        clickAddService();
        fillServiceGeneralPage(service);
        GeneralPageElements.clickCreateButton();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("The service %s was created", service.getName()));
    }

    public static void setServiceCategory(ServiceReqDetails service, ServiceCategoriesEnum category) {
        CategoryDefinition categoryDefinition = new CategoryDefinition();
        categoryDefinition.setName(category.getValue());
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(categoryDefinition);
        service.setCategories(categories);
    }

    public static void createServiceWithDefaultTagAndUserId(ServiceReqDetails service, User user) {
        clickAddService();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Defining General Page fields"));
        ServiceGeneralPage.defineName(service.getName());
        ServiceGeneralPage.defineDescription(service.getDescription());
        ServiceGeneralPage.defineCategory(service.getCategories().get(0).getName());
        ServiceGeneralPage.defineProjectCode(service.getProjectCode());
        ServiceGeneralPage.defineInstantiationType(service.getInstantiationType());
        GeneralUIUtils.ultimateWait();
        GeneralPageElements.clickCreateButton();
        SetupCDTest.getExtendTest().log(Status.INFO, "Done creating service over the UI, "
                + "about to move into Tosca Artifacts section.");
    }

    public static void clickAddService() {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking the Add Service button"));
        try {
            GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.Dashboard.ADD_AREA.getValue());
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue()).click();
            //GeneralUIUtils.hoverAndClickOnButtonByTestId(DataTestIdEnum.Dashboard.ADD_AREA.getValue(),DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue());
            GeneralUIUtils.ultimateWait();
        } catch (Exception e) {
            SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exception on catched on Add Service button, retrying ..."));
            GeneralUIUtils.hoverOnAreaByClassName("w-sdc-dashboard-card-new");
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue()).click();
            GeneralUIUtils.ultimateWait();
        }
    }
}
