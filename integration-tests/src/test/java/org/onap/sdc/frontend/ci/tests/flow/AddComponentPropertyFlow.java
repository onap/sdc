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
import org.onap.sdc.frontend.ci.tests.pages.ResourcePropertiesAssignmentPage;
import org.openqa.selenium.WebDriver;

public class AddComponentPropertyFlow extends AbstractUiTestFlow {

    private final Map<String, String> propertiesMap;
    private ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage;

    public AddComponentPropertyFlow(final WebDriver webDriver, final Map<String, String> propertiesMap) {
        super(webDriver);
        this.propertiesMap = propertiesMap;
    }

    @Override
    public Optional<ResourcePropertiesAssignmentPage> run(final PageObject... pageObjects) {
        if (MapUtils.isEmpty(propertiesMap)) {
            return Optional.of(resourcePropertiesAssignmentPage);
        }
        resourcePropertiesAssignmentPage = findParameter(pageObjects, ResourcePropertiesAssignmentPage.class);
        resourcePropertiesAssignmentPage.isLoaded();
        final String propertyNames = String.join(", ", propertiesMap.keySet());
        extendTest.log(Status.INFO, "Adding properties " + propertyNames);
        resourcePropertiesAssignmentPage.addProperties(propertiesMap);
        ExtentTestActions.takeScreenshot(Status.INFO, "added-properties", String.format("Properties added: %s", propertyNames));
        return Optional.of(this.resourcePropertiesAssignmentPage);
    }

    @Override
    public Optional<? extends PageObject> getLandedPage() {
        return Optional.empty();
    }
}
