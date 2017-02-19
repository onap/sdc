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

public enum CheckBoxStatusEnum {
	CHECKOUT("", "checkbox-checkout"), 
	CHECKIN("", "checkbox-checkin"), 
	READY_FOR_TESTING("checkbox-readyfortesting","checkbox-1"), 
	IN_TESTING("checkbox-intesting", "checkbox-2"), 
	WAITING_FOR_DISTRIBUTION("", "checkbox-waitingforapproval"), 
	DISTRIBUTION_REJECTED("", "checkbox-distributionrejected"), 
	DISTRIBUTION_APPROVED("", "checkbox-distributionapproved"), 
	CERTIFIED("checkbox-certified", "checkbox-3"), 
	DISTRIBUTED("", "checkbox-4"), 
	IN_DESIGN("", "checkbox-0");

	private String value;
	private String value2;

	public String getValue() {
		return value;
	}

	public String getCatalogValue() {
		return value2;
	}

	private CheckBoxStatusEnum(String value, String value2) {
		this.value = value;
		this.value2 = value2;
	}
}
