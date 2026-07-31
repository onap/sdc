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
 *  Modifications copyright (c) 2026 Deutsche Telekom.
 *  ================================================================================
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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositionCanvasComponent extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositionCanvasComponent.class);

    /**
     * Must match {@code CytoscapeEdgeEditation.HANDLE_SIZE} and {@code GraphUIObjects.HANDLE_SIZE} in catalog-ui.
     */
    private static final int HANDLE_SIZE = 18;
    private static final int CREATE_LINK_MAX_ATTEMPTS = 3;
    private static final int HANDLE_ARM_TIMEOUT_IN_SECONDS = 5;
    private static final int WIZARD_RETRY_TIMEOUT_IN_SECONDS = 5;
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

    /**
     * Reproduces {@code CytoscapeEdgeEditation._getHandlePosition()}, the reference point the extension measures the mouse distance against in
     * {@code _hitTestHandles()}. It must use the rendered dimensions rather than the {@code imgWidth} data attribute, because the node styles do
     * not derive width and height from {@code imgWidth} alone and do not necessarily make them equal.
     */
    private static final String handlePositionJs = "var cy = window.jQuery('.sdc-composition-graph-wrapper').cytoscape('get');%n"
        + "var n = cy.nodes('[name=\"%s\"]');%n"
        + "var nPos = n.renderedPosition();%n"
        + "var handleSize = " + HANDLE_SIZE + " * cy.zoom();%n"
        + "return JSON.stringify({%n"
        + "    x: nPos.x + n.renderedWidth() / 2 - handleSize,%n"
        + "    y: nPos.y - n.renderedHeight() / 2%n"
        + "})";

    /**
     * The edge-editation extension switches the body cursor to {@code pointer} from {@code _mouseMove()} the moment its handle hit test succeeds,
     * and nothing else in catalog-ui writes that property. It is therefore the only externally observable proof that the handle is armed.
     */
    private static final String bodyCursorJs = "return window.jQuery('body').css('cursor');";
    private static final String clearBodyCursorJs = "window.jQuery('body').css('cursor', 'inherit');";

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
            .pause(Duration.ofMillis(500))
            .click()
            .perform();
        ExtentTestActions.takeScreenshot(Status.INFO, "canvas-node-selected", String.format("'%s' was selected", elementName));
    }

    public ComponentInstance createNodeOnServiceCanvas(final String serviceName, final String serviceVersion, final String resourceName,
                                                       final String resourceVersion) {
        final int maxRetries = 5;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
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
                lastException = e;
                LOGGER.warn("Attempt {}/{} to create node on service canvas failed: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(2000L * attempt);
                    } catch (final InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new CompositionCanvasRuntimeException("Could not create node through the API", lastException);
    }

    public ComponentInstance createNodeOnResourceCanvas(final String serviceName, final String serviceVersion, final String resourceName,
                                                        final String resourceVersion) {
        final int maxRetries = 5;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
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
                lastException = e;
                LOGGER.warn("Attempt {}/{} to create node on resource canvas failed: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(2000L * attempt);
                    } catch (final InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new CompositionCanvasRuntimeException("Could not create node through the API", lastException);
    }

    private Point getFreePositionInCanvas(int maxAttempts) {
        boolean isPositionFree;
        final int minSpace = 150;
        for (int attemptCount = 0; attemptCount < maxAttempts; attemptCount++) {
            final Point randomPositionInCanvas = getRandomPositionInCanvas();
            isPositionFree = canvasElementList.stream()
                .noneMatch(canvasNodeElement -> Math.abs(canvasNodeElement.getPositionX() - randomPositionInCanvas.getX()) < minSpace
                    && Math.abs(canvasNodeElement.getPositionY() - randomPositionInCanvas.getY()) < minSpace);
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

        final Point toElementPositionFromCenter = calculateOffsetFromCenter(toCanvasElement.getPositionX(), toCanvasElement.getPositionY());

        for (int attempt = 1; attempt <= CREATE_LINK_MAX_ATTEMPTS; attempt++) {
            if (!hoverOverGreenPlus(fromCanvasElement.getName(), attempt)) {
                LOGGER.warn("Attempt {}/{} to hover the green plus of node '{}' did not arm the link handle", attempt, CREATE_LINK_MAX_ATTEMPTS,
                    fromNodeName);
                continue;
            }
            new Actions(webDriver)
                .clickAndHold()
                .pause(Duration.ofMillis(500))
                .moveToElement(canvasWebElement, toElementPositionFromCenter.getX(), toElementPositionFromCenter.getY())
                .pause(Duration.ofMillis(500))
                .release()
                .perform();

            // Probed with its own instance, so that the component handed back to the caller always carries the default timeout for its remaining
            // interactions. Only the last attempt waits the full timeout; the earlier ones fail over quickly.
            final RelationshipWizardComponent wizardProbe = new RelationshipWizardComponent(webDriver);
            if (attempt < CREATE_LINK_MAX_ATTEMPTS) {
                wizardProbe.setTimeout(WIZARD_RETRY_TIMEOUT_IN_SECONDS);
            }
            try {
                wizardProbe.isLoaded();
                return new RelationshipWizardComponent(webDriver);
            } catch (final TimeoutException e) {
                LOGGER.warn("Attempt {}/{} dragged a link from '{}' to '{}' but the relationship wizard did not open", attempt,
                    CREATE_LINK_MAX_ATTEMPTS, fromNodeName, toNodeName);
            }
        }
        throw new CompositionCanvasRuntimeException(
            String.format("Could not create a link from '%s' to '%s' in %s attempts", fromNodeName, toNodeName, CREATE_LINK_MAX_ATTEMPTS));
    }

    /**
     * Moves the mouse over the "add edge" (green plus) handle of the given node and waits for the edge-editation extension to arm it.
     *
     * <p>The handle must be armed before the mouse goes down: {@code CytoscapeEdgeEditation._mouseDown()} has its own hit test commented out and
     * only starts a drag when {@code _hit} is already set, which happens exclusively in {@code _mouseMove()} and only while a node is hovered. A
     * mouse down on a handle that was never hit-tested is silently a no-op, so no amount of waiting afterwards can recover it.
     *
     * <p>One move is not enough either, because {@code _hover} is set from the same event that the hit test runs on, leaving the first move into a
     * node hit-testing with nothing hovered. Hence the two-step move: the first brings the node under the cursor, the second hit-tests the handle.
     *
     * @param elementName the name of the node to hover
     * @param attempt     the current attempt number, used to vary the approach point
     * @return true if the handle was armed before the timeout
     */
    private boolean hoverOverGreenPlus(final String elementName, final int attempt) {
        final Point greenPlusPosition = calculateOffsetFromCenter(getElementGreenPlusPosition(elementName));
        // The node's top edge runs through the handle's drawing origin, so aim slightly inside the node: on the edge itself, rounding can put the
        // pointer outside the node, where no handle is hovered at all. This stays far inside the HANDLE_SIZE * zoom hit radius.
        final int inset = HANDLE_SIZE / 4;
        final int aimX = greenPlusPosition.getX() + inset;
        final int aimY = greenPlusPosition.getY() + inset;
        // A "pointer" read below must be attributable to this hover, not left over from a previous attempt.
        ((JavascriptExecutor) webDriver).executeScript(clearBodyCursorJs);
        new Actions(webDriver)
            .moveToElement(canvasWebElement, aimX + attempt, aimY + attempt)
            .moveToElement(canvasWebElement, aimX, aimY)
            .perform();
        try {
            getWait(HANDLE_ARM_TIMEOUT_IN_SECONDS)
                .until(driver -> "pointer".equals(((JavascriptExecutor) driver).executeScript(bodyCursorJs)));
            return true;
        } catch (final TimeoutException e) {
            return false;
        }
    }

    /**
     * Returns the position of the "add edge" (green plus) handle of a node, relative to the canvas element.
     *
     * @param elementName the name of the node whose handle position is wanted
     * @return the handle position
     */
    public Point getElementGreenPlusPosition(final String elementName) {
        final String scriptJS = String.format(handlePositionJs, elementName);
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
