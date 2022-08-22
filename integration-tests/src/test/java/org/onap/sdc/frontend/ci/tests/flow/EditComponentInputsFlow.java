/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
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
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.ResourcePropertiesAssignmentPage;
import org.openqa.selenium.WebDriver;

public class EditComponentInputsFlow extends AbstractUiTestFlow {

    private final Map<String, Object> inputsMap;
    private ComponentPage componentPage;

    public EditComponentInputsFlow(final WebDriver webDriver, final Map<String, Object> inputsMap) {
        super(webDriver);
        this.inputsMap = inputsMap;
    }

    @Override
    public Optional<ComponentPage> run(final PageObject... pageObjects) {
        componentPage = getParameter(pageObjects, ComponentPage.class).orElseGet(() -> new ComponentPage(webDriver));
        componentPage.isLoaded();
        if (MapUtils.isEmpty(inputsMap)) {
            return Optional.of(componentPage);
        }
        final ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage = componentPage.goToPropertiesAssignment();
        resourcePropertiesAssignmentPage.isLoaded();
        resourcePropertiesAssignmentPage.selectInputTab();
        final String inputNames = String.join(", ", inputsMap.keySet());
        ExtentTestActions.takeScreenshot(Status.INFO, "etsi-ns-edited-properties", String.format("Before editing inputs: %s", inputNames));
        extendTest.log(Status.INFO, "Editing inputs " + inputNames);

        inputsMap.forEach(resourcePropertiesAssignmentPage::setInputValue);
        resourcePropertiesAssignmentPage.saveInputs();
        ExtentTestActions.takeScreenshot(Status.INFO, "etsi-ns-edited-properties", String.format("Inputs edited: %s", inputNames));
        resourcePropertiesAssignmentPage.selectPropertiesTab();
        return Optional.of(componentPage);
    }

    @Override
    public Optional<ComponentPage> getLandedPage() {
        return Optional.ofNullable(componentPage);
    }
}
