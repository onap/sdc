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

package org.onap.sdc.frontend.ci.tests.pages.component.workspace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Represents the composition page, details panel, Deployment Artifacts tab
 */
public class CompositionDeploymentArtifactsTab extends AbstractPageObject {

    public CompositionDeploymentArtifactsTab(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.DEPLOYMENT_ARTIFACTS_TAB.getXpath()));
    }

    @AllArgsConstructor
    @Getter
    private enum XpathSelector {
        DEPLOYMENT_ARTIFACTS_TAB("//artifacts-tab[.//header[contains(text(), 'Deployment Artifacts')]]");

        private final String xpath;

    }
}
