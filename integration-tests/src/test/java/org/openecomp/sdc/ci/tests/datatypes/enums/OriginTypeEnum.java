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

package org.openecomp.sdc.ci.tests.datatypes.enums;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

public enum OriginTypeEnum {
	PRODUCT("Product", "Product", "product instance", ComponentTypeEnum.PRODUCT), 
	SERVICE("Service", "Service", "service instance", ComponentTypeEnum.SERVICE), 
	VF("VF", "VF (Virtual Function)", "resource instance", ComponentTypeEnum.RESOURCE),
	VFC("VFC", "VFC (Virtual Function Component)", "resource instance", ComponentTypeEnum.RESOURCE),
	CP("CP", "CP (Connection Point)", "resource instance", ComponentTypeEnum.RESOURCE), 
	VL("VL", "VL (Virtual Link)", "resource instance", ComponentTypeEnum.RESOURCE),
	VFCMT("VFCMT", "VFCMT (VFC Monitoring Template)", "resource instance", ComponentTypeEnum.RESOURCE),
	VFi("VFi", "VFi (Virtual Function Instance)", "resource instance", ComponentTypeEnum.RESOURCE_INSTANCE);
	

	private String value;
	private String displayValue;
	private String instanceType;
	private ComponentTypeEnum componentType;

	private OriginTypeEnum(String value, String displayValue, String instanceType, ComponentTypeEnum componentType) {
		this.value = value;
		this.displayValue = displayValue;
		this.instanceType = instanceType;
		this.componentType = componentType;
	}

	public String getValue() {
		return value;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public ComponentTypeEnum getComponentType() {
		return componentType;
	}

	public static OriginTypeEnum findByValue(String value) {
		OriginTypeEnum ret = null;
		for (OriginTypeEnum curr : OriginTypeEnum.values()) {
			if (curr.getValue().equals(value)) {
				ret = curr;
				break;
			}
		}
		return ret;
	}
}
