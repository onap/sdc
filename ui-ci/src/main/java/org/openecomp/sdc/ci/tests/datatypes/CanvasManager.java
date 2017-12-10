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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.ci.tests.datatypes.enums.CircleSize;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;

public final class CanvasManager {
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
		int maxWait = 5000;
		int sumOfWaiting = 0;
		int napPeriod = 200;
		boolean isKeepWaiting = false;
		while (!isKeepWaiting) {
			ImmutablePair<Integer, Integer> freePosition = getFreePosition();
			actions.moveToElement(canvas, freePosition.left, freePosition.right);
			actions.clickAndHold();
			actions.release();
			actions.perform();
			isKeepWaiting = GeneralUIUtils.getWebElementByTestID("selectedCompTitle").getText()
					.equals(containerName);
			sumOfWaiting += napPeriod;
			if (sumOfWaiting > maxWait) {
				Assert.fail("Can't click on VF");
			}
		}
	}

	public void clickOnCanvaElement(CanvasElement canvasElement) {
		actions.moveToElement(canvas, canvasElement.getLocation().left, canvasElement.getLocation().right);
		actions.clickAndHold();
		actions.release();
		actions.perform();
		actions.click().perform();
		GeneralUIUtils.ultimateWait();

	    validateInstanceSelected(canvasElement);
		ExtentTestActions.log(Status.INFO, String.format("Canvas element %s selected", canvasElement.getElementType()));
	}

	public void moveElementOnCanvas(CanvasElement canvasElement) throws Exception {
		moveElementOnCanvas(canvasElement, getFreePosition());
	}

	public void deleteElementFromCanvas(CanvasElement canvasElement) throws Exception {
		GeneralUIUtils.waitForLoader();
		actions.moveToElement(canvas, canvasElement.getLocation().left, canvasElement.getLocation().right);
		actions.click();
		actions.perform();
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralCanvasItems.DELETE_INSTANCE_BUTTON.getValue())
				.click();
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.OK.getValue()).click();
		canvasElements.remove(canvasElement.getUniqueId());
		GeneralUIUtils.ultimateWait();
		if (canvasElement.getElementType().contains("-")){
			ExtentTestActions.log(Status.INFO, String.format("Canvas element %s removed", canvasElement.getElementType().split("-")[4]));
		}
		else{
			ExtentTestActions.log(Status.INFO, String.format("Canvas element %s removed", canvasElement.getElementType()));
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

		if (canvasElement != null){
			ExtentTestActions.log(Status.INFO, String.format("The element %s should now be on the canvas", elementName), actionDuration);
		}
		return canvasElement;
	}

	private CanvasElement createElementOnCanvasWithoutDuration(String elementDataTestId) throws Exception {
		try {
			WebElement element = findClickElement(elementDataTestId);
			ImmutablePair<Integer, Integer> freePosition = getFreePosition();
			actions.moveToElement(element, 20, 20);
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
		}
		catch (Exception e) {
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
		int minSpace = 150;
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
		int edgeBuffer = 50;
		Random random = new Random();
		int xElement = random.nextInt(canvas.getSize().width - 2 * edgeBuffer - reduceCanvasWidthFactor) + edgeBuffer;
		int yElement = random.nextInt(canvas.getSize().height - 2 * edgeBuffer) + edgeBuffer;
		return new ImmutablePair<Integer, Integer>(xElement, yElement);
	}

	// Will work only if 2 elements are big sized (VF size), if one of the elements is Small use the function linkElements
	public void linkElements(CanvasElement firstElement, CanvasElement secondElement) throws Exception {
		ExtentTestActions.log(Status.INFO, String.format("Linking between the %s instance and the %s instance.", firstElement.getElementType(), secondElement.getElementType()));
		drawSimpleLink(firstElement, secondElement);
		selectReqAndCapAndConnect();
		ExtentTestActions.log(Status.INFO, String.format("The instances %s and %s should now be connected.", firstElement.getElementType(), secondElement.getElementType()));
	}

	public void linkElements(CanvasElement firstElement, CircleSize firstElementSize, CanvasElement secondElement, CircleSize secondElementSize) throws Exception {
		drawSimpleLink(firstElement,firstElementSize, secondElement,secondElementSize);
		selectReqAndCapAndConnect();
		ExtentTestActions.log(Status.INFO, String.format("The instances %s and %s should now be connected.", firstElement.getElementType(), secondElement.getElementType()));
	}

	private void selectReqAndCapAndConnect() throws Exception {
		addFitstReqOrCapAndPressNext();
		addFitstReqOrCapAndPressNext();
		linkMenuClickOnFinishButton();
	}

	private void addFitstReqOrCapAndPressNext() throws Exception {
		addFirstReqOrCap();
		linkMenuClickOnNextButton();
	}

	private void addFirstReqOrCap() {
		GeneralUIUtils.getWebElementsListByClassName(DataTestIdEnum.LinkMenuItems.LINK_ITEM_CAP_Or_REQ.getValue()).get(0).click();
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
		actions.perform();
		GeneralUIUtils.ultimateWait();
	}

	private Pair<Integer,Integer> getCircleOffset(CircleSize circleSize)
	{
		Pair<Integer,Integer> circleSizes;
		if(circleSize.equals(CircleSize.VF))
		{
			circleSizes = new Pair <Integer,Integer> (CANVAS_VF_X_OFFSET,CANVAS_VF_Y_OFFSET);
		}
		else if (circleSize.equals(CircleSize.NORMATIVE))
		{
			circleSizes = new Pair <Integer,Integer> (CANVAS_NORMATIVE_ELEMENT_X_OFFSET,CANVAS_NORMATIVE_ELEMENT_Y_OFFSET);
		}
		else
		{
			circleSizes = new Pair <Integer,Integer> (CANVAS_SERVICE_X_OFFSET,CANVAS_SERVICE_Y_OFFSET);
		}
		return circleSizes;
	}

	public String updateElementNameInCanvas(CanvasElement canvasElement, String newInstanceName) throws Exception {
		GeneralUIUtils.ultimateWait();;
		clickOnCanvaElement(canvasElement);
		WebElement updateInstanceName = GeneralUIUtils.getWebElementBy(By.id("editPencil"));
		updateInstanceName.click();
		WebElement instanceNameField = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralCanvasItems.INSTANCE_NAME_FIELD.getValue());
		String oldInstanceName = instanceNameField.getAttribute("value");
		instanceNameField.clear();
		instanceNameField.sendKeys(newInstanceName);
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.OK.getValue()).click();
		GeneralUIUtils.ultimateWait();
		GeneralUIUtils.waitForElementInVisibilityByTestId(By.className("w-sdc-modal-resource-instance-name"));
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Name of element instance changed from %s to %s", oldInstanceName, newInstanceName));
		return oldInstanceName;
	}

	/**
	 * @param canvasElement
	 * Validate that instance was selected on right sidebar
	 */
	public void validateInstanceSelected(CanvasElement canvasElement) {
		long maxWait = 3000;
		long sumOfWaiting = 0;
		long napPeriod = 200;
		boolean isInstanceSelected;
		do {
			isInstanceSelected = CompositionPage.getSelectedInstanceName().contains(canvasElement.getElementType());

			if (!isInstanceSelected) {
				try {
					TimeUnit.MILLISECONDS.sleep(napPeriod);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			sumOfWaiting += napPeriod;
			if (sumOfWaiting > maxWait) {
				Assert.fail(String.format("Can't select instance properly, waited for %s seconds", (int) (maxWait/1000)));
			}
		} while (!isInstanceSelected);
	}

	private void selectReqCapByName(String reqCapName)
	{
	    GeneralUIUtils.clickOnElementByText(reqCapName);
        GeneralUIUtils.ultimateWait();
    }

	private void selectTypeOfReqCap(String reqCapType)
	{
        GeneralUIUtils.getSelectList(reqCapType,DataTestIdEnum.LinkMenuItems.REQ_CAP_SELECT_DATA_TESTS_ID.getValue());
        GeneralUIUtils.ultimateWait();
	}

	public void linkElementsAndSelectCapReqTypeAndCapReqName(CanvasElement firstElement, CircleSize firstElementSize, CanvasElement secondElement, CircleSize secondElementSize, ConnectionWizardPopUpObject connectionWizardPopUpObject) throws Exception {
        drawSimpleLink(firstElement, firstElementSize, secondElement, secondElementSize);
        selectTypeOfReqCap(connectionWizardPopUpObject.getCapabilityTypeSecondItem());
		addFitstReqOrCapAndPressNext();
		selectReqCapByName(connectionWizardPopUpObject.getCapabilityNameSecondItem());
		linkMenuClickOnNextButton();
        linkMenuClickOnFinishButton();
    }
}
