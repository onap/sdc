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

package org.openecomp.sdc.common.rest.api;

public class RestResponse {

	private String response;

	private String statusDescription;

	private int httpStatusCode = 0;

	public RestResponse(String response, String statusDescription, int httpStatusCode) {
		super();
		this.response = response;
		this.statusDescription = statusDescription;
		this.httpStatusCode = httpStatusCode;
	}

	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(String response) {
		this.response = response;
	}

	/**
	 * @return the httpStatusCode
	 */
	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	/**
	 * @param httpStatusCode
	 *            the httpStatusCode to set
	 */
	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	/**
	 * @return the statusDescription
	 */
	public String getStatusDescription() {
		return statusDescription;
	}

	/**
	 * @param statusDescription
	 *            the statusDescription to set
	 */
	public void setStatusDescription(String statusDescription) {
		this.statusDescription = statusDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 * ERROR: Error from Rest Server:Status: 400 Message: Bad Request Body:
	 * Invalid cell: Cell with cell name dmgrCell22 does not exist!
	 * 
	 */
	public String toString() {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("Status: ");
		stringBuilder.append(httpStatusCode);
		stringBuilder.append("\n");
		stringBuilder.append("Message: ");
		stringBuilder.append(statusDescription);
		stringBuilder.append("\n");
		stringBuilder.append("Body: ");
		stringBuilder.append(response);

		return stringBuilder.toString();

	}
}
