/**
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
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;

import java.util.List;

public class VspValidationResultsPage extends GeneralPageElements {

    public VspValidationResultsPage() { super(); }

    public static void navigateToVspValidationResultsPageUsingNavbar() throws Exception {
        clickOnElementUsingTestId(DataTestIdEnum.VspValidationResultsPage.VSP_VALIDATION_RESULTS_PAGE_NAVBAR);
    }

    public static void navigateToVspValidationResultsPageUsingBreadcrumbs() throws Exception {
        clickOnElementUsingTestId(DataTestIdEnum.VspValidationResultsPage.VSP_VALIDATION_RESULTS_PAGE_BREADCRUMBS);
    }

    public static boolean checkResultsExist() throws Exception {
        List<WebElement> results = GeneralUIUtils.findElementsByXpath("//h4[contains(text(),'No Validation Checks Performed')]");
        return results.size() == 0;
    }

    public static void clickOnElementUsingTestId(DataTestIdEnum.VspValidationResultsPage elementTestId) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s", elementTestId.name()));
        GeneralUIUtils.getWebElementByTestID(elementTestId.getValue()).click();
        GeneralUIUtils.ultimateWait();
    }
}