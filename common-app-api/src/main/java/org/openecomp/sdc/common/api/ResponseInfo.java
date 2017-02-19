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

package org.openecomp.sdc.common.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseInfo {

	public static enum ResponseStatusEnum {
		SUCCESS("success"), 
		LOGIN_FAILED("loginFailed"), 
		INTERNAL_ERROR("internalError"), 
		MISSING_HEADERS("required headers are missing"), 
		TIMEOUT("timeout"), 
		PARSING_ERROR("parsingFailed");

		ResponseStatusEnum(String status) {
			this.statusDescription = status;
		}

		public String statusDescription;
	}

	private ResponseStatusEnum applicativeStatus;
	private String description;

	public ResponseInfo(ResponseStatusEnum applicativeStatus, String description) {
		super();
		this.applicativeStatus = applicativeStatus;
		this.description = description;
	}

	public ResponseInfo(ResponseStatusEnum applicativeStatus) {
		super();
		this.applicativeStatus = applicativeStatus;
	}

	public ResponseStatusEnum getApplicativeStatus() {
		return applicativeStatus;
	}

	public void setApplicativeStatus(ResponseStatusEnum applicativeStatus) {
		this.applicativeStatus = applicativeStatus;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		String tostring = super.toString();
		try {
			tostring = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {

		}
		return tostring;
	}
}
