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

public enum ComponentTypeEnum {
	RESOURCE("Resource"), 
	SERVICE("Service"), 
	RESOURCE_INSTANCE("Resource Instance"),
	PRODUCT("Product"), 
	SERVICE_INSTANCE("Service Instance");

	private String value;

	private ComponentTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	// Those values cannot be another field in enum, because they are needed
	// as constants for Swagger allowedValues param
	public static final String RESOURCE_PARAM_NAME = "resources";
	public static final String SERVICE_PARAM_NAME = "services";
	public static final String PRODUCT_PARAM_NAME = "products";

	public NodeTypeEnum getNodeType() {

		switch (this) {
		case RESOURCE:
			return NodeTypeEnum.Resource;
		case SERVICE:
			return NodeTypeEnum.Service;
		case PRODUCT:
			return NodeTypeEnum.Product;
		case RESOURCE_INSTANCE:
			return NodeTypeEnum.ResourceInstance;
		default:
			throw new UnsupportedOperationException("No nodeType is defined for: " + this.getValue());
		}
	}

	public static ComponentTypeEnum findByValue(String value) {
		ComponentTypeEnum ret = null;
		for (ComponentTypeEnum curr : ComponentTypeEnum.values()) {
			if (curr.getValue().equals(value)) {
				ret = curr;
				return ret;
			}
		}
		return ret;
	}

	public static ComponentTypeEnum findByParamName(String paramName) {
		ComponentTypeEnum ret = null;
		switch (paramName) {
		case RESOURCE_PARAM_NAME:
			ret = RESOURCE;
			break;
		case SERVICE_PARAM_NAME:
			ret = SERVICE;
			break;
		case PRODUCT_PARAM_NAME:
			ret = PRODUCT;
			break;
		default:
			break;
		}
		return ret;
	}

	public static String findParamByType(ComponentTypeEnum type) {
		String ret = null;
		switch (type) {
		case RESOURCE:
			ret = RESOURCE_PARAM_NAME;
			break;
		case SERVICE:
			ret = SERVICE_PARAM_NAME;
			break;
		case PRODUCT:
			ret = PRODUCT_PARAM_NAME;
			break;
		default:
			break;
		}
		return ret;
	}
}
