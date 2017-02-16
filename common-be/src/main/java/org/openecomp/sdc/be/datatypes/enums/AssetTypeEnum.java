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

package org.openecomp.sdc.be.datatypes.enums;

public enum AssetTypeEnum {
	RESOURCES("resources", "Resource"), SERVICES("services", "Service"), PRODUCTS("products", "Product");

	private String value;

	public String getValue() {
		return value;
	}

	public String getCorrespondingComponent() {
		return correspondingComponent;
	}

	private String correspondingComponent;

	private AssetTypeEnum(String value, String correspondingComponent) {
		this.value = value;
		this.correspondingComponent = correspondingComponent;
	}

	public static ComponentTypeEnum convertToComponentTypeEnum(String assetType) {
		ComponentTypeEnum ret = null;
		for (AssetTypeEnum curr : AssetTypeEnum.values()) {
			if (curr.value.equals(assetType)) {
				return ComponentTypeEnum.findByValue(curr.correspondingComponent);
			}
		}

		return ret;
	}
}
