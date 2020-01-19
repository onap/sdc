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

import static org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest.getExtendTest;
import static org.openecomp.sdc.ci.tests.pages.HomePage.PageElement.REPOSITORY_ICON;

import com.aventstack.extentreports.Status;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.exception.HomePageRuntimeException;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.utilities.DownloadManager;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePage {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomePage.class);

    private static final int WAIT_FOR_ELEMENT_TIME_OUT = 30;
    private static final int WAIT_FOR_LOADER_TIME_OUT = 600;

    private HomePage() {

    }

    public static void showVspRepository() {
        GeneralUIUtils.waitForElementInVisibilityBy(By.className("ui-notification"), WAIT_FOR_ELEMENT_TIME_OUT);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.MainMenuButtons.REPOSITORY_ICON.getValue());
    }

    public static boolean searchForVSP(String vspName) {
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ImportVfRepository.SEARCH.getValue()).clear();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ImportVfRepository.SEARCH.getValue()).sendKeys(vspName);
        GeneralUIUtils.ultimateWait();
        return true;
    }

    public static void importVSP(String vspName) {
        HomePage.showVspRepository();
        boolean vspFound = HomePage.searchForVSP(vspName);
        if (vspFound) {
            List<WebElement> elementsFromTable = GeneralPageElements.getElementsFromTable();
            WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), WAIT_FOR_ELEMENT_TIME_OUT);
            WebElement findElement = wait.until(ExpectedConditions.visibilityOf(elementsFromTable.get(1)));
            findElement.click();
            GeneralUIUtils.waitForLoader();
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.IMPORT_VSP.getValue());
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue());
            GeneralUIUtils.waitForLoader(WAIT_FOR_LOADER_TIME_OUT);
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue());
        }
    }

    public static boolean navigateToHomePage() {
        try {
            LOGGER.debug("Searching for repository icon");
            final WebElement repositoryIcon = GeneralUIUtils.getInputElement(REPOSITORY_ICON.getTestId());
            if (repositoryIcon != null) {
                return true;
            }
            GeneralUIUtils.ultimateWait();
            final List<WebElement> homeButtons = GeneralUIUtils
                .getElementsByLocator(By.xpath("//a[contains(.,'HOME')]"));
            if (!homeButtons.isEmpty()) {
                homeButtons.stream().filter(WebElement::isDisplayed).findFirst().ifPresent(webElement -> {
                    webElement.click();
                    LOGGER.debug("Clicked on home button");
                });
            }

            GeneralUIUtils.closeErrorMessage();
            WebElement homeButton = GeneralUIUtils
                .getInputElement(DataTestIdEnum.MainMenuButtons.HOME_BUTTON.getValue());
            return homeButton != null && homeButton.isDisplayed();

        } catch (final Exception e) {
            final String msg = "Could not click on home button";
            getExtendTest()
                .log(Status.WARNING, msg);
            LOGGER.warn(msg, e);
            return false;
        }
    }

    public static File downloadVspCsarToDefaultDirectory(String vspName) throws Exception {
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        DownloadManager.downloadCsarByNameFromVSPRepository(vspName, "");
        return FileHandling.getLastModifiedFileNameFromDir();
    }

    public static void findComponentAndClick(final String resourceName) {
        findComponent(resourceName);
        clickComponent(resourceName);
    }

    public static void findComponent(final String resourceName) {
        LOGGER.debug("Searching for component '{}'", resourceName);
        getExtendTest().log(Status.INFO, "Searching for " + resourceName + " in home tab");
        clearSearchResults(getSearchInput());
        searchForComponent(resourceName);
    }

    private static WebElement getSearchInput() {
        WebElement searchTextbox;
        try {
            searchTextbox = TopSearchComponent.getComponentInput();
            LOGGER.debug("Search textbox '{}' selected", TopSearchComponent.SEARCH_INPUT_TEST_ID);
        } catch (final Exception e) {
            final String errorMsg = "Top Search bar was not visible";
            getExtendTest().log(Status.ERROR, errorMsg);
            throw new HomePageRuntimeException(errorMsg, e);
        }
        return searchTextbox;
    }

    private static void clearSearchResults(final WebElement searchTextbox) {
        try {
            LOGGER.debug("Clearing search results before searching");
            TopSearchComponent.replaceSearchValue(searchTextbox, UUID.randomUUID().toString());
            MainRightContainer.isEmptyResult();
        } catch (final Exception e) {
            final String errorMsg = "Could not clean up the search result";
            getExtendTest().log(Status.ERROR, errorMsg);
            throw new HomePageRuntimeException(errorMsg, e);
        }
    }

    private static void searchForComponent(final String resourceName) {
        try {
            LOGGER.debug("Searching for '{}'", resourceName);
            TopSearchComponent.replaceSearchValue(resourceName);
            MainRightContainer.isResultVisible(resourceName);
        } catch (final Exception e) {
            final String errorMsg = String.format("Could not find the component '%s' after search", resourceName);
            getExtendTest().log(Status.ERROR, errorMsg);
            throw new HomePageRuntimeException(errorMsg, e);
        }
    }

    public static void clickComponent(final String resourceName) {
        LOGGER.debug("Clicking on the component " + resourceName);
        try {
            getExtendTest()
                .log(Status.INFO, String.format("Clicking on the '%s' component from home tab", resourceName));
            GeneralUIUtils.clickOnElementByTestId(resourceName);
        } catch (final Exception e) {
            final String errorMsg = String.format("Could not click on home tab component '%s' ", resourceName);
            getExtendTest().log(Status.ERROR, e.getMessage());
            throw new HomePageRuntimeException(errorMsg, e);
        }
        final String datetimeString =
            new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(Calendar.getInstance().getTime());
        try {
            ExtentTestActions
                .addScreenshot(Status.INFO,
                    String.format("after-click-resource-%s-%s", resourceName, datetimeString),
                    String.format("Clicked on resource '%s'", resourceName)
                );
        } catch (final IOException e) {
            LOGGER.warn("Could take screenshot after resource {} click", resourceName, e);
        }
        try {
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue());
        } catch (final Exception e) {
            final String errorMsg = String.format("Expecting to be inside component '%s' screen", resourceName);
            getExtendTest().log(Status.ERROR, e.getMessage());
            throw new HomePageRuntimeException(errorMsg, e);
        }
    }

    public static void waitForElement(PageElement homePageElement) {
        final String cssClass = homePageElement.getCssClass();
        LOGGER.debug("Waiting for{} visibility", cssClass);
        GeneralUIUtils.getWebElementByClassName(cssClass);
        LOGGER.debug("{} is visible", cssClass);
    }

    public enum PageElement {
        COMPONENT_PANEL("w-sdc-main-right-container", null),
        REPOSITORY_ICON(null, "repository-icon");

        private final String cssClass;
        private final String testId;

        PageElement(String cssClass, String testId) {
            this.cssClass = cssClass;
            this.testId = testId;
        }

        public String getCssClass() {
            return cssClass;
        }

        public String getTestId() {
            return testId;
        }
    }

}
