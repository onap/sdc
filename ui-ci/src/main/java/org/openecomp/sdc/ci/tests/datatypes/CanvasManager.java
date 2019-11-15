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

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.ci.tests.datatypes.enums.CircleSize;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.PropertiesAssignmentPage;
import org.openecomp.sdc.ci.tests.pages.PropertyNameBuilder;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.SkipException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class CanvasManager {
    public static final int VIEW_BUTTON_X_DELTA = 30;
    public static final int VIEW_BUTTON_Y_DELTA = 11;
    public static final int DELTET_BUTTON_X_DELTA = 30;
    public static final int CANVAS_ELEMENT_TYPE_SPLITER = 4;
    public static final int MAX_WAIT_DIVIDER = 1000;
    public static final int THREAD_SLEEP_TIME = 5000;
    private Map<String, CanvasElement> canvasElements;
    private Actions actions;
    private WebElement canvas;
    private int reduceCanvasWidthFactor;
    private CanvasElement canvasElement;
    // Offsets Are used to find upper right corner of canvas element in order to
    // connect links
    private static final int CANVAS_VF_Y_OFFSET = 30;
    private static final int CANVAS_VF_X_OFFSET = 18; // 14 - 27

    private static final int CANVAS_NORMATIVE_ELEMENT_Y_OFFSET = 12;
    private static final int CANVAS_NORMATIVE_ELEMENT_X_OFFSET = 7;

    private static final int CANVAS_SERVICE_Y_OFFSET = 27;
    private static final int CANVAS_SERVICE_X_OFFSET = 16;

    private CanvasManager() {
        canvasElements = new HashMap<>();
        actions = new Actions(GeneralUIUtils.getDriver());
        canvas = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralCanvasItems.CANVAS.getValue());
        try {
            WebElement webElement = GeneralUIUtils
                    .getWebElementByTestID(DataTestIdEnum.GeneralCanvasItems.CANVAS_RIGHT_PANEL.getValue());
            reduceCanvasWidthFactor = webElement.getSize().width;
        } catch (Exception e) {
            reduceCanvasWidthFactor = 0;
        }
    }

    public static CanvasManager getCanvasManager() {
        return new CanvasManager();
    }

    public List<CanvasElement> getCanvasElements() {
        return canvasElements.values().stream().collect(Collectors.toList());
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
        if (canvasElementsFromSameTemplate.size() > 0) {
            String newName = prefix + " " + canvasElementsFromSameTemplate.size();
            element.setElementNameOnCanvas(newName);
        }

        canvasElements.put(element.getUniqueId(), element);
    }

    private void moveElementOnCanvas(CanvasElement canvasElement, ImmutablePair<Integer, Integer> newLocation)
            throws Exception {
        GeneralUIUtils.waitForLoader();
        actions.moveToElement(canvas, canvasElement.getLocation().left, canvasElement.getLocation().right);
        actions.clickAndHold();
        actions.moveToElement(canvas, newLocation.left, newLocation.right);
        actions.release();
        actions.perform();
        canvasElement.setLocation(newLocation);
        GeneralUIUtils.waitForLoader();

    }

    public void moveToFreeLocation(String containerName) {
        final int maxWait = 5000;
        final int napPeriod = 200;
        int sumOfWaiting = 0;
        boolean isKeepWaiting = false;
        while (!isKeepWaiting) {
            ImmutablePair<Integer, Integer> freePosition = getFreePosition();
            actions.moveToElement(canvas, freePosition.left, freePosition.right);
            actions.clickAndHold();
            actions.release();
            actions.perform();
            isKeepWaiting = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionRightPanel.COMPONENT_TITLE.getValue()).getText()
                    .equals(containerName);
            sumOfWaiting += napPeriod;
            if (sumOfWaiting > maxWait) {
                Assert.fail("Can't click on VF");
            }
        }
    }

    public void clickOnCanvaElement(CanvasElement canvasElement) {
        ImmutablePair<Integer, Integer> coordinates = getElementCoordinates(canvasElement.getElementNameOnCanvas());
        actions.moveToElement(canvas, coordinates.left, coordinates.right);
        actions.clickAndHold();
        actions.release();
        actions.perform();
        GeneralUIUtils.ultimateWait();
        actions.click().perform();
        GeneralUIUtils.ultimateWait();

        validateInstanceSelected(canvasElement);
        ExtentTestActions.log(Status.INFO, String.format("Canvas element %s selected", canvasElement.getElementType()));
    }

    public void openLinkPopupReqsCapsConnection(CanvasElement canvasElement) {
        ExtentTestActions.log(Status.INFO, "Open Link popup");
        clickOnCanvasLink(canvasElement);
        int x = canvasElement.getLocation().getLeft() + VIEW_BUTTON_X_DELTA;
        int y = canvasElement.getLocation().getRight() + VIEW_BUTTON_Y_DELTA;
        clickOnCanvasPosition(x, y);
        GeneralUIUtils.ultimateWait();
    }

    public void openLinkPopupReqsCapsConnection(CanvasElement sourceElement, CanvasElement destElement) {
        ExtentTestActions.log(Status.INFO, "Open Link popup");
        ImmutablePair<Integer, Integer> sourceCoordinates = getElementCoordinates(sourceElement.getElementNameOnCanvas());
        ImmutablePair<Integer, Integer> destCoordinates = getElementCoordinates(destElement.getElementNameOnCanvas());
        ImmutablePair<Integer, Integer> linkPosition = calcMidOfLink(sourceCoordinates, destCoordinates);

        clickOnCanvasPosition(linkPosition.left, linkPosition.right); // click on link
        int x = linkPosition.left + VIEW_BUTTON_X_DELTA;
        int y = linkPosition.right + VIEW_BUTTON_Y_DELTA;
        clickOnCanvasPosition(x, y); // click on view popup
        GeneralUIUtils.ultimateWait();
    }

    public void closeLinkPopupReqsCapsConnection() {
        GeneralUIUtils.clickOnElementByTestId("Cancel");
        //GeneralUIUtils.ultimateWait();
    }

    public void clickSaveOnLinkPopup() {
        ExtentTestActions.log(Status.INFO, "Click save on link popup");
        GeneralUIUtils.clickOnElementByTestId("Save");
        //GeneralUIUtils.ultimateWait();
    }

    public void deleteLinkPopupReqsCapsConnection(CanvasElement canvasElement) {
        ExtentTestActions.log(Status.INFO, "Delete Link ");
        clickOnCanvasLink(canvasElement);
        int x = canvasElement.getLocation().getLeft() + DELTET_BUTTON_X_DELTA;
        int y = canvasElement.getLocation().getRight() + DELTET_BUTTON_X_DELTA;
        clickOnCanvasPosition(x, y);
    }

    public void deleteLinkPopupReqsCapsConnection(CanvasElement sourceElement, CanvasElement destElement) {
        ExtentTestActions.log(Status.INFO, "Delete Link ");
        ImmutablePair<Integer, Integer> sourceCoordinates = getElementCoordinates(sourceElement.getElementNameOnCanvas());
        ImmutablePair<Integer, Integer> destCoordinates = getElementCoordinates(destElement.getElementNameOnCanvas());
        ImmutablePair<Integer, Integer> linkPosition = calcMidOfLink(sourceCoordinates, destCoordinates);
        clickOnCanvasPosition(linkPosition.left, linkPosition.right); // click on link
        int x = linkPosition.left + DELTET_BUTTON_X_DELTA;
        int y = linkPosition.right + DELTET_BUTTON_X_DELTA;
        clickOnCanvasPosition(x, y);
    }

    public void clickOnCanvasLink(CanvasElement canvasElement) {
        actions.moveToElement(canvas, canvasElement.getLocation().left, canvasElement.getLocation().right);
        actions.click().perform();
        GeneralUIUtils.ultimateWait();
    }

    public void clickOnCanvasPosition(int x, int y) {

        try {
            actions.moveToElement(canvas, x, y);
            actions.click();
            actions.perform();
            GeneralUIUtils.ultimateWait();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void moveElementOnCanvas(CanvasElement canvasElement) throws Exception {
        moveElementOnCanvas(canvasElement, getFreePosition());
    }

    public void deleteElementFromCanvas(CanvasElement canvasElement) throws Exception {
        GeneralUIUtils.waitForLoader();
        actions.moveToElement(canvas, canvasElement.getLocation().left, canvasElement.getLocation().right);
        actions.click();
        actions.perform();
        ExtentTestActions.log(Status.INFO, String.format("Removing canvas element %s ", canvasElement.getElementType()));
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralCanvasItems.DELETE_INSTANCE_BUTTON.getValue())
                .click();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.DELETE_INSTANCE_CANCEL.getValue()).click();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralCanvasItems.DELETE_INSTANCE_BUTTON.getValue())
                .click();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.DELETE_INSTANCE_OK.getValue()).click();
        canvasElements.remove(canvasElement.getUniqueId());
        GeneralUIUtils.ultimateWait();
        if (canvasElement.getElementType().contains("-")) {
            ExtentTestActions.log(Status.INFO, String.format(
                    "Canvas element %s is removed",
                    canvasElement.getElementType().split("-")[CANVAS_ELEMENT_TYPE_SPLITER]));
        } else {
            ExtentTestActions.log(Status.INFO, String.format(
                    "Canvas element %s is removed",
                    canvasElement.getElementType()));
        }
    }

    private WebElement findClickElement(String dataTestId) {
        int attempts = 0;
        while (attempts < 2) {
            try {
                return GeneralUIUtils.getWebElementByTestID(dataTestId);
            } catch (StaleElementReferenceException e) {
            }
            attempts++;
        }
        return null;
    }

    public CanvasElement createElementOnCanvas(String elementName) throws Exception {
        String actionDuration = GeneralUIUtils.getActionDuration(() -> {
            try {
                canvasElement = createElementOnCanvasWithoutDuration(elementName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (canvasElement != null) {
            ExtentTestActions.log(Status.INFO, String.format("The element %s should now be on the canvas", elementName), actionDuration);
        }
        return canvasElement;
    }

    private CanvasElement createElementOnCanvasWithoutDuration(String elementDataTestId) throws Exception {
        final int offset = 20;
        try {
            CompositionPage.searchForElement(elementDataTestId);
            WebElement element = findClickElement(elementDataTestId);
            ImmutablePair<Integer, Integer> freePosition = getFreePosition();
            actions.moveToElement(element, offset, offset);
            actions.clickAndHold();
            actions.moveToElement(canvas, freePosition.left, freePosition.right);
            actions.release();
            actions.perform();
            GeneralUIUtils.ultimateWait();
            String uniqueId = elementDataTestId + "_" + UUID.randomUUID().toString();
            CanvasElement canvasElement = new CanvasElement(uniqueId, freePosition, elementDataTestId);
            addCanvasElement(canvasElement);
            GeneralUIUtils.ultimateWait();
            return canvasElement;
        } catch (Exception e) {
            System.out.println("Can't create element on canvas");
            e.printStackTrace();
        }
        return null;
    }

    public CanvasElement createElementOnCanvas(LeftPanelCanvasItems canvasItem) throws Exception {
        return createElementOnCanvas(canvasItem.getValue());
    }

    private ImmutablePair<Integer, Integer> getFreePosition() {
        ImmutablePair<Integer, Integer> randomPosition = null;
        boolean freePosition = false;
        final int minSpace = 150;
        while (!freePosition) {
            ImmutablePair<Integer, Integer> tempRandomPosition = getRandomPosition();
            freePosition = !canvasElements.values().stream().map(e -> e.getLocation())
                    .filter(e -> Math.abs(e.left - tempRandomPosition.left) < minSpace
                            && Math.abs(e.right - tempRandomPosition.right) < minSpace)
                    .findAny().isPresent();
            randomPosition = tempRandomPosition;
        }
        return randomPosition;
    }

    private ImmutablePair<Integer, Integer> getRandomPosition() {
        final int edgeBuffer = 50;
        Random random = new Random();
        int xElement = random.nextInt(canvas.getSize().width - 2 * edgeBuffer - reduceCanvasWidthFactor) + edgeBuffer;
        int yElement = random.nextInt(canvas.getSize().height - 2 * edgeBuffer) + edgeBuffer;
        return new ImmutablePair<Integer, Integer>(xElement, yElement);
    }

    // Will work only if 2 elements are big sized (VF size), if one of the elements is Small use the function linkElements
    public void linkElements(CanvasElement firstElement, CanvasElement secondElement) throws Exception {
        ExtentTestActions.log(Status.INFO, String.format("Linking between the %s instance and the %s instance.", firstElement.getElementType(), secondElement.getElementType()));
        drawSimpleLink(firstElement.getElementNameOnCanvas(), secondElement.getElementNameOnCanvas());
        selectReqAndCapAndConnect();
        ExtentTestActions.log(Status.INFO, String.format("The instances %s and %s should now be connected.", firstElement.getElementType(), secondElement.getElementType()));
    }

    // old version, depricated
    public void linkElements(CanvasElement firstElement, CircleSize firstElementSize, CanvasElement secondElement, CircleSize secondElementSize) throws Exception {
        drawSimpleLink(firstElement, firstElementSize, secondElement, secondElementSize);
        selectReqAndCapAndConnect();
        ExtentTestActions.log(Status.INFO, String.format("The instances %s and %s should now be connected.", firstElement.getElementType(), secondElement.getElementType()));
    }

    public void linkElements(String firstElement, String secondElement) throws Exception {
        drawSimpleLink(firstElement, secondElement);
        selectReqAndCapAndConnect();
        ExtentTestActions.log(Status.INFO, String.format("The instances %s and %s should now be connected.", firstElement, secondElement));
    }

    // use JS to get coordinates of elements
    private void drawSimpleLink(String firstElement, String secondElement) {
        ImmutablePair<Integer, Integer> firstElementCoordinates = getGreenDotCoordinatesOfElement(firstElement);
        ImmutablePair<Integer, Integer> secondElementCoordinates = getElementCoordinates(secondElement);

        actions.moveToElement(canvas, firstElementCoordinates.left, firstElementCoordinates.right);
        actions.perform();
        actions.moveToElement(canvas, firstElementCoordinates.left, firstElementCoordinates.right);
        actions.clickAndHold();
        actions.moveToElement(canvas, secondElementCoordinates.left, secondElementCoordinates.right);
        actions.release();
        actions.perform();
        GeneralUIUtils.ultimateWait();
    }

    private void selectReqAndCapAndConnect() throws Exception {
        addFirstReqOrCapAndPressNext();
        addFirstReqOrCapAndPressNext();
        linkMenuClickOnFinishButton();
    }

    private void addFirstReqOrCapAndPressNext() throws Exception {
        addFirstReqOrCap();
        linkMenuClickOnNextButton();
    }

    private void addFirstReqOrCap() {
        GeneralUIUtils.getWebElementsListByClassName(DataTestIdEnum.LinkMenuItems.LINK_ITEM_CAP_OR_REQ.getValue()).get(0).click();
    }

    private void linkMenuClickOnNextButton() throws Exception {
        GeneralUIUtils.clickOnElementByText("Next");
        GeneralUIUtils.ultimateWait();
    }

    private void linkMenuClickOnFinishButton() throws Exception {
        GeneralUIUtils.clickOnElementByText("Finish");
        GeneralUIUtils.ultimateWait();
    }


    private void drawSimpleLink(CanvasElement firstElement, CanvasElement secondElement) throws Exception {
        int yOffset = CANVAS_VF_Y_OFFSET;
        int xOffset = CANVAS_VF_X_OFFSET;

        actions.moveToElement(canvas, firstElement.getLocation().left + xOffset,
                firstElement.getLocation().right - yOffset);

        actions.clickAndHold();
        actions.moveToElement(canvas, secondElement.getLocation().left + xOffset, secondElement.getLocation().right - yOffset);
        actions.release();
        actions.perform();
        GeneralUIUtils.ultimateWait();
    }

    private void drawSimpleLink(CanvasElement firstElement, CircleSize firstElementSize, CanvasElement secondElement, CircleSize secondElementSize) throws Exception {
        ExtentTestActions.log(Status.INFO, String.format("Linking between the %s instance and the %s instance.", firstElement.getElementType(), secondElement.getElementType()));
        Integer yOffset = getCircleOffset(firstElementSize).right;
        Integer xOffset = getCircleOffset(firstElementSize).left;
        firstElement.getElementType();


        actions.moveToElement(canvas, firstElement.getLocation().left + xOffset,
                firstElement.getLocation().right - yOffset);

        actions.clickAndHold();

        yOffset = getCircleOffset(secondElementSize).right;
        xOffset = getCircleOffset(secondElementSize).left;

        actions.moveToElement(canvas, secondElement.getLocation().left + xOffset, secondElement.getLocation().right - yOffset);
        actions.release();
        actions.build();
        actions.perform();
        GeneralUIUtils.ultimateWait();
    }

    private Pair<Integer, Integer> getCircleOffset(CircleSize circleSize) {
        Pair<Integer, Integer> circleSizes;
        if (circleSize.equals(CircleSize.VF)) {
            circleSizes = new Pair<Integer, Integer>(CANVAS_VF_X_OFFSET, CANVAS_VF_Y_OFFSET);
        } else if (circleSize.equals(CircleSize.NORMATIVE)) {
            circleSizes = new Pair<Integer, Integer>(CANVAS_NORMATIVE_ELEMENT_X_OFFSET, CANVAS_NORMATIVE_ELEMENT_Y_OFFSET);
        } else {
            circleSizes = new Pair<Integer, Integer>(CANVAS_SERVICE_X_OFFSET, CANVAS_SERVICE_Y_OFFSET);
        }
        return circleSizes;
    }

    public String updateElementNameInCanvas(CanvasElement canvasElement, String newInstanceName) {
        GeneralUIUtils.ultimateWait();
        clickOnCanvaElement(canvasElement);
        GeneralUIUtils.getWebElementBy(By.id("editPencil")).click();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.RENAME_INSTANCE_CANCEL.getValue()).click();
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            sumOfWaiting += napPeriod;
            if (sumOfWaiting > maxWait) {
                throw new SkipException(String.format("Bug 342260, can't select instance properly, waited for %s seconds after click on instance", (int) (maxWait / MAX_WAIT_DIVIDER)));
            }
        } while (!isInstanceSelected);
    }

    public void validateLinkIsSelected() {
        final long maxWait = 5000;
        final long napPeriod = 200;
        long sumOfWaiting = 0;
        boolean isInstanceSelected;
        do {
            isInstanceSelected = GeneralUIUtils.isWebElementExistByClass("w-sdc-menu-item w-sdc-canvas-menu-item-view");

            if (!isInstanceSelected) {
                try {
                    TimeUnit.MILLISECONDS.sleep(napPeriod);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            sumOfWaiting += napPeriod;
            if (sumOfWaiting > maxWait) {
                Assert.fail(String.format("Can't select link properly, waited for %s seconds", (int) (maxWait / MAX_WAIT_DIVIDER)));
            }
        } while (!isInstanceSelected);
    }

    private void selectReqCapByName(String reqCapName) {
        GeneralUIUtils.clickOnElementByText(reqCapName);
        GeneralUIUtils.ultimateWait();
    }

    private void selectTypeOfReqCap(String dataTestId, String reqCapType) {
        GeneralUIUtils.selectByValueTextContained(dataTestId, reqCapType);
    }

    public void linkElementsAndSelectCapReqTypeAndCapReqName(CanvasElement firstElement, CanvasElement secondElement, ConnectionWizardPopUpObject connectionWizardPopUpObject) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating link between %s and %s", firstElement.getElementType(), secondElement.getElementType()));
        drawSimpleLink(firstElement.getElementNameOnCanvas(), secondElement.getElementNameOnCanvas());
        selectTypeOfReqCap(DataTestIdEnum.LinkMenuItems.REQ_CAP_SELECT_DATA_TESTS_ID.getValue(), connectionWizardPopUpObject.getCapabilityTypeSecondItem());
        addFirstReqOrCapAndPressNext();
        selectReqCapByName(connectionWizardPopUpObject.getCapabilityNameSecondItem());
        linkMenuClickOnNextButton();
        linkMenuClickOnFinishButton();
    }

    public Map<String, String> linkElementsWithCapPropAssignment(CanvasElement firstElement, CanvasElement secondElement, ConnectionWizardPopUpObject connectionWizardPopUpObject) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating link between %s and %s", firstElement.getElementType(), secondElement.getElementType()));
        drawSimpleLink(firstElement.getElementNameOnCanvas(), secondElement.getElementNameOnCanvas());
        selectTypeOfReqCap(DataTestIdEnum.LinkMenuItems.REQ_CAP_SELECT_DATA_TESTS_ID.getValue(), connectionWizardPopUpObject.getCapabilityTypeSecondItem());
        addFirstReqOrCapAndPressNext();
        selectReqCapByName(connectionWizardPopUpObject.getCapabilityNameSecondItem());
        linkMenuClickOnNextButton();
        Map<String, String> mapOfValues = connectionWizardAssignCapPropValues();
        linkMenuClickOnFinishButton();
        Thread.sleep(THREAD_SLEEP_TIME);
        return mapOfValues;
    }


    public Map<String, String> connectionWizardAssignCapPropValues() throws Exception {
        //get list of capability property value fields data-tests-ids in connection wizard
        List<String> valueField = getListOfValueFieldIDs();
        //get map of field ids and their values, fill in values if empty
        Map<String, String> propValues = getMapOfCapPropValues(valueField, true);
        return propValues;
    }

    public Map<String, String> connectionWizardCollectCapPropValues() throws Exception {
        //get list of capability property value fields data-tests-ids in connection wizard
        List<String> valueField = getListOfValueFieldIDs();
        //get map of field ids and their values, collect existing values
        Map<String, String> propValues = getMapOfCapPropValues(valueField, false);
        return propValues;
    }

    private List<String> getListOfValueFieldIDs() {
        String propName = GeneralUIUtils.getWebElementsListByContainsClassName("multiline-ellipsis-content").get(0).getText();
        List<WebElement> valueNameElement = GeneralUIUtils.findElementsByXpath("//div[@class='dynamic-property-row nested-level-1']/div[1]");
        List<String> valueName = new ArrayList<>();
        for (int i = 0; i < valueNameElement.size(); i++) {
            valueName.add(valueNameElement.get(i).getText());
        }
        //get list of value field names as appear in data-tests-id
        List<String> valueField = new ArrayList<>();
        for (int i = 0; i < valueName.size(); i++) {
            valueField.add(PropertyNameBuilder.buildIComplexField(propName, valueName.get(i)));
        }
        return valueField;
    }

    private Map<String, String> getMapOfCapPropValues(List<String> valueField, boolean isValueAssign) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Assigning values to properties of capabilities and/or collecting existing ones"));
        Map<String, String> propValues = new HashMap<>();
        for (int i = 0; i < valueField.size(); i++) {
            String fieldId = valueField.get(i);
            if (GeneralUIUtils.getWebElementByTestID(fieldId).getAttribute("value").isEmpty() && isValueAssign) {
                //add value and put into map
                propValues.put(fieldId, "value" + i);
                PropertiesAssignmentPage.editPropertyValue(fieldId, "value" + i);
            } else {
                //put existing value into map
                propValues.put(fieldId, GeneralUIUtils.getWebElementByTestID(valueField.get(i)).getAttribute("value"));
            }
        }
        return propValues;
    }


    public ImmutablePair<Integer, Integer> calcMidOfLink(ImmutablePair<Integer, Integer> location1, ImmutablePair<Integer, Integer> location2) {
        int x = (location1.getLeft() + location2.getLeft()) / 2;
        int y = (location1.getRight() + location2.getRight()) / 2;

        ImmutablePair<Integer, Integer> location = new ImmutablePair<>(x, y);
        return location;
    }

    public ImmutablePair<Integer, Integer> getElementCoordinates(String elementName) {
        Object position = GeneralUIUtils.getElementPositionOnCanvas(elementName);
        return converJSJsonToCoordinates(position);
    }

    public ImmutablePair<Integer, Integer> getGreenDotCoordinatesOfElement(String elementName) {
        Object position = GeneralUIUtils.getElementGreenDotPositionOnCanvas(elementName);
        return converJSJsonToCoordinates(position);
    }

    public ImmutablePair<Integer, Integer> converJSJsonToCoordinates(Object position) {
        JsonElement root = new JsonParser().parse(position.toString());
        int xElement = root.getAsJsonObject().get("x").getAsInt();
        int yElement = root.getAsJsonObject().get("y").getAsInt();
        return new ImmutablePair<Integer, Integer>(xElement, yElement);
    }
}
