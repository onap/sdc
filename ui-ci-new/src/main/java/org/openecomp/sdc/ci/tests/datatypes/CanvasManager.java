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

package org.openecomp.sdc.ci.tests.datatypes;

import static org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils.getDriver;

import com.aventstack.extentreports.Status;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.ci.tests.exception.CanvasManagerRuntimeException;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;

public final class CanvasManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanvasManager.class);

    public static final int MAX_WAIT_DIVIDER = 1000;
    private Map<String, CanvasElement> canvasElements;
    private WebElement canvas;
    private int canvasRightWidthMargin;
    private final Random random = new Random();
    private static final int HEIGHT_TOP_MARGIN = 100;
    private static final int HEIGHT_BOTTOM_MARGIN = 0;
    private static final int WIDTH_LEFT_MARGIN = 0;

    private CanvasManager() {
        canvasElements = new HashMap<>();
        canvas = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralCanvasItems.CANVAS.getValue());
        loadRightWidthMargin();
    }

    private void loadRightWidthMargin() {
        int canvasRightPalletWidth = 100;
        try {
            WebElement webElement = GeneralUIUtils
                    .getWebElementByTestID(DataTestIdEnum.GeneralCanvasItems.CANVAS_RIGHT_PANEL.getValue());

            canvasRightWidthMargin = webElement.getSize().getWidth();
        } catch (final Exception e) {
            LOGGER.debug("An error occurred while obtaining Canvas right panel size", e);
            canvasRightWidthMargin = 0;
        }
        canvasRightWidthMargin += canvasRightPalletWidth;
        LOGGER.debug("Canvas right width margin {}", canvasRightWidthMargin);
    }

    public static CanvasManager getCanvasManager() {
        return new CanvasManager();
    }

    private void addCanvasElement(CanvasElement element) {
        String prefix = element.getElementType();
        List<CanvasElement> canvasElementsFromSameTemplate = new ArrayList<>();

        // collect all elements from from same template
        for (CanvasElement currElement : canvasElements.values()) {
            if (currElement.getElementNameOnCanvas().toLowerCase().startsWith(prefix.toLowerCase())) {
                canvasElementsFromSameTemplate.add(currElement);
            }
        }

        // match element name to actual name on canvas
        if (!canvasElementsFromSameTemplate.isEmpty()) {
            String newName = prefix + " " + canvasElementsFromSameTemplate.size();
            element.setElementNameOnCanvas(newName);
        }

        canvasElements.put(element.getUniqueId(), element);
    }

    public void clickOnCanvaElement(final CanvasElement canvasElement) {
        final ImmutablePair<Integer, Integer> coordinates = getElementCoordinates(canvasElement.getElementNameOnCanvas());
        final Actions actions1 = new Actions(getDriver());

        LOGGER.debug("Canvas {},{}", canvas.getSize().getWidth(), canvas.getSize().getHeight());
        LOGGER.debug("Canvas {},{}", getWidth(), getHeight());
        LOGGER.debug("ClickOnCanvaElement {},{}", coordinates.left, coordinates.right);
        final ImmutablePair<Integer, Integer> offsetFromCenter =
            calculateOffsetFromCenter(coordinates.getKey(), coordinates.getValue());
        LOGGER.debug("Offset from center {},{}", offsetFromCenter.left, offsetFromCenter.right);
        actions1.moveToElement(canvas, offsetFromCenter.left, offsetFromCenter.right)
            .clickAndHold()
            .release()
            .perform();
        GeneralUIUtils.ultimateWait();
        actions1.click().perform();
        GeneralUIUtils.ultimateWait();

        validateInstanceSelected(canvasElement);
        ExtentTestActions.log(Status.INFO, String.format("Canvas element %s selected", canvasElement.getElementType()));
    }

    private WebElement findClickElement(String dataTestId) {
        int attempts = 0;
        while (attempts < 2) {
            try {
                return GeneralUIUtils.getWebElementByTestID(dataTestId);
            } catch (final StaleElementReferenceException e) {
                LOGGER.debug("Could not get element with test id {}", dataTestId, e);
            }
            attempts++;
        }
        return null;
    }

    public CanvasElement createElementOnCanvas(String elementName) {
        final CanvasElement[] canvasElement = new CanvasElement[1];
        String actionDuration = GeneralUIUtils
            .getActionDuration(() -> canvasElement[0] = createElementOnCanvasWithoutDuration(elementName));

        if (canvasElement[0] != null) {
            ExtentTestActions.log(Status.INFO, String.format("The element %s should now be on the canvas", elementName), actionDuration);
        }
        return canvasElement[0];
    }

    private CanvasElement createElementOnCanvasWithoutDuration(final String elementDataTestId) {
        final int offset = 20;
        try {
            CompositionPage.searchForElement(elementDataTestId);
            final WebElement element = findClickElement(elementDataTestId);
            if (element == null) {
                throw new CanvasManagerRuntimeException(
                    String.format("Could not find element with test id '%s'", elementDataTestId));
            }
            final ImmutablePair<Integer, Integer> freePositionInCanvas = getFreePositionInCanvas(500);
            final ImmutablePair<Integer, Integer> offsetFromCenter = calculateOffsetFromCenter(
                freePositionInCanvas.getKey(), freePositionInCanvas.getValue());
            new Actions(getDriver())
                .moveToElement(element, offset, offset)
                .clickAndHold()
                .moveToElement(canvas, offsetFromCenter.getKey(), offsetFromCenter.getValue())
                .release()
                .perform();
            GeneralUIUtils.ultimateWait();
            final String uniqueId = elementDataTestId + "_" + UUID.randomUUID().toString();
            final CanvasElement canvasElement = new CanvasElement(uniqueId, offsetFromCenter, elementDataTestId);
            addCanvasElement(canvasElement);
            GeneralUIUtils.ultimateWait();
            return canvasElement;
        } catch (final Exception e) {
            throw new CanvasManagerRuntimeException("Could not create element on canvas", e);
        }
    }

    public CanvasElement createElementOnCanvas(LeftPanelCanvasItems canvasItem) {
        return createElementOnCanvas(canvasItem.getValue());
    }

    private ImmutablePair<Integer, Integer> getFreePositionInCanvas(int maxAttempts) {
        boolean isPositionFree;
        final int minSpace = 150;
        for (int attemptCount = 0; attemptCount < maxAttempts; attemptCount++) {
            final ImmutablePair<Integer, Integer> randomPositionInCanvas = getRandomPositionInCanvas();
            isPositionFree = canvasElements.values().stream()
                .map(CanvasElement::getLocation)
                .noneMatch(location -> Math.abs(location.left - randomPositionInCanvas.left) < minSpace
                    && Math.abs(location.right - randomPositionInCanvas.right) < minSpace);
            if(isPositionFree) {
                return randomPositionInCanvas;
            }
        }
        throw new CanvasManagerRuntimeException("Could not find a free Canvas position");
    }

    private ImmutablePair<Integer, Integer> getRandomPositionInCanvas() {
        int x = random.nextInt(getWidth());
        final int maxAllowedWidth = getWidth() - getWidthRightMargin();
        if (x > maxAllowedWidth) {
            x = x - getWidthRightMargin();
        } else if (x < WIDTH_LEFT_MARGIN) {
            x = x + WIDTH_LEFT_MARGIN;
        }

        int y = random.nextInt(getHeight());
        int maxAllowedHeight = getHeight() - HEIGHT_BOTTOM_MARGIN;

        if (y > maxAllowedHeight) {
            y = y - HEIGHT_BOTTOM_MARGIN;
        } else if (y < HEIGHT_TOP_MARGIN) {
            y = y + HEIGHT_TOP_MARGIN;
        }

        LOGGER.debug("Generated random position in canvas {},{}", x, y);

        return new ImmutablePair<>(x, y);
    }

    private ImmutablePair<Integer, Integer> calculateOffsetFromCenter(final int realPositionX, final int realPositionY) {
        final ImmutablePair<Integer, Integer> canvasCenter = getCanvasCenter();
        final int centerX = canvasCenter.getKey();
        final int centerY = canvasCenter.getValue();
        final int positionX = realPositionX - centerX;
        final int positionY = realPositionY - centerY;
        LOGGER.debug("Position {},{} offset from center {},{} is {},{}", realPositionX, realPositionY,
            centerX, centerY, positionX, positionY);
        return new ImmutablePair<>(positionX, positionY);
    }

    private ImmutablePair<Integer, Integer> getCanvasCenter() {
        return new ImmutablePair<>(getWidth() / 2, getHeight() / 2);
    }

    private int getWidth() {
        return canvas.getSize().getWidth();
    }

    private int getHeight() {
        return canvas.getSize().getHeight();
    }

    private int getWidthRightMargin() {
        return canvasRightWidthMargin;
    }

    public String updateElementNameInCanvas(CanvasElement canvasElement, String newInstanceName) {
        GeneralUIUtils.ultimateWait();
        clickOnCanvaElement(canvasElement);
        GeneralUIUtils.getWebElementBy(By.id("editPencil")).click();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.RENAME_INSTANCE_CANCEL.getValue()).click();
        final WebDriverWait wait = new WebDriverWait(getDriver(), 10);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("modal-background")));
        GeneralUIUtils.getWebElementBy(By.id("editPencil")).click();
        WebElement instanceNameField = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralCanvasItems.INSTANCE_NAME_FIELD.getValue());
        String oldInstanceName = instanceNameField.getAttribute("value");
        instanceNameField.clear();
        instanceNameField.sendKeys(newInstanceName);
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.RENAME_INSTANCE_OK.getValue()).click();
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.waitForElementInVisibilityByTestId(By.className("w-sdc-modal-resource-instance-name"));
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Name of element instance changed from %s to %s", oldInstanceName, newInstanceName));
        return oldInstanceName;
    }

    /**
     * @param canvasElement Validate that instance was selected on right sidebar
     */
    private void validateInstanceSelected(CanvasElement canvasElement) {
        final long maxWait = 5000;
        final long napPeriod = 200;
        long sumOfWaiting = 0;
        boolean isInstanceSelected;
        do {
            isInstanceSelected = CompositionPage.getSelectedInstanceName().toLowerCase().contains(canvasElement.getElementType().toLowerCase());

            if (!isInstanceSelected) {
                try {
                    TimeUnit.MILLISECONDS.sleep(napPeriod);
                } catch (final InterruptedException e) {
                    LOGGER.debug("Sleep interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }

            sumOfWaiting += napPeriod;
            if (sumOfWaiting > maxWait) {
                throw new SkipException(String.format("Bug 342260, can't select instance properly, waited for %s seconds after click on instance", (int) (maxWait / MAX_WAIT_DIVIDER)));
            }
        } while (!isInstanceSelected);
    }

    public ImmutablePair<Integer, Integer> getElementCoordinates(String elementName) {
        Object position = GeneralUIUtils.getElementPositionOnCanvas(elementName);
        return converJSJsonToCoordinates(position);
    }

    public ImmutablePair<Integer, Integer> converJSJsonToCoordinates(Object position) {
        JsonElement root = new JsonParser().parse(position.toString());
        int xElement = root.getAsJsonObject().get("x").getAsInt();
        int yElement = root.getAsJsonObject().get("y").getAsInt();
        return new ImmutablePair<>(xElement, yElement);
    }
}
