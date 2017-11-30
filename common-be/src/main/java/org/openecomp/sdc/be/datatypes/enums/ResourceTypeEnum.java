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

/**
 * Resource Type Enum
 * @author mshitrit
 *
 */
public enum ResourceTypeEnum {

	VFC("VFC (Virtual Function Component)"), 
	VF("VF"/* (Virtual Function)" */), 
	CP("CP (Connection Point)"), 
	PNF("PNF"/* (Physical Network Function)" */),
	CVFC("CVFC"/*Complex Virtual Function Component*/),
	VL( "VL (Virtual Link)"), 
	VFCMT("VFCMT (VFC Monitoring Template)"),
	Configuration("Configuration ()"),
	ServiceProxy("ServiceProxy ()"),
	ABSTRACT("Abstract (Generic VFC/VF/PNF/Service Type)");

	private String value;

	private ResourceTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	public static ResourceTypeEnum getType(String type) {
		for (ResourceTypeEnum e : ResourceTypeEnum.values()) {
			if (e.name().equals(type)) {
				return e;
			}
		}
		return null;
	}
	
	public static ResourceTypeEnum getTypeByName(String type) {
		for (ResourceTypeEnum e : ResourceTypeEnum.values()) {
			if (e.name().equalsIgnoreCase(type)) {
				return e;
			}
		}
		return null;
	}
/**
 * Returns ResourceTypeEnum matching to received String ignore case
 * @param type
 * @return
 */
	public static ResourceTypeEnum getTypeIgnoreCase(String type) {
		for (ResourceTypeEnum e : ResourceTypeEnum.values()) {
			if (e.name().toLowerCase().equals(type.toLowerCase())) {
				return e;
			}
		}
		return null;
	}
	/**
	 * Checks if enum exist with given type
	 * @param type
	 * @return
	 */
	public static boolean containsName(String type) {

		for (ResourceTypeEnum e : ResourceTypeEnum.values()) {
			if (e.name().equals(type)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Checks if enum exist with given type ignore case
	 * @param type
	 * @return
	 */
	public static boolean containsIgnoreCase(String type) {

		for (ResourceTypeEnum e : ResourceTypeEnum.values()) {
			if (e.name().toLowerCase().equals(type.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
