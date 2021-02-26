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

package org.onap.sdc.frontend.ci.tests.flow;

import com.aventstack.extentreports.ExtentTest;
import java.util.Optional;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestManager;
import org.onap.sdc.frontend.ci.tests.flow.exception.MissingParameterRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.openqa.selenium.WebDriver;

/**
 * The base class for a UI test flow.
 */
public abstract class AbstractUiTestFlow implements UiTestFlow {

    protected final WebDriver webDriver;
    protected final ExtentTest extendTest = ExtentTestManager.getInstance().getTest();

    public AbstractUiTestFlow(final WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    /**
     * Find a page object within the the page objects array, based on the given class. If the page object is not found, throws an error.
     *
     * @param pageObjects           the page object array
     * @param expectedParameterType the class of the page object to find
     * @param <T>                   a child class of the page object
     * @return the found page object
     * @throws MissingParameterRuntimeException when the page object is not found
     */
    public <T extends PageObject> T findParameter(final PageObject[] pageObjects,
                                                  final Class<T> expectedParameterType) {
        final Optional<T> parameter = getParameter(pageObjects, expectedParameterType);
        if (parameter.isEmpty()) {
            throw new MissingParameterRuntimeException(expectedParameterType.getName());
        }
        return parameter.get();
    }

    /**
     * Find a page object within the the page objects array, based on the given class.
     *
     * @param pageObjects           the page object array
     * @param expectedParameterType the class of the page object to find
     * @param <T>                   a child class of the page object
     * @return an optional page object
     */
    public <T extends PageObject> Optional<T> getParameter(final PageObject[] pageObjects,
                                                           final Class<T> expectedParameterType) {
        for (final PageObject uiTestFlow : pageObjects) {
            if (expectedParameterType.isInstance(uiTestFlow)) {
                return Optional.of((T) uiTestFlow);
            }
        }

        return Optional.empty();
    }
}
