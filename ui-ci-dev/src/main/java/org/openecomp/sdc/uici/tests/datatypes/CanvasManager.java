package org.openecomp.sdc.uici.tests.datatypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.uici.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public final class CanvasManager {
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
		canvas = GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.GeneralCanvasItems.CANVAS.getValue());
		try {
			WebElement webElement = GeneralUIUtils
					.getWebElementWaitForVisible(DataTestIdEnum.GeneralCanvasItems.CANVAS_RIGHT_PANEL.getValue());
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

	private void moveElementOnCanvas(CanvasElement canvasElement, ImmutablePair<Integer, Integer> newLocation) {
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

	public void moveElementOnCanvas(CanvasElement canvasElement) {
		moveElementOnCanvas(canvasElement, getFreePosition());
	}

	public void deleteElementFromCanvas(CanvasElement canvasElement) {
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

	public void selectElementFromCanvas(CanvasElement canvasElement) {
		GeneralUIUtils.waitForLoader();
		actions.moveToElement(canvas, canvasElement.getLocation().left, canvasElement.getLocation().right);
		actions.click();
		actions.perform();
		GeneralUIUtils.waitForLoader();
	}

	public CanvasElement createElementOnCanvas(LeftPanelCanvasItems canvasItem) {
		return createElementOnCanvas(canvasItem.getValue(), false);
	}

	/**
	 * Creates Element on the Canvas - use the element name.
	 * 
	 * @param elementName
	 * @return
	 */
	public CanvasElement createElementOnCanvas(String elementName) {
		return createElementOnCanvas(elementName, true);
	}

	private CanvasElement createElementOnCanvas(String elementName, boolean addPrefix) {
		if (addPrefix) {
			elementName = DataTestIdEnum.LEFT_PANEL_PREFIX + elementName;
		}
		GeneralUIUtils.waitForLoader();
		WebElement element = GeneralUIUtils.getWebElementWaitForVisible(elementName);
		ImmutablePair<Integer, Integer> freePosition = getFreePosition();
		actions.moveToElement(element, 0, 0);
		actions.clickAndHold();
		actions.moveToElement(canvas, freePosition.left, freePosition.right);
		actions.release();
		actions.perform();

		String uniqueId = elementName + "_" + UUID.randomUUID().toString();
		CanvasElement canvasElement = new CanvasElement(uniqueId, elementName, freePosition);
		addCanvasElement(canvasElement);
		GeneralUIUtils.waitForLoader();
		return canvasElement;
	}

	public CanvasElement createUniqueVFOnCanvas(LeftPanelCanvasItems canvasItem) {
		GeneralUIUtils.waitForLoader();
		WebElement element = GeneralUIUtils.getWebElementWaitForVisible(canvasItem.getValue());
		ImmutablePair<Integer, Integer> freePosition = getFreePosition();
		actions.moveToElement(element, 0, 0);
		actions.clickAndHold();
		actions.moveToElement(canvas, freePosition.left, freePosition.right);
		actions.release();
		actions.perform();

		String uniqueId = canvasItem.name() + "_" + UUID.randomUUID().toString();
		CanvasElement canvasElement = new CanvasElement(uniqueId, canvasItem.getValue(), freePosition);
		addCanvasElement(canvasElement);
		GeneralUIUtils.waitForLoader();
		return canvasElement;
	}

	private ImmutablePair<Integer, Integer> getFreePosition() {
		// TODO ui-ci use better method
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

	/**
	 * Links two elements on canvas.<br>
	 * Currently Supports Only elements in the default size.<br>
	 * Will not work for container type or smaller elements (cp, vl etc...)<br>
	 * 
	 * @param firstElement
	 * @param secondElement
	 */
	public void linkElements(CanvasElement firstElement, CanvasElement secondElement) {
		GeneralUIUtils.waitForLoader();
		drawSimpleLink(firstElement, secondElement);
		selectReqAndCapAndConnect();

		GeneralUIUtils.waitForLoader();

	}

	private void selectReqAndCapAndConnect() {
		// Select First Cap
		GeneralUIUtils.getWebElementsListWaitForVisible(DataTestIdEnum.LinkMenuItems.LINK_ITEM_CAP.getValue()).get(0)
				.click();
		// Select First Req
		GeneralUIUtils.getWebElementsListWaitForVisible(DataTestIdEnum.LinkMenuItems.LINK_ITEM_REQ.getValue()).get(0)
				.click();
		// Connect
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.LinkMenuItems.CONNECT_BUTTON.getValue()).click();

	}

	private void drawSimpleLink(CanvasElement firstElement, CanvasElement secondElement) {

		int yOffset = CANVAS_ELEMENT_Y_OFFSET;
		int xOffset = CANVAS_ELEMENT_X_OFFSET;
		actions.moveToElement(canvas, firstElement.getLocation().left + xOffset,
				firstElement.getLocation().right - yOffset);

		actions.clickAndHold();
		actions.moveToElement(canvas, secondElement.getLocation().left + xOffset,
				secondElement.getLocation().right - yOffset);
		actions.release();
		actions.perform();
		GeneralUIUtils.waitForLoader();
	}
}
