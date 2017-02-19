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

import java.util.Properties;

/**
 * This interface describe the methods of the REST generic client. Each method
 * will receive the destination URI and specific list of headers. With this
 * information the REST Client will create a request to the specific REST Web
 * server. Error from the REST Web server will be return by proprietary
 * exception object.
 * 
 * @author esofer
 *
 */
public interface IRestClient {

	/**
	 * This method will return resource according to the given URI.
	 * 
	 * @param uri
	 *            Full URL path to the desire resource.
	 * @param headers
	 *            - list of headers in format of name and value, to be add as
	 *            part of the HTTP request.
	 * @return JSON representation of the requested resource.
	 */
	public RestResponse doGET(String uri, Properties headers);

	/**
	 * This method will CREATE resource according to the given URI.
	 * 
	 * @param uri
	 *            Full URL path to the desire resource.
	 * @param headers
	 *            - list of headers in format of name and value, to be add as
	 *            part of the HTTP request.
	 * @param objectToCreate
	 *            - JSON representation of the resource.
	 */
	public RestResponse doPOST(String uri, Properties headers, Object objectToCreate);

	/**
	 * This method will UPDATE resource according to the given URI.
	 * 
	 * @param uri
	 *            Full URL path to the desire resource.
	 * @param headers
	 *            - list of headers in format of name and value, to be add as
	 *            part of the HTTP request.
	 * @param objectToUpdate
	 *            - JSON representation of the resource.
	 */
	public RestResponse doPUT(String uri, Properties headers, Object objectToUpdate);

	/**
	 * This method will return resource according to the given URI.
	 * 
	 * @param uri
	 *            Full URL path to the desire resource.
	 * @param headers
	 *            - list of headers in format of name and value, to be add as
	 *            part of the HTTP request.
	 * 
	 */
	public RestResponse doDELETE(String uri, Properties headers);

	/**
	 * initialize the rest client instance. The timeout is infinite.
	 */
	public boolean init() throws Exception;

	/**
	 * initialize the rest client instance with a given timeout in milliseconds.
	 * 
	 * @param restConfigurationInfo
	 */
	public boolean init(RestConfigurationInfo restConfigurationInfo);

	/**
	 * destroy the connections
	 */
	public void destroy();
}
