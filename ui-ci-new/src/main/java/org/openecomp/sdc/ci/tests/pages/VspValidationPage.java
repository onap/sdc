/*-
 * Copyright (c) 2019 Vodafone Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.ci.tests.pages;

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;

public final class VspValidationPage {

    private VspValidationPage() {

    }

    public static void navigateToVspValidationPageUsingNavbar() {
        clickOnElementUsingTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_NAVBAR);
    }

    public static boolean checkNextButtonDisabled() {
        return GeneralUIUtils.isElementDisabled(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_PROCEED_TO_INPUTS_BUTTON.getValue());
    }

    public static void clickOnElementUsingTestId(DataTestIdEnum.VspValidationPage elementTestId) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s", elementTestId.name()));
        GeneralUIUtils.getWebElementByTestID(elementTestId.getValue()).click();
        GeneralUIUtils.ultimateWait();
    }

}
