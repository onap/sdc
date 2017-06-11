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

public enum HttpHeaderEnum {

	Content_MD5("Content-MD5"), 
	USER_ID("USER_ID"), 
	HTTP_CSP_FIRSTNAME("HTTP_CSP_FIRSTNAME"), 
	HTTP_CSP_LASTNAME("HTTP_CSP_LASTNAME"), 
	HTTP_CSP_WSTYPE("HTTP_CSP_WSTYPE"), 
	HTTP_IV_REMOTE_ADDRESS("HTTP_IV_REMOTE_ADDRESS"), 
	HTTP_IV_USER("HTTP_IV_USER"), 
	HTTP_CSP_EMAIL("HTTP_CSP_EMAIL"), 
	CONTENT_TYPE("Content-Type"), 
	ACCEPT("Accept"), 
	X_ECOMP_REQUEST_ID_HEADER("X-ECOMP-RequestID"), 
	CACHE_CONTROL("Cache-Control"), 
	X_ECOMP_INSTANCE_ID("X-ECOMP-InstanceID"), 
	AUTHORIZATION("Authorization"), 
	CONTENT_LENGTH("Content-Length"), 
	CONTENT_DISPOSITION("Content-Disposition"), 
	HOST("Host"), 
	X_ECOMP_SERVICE_ID_HEADER("X-ECOMP-ServiceID"), 
	WWW_AUTHENTICATE("WWW-Authenticate"),
	ECOMP_PASSWORD("password"), 
	ECOMP_USERNAME("username");
	
	String value;

	private HttpHeaderEnum(String value) {
		this.value = value;
	}

	public String getValue() {

		return value;
	}

}
