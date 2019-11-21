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

import com.aventstack.extentreports.ExtentTest;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestManager;
import org.openecomp.sdc.ci.tests.flow.exception.MissingParameterRuntimeException;
import org.openecomp.sdc.ci.tests.pages.PageObject;
import org.openqa.selenium.WebDriver;

public abstract class AbstractUiTestFlow implements UiTestFlow {

    protected final WebDriver webDriver;
    protected final ExtentTest extendTest = ExtentTestManager.getInstance().getTest();

    public AbstractUiTestFlow(final WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public <T extends PageObject> T findParameter(final PageObject[] pageObjects,
                                                  final Class<T> expectedParameterType) {
        for (final PageObject uiTestFlow : pageObjects) {
            if(expectedParameterType.isInstance(uiTestFlow)) {
                return (T) uiTestFlow;
            }
        }

        throw new MissingParameterRuntimeException(expectedParameterType.getName());
    }

}
