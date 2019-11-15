/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.ci.tests.pages;

import com.aventstack.extentreports.Status;
import java.io.IOException;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GovernorOperationPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(GovernorOperationPage.class);

    private GovernorOperationPage() {
        super();
    }

    public static void approveService(final String serviceName) {
        SetupCDTest.getExtendTest()
            .log(Status.INFO, String.format("Approving the distribution of the service '%s'", serviceName));
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DistributionChangeButtons.APPROVE.getValue()).click();
        GeneralUIUtils.waitForLoader();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DistributionChangeButtons.APPROVE_MESSAGE.getValue())
                .sendKeys("service " + serviceName + " tested successfully");
        GeneralPageElements.clickOKButton();
        final String distributionApprovedMsg = String.format("The distribution of the service '%s' was approved", serviceName);
        try {
            ExtentTestActions
                .addScreenshot(Status.INFO,
                    String.format("governor-distribution-approved-%s", serviceName),
                    distributionApprovedMsg
                );
        } catch (final IOException e) {
            LOGGER.error("Could not take screenshot of governor distribution approval", e);
            SetupCDTest.getExtendTest().log(Status.INFO, distributionApprovedMsg);
        }
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue());
    }

}
