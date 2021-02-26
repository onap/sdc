/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.sdc.frontend.ci.tests.pages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.core.Is.is;
import static org.onap.sdc.frontend.ci.tests.pages.ServiceCreatePage.XpathSelector.CATEGORY_SELECT;
import static org.onap.sdc.frontend.ci.tests.pages.ServiceCreatePage.XpathSelector.DESCRIPTION_TEXT_AREA;
import static org.onap.sdc.frontend.ci.tests.pages.ServiceCreatePage.XpathSelector.ETSI_VERSION_SELECT;
import static org.onap.sdc.frontend.ci.tests.pages.ServiceCreatePage.XpathSelector.NAME_INPUT;

import org.onap.sdc.frontend.ci.tests.datatypes.LifeCycleStateEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.ServiceCreateData;
import org.onap.sdc.frontend.ci.tests.utilities.LoaderHelper;
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent;
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent.NotificationType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/**
 * Represents the Service Create Page
 */
public class ServiceCreatePage extends AbstractPageObject {

    private final LoaderHelper loaderHelper;
    private final NotificationComponent notificationComponent;
    private final ResourceWorkspaceTopBarComponent topBarComponent;

    public ServiceCreatePage(final WebDriver webDriver) {
        super(webDriver);
        this.loaderHelper = new LoaderHelper(webDriver);
        this.notificationComponent = new NotificationComponent(webDriver);
        this.topBarComponent = new ResourceWorkspaceTopBarComponent(webDriver);
    }

    @Override
    public void isLoaded() {
        topBarComponent.isLoaded();
        final String lifeCycleState = topBarComponent.getLifecycleState();
        assertThat("Life cycle state should be as expected",
            lifeCycleState, is(equalToIgnoringCase(LifeCycleStateEnum.IN_DESIGN.getValue())));
    }

    /**
     * Fill the service create form based on the given ServiceCreateData
     * @param serviceCreateData the form data
     */
    public void fillForm(final ServiceCreateData serviceCreateData) {
        fillName(serviceCreateData.getName());
        setCategory(serviceCreateData.getCategory());
        setEtsiVersion(serviceCreateData.getEtsiVersion());
        fillDescription(serviceCreateData.getDescription());
    }

    private void setEtsiVersion(final String etsiVersion) {
        if (etsiVersion == null) {
            return;
        }
        final Select categorySelect = new Select(waitForElementVisibility(By.xpath(ETSI_VERSION_SELECT.getXpath())));
        categorySelect.selectByVisibleText(etsiVersion);
    }

    private void setCategory(final String category) {
        if (category == null) {
            return;
        }
        final Select categorySelect = new Select(findElement(By.xpath(CATEGORY_SELECT.getXpath())));
        categorySelect.selectByVisibleText(category);
    }

    public void fillDescription(final String description) {
        if (description == null) {
            return;
        }
        findElement(By.xpath(DESCRIPTION_TEXT_AREA.getXpath()))
            .sendKeys(description);
    }

    public void fillName(final String name) {
        if (name == null) {
            return;
        }
        findElement(By.xpath(NAME_INPUT.getXpath()))
            .sendKeys(name);
    }

    public void clickOnCreate() {
        topBarComponent.clickOnCreate();
        loaderHelper.waitForLoader(20);
        notificationComponent.waitForNotification(NotificationType.SUCCESS, 20);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    public enum XpathSelector {
        CREATE_BTN("create/save", "//button[@data-tests-id='%s']"),
        FORM_LIFE_CYCLE_STATE("formlifecyclestate", "//span[@data-tests-id='%s']"),
        NAME_INPUT("name", "//input[@data-tests-id='%s']"),
        CATEGORY_SELECT("selectGeneralCategory", "//select[@data-tests-id='%s']"),
        ETSI_VERSION_SELECT("ETSI Version", "//select[@data-tests-id='%s']"),
        DESCRIPTION_TEXT_AREA("description", "//textarea[@data-tests-id='%s']");

        private final String id;
        private final String xpathFormat;

        XpathSelector(final String id, final String xpathFormat) {
            this.id = id;
            this.xpathFormat = xpathFormat;
        }

        public String getId() {
            return id;
        }

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }
}
