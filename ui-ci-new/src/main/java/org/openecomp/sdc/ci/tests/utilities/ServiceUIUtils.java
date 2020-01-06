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
import java.util.ArrayList;
import java.util.List;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServiceUIUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUIUtils.class);

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
        ResourceGeneralPage.defineDescription(service.getDescription());
        ResourceGeneralPage.defineCategory(service.getCategories().get(0).getName());
        ServiceGeneralPage.defineProjectCode(service.getProjectCode());
        defineTagsList2(service.getTags());
        ResourceGeneralPage.defineContactId(service.getContactId());
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

    public static void clickAddService() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking the Add Service button");
        try {
            GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.Dashboard.ADD_AREA.getValue());
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue()).click();
            GeneralUIUtils.ultimateWait();
        } catch (Exception e) {
            final String message = String.format("Exception '%s' caught on Add Service button, retrying.", e);
            LOGGER.debug(message, e);
            SetupCDTest.getExtendTest().log(Status.WARNING, message);
            GeneralUIUtils.hoverOnAreaByClassName("w-sdc-dashboard-card-new");
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue()).click();
            GeneralUIUtils.ultimateWait();
        }
    }

}
