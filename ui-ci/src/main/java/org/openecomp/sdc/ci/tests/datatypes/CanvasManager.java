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

import static org.testng.AssertJUnit.assertNotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;

import com.relevantcodes.extentreports.LogStatus;

public final class CanvasManager {
	private static final String LEFT_PANEL_ELEMENT_NAME_PREFIX = "leftbar-section-content-item-";
	private Map<String, CanvasElement> canvasElements;
	private Actions actions;
	private WebElement canvas;
	private int reduceCanvasWidthFactor;
	// Offsets Are used to find upper right corner of canvas element in order to
	// connect links
	private static final int CANVAS_ELEMENT_Y_OFFSET = 40;
	private static final int CANVAS_ELEMENT_X_OFFSET = 21; // 14 - 27

	private CanvasManager() {
		canvasElements = new HashMap<>();
		actions = new Actions(GeneralUIUtils.getDriver());
//		canvas = GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.GeneralCanvasItems.CANVAS.getValue());
		canvas = GeneralUIUtils.waitForClassNameVisibility("w-sdc-designer-canvas");
		try {
			WebElement webElement = GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.GeneralCanvasItems.CANVAS_RIGHT_PANEL.getValue());
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
		GeneralUIUtils.sleep(500);
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
			GeneralUIUtils.sleep(napPeriod);
			isKeepWaiting = GeneralUIUtils.getWebElementWaitForVisible("selectedCompTitle").getText()
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
	}

	public void moveElementOnCanvas(CanvasElement canvasElement) throws Exception {
		moveElementOnCanvas(canvasElement, getFreePosition());
	}

	public void deleteElementFromCanvas(CanvasElement canvasElement) throws Exception {
		GeneralUIUtils.waitForLoader();
		actions.moveToElement(canvas, canvasElement.getLocation().left, canvasElement.getLocation().right);
		actions.click();
		actions.perform();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.GeneralCanvasItems.DELETE_INSTANCE_BUTTON.getValue())
				.click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.OK.getValue()).click();
		canvasElements.remove(canvasElement.getUniqueId());
		GeneralUIUtils.waitForLoader();
	}

	private WebElement findClickElement(String dataTestId) {
		int attempts = 0;
		while (attempts < 2) {
			try {
				return GeneralUIUtils.getWebElementWaitForVisible(dataTestId);
			} catch (StaleElementReferenceException e) {
			}
			attempts++;
		}
		return null;
	}

	public CanvasElement createElementOnCanvas(String elementName) throws Exception {
		final String elementDataTestId = LEFT_PANEL_ELEMENT_NAME_PREFIX + elementName;
		try {
			WebElement element = findClickElement(elementDataTestId);
			ImmutablePair<Integer, Integer> freePosition = getFreePosition();
			actions.moveToElement(element, 0, 0);
			actions.clickAndHold();
			actions.moveToElement(canvas, freePosition.left, freePosition.right);
			actions.release();
			actions.perform();
			GeneralUIUtils.waitForLoader();
			String uniqueId = elementDataTestId + "_" + UUID.randomUUID().toString();
			CanvasElement canvasElement = new CanvasElement(uniqueId, freePosition, elementDataTestId);
			addCanvasElement(canvasElement);
			SetupCDTest.getExtendTest().log(LogStatus.PASS,
					String.format("element %s is in canvas now..", elementName));
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

	public void linkElements(CanvasElement firstElement, CanvasElement secondElement) throws Exception {
		drawSimpleLink(firstElement, secondElement);
		selectReqAndCapAndConnect();
	}

	private void selectReqAndCapAndConnect() throws Exception {
		// Select First Cap
		GeneralUIUtils.getWebElementsListByDataTestId(DataTestIdEnum.LinkMenuItems.LINK_ITEM_CAP.getValue()).get(0)
				.click();
		// Select First Req
		GeneralUIUtils.getWebElementsListByDataTestId(DataTestIdEnum.LinkMenuItems.LINK_ITEM_REQ.getValue()).get(0)
				.click();
		// Connect
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.LinkMenuItems.CONNECT_BUTTON.getValue()).click();

		GeneralUIUtils.waitForLoader();
	}

	private void drawSimpleLink(CanvasElement firstElement, CanvasElement secondElement) throws Exception {
		int yOffset = CANVAS_ELEMENT_Y_OFFSET;
		int xOffset = CANVAS_ELEMENT_X_OFFSET;

		actions.moveToElement(canvas, firstElement.getLocation().left + xOffset, firstElement.getLocation().right - yOffset);
		actions.clickAndHold();
		actions.moveToElement(canvas, secondElement.getLocation().left + xOffset, secondElement.getLocation().right - yOffset);
		actions.release();
		actions.perform();
		GeneralUIUtils.ultimateWait();
		SetupCDTest.getExtendTest().log(LogStatus.INFO, String.format("Elements %s and %s now connected", firstElement.getElementType().split("-")[4], secondElement.getElementType().split("-")[4]));
	}

	public String updateElementNameInCanvas(CanvasElement canvasElement, String newInstanceName) throws Exception {
		GeneralUIUtils.waitForLoader();
		actions.moveToElement(canvas, canvasElement.getLocation().left, canvasElement.getLocation().right);
		actions.click();
		actions.perform();
		WebElement updateInstanceName = GeneralUIUtils.getDriver().findElement(By.id("editPencil"));
		updateInstanceName.click();
		WebElement instanceNameField = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.GeneralCanvasItems.INSTANCE_NAME_FIELD.getValue());
		String oldInstanceName = instanceNameField.getText();
		instanceNameField.clear();
		instanceNameField.sendKeys(newInstanceName);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.OK.getValue()).click();
		GeneralUIUtils.waitForLoader();
		return oldInstanceName;
	}
}
