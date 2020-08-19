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

package org.openecomp.sdc.ci.tests.users;

public enum UserResponseMessageEnum {

	SUCCESS_MESSAGE("OK"), MISSING_INFORMATION("Error: Missing information"), METHOD_NOT_ALLOWED("Error: Method not allowed"), RESTRICTED_OPERATION("Error: Restricted operation"), USER_ALREADY_EXISTS("Error: User with %s ID already exists"), 
	INVALID_EMAIL("Error: Invalid Content. Invalid e-mail address %s"), INVALID_ROLE("Error: Invalid Content. Invalid role %s"), INVALID_CONTENT("Error: Invalid content"), USER_NOT_FOUND("Error: User with %s ID is not found"), 
	INTERNAL_SERVER_ERROR("Error: Internal Server Error. Try later again"), ADMINISTARTOR_CAN_BE_DELETED("Error: Administrator can be deleted by other administrator only"), RESTRICTED_ACCESS("Error: Restricted access");

	String value;

	private UserResponseMessageEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
