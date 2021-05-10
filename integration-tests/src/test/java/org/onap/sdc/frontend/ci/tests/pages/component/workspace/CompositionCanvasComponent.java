/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

package org.onap.sdc.frontend.ci.tests.pages.component.workspace;

import static org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum.DESIGNER;

import com.aventstack.extentreports.Status;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasNodeElement;
import org.onap.sdc.frontend.ci.tests.exception.CompositionCanvasRuntimeException;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositionCanvasComponent extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositionCanvasComponent.class);
    private static final String nodePositionJs = "var cy = window.jQuery('.sdc-composition-graph-wrapper').cytoscape('get');%n"
        + "var n = cy.nodes('[name=\"%s\"]');%n"
        + "var nPos = n.renderedPosition();%n"
        + "return JSON.stringify({%n"
        + "    x: nPos.x,%n"
        + "    y: nPos.y%n"
        + "})";

    private static final String getNodesJs = "var cy = window.jQuery('.sdc-composition-graph-wrapper').cytoscape('get');\n"
        + "var nodes = [];"
        + "cy.nodes().forEach((node) => {nodes.push(JSON.stringify({name: node.data('name'), position: node.renderedPosition()}))});\n"
        + "return nodes;";

    private final CompositionElementsComponent compositionElementsComponent;
    private final CompositionDetailSideBarComponent compositionDetailSideBarComponent;

    private WebElement canvasWebElement;
    private Set<CanvasNodeElement> canvasElementList;
    private int canvasCenterX;
    private int canvasCenterY;
    private int canvasWidth;
    private int canvasHeight;

    public CompositionCanvasComponent(final WebDriver webDriver) {
        super(webDriver);
        compositionElementsComponent = new CompositionElementsComponent(webDriver);
        compositionDetailSideBarComponent = new CompositionDetailSideBarComponent(webDriver);
    }

    @Override
    public void isLoaded() {
        //waiting the canvas data to be load and animation finishes.
        new Actions(webDriver).pause(Duration.ofSeconds(2)).perform();
        canvasWebElement = waitToBeClickable(XpathSelector.CANVAS_ELEMENT.getXpath());
        compositionElementsComponent.isLoaded();
        compositionDetailSideBarComponent.isLoaded();
        loadCanvas();
        loadElements();
    }

    private void loadCanvas() {
        canvasWidth = canvasWebElement.getSize().getWidth();
        canvasHeight = canvasWebElement.getSize().getHeight();
        canvasCenterX = canvasWidth / 2;
        canvasCenterY = canvasHeight / 2;
        LOGGER.debug("Canvas with size [{}, {}] and center [{}, {}]", canvasWidth, canvasHeight, canvasCenterX, canvasCenterY);
    }

    private void loadElements() {
        canvasElementList = new HashSet<>();
        final Object nodeListObj = ((JavascriptExecutor) webDriver).executeScript(getNodesJs);
        if (!(nodeListObj instanceof ArrayList)) {
            return;
        }
        final ArrayList<String> nodeList = (ArrayList<String>) nodeListObj;
        if (nodeList.isEmpty()) {
            return;
        }
        nodeList.forEach(nodeString -> {
            final JsonObject node = new JsonParser().parse(nodeString).getAsJsonObject();
            final JsonObject position = node.get("position").getAsJsonObject();
            final CanvasNodeElement canvasElement =
                new CanvasNodeElement(node.get("name").getAsString(), position.get("x").getAsInt(), position.get("y").getAsInt());
            canvasElementList.add(canvasElement);
        });
    }

    public void selectNode(final String elementName) {
        final Optional<CanvasNodeElement> canvasElementOptional = canvasElementList.stream()
            .filter(canvasNodeElement -> canvasNodeElement.getName().startsWith(elementName))
            .findFirst();
        if (canvasElementOptional.isEmpty()) {
            throw new CompositionCanvasRuntimeException(String.format("Given element '%s' does not exist on the element list", elementName));
        }
        final CanvasNodeElement canvasNodeElement = canvasElementOptional.get();
        final Point positionFromCenter = calculateOffsetFromCenter(canvasNodeElement.getPositionX(),
            canvasNodeElement.getPositionY());
        final Actions actions = new Actions(webDriver);
        int offsetFromElementCenter = 10;
        actions.moveToElement(canvasWebElement, positionFromCenter.getX() - offsetFromElementCenter,
            positionFromCenter.getY() + offsetFromElementCenter)
            .pause(Duration.ofSeconds(1))
            .click()
            .perform();
        ExtentTestActions.takeScreenshot(Status.INFO, "canvas-node-selected", String.format("'%s' was selected", elementName));
    }

    public ComponentInstance createNodeOnServiceCanvas(final String serviceName, final String serviceVersion, final String resourceName,
                                                       final String resourceVersion) {
        final Point freePositionInCanvas = getFreePositionInCanvas(20);
        final Point pointFromCanvasCenter = calculateOffsetFromCenter(freePositionInCanvas);
        try {
            final Service service =
                AtomicOperationUtils.getServiceObjectByNameAndVersion(DESIGNER, serviceName, serviceVersion);
            final Resource resourceToAdd =
                AtomicOperationUtils.getResourceObjectByNameAndVersion(DESIGNER, resourceName, resourceVersion);
            final ComponentInstance componentInstance = AtomicOperationUtils
                .addComponentInstanceToComponentContainer(resourceToAdd, service, DESIGNER, true,
                    String.valueOf(pointFromCanvasCenter.getX()), String.valueOf(pointFromCanvasCenter.getY()))
                .left().value();

            LOGGER.debug("Created instance {} in the Service {}", componentInstance.getName(), serviceName);
            return componentInstance;
        } catch (final Exception e) {
            throw new CompositionCanvasRuntimeException("Could not create node through the API", e);
        }
    }

    public ComponentInstance createNodeOnResourceCanvas(final String serviceName, final String serviceVersion, final String resourceName,
                                                        final String resourceVersion) {
        final Point freePositionInCanvas = getFreePositionInCanvas(20);
        final Point pointFromCanvasCenter = calculateOffsetFromCenter(freePositionInCanvas);
        try {
            final Resource service = AtomicOperationUtils.getResourceObjectByNameAndVersion(DESIGNER, serviceName, serviceVersion);
            final Resource resourceToAdd = AtomicOperationUtils.getResourceObjectByNameAndVersion(DESIGNER, resourceName, resourceVersion);
            final ComponentInstance componentInstance =
                AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceToAdd, service, DESIGNER, true,
                    String.valueOf(pointFromCanvasCenter.getX()), String.valueOf(pointFromCanvasCenter.getY())).left().value();

            LOGGER.debug("Created instance {} in the Service {}", componentInstance.getName(), serviceName);
            return componentInstance;
        } catch (final Exception e) {
            throw new CompositionCanvasRuntimeException("Could not create node through the API", e);
        }
    }

    private Point getFreePositionInCanvas(int maxAttempts) {
        boolean isPositionFree;
        final int minSpace = 150;
        for (int attemptCount = 0; attemptCount < maxAttempts; attemptCount++) {
            final Point randomPositionInCanvas = getRandomPositionInCanvas();
            isPositionFree = canvasElementList.stream()
                .noneMatch(canvasNodeElement -> Math.abs(canvasNodeElement.getPositionX() - randomPositionInCanvas.getX()) < minSpace
                    && Math.abs(canvasNodeElement.getPositionX() - randomPositionInCanvas.getY()) < minSpace);
            if (isPositionFree) {
                return randomPositionInCanvas;
            }
        }
        throw new CompositionCanvasRuntimeException("Could not find a free Canvas position");
    }

    private Point getRandomPositionInCanvas() {
        final Random random = new Random();
        int x = random.nextInt(canvasWidth);
        final int maxAllowedWidth = canvasWidth - getRightMarginWidth();
        final int minAllowedWidth = 30;
        if (x > maxAllowedWidth) {
            x = x - getRightMarginWidth();
        } else if (x < minAllowedWidth) {
            x = x + minAllowedWidth;
        }
        int bottomMargin = 0;
        int heightTopMargin = 100;
        int y = random.nextInt(canvasHeight);
        int maxAllowedHeight = canvasHeight - bottomMargin;

        if (y > maxAllowedHeight) {
            y = y - bottomMargin;
        } else if (y < heightTopMargin) {
            y = y + heightTopMargin;
        }
        LOGGER.debug("Generated random position in canvas [{},{}]", x, y);

        return new Point(x, y);
    }

    private int getRightMarginWidth() {
        int canvasIconsOffset = 100;
        final Dimension sideBarSize = compositionDetailSideBarComponent.getSize();
        return sideBarSize.getWidth() + canvasIconsOffset;
    }

    private Point calculateOffsetFromCenter(final Point point) {
        return calculateOffsetFromCenter(point.getX(), point.getY());
    }

    private Point calculateOffsetFromCenter(final int xPosition, final int yPosition) {
        final int positionX = xPosition - canvasCenterX;
        final int positionY = yPosition - canvasCenterY;
        return new Point(positionX, positionY);
    }

    public ImmutablePair<Integer, Integer> getElementPositionByName(final String elementName) {
        final String scriptJs = String.format(nodePositionJs, elementName);
        final Object position = ((JavascriptExecutor) webDriver).executeScript(scriptJs);
        final JsonObject positionAsJson = new JsonParser().parse(position.toString()).getAsJsonObject();
        int xElement = positionAsJson.get("x").getAsInt();
        int yElement = positionAsJson.get("y").getAsInt();
        return new ImmutablePair<>(xElement, yElement);
    }

    public RelationshipWizardComponent createLink(final String fromNodeName, final String toNodeName) {
        final CanvasNodeElement fromCanvasElement = canvasElementList.stream()
            .filter(canvasNodeElement -> canvasNodeElement.getName().equals(fromNodeName)).findFirst()
            .orElseThrow(() -> new UiTestFlowRuntimeException(String.format("Could not find node '%s'", fromNodeName)));
        final CanvasNodeElement toCanvasElement = canvasElementList.stream()
            .filter(canvasNodeElement -> canvasNodeElement.getName().equals(toNodeName)).findFirst()
            .orElseThrow(() -> new UiTestFlowRuntimeException(String.format("Could not find node '%s'", toNodeName)));

        final Point greenPlusPosition = getElementGreenPlusPosition(fromCanvasElement.getName());
        final Point greenPlusPositionFromCenter = calculateOffsetFromCenter(greenPlusPosition);
        final Point toElementPositionFromCenter = calculateOffsetFromCenter(toCanvasElement.getPositionX(), toCanvasElement.getPositionY());
        new Actions(webDriver)
            .moveToElement(canvasWebElement, greenPlusPositionFromCenter.getX(), greenPlusPositionFromCenter.getY())
            .moveByOffset(3, 3).moveByOffset(-3, -3)
            .pause(Duration.ofSeconds(2))
            .clickAndHold()
            .pause(Duration.ofSeconds(1))
            .moveToElement(canvasWebElement, toElementPositionFromCenter.getX(), toElementPositionFromCenter.getY())
            .pause(Duration.ofSeconds(1))
            .release()
            .perform();
        return new RelationshipWizardComponent(webDriver);
    }

    public Point getElementGreenPlusPosition(final String elementName) {
        String scriptJS = "var cy = window.jQuery('.sdc-composition-graph-wrapper').cytoscape('get');\n"
            + "var cyZoom = cy.zoom();\n"
            + "var n = cy.nodes('[name=\"" + elementName + "\"]');\n"
            + "var nPos = n.renderedPosition();\n"
            + "var nData = n.data();\n"
            + "var nImgSize = nData.imgWidth;\n"
            + "var shiftSize = (nImgSize-18)*cyZoom/2;\n"
            + "return JSON.stringify({\n"
            + "\tx: nPos.x + shiftSize,\n"
            + "\ty: nPos.y - shiftSize\n"
            + "});";
        final String o = (String) ((JavascriptExecutor) webDriver).executeScript(scriptJS);
        final JsonObject node = new JsonParser().parse(o).getAsJsonObject();
        final int x = node.get("x").getAsInt();
        final int y = node.get("y").getAsInt();
        return new Point(x, y);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        CANVAS_ELEMENT("canvas", "//*[@data-tests-id='%s']//canvas[1]");

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }
}
