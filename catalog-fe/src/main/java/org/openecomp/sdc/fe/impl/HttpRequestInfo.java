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

package org.openecomp.sdc.fe.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class HttpRequestInfo {

	public HttpRequestInfo(HttpServletRequest request, Map<String, String> headersMap, String data) {
		headers = headersMap;
		requestURL = request.getRequestURI();
		requestData = new ByteArrayInputStream(data.getBytes());
		originServletContext = request.getContextPath();
	}

	private Map<String, String> headers;
	private String requestURL;
	private InputStream requestData;
	private String originServletContext;

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public InputStream getRequestData() {
		return requestData;
	}

	public void setRequestData(InputStream requestData) {
		this.requestData = requestData;
	}

	public String getOriginServletContext() {
		return originServletContext;
	}

	public void setOriginServletContext(String originServletContext) {
		this.originServletContext = originServletContext;
	}
}
