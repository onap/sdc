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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;

public final class CanvasElement {
	private final String uniqueId;
	private ImmutablePair<Integer, Integer> location;
	private LeftPanelCanvasItems normativeElementType;
	private String elementType;

	CanvasElement(String name, ImmutablePair<Integer, Integer> location, LeftPanelCanvasItems canvasItem) {
		super();
		this.uniqueId = name;
		this.location = location;
		normativeElementType = canvasItem;
	}

	CanvasElement(String name, ImmutablePair<Integer, Integer> location, String canvasItem) {
		super();
		this.uniqueId = name;
		this.location = location;
		elementType = canvasItem;
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

	public LeftPanelCanvasItems getNormativeElementType() {
		return normativeElementType;
	}

	public String getElementType() {
		return elementType;
	}
}
