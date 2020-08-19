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

package org.openecomp.sdc.ci.tests.datatypes.http;

import java.util.List;
import java.util.Map;

public class RestResponse {

	Integer errorCode;
	String response;
	Map<String, List<String>> headerFields;
	String responseMessage;

	public RestResponse() {
		super();
	}

	public RestResponse(Integer errorCode, String response, Map<String, List<String>> headerFields,
			String responseMessage) {
		super();
		this.errorCode = errorCode;
		this.response = response;
		this.headerFields = headerFields;
		this.responseMessage = responseMessage;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Map<String, List<String>> getHeaderFields() {
		return headerFields;
	}

	public void setHeaderFields(Map<String, List<String>> headerFields) {
		this.headerFields = headerFields;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	@Override
	public String toString() {
		return "RestResponse [errorCode=" + errorCode + ", response=" + response + ", headerFields=" + headerFields
				+ ", responseMessage=" + responseMessage + "]";
	}

}
