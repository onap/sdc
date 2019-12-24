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
import java.io.File;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

public class VspValidationPage extends GeneralPageElements {

    private VspValidationPage() {
        super();
    }

    public static void navigateToVspValidationPageUsingNavbar() throws Exception {
        clickOnElementUsingTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_NAVBAR);
    }

    public static void navigateToVspValidationPageUsingBreadcrumbs() throws Exception {
        clickOnElementUsingTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_BREADCRUMBS);
    }

    public static void clickOnNextButton() throws Exception {
        clickOnElementUsingTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_PROCEED_TO_INPUTS_BUTTON);
    }

    public static void clickOnBackButton() throws Exception {
        clickOnElementUsingTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_PROCEED_TO_SETUP_BUTTON);
    }

    public static void clickOnSubmitButton() throws Exception {
        clickOnElementUsingTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_PROCEED_TO_RESULTS_BUTTON);
    }

    public static void loadVSPFile(String path, String filename) {
        List<WebElement> checkboxes =
                GeneralUIUtils.findElementsByXpath("//div[@class='validation-input-wrapper']//input");
        boolean hasValue = CollectionUtils.isNotEmpty(checkboxes);
        if (hasValue) {
            WebElement browseWebElement = checkboxes.get(0);
            browseWebElement.sendKeys(path + File.separator + filename);
            GeneralUIUtils.ultimateWait();
        } else {
            Assert.fail("Did not find File input field in the page for loading VSP test file");
        }

    }
    public static boolean checkNextButtonDisabled() throws Exception {
        return GeneralUIUtils.isElementDisabled(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_PROCEED_TO_INPUTS_BUTTON.getValue());
    }

    public static void clickCertificationQueryAll() throws Exception {
        List<WebElement> checkboxes = GeneralUIUtils.findElementsByXpath("//div[@data-test-id='vsp-validation-certifications-query-checkbox-tree']//label//span[@class='rct-checkbox']");
        if (!checkboxes.isEmpty()) {
            checkboxes.get(0).click();
        } else {
            Assert.fail("Did not find certification test checkbox in the page");
        }
    }

    public static void clickComplianceChecksAll() throws Exception {
        List<WebElement> vnfComplianceCheckboxes = GeneralUIUtils.findElementsByXpath("//div[@data-test-id='vsp-validation-compliance-checks-checkbox-tree']//span[@class='rct-text' and .//label//text()='vnf-compliance']//button");
        if (!vnfComplianceCheckboxes.isEmpty()) {
            vnfComplianceCheckboxes.get(vnfComplianceCheckboxes.size() - 1).click();
        } else {
            Assert.fail("Did not find vnf-compliance test checkbox in the page");
        }
        List<WebElement> checkboxes = GeneralUIUtils.findElementsByXpath("//div[@data-test-id='vsp-validation-compliance-checks-checkbox-tree']//label//span[@class='rct-title' and text()='csar-validate']");
        if (!checkboxes.isEmpty()) {
            checkboxes.get(checkboxes.size() - 1).click();
        } else {
            Assert.fail("Did not find csar-validate test Checkbox in the page");
        }
    }

    public static boolean checkCertificationQueryExists() throws Exception {
        WebElement parentDiv = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_CERTIFICATION_CHECKBOX_TREE.getValue());
        List<WebElement> checkboxTreeDivs = getChildElements(parentDiv);
        List<WebElement> orderedList = getChildElements(checkboxTreeDivs.get(0));
        return (!orderedList.isEmpty());
    }

    public static boolean checkComplianceCheckExists() throws Exception {
        WebElement parentDiv = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_COMPLIANCE_CHECKBOX_TREE.getValue());
        List<WebElement> checkboxTreeDivs = getChildElements(parentDiv);
        List<WebElement> orderedList = getChildElements(checkboxTreeDivs.get(0));
        return (!orderedList.isEmpty());
    }

    public static boolean checkSelectedComplianceCheckExists() throws Exception {
        WebElement selectedTests = GeneralUIUtils.findElementsByXpath("//div[contains(text(),'Selected Compliance Tests')]/..//select[@class='validation-setup-selected-tests']").get(0);
        List<WebElement> options = getChildElements(selectedTests);
        return (!options.isEmpty());
    }

    public static boolean checkSelectedCertificationQueryExists() throws Exception {
        WebElement selectedTests = GeneralUIUtils.findElementsByXpath("//div[contains(text(),'Selected Certifications Query')]/..//select[@class='validation-setup-selected-tests']").get(0);
        List<WebElement> options = getChildElements(selectedTests);
        return (!options.isEmpty());
    }

    public static void clickOnElementUsingTestId(DataTestIdEnum.VspValidationPage elementTestId) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s", elementTestId.name()));
        GeneralUIUtils.getWebElementByTestID(elementTestId.getValue()).click();
        GeneralUIUtils.ultimateWait();
    }

    private static List<WebElement> getChildElements(WebElement webElement) throws Exception {
        return webElement.findElements(By.xpath(".//*"));
    }


}
