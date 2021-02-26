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

package org.onap.sdc.frontend.ci.tests.flow;

import com.aventstack.extentreports.Status;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.ResourceLeftSideMenu;
import org.onap.sdc.frontend.ci.tests.pages.ResourcePropertiesAssignmentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceWorkspaceTopBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.ServiceComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.openqa.selenium.WebDriver;

public class EditServicePropertiesFlow extends AbstractUiTestFlow {

    private final Map<String, Object> propertiesMap;
    private ServiceComponentPage serviceComponentPage;

    public EditServicePropertiesFlow(final WebDriver webDriver, final Map<String, Object> propertiesMap) {
        super(webDriver);
        this.propertiesMap = propertiesMap;
    }

    @Override
    public Optional<ServiceComponentPage> run(final PageObject... pageObjects) {
        serviceComponentPage = getParameter(pageObjects, ServiceComponentPage.class)
            .orElseGet(() -> {
                final TopNavComponent topNavComponent = getParameter(pageObjects, TopNavComponent.class).orElse(new TopNavComponent(webDriver));
                final ResourceLeftSideMenu resourceLeftSideMenu =
                    getParameter(pageObjects, ResourceLeftSideMenu.class).orElse(new ResourceLeftSideMenu(webDriver));
                final ResourceWorkspaceTopBarComponent workspaceTopBarComponent =
                    getParameter(pageObjects, ResourceWorkspaceTopBarComponent.class).orElse(new ResourceWorkspaceTopBarComponent(webDriver));
                return new ServiceComponentPage(webDriver, topNavComponent, resourceLeftSideMenu, workspaceTopBarComponent);
            });
        serviceComponentPage.isLoaded();
        final ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage = serviceComponentPage.goToPropertiesAssignment();
        if (MapUtils.isEmpty(propertiesMap)) {
            return Optional.of(serviceComponentPage);
        }
        final String propertyNames = String.join(", ", propertiesMap.keySet());
        ExtentTestActions.takeScreenshot(Status.INFO, "etsi-ns-edited-properties",
            String.format("Before editing properties: %s", propertyNames));
        extendTest.log(Status.INFO, "Editing properties " + propertyNames);
        propertiesMap.forEach(resourcePropertiesAssignmentPage::setPropertyValue);
        resourcePropertiesAssignmentPage.saveProperties();
        ExtentTestActions.takeScreenshot(Status.INFO, "etsi-ns-edited-properties",
            String.format("Properties edited: %s", propertyNames));
        return Optional.of(serviceComponentPage);
    }

    @Override
    public Optional<ServiceComponentPage> getLandedPage() {
        return Optional.ofNullable(serviceComponentPage);
    }
}
