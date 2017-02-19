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

import java.util.Arrays;

import org.apache.http.HttpStatus;

public class RestResponseAsByteArray {

	private byte[] response;

	private String statusDescription;

	private int httpStatusCode = 0;

	public RestResponseAsByteArray(byte[] response, String statusDescription, int httpStatusCode) {
		super();
		this.response = response;
		this.statusDescription = statusDescription;
		this.httpStatusCode = httpStatusCode;
	}

	/**
	 * @return the response
	 */
	public byte[] getResponse() {
		return response;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(byte[] response) {
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

	public String toString() {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("Status: ");
		stringBuilder.append(httpStatusCode);
		stringBuilder.append("\n");
		stringBuilder.append("Message: ");
		stringBuilder.append(statusDescription);
		stringBuilder.append("\n");
		stringBuilder.append("Body length: ");
		stringBuilder.append(response == null ? 0 : response.length);

		return stringBuilder.toString();

	}

	public String toPrettyString() {

		int maxBytesToDisplay = 200;

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("Status: ");
		stringBuilder.append(httpStatusCode);
		stringBuilder.append("\n");
		stringBuilder.append("Message: ");
		stringBuilder.append(statusDescription);
		stringBuilder.append("\n");
		if (httpStatusCode != HttpStatus.SC_OK) {
			stringBuilder.append("Body(maximum " + maxBytesToDisplay + " bytes): ");
			if (response != null) {
				byte[] subArray = Arrays.copyOfRange(response, 0, Math.min(maxBytesToDisplay, response.length));
				if (subArray != null && subArray.length > 0) {
					String responseStr = new String(subArray);
					stringBuilder.append(responseStr);
				}
			}
		} else {
			stringBuilder.append("Body length: ");
			stringBuilder.append(response == null ? 0 : response.length);
		}

		return stringBuilder.toString();

	}
}
