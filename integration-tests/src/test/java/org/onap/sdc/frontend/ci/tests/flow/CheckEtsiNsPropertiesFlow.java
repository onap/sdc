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
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.ResourceLeftSideMenu;
import org.onap.sdc.frontend.ci.tests.pages.ResourcePropertiesAssignmentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceWorkspaceTopBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.ServiceComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.openqa.selenium.WebDriver;

/**
 * Check the required properties in a Service of category ETSI NFV Network Service.
 */
public class CheckEtsiNsPropertiesFlow extends AbstractUiTestFlow {

    private ServiceComponentPage serviceComponentPage;

    public CheckEtsiNsPropertiesFlow(final WebDriver webDriver) {
        super(webDriver);
    }

    /**
     * Starts the flow in the {@link ServiceComponentPage}. From there go to the {@link ResourcePropertiesAssignmentPage} and check for the
     * properties. It does not require any page object, but can receive:
     * <ul>
     *     <li>{@link ServiceComponentPage} or its children:</li>
     *     <ul>
     *         <li>{@link TopNavComponent}</li>
     *         <li>{@link ResourceLeftSideMenu}</li>
     *         <li>{@link ResourceWorkspaceTopBarComponent}</li>
     *     </ul>
     * </ul>
     *
     * @param pageObjects any required page object for the flow
     * @return the {@link ServiceComponentPage}
     */
    @Override
    public Optional<ServiceComponentPage> run(final PageObject... pageObjects) {
        extendTest.log(Status.INFO, "Checking ETSI NFV Network Service properties");
        serviceComponentPage = getParameter(pageObjects, ServiceComponentPage.class).orElseGet(() -> new ServiceComponentPage(webDriver));
        serviceComponentPage.isLoaded();
        final ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage = serviceComponentPage.goToPropertiesAssignment();
        checkProperty(resourcePropertiesAssignmentPage, "descriptor_id");
        checkProperty(resourcePropertiesAssignmentPage, "designer");
        checkProperty(resourcePropertiesAssignmentPage, "flavour_id");
        checkProperty(resourcePropertiesAssignmentPage, "invariant_id");
        checkProperty(resourcePropertiesAssignmentPage, "name");
        checkProperty(resourcePropertiesAssignmentPage, "ns_profile");
        checkProperty(resourcePropertiesAssignmentPage, "version");
        checkProperty(resourcePropertiesAssignmentPage, "ns_profile");
        checkProperty(resourcePropertiesAssignmentPage, "service_availability_level");
        ExtentTestActions.takeScreenshot(Status.INFO, "etsi-ns-properties-present", "ETSI NS properties are present");
        extendTest.log(Status.INFO, "Finished checking ETSI NFV Network Service properties");
        return Optional.of(serviceComponentPage);
    }

    @Override
    public Optional<ServiceComponentPage> getLandedPage() {
        return Optional.ofNullable(serviceComponentPage);
    }

    private void checkProperty(final ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage, final String propertyName) {
        extendTest.log(Status.INFO, String.format("Checking property '%s'", propertyName));
        assertThat(String.format("'%s' property should be present", propertyName),
            resourcePropertiesAssignmentPage.isPropertyPresent(propertyName), is(true));
    }
}
