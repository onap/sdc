/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.sdc.ci.tests.flow;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.aventstack.extentreports.Status;
import java.util.List;
import java.util.Optional;
import org.openecomp.sdc.ci.tests.pages.PageObject;
import org.openecomp.sdc.ci.tests.pages.ResourceLeftSideMenu;
import org.openecomp.sdc.ci.tests.pages.ResourcePropertiesAssignmentPage;
import org.openqa.selenium.WebDriver;

/**
 * UI Flow for checking the software version property in a resource
 */
public class CheckSoftwareVersionPropertyFlow extends AbstractUiTestFlow {

    private final List<String> expectedSoftwareVersionList;

    public CheckSoftwareVersionPropertyFlow(final WebDriver webDriver, final List<String> expectedSoftwareVersionList) {
        super(webDriver);
        this.expectedSoftwareVersionList = expectedSoftwareVersionList;
    }

    @Override
    public Optional<PageObject> run(final PageObject... pageObjects) {
        final ResourceLeftSideMenu resourceLeftSideMenu = new ResourceLeftSideMenu(webDriver);
        resourceLeftSideMenu.isLoaded();

        final ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage = accessPropertiesAssignmentPage();

        checkSoftwareVersionProperty(resourcePropertiesAssignmentPage);
        return Optional.empty();
    }

    /**
     * Checks if the software_version property values are as expected by the {@link #expectedSoftwareVersionList}.
     *
     * @param resourcePropertiesAssignmentPage the resource properties assignment page
     */
    private void checkSoftwareVersionProperty(final ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage) {
        extendTest.log(Status.INFO,
            String.format("Checking the presence of software versions '%s' in 'software_versions' property",
                getSoftwareVersionListAsString())
        );
        final List<String> actualSoftwareVersionList = resourcePropertiesAssignmentPage.getSoftwareVersionProperty();
        assertThat("Software Version should have the expected size", actualSoftwareVersionList,
            hasSize(expectedSoftwareVersionList.size()));
        assertThat("Software Version should be as expected", actualSoftwareVersionList,
            containsInAnyOrder(expectedSoftwareVersionList.toArray(new String[0])));
    }

    /**
     * Accesses the properties assignment page by clicking in the resource left side menu.
     *
     * @return the resulting resource properties assignment page
     */
    private ResourcePropertiesAssignmentPage accessPropertiesAssignmentPage() {
        final ResourceLeftSideMenu resourceLeftSideMenu = new ResourceLeftSideMenu(webDriver);
        resourceLeftSideMenu.isLoaded();

        extendTest.log(Status.INFO,
            String.format("Accessing the Properties Assignment page to check the software versions '%s'",
                getSoftwareVersionListAsString())
        );
        final ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage =
            resourceLeftSideMenu.clickOnPropertiesAssignmentMenuItem();
        resourcePropertiesAssignmentPage.isLoaded();
        return resourcePropertiesAssignmentPage;
    }

    /**
     * Converts the {@link #expectedSoftwareVersionList} in a comma + space separated string.
     *
     * @return the software version list as a comma + space separated string
     */
    private String getSoftwareVersionListAsString() {
        return String.join(", ", expectedSoftwareVersionList);
    }
}
