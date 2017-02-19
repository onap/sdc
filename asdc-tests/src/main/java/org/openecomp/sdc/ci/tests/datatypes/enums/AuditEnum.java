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

public enum AuditEnum {

	ACTION("ACTION"), 
	RESOURCE_NAME("RESOURCE_NAME"), 
	RESOURCE_TYPE("RESOURCE_TYPE"), 
	PREV_VERSION("PREV_VERSION"), 
	CURR_VERSION("CURR_VERSION"), 
	MODIFIER("MODIFIER"),
    PREV_STATE("PREV_STATE"), 
    CURR_STATE("CURR_STATE"), 
    STATUS("STATUS-Type"), 
    DESC("DESC"), 
    URL("URL"), 
    USER("USER"), 
    AUTH_STATUS("AUTH_STATUS"), 
    REALM("REALM");

	String value;

	private AuditEnum(String value) {
		this.value = value;
	}

	public String getValue() {

		return value.toLowerCase();
	}

}
