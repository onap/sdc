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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.aventstack.extentreports.Status;
import java.util.Optional;
import java.util.Set;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentProperty;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.ResourcePropertiesAssignmentPage;
import org.openqa.selenium.WebDriver;

/**
 * Check the properties in a Component
 */
public class CheckComponentPropertiesFlow extends AbstractUiTestFlow {

    private final Set<ComponentProperty<?>> componentPropertySet;
    private ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage;

    public CheckComponentPropertiesFlow(final Set<ComponentProperty<?>> componentPropertySet, final WebDriver webDriver) {
        super(webDriver);
        this.componentPropertySet = componentPropertySet;
    }

    /**
     * Starts the flow in a {@link ComponentPage}. From there go to the {@link ResourcePropertiesAssignmentPage} and check for the
     * properties. It does not require any page object, but can receive a {@link ComponentPage}
     *
     * @param pageObjects any required page object for the flow
     * @return the {@link ComponentPage}
     */
    @Override
    public Optional<ResourcePropertiesAssignmentPage> run(final PageObject... pageObjects) {
        extendTest.log(Status.INFO, "Checking component properties");
        final ComponentPage componentPage = getParameter(pageObjects, ComponentPage.class).orElseGet(() -> new ComponentPage(webDriver));
        componentPage.isLoaded();
        resourcePropertiesAssignmentPage = componentPage.goToPropertiesAssignment();
        resourcePropertiesAssignmentPage.isLoaded();
        if (componentPropertySet.isEmpty()) {
            extendTest.log(Status.INFO, "Finished checking component properties. No properties were given.");
            return Optional.of(resourcePropertiesAssignmentPage);
        }
        componentPropertySet.forEach(this::checkProperty);
        ExtentTestActions.takeScreenshot(Status.INFO, "vf-properties-present", "VF properties are present");
        extendTest.log(Status.INFO, "Finished checking component properties");
        return Optional.of(resourcePropertiesAssignmentPage);
    }

    @Override
    public Optional<ResourcePropertiesAssignmentPage> getLandedPage() {
        return Optional.ofNullable(resourcePropertiesAssignmentPage);
    }

    private void checkProperty(final ComponentProperty<?> componentProperty) {
        extendTest.log(Status.INFO, String.format("Checking property '%s'", componentProperty));
        assertThat(String.format("'%s' property should be present", componentProperty),
            resourcePropertiesAssignmentPage.isPropertyPresent(componentProperty.getName()), is(true));
        if (componentProperty.getValue() != null) {
            final Object propertyValue = resourcePropertiesAssignmentPage.getPropertyValue(componentProperty.getName());
            assertThat("'%s' property should have the expected value", propertyValue, is(componentProperty.getValue()));
        }
    }
}
