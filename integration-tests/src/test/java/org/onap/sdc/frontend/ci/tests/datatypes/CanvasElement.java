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

package org.onap.sdc.frontend.ci.tests.datatypes;

import org.apache.commons.lang3.tuple.ImmutablePair;

public final class CanvasElement {
    private final String uniqueId;
    private ImmutablePair<Integer, Integer> location;
    private DataTestIdEnum.LeftPanelCanvasItems normativeElementType;
    private String elementType;
    private String elementNameOnCanvas;

    CanvasElement(String name, ImmutablePair<Integer, Integer> location, DataTestIdEnum.LeftPanelCanvasItems canvasItem) {
        super();
        this.uniqueId = name;
        this.location = location;
        normativeElementType = canvasItem;
        this.elementNameOnCanvas = generateCanvasName(name);
    }

    CanvasElement(String name, ImmutablePair<Integer, Integer> location, String canvasItem) {
        super();
        this.uniqueId = name;
        this.location = location;
        elementType = canvasItem;
        this.elementNameOnCanvas = generateCanvasNameFromCanvasItem(canvasItem);
    }

    public CanvasElement(String name, ImmutablePair<Integer, Integer> location) {
        super();
        this.uniqueId = name;
        this.location = location;
        this.elementNameOnCanvas = generateCanvasName(name);
    }

    public String generateCanvasName(String name) {
        if (name.toLowerCase().contains("service")) {
            return name.toLowerCase().substring(0, name.indexOf("_")) + "_proxy 0";
        }
        return name.substring(0, name.indexOf("_")) + " 0";
    }

    public String generateCanvasNameFromCanvasItem(String name) {
        if (name.toLowerCase().contains("service")) {
            return name.toLowerCase() + "_proxy 0";
        }
        return name + " 0";
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public ImmutablePair<Integer, Integer> getLocation() {
        return location;
    }

    public void setLocation(ImmutablePair<Integer, Integer> location) {
        this.location = location;
    }

    public DataTestIdEnum.LeftPanelCanvasItems getNormativeElementType() {
        return normativeElementType;
    }

    public String getElementType() {
        return elementType;
    }

    public String getElementNameOnCanvas() {
        return elementNameOnCanvas;
    }

    public void setElementNameOnCanvas(String newName) {
        elementNameOnCanvas = newName;
    }


}
