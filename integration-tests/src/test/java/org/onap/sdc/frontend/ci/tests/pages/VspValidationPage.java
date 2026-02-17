/**
 * Copyright (c) 2019 Vodafone Group
 * Copyright (C) 2021 Nokia. All rights reserved.
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

package org.onap.sdc.frontend.ci.tests.pages;

import com.aventstack.extentreports.Status;
import org.apache.commons.collections.CollectionUtils;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.File;
import java.util.List;

public class VspValidationPage extends GeneralPageElements {

    private static final Logger LOGGER = LoggerFactory.getLogger(VspValidationPage.class);

    private static final String ATTACHMENT_NAME_TEST_ID = ".//*[@data-test-id='validation-tree-node-name']";
    private static final String VALIDATION_ERROR_COUNT = ".//*[@data-test-id='validation-error-count']";
    private static final String VALIDATION_WARNING_COUNT = ".//*[@data-test-id='validation-warning-count']";
    private static final String SOFTWARE_PRODUCT_ATTACHMENTS = "navbar-group-item-SOFTWARE_PRODUCT_ATTACHMENTS";
    private static final String ATTACHMENTS_TAB_VALIDATION = "attachments-tab-validation";
    private static final String SDC_TAB_ACTIVE_CLASS_NAME = "sdc-tab-active";
    private static final String ATTACHMENTS_NODE_ID = "validation-tree-node";
    private static final String HELM_ATTACHMENT_EXTENSION = ".tgz";
    private static final String SUBMIT_BUTTON_ID = "vc-submit-btn";
    private static final int TWO_SECONDS_TIMEOUT = 2;

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

    public static boolean isSubmitButtonEnabled() {
        return !GeneralUIUtils.isElementDisabled(SUBMIT_BUTTON_ID);
    }

    public static boolean hasHelmAttachmentsAnyWarnings() {
        return GeneralUIUtils.getWebElementsListByTestID(ATTACHMENTS_NODE_ID)
            .stream()
            .filter(webElement -> hasAttachmentFileExtension(webElement, HELM_ATTACHMENT_EXTENSION))
            .anyMatch(VspValidationPage::elementHasWarningCount);
    }

    public static boolean hasHelmAttachmentsAnyError() {
        return GeneralUIUtils.getWebElementsListByTestID(ATTACHMENTS_NODE_ID)
            .stream()
            .filter(webElement -> hasAttachmentFileExtension(webElement, HELM_ATTACHMENT_EXTENSION))
            .anyMatch(VspValidationPage::elementHasErrorCount);
    }

    public static boolean isVspAttachmentsValidationPage() {
        WebElement webElementByTestID = GeneralUIUtils.getWebElementByTestID(
            ATTACHMENTS_TAB_VALIDATION, TWO_SECONDS_TIMEOUT);
        return webElementByTestID != null && webElementByTestID.getAttribute("class").contains(
            SDC_TAB_ACTIVE_CLASS_NAME);
    }

    public static void navigateToVspAttachmentsValidationPage() {
        GeneralUIUtils.clickOnElementByTestId(SOFTWARE_PRODUCT_ATTACHMENTS);
        GeneralUIUtils.clickOnElementByTestId(ATTACHMENTS_TAB_VALIDATION);
        GeneralUIUtils.ultimateWait();
    }

    private static boolean elementHasWarningCount(WebElement webElement) {
        try {
            webElement.findElement(By.xpath(VALIDATION_WARNING_COUNT));
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        } catch (Exception ex) {
            LOGGER.warn("Unexpected exception while checking for warning count", ex);
            return false;
        }
    }

    private static boolean elementHasErrorCount(WebElement webElement) {
        try {
            webElement.findElement(By.xpath(VALIDATION_ERROR_COUNT));
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        } catch (Exception ex) {
            LOGGER.warn("Unexpected exception while checking for error count", ex);
            return false;
        }
    }

    private static boolean hasAttachmentFileExtension(WebElement webElement, String extension) {
        try {
            return webElement
                .findElement(By.xpath(ATTACHMENT_NAME_TEST_ID))
                .getText()
                .endsWith(extension);
        } catch (NoSuchElementException ex) {
            return false;
        } catch (Exception ex) {
            LOGGER.warn("Unexpected exception while checking attachment file extension", ex);
            return false;
        }
    }

    private static List<WebElement> getChildElements(WebElement webElement) throws Exception {
        return webElement.findElements(By.xpath(".//*"));
    }

}
