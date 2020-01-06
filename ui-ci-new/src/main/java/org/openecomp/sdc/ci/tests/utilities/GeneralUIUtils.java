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

package org.openecomp.sdc.ci.tests.utilities;

import static org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest.getExtendTest;

import com.aventstack.extentreports.Status;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.exception.GeneralUiRuntimeException;
import org.openecomp.sdc.ci.tests.execute.setup.DriverFactory;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GeneralUIUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralUIUtils.class);

    private static final String DATA_TESTS_ID = "//*[@data-tests-id='%1$s' or @data-test-id='%1$s']";
    private static final String COLOR_YELLOW_BORDER_4PX_SOLID_YELLOW = "color: yellow; border: 4px solid yellow;";

    private static final int TIME_OUT = (int) (60 * 1.5);
    private static final int SLEEP_DURATION = 1000;
    private static final int NAP_PERIOD = 100;
    private static final int DURATION_FORMATIN = 60;

    private GeneralUIUtils() {

    }

    public static int getTimeOut() {
        return TIME_OUT;
    }

    public static WebDriver getDriver() {
        return DriverFactory.getDriver();
    }

    public static List<WebElement> getElementsByLocator(By by) {
        return getDriver().findElements(by);
    }

    public static File takeScreenshot(String screenshotFilename, final String dir, final String testName) {
        if (screenshotFilename == null) {
            if (testName != null) {
                screenshotFilename = testName;
            } else {
                screenshotFilename = UUID.randomUUID().toString();
            }
        }
        try {
            File scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
            File filePath = new File(String.format("%s/%s.png", dir, screenshotFilename));
            new File(dir).mkdirs();
            FileUtils.copyFile(scrFile, filePath);
            return filePath;
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public static File takeScreenshot(String screenshotFilename, String dir) {
        return takeScreenshot(screenshotFilename, dir, null);
    }

    public static WebElement getWebElementByTestID(String dataTestId) {
        return getWebElementByTestID(dataTestId, TIME_OUT);
    }

    public static WebElement getWebElementByTestID(final String dataTestId, final int timeout) {
        final WebDriverWait wait = new WebDriverWait(getDriver(), timeout);
        return wait
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(String.format(DATA_TESTS_ID, dataTestId))));
    }

    public static WebElement getInputElement(String dataTestId) {
        try {
            ultimateWait();
            return getDriver().findElement(By.xpath(String.format(DATA_TESTS_ID, dataTestId)));
        } catch (Exception e) {
            return null;
        }
    }

    public static WebElement getWebElementBy(By by) {
        return getWebElementBy(by, TIME_OUT);
    }

    public static WebElement getWebElementBy(By by, int timeOut) {
        WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement getWebElementByClassName(String className) {
        WebDriverWait wait = new WebDriverWait(getDriver(), TIME_OUT);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(className)));
    }

    public static List<WebElement> getWebElementsListByTestID(String dataTestId) {
        WebDriverWait wait = new WebDriverWait(getDriver(), TIME_OUT);
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(String.format(DATA_TESTS_ID, dataTestId))));
    }

    public static Boolean isElementInvisibleByTestId(String dataTestId) {
        WebDriverWait wait = new WebDriverWait(getDriver(), TIME_OUT);
        return wait.until(
                ExpectedConditions.invisibilityOfElementLocated(By.xpath(String.format(DATA_TESTS_ID, dataTestId))));
    }

    public static Boolean isElementVisibleByTestId(String dataTestId) {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), TIME_OUT);
            return wait.until(ExpectedConditions.visibilityOfElementLocated((By.xpath(String.format(DATA_TESTS_ID, dataTestId))))).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public static void clickOnElementByTestId(String dataTestId) {
        LOGGER.debug("Clicking on the element by test id " + dataTestId);
        clickOnElementByTestIdWithoutWait(dataTestId);
        LOGGER.debug("Waiting after clicking element by test id " + dataTestId);
        ultimateWait();
        LOGGER.debug(String.format("Waiting after clicking element by test id '%s' finished", dataTestId));
    }

    public static void clickOnElementChildByTestId(String dataTestId) {
        clickOnElementChildByTestIdWithoutWait(dataTestId);
        ultimateWait();
    }

    public static void clickOnElementByTestIdWithoutWait(final String dataTestId) {
        final WebDriverWait wait = new WebDriverWait(getDriver(), TIME_OUT);
        wait
            .until(ExpectedConditions.elementToBeClickable(By.xpath(String.format(DATA_TESTS_ID, dataTestId)))).click();
    }

    public static void clickOnElementChildByTestIdWithoutWait(String dataTestId) {
        WebDriverWait wait = new WebDriverWait(getDriver(), TIME_OUT);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(String.format(DATA_TESTS_ID, dataTestId) + "//*"))).click();
    }

    public static WebElement waitForElementVisibilityByTestId(String dataTestId) {
        WebDriverWait wait = new WebDriverWait(getDriver(), TIME_OUT);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(String.format(DATA_TESTS_ID, dataTestId))));
    }

    public static Boolean waitForElementInVisibilityByTestId(By by) {
        return waitForElementInVisibilityBy(by, TIME_OUT);
    }


    public static Boolean waitForElementInVisibilityBy(By by, int timeOut) {
        WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
        boolean displayed = getDriver().findElements(by).isEmpty();
        if (!displayed) {
            Boolean until = wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
            sleep(SLEEP_DURATION);
            return until;
        }
        return false;
    }


    public static WebElement hoverOnAreaByTestId(String areaId) {
        Actions actions = new Actions(getDriver());
        WebElement area = getWebElementByTestID(areaId);
        actions.moveToElement(area).perform();
        ultimateWait();
        return area;
    }

    public static WebElement hoverOnAreaByClassName(String className) {
        Actions actions = new Actions(getDriver());
        WebElement area = getWebElementByClassName(className);
        actions.moveToElement(area).perform();
        GeneralUIUtils.ultimateWait();
        return area;
    }

    public static void waitForLoader() {
        waitForLoader(TIME_OUT);
    }

    public static void waitForLoader(int timeOut) {
        final String loaderClass = "tlv-loader";
        final int sleepDuration = 500;
        sleep(sleepDuration);
        LOGGER.debug("Waiting {}s for '.{}'", timeOut, loaderClass);
        waitForElementInVisibilityBy(By.className(loaderClass), timeOut);
    }

    public static void findComponentAndClick(final String resourceName) {
        HomePage.findComponentAndClick(resourceName);
    }

    public static void zoomOut(final int zoomOutFactor) throws AWTException {
        final Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        for (int i = 0; i < zoomOutFactor; i++) {
            robot.keyPress(KeyEvent.VK_SUBTRACT);
            robot.keyRelease(KeyEvent.VK_SUBTRACT);
        }
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public static void resetZoom() throws AWTException {
        final Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_0);
        robot.keyRelease(KeyEvent.VK_0);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public static void windowZoomOutUltimate() {
        try {
            resetZoom();
            zoomOut(3);
        } catch (final AWTException e) {
            LOGGER.warn("Could not zoom out. The test will possibly fail at some stage because of non visible elements.", e);
        }
    }

    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GeneralUiRuntimeException("The thread was interrupted during a sleep", e);
        }
    }

    public static void moveToStep(final DataTestIdEnum.StepsEnum stepName) {
        getExtendTest().log(Status.INFO, String.format("Going to %s page ", stepName.toString()));
        moveToStep(stepName.getValue());
    }

    public static void moveToStep(final String dataTestId) {
        clickOnElementChildByTestId(dataTestId);
    }


    public static Select getSelectList(String item, String datatestsid) {
        Select selectList = new Select(getWebElementByTestID(datatestsid));
        if (item != null) {
            selectList.selectByVisibleText(item);
        }
        return selectList;
    }

    public static List<WebElement> getElementsByCSS(String cssString) /*throws InterruptedException*/ {
        GeneralUIUtils.waitForLoader();
        return getDriver().findElements(By.cssSelector(cssString));
    }

    private static WebElement highlightMyElement(WebElement element) {
        JavascriptExecutor javascript = (JavascriptExecutor) getDriver();
        javascript.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, COLOR_YELLOW_BORDER_4PX_SOLID_YELLOW);
        return element;
    }

    public static WebElement getSelectedElementFromDropDown(String dataTestId) {
        GeneralUIUtils.ultimateWait();
        return new Select(getDriver().findElement(By.xpath(String.format(DATA_TESTS_ID, dataTestId)))).getFirstSelectedOption();
    }

    public static String getActionDuration(Runnable func) {
        long startTime = System.nanoTime();
        func.run();
        long estimateTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toSeconds(estimateTime - startTime);
        return String.format("%02d:%02d", duration / DURATION_FORMATIN, duration % DURATION_FORMATIN);
    }

    public static WebElement clickOnAreaJS(String areaId) {
        return clickOnAreaJS(areaId, TIME_OUT);
    }


    public static WebElement clickOnAreaJS(String areaId, int timeout) {
        try {
            ultimateWait();
            WebElement area = getWebElementByTestID(areaId);
            JavascriptExecutor javascript = (JavascriptExecutor) getDriver();
            javascript.executeScript("arguments[0].click();", area, COLOR_YELLOW_BORDER_4PX_SOLID_YELLOW);
            waitForLoader(timeout);
            ultimateWait();
            return area;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void clickSomewhereOnPage() {
        getDriver().findElement(By.cssSelector(".asdc-app-title")).click();
    }

    public static void clickOnElementByText(String textInElement) {
        WebDriverWait wait = new WebDriverWait(getDriver(), TIME_OUT);
        highlightMyElement(wait.until(
                ExpectedConditions.elementToBeClickable(findByText(textInElement)))).click();
    }

    public static void waitForAngular() {
        LOGGER.debug("Waiting for angular");
        final int webDriverWaitingTime = 90;
        WebDriverWait wait = new WebDriverWait(getDriver(), webDriverWaitingTime, NAP_PERIOD);
        wait.until(AdditionalConditions.pageLoadWait());
        wait.until(AdditionalConditions.angularHasFinishedProcessing());
        LOGGER.debug("Waiting for angular finished");
    }

    public static boolean isElementDisabled(WebElement element) {
        return highlightMyElement(element).getAttribute("class").contains("view-mode")
                || element.getAttribute("class").contains("disabled") || element.getAttribute("disabled") != null;
    }

    public static boolean isElementDisabled(String dataTestId) {
        return isElementDisabled(
                waitForElementVisibilityByTestId(dataTestId));
    }

    public static void ultimateWait() {
        long startTime = System.nanoTime();

        GeneralUIUtils.waitForLoader();
        GeneralUIUtils.waitForBackLoader();
        GeneralUIUtils.waitForAngular();

        long estimateTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toSeconds(estimateTime - startTime);
        if (duration > TIME_OUT) {
            getExtendTest().log(Status.WARNING, String.format("Delays on page, %d seconds", duration));
        }
    }

    public static WebElement unhideElement(WebElement element, String attributeValue) {
        String js = "arguments[0].setAttribute('class','" + attributeValue + "');";
        ((JavascriptExecutor) getDriver()).executeScript(js, element);
        return element;
    }

    public static WebElement findByText(String textInElement) {
        return getDriver().findElement(searchByTextContaining(textInElement));
    }

    public static By searchByTextContaining(String textInElement) {
        return By.xpath("//*[contains(text(),'" + textInElement + "')]");
    }

    public static WebElement getClickableButtonBy(By by, int timout) {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), timout);
            return wait.until(ExpectedConditions.elementToBeClickable(by));
        } catch (Exception e) {
            return null;
        }
    }


    public static WebElement getButtonWithText(String textInButton) {
        try {
            return getDriver().findElement(By.xpath("//button[contains(text(),'" + textInButton + "')]"));
        } catch (Exception e) {
            return null;
        }
    }

    public static void closeErrorMessage() {
        WebElement okWebElement = getButtonWithText("OK");
        if (okWebElement != null) {
            okWebElement.click();
            ultimateWait();
        }
    }

    public static void waitForBackLoader() {
        waitForBackLoader(TIME_OUT);
    }

    public static void waitForBackLoader(int timeOut) {
        sleep(NAP_PERIOD);
        final String backLoaderClass = "tlv-loader-back";
        LOGGER.debug("Waiting {}s for '.{}'", timeOut, backLoaderClass);
        waitForElementInVisibilityBy(By.className(backLoaderClass), timeOut);
    }

    public static Object getElementPositionOnCanvas(String elementName) {
        String scriptJS = "var cy = window.jQuery('.sdc-composition-graph-wrapper').cytoscape('get');\n"
                + "var n = cy.nodes('[name=\"" + elementName + "\"]');\n"
                + "var nPos = n.renderedPosition();\n"
                + "return JSON.stringify({\n"
                + "\tx: nPos.x,\n"
                + "\ty: nPos.y\n"
                + "})";
        return ((JavascriptExecutor) getDriver()).executeScript(scriptJS);
    }

}
