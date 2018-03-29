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

public class RestClientServiceExeption extends Exception {

	private static final long serialVersionUID = 8258477052369440242L;

	/**
	 * Default Constructor.
	 */
	public RestClientServiceExeption() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            String
	 */
	public RestClientServiceExeption(String msg) {
		super(msg);
	}

	/**
	 * Constructor.
	 * 
	 * @param cause
	 *            Throwable
	 */
	public RestClientServiceExeption(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            String
	 * @param cause
	 *            Throwable
	 */
	public RestClientServiceExeption(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * 
	 * @param response
	 *
	 *            public RSClientServiceExeption(ClientResponse response) { super(); this.response = response; }
	 * 
	 *            public ClientResponse getResponse() { return response; }
	 * 
	 *            public void setResponse(ClientResponse response) { this.response = response; }
	 */
}
