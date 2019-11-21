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

package org.openecomp.sdc.ci.tests.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base UI test object that represents a page or component in a html page
 */
public abstract class AbstractPageObject implements PageObject {

    protected final WebDriver webDriver;
    protected int timeoutInSeconds;

    public AbstractPageObject(final WebDriver webDriver) {
        this.webDriver = webDriver;
        timeoutInSeconds = 10;
    }

    /**
     * Sets the default timeout for Page Object actions.
     */
    public void setTimeout(final int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    /**
     * Creates a WebDriverWait instance with the default timeout.
     *
     * @return a new WebDriverWait instance
     */
    protected WebDriverWait getWait() {
        return new WebDriverWait(webDriver, timeoutInSeconds);
    }

    /**
     * Creates a WebDriverWait instance with the provided timeout.
     *
     * @param timeoutInSeconds the wait timeout in seconds
     * @return a new WebDriverWait instance
     */
    protected WebDriverWait getWait(final int timeoutInSeconds) {
        return new WebDriverWait(webDriver, timeoutInSeconds);
    }

    /**
     * Find an element based on the provided locator.
     *
     * @param locator the By locator
     * @return the WebElement if found, otherwise throws an exception
     */
    protected WebElement findElement(final By locator) {
        return webDriver.findElement(locator);
    }

    /**
     * Find elements based on the provided locator.
     *
     * @param locator the By locator
     * @return the list of WebElement if any found, otherwise throws an exception
     */
    protected List<WebElement> findElements(final By locator) {
        return webDriver.findElements(locator);
    }

    /**
     * Find an element inside the provided element using the provided xpath.
     *
     * @param element the parent element
     * @param xpath the xpath expression to search for the internal element
     * @return the WebElement if found, otherwise throws an exception
     */
    protected WebElement findSubElement(final WebElement element, final String xpath) {
        return findSubElement(element, By.xpath(xpath));
    }

    /**
     * Find an element inside the provided element using the provided By locator.
     *
     * @param element the parent element
     * @param locator the By locator to search for the internal element
     * @return the WebElement if found, otherwise throws an exception
     */
    protected WebElement findSubElement(final WebElement element, final By locator) {
        return element.findElement(locator);
    }

    /**
     * Find elements inside the provided element using the provided By locator.
     *
     * @param element the parent element
     * @param locator the By locator to search for the internal element
     * @return the list of WebElement if any found, otherwise throws an exception
     */
    protected List<WebElement> findSubElements(final WebElement element, final By locator) {
        return element.findElements(locator);
    }

    /**
     * Waits for element visibility with the default timeout.
     *
     * @param xpath the xpath expression to search for the element
     * @return the WebElement if visible before timeout, otherwise throws an exception
     */
    protected WebElement waitForElementVisibility(final String xpath) {
        return waitForElementVisibility(By.xpath(xpath));
    }

    /**
     * Waits for element visibility with the default timeout.
     *
     * @param locator the By locator to search for the element
     * @return the WebElement if visible before timeout, otherwise throws an exception
     */
    protected WebElement waitForElementVisibility(final By locator) {
        return getWait(timeoutInSeconds)
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits for element visibility with the provided timeout.
     *
     * @param locator the By locator to search for the element
     * @param timeoutInSeconds the wait timeout in seconds
     * @return the WebElement if visible before timeout, otherwise throws an exception
     */
    protected WebElement waitForElementVisibility(final By locator, final int timeoutInSeconds) {
        return getWait(timeoutInSeconds)
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits for element invisibility with the default timeout.
     *
     * @param locator the By locator to search for the element
     * @return the WebElement if invisible before timeout, false otherwise
     */
    protected Boolean waitForElementInvisibility(final By locator) {
        return getWait(timeoutInSeconds)
            .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

}
