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

package org.openecomp.sdc.be.dao.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.common.rest.api.RestResponse;
import org.openecomp.sdc.common.rest.api.RestResponseAsByteArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRestClient {

	public final static int DEFAULT_CONNECTION_POOL_SIZE = 10;

	public final static int DEFAULT_READ_TIMEOUT_IN_SEC = 30;

	public final static int DEFAULT_CONNECT_TIMEOUT = 30;

	public final static int DEFAULT_SOCKET_TIMEOUT = 30;

	private CloseableHttpClient httpClient = null;

	private PoolingHttpClientConnectionManager cm = null;

	private static Logger logger = LoggerFactory.getLogger(HttpRestClient.class.getName());

	private RestResponse errorRestResponse = new RestResponse("internal server error", null, 500);
	private RestResponseAsByteArray errorRestResponseAsByteArray = new RestResponseAsByteArray(
			"internal server error".getBytes(), null, 500);

	boolean isInitialized = false;

	// public static void main(String[] argv) {
	// try {
	// RestClientService restClientService =
	// RestClientServiceFactory.createHttpRestClientService(new
	// RestConfigurationInfo());
	//
	// String uriCreateCell =
	// "http://172.20.37.245:9082/topology/management/cell/update";
	// String jsonStr = " { \"cellName\" : \"mycell118\" }";
	// String jsonUpdateStr =
	// " { \"cellName\" : \"mycell118\", \"hostName\" : \"myhost333\" }";
	//
	//
	// // jsonStr = " <note>dfd</note>";
	//
	// Properties headers = new Properties();
	// headers.put("Content-type", "application/json");
	// headers.put("Accept", "*/*");
	//
	// // RestResponse restResponse = restClientService.doPOST(uriCreateCell,
	// headers, jsonStr);
	//
	// RestResponse restResponse = restClientService.doPUT(uriCreateCell,
	// headers, jsonUpdateStr);
	//
	// System.out.println(restResponse);
	//
	//
	// } catch (RestClientServiceExeption e) {
	// e.printStackTrace();
	// }
	//
	// }

	public HttpRestClient() {
		super();
		isInitialized = init(new RestConfigurationInfo());
	}

	public HttpRestClient(RestConfigurationInfo restConfigurationInfo) {
		super();
		isInitialized = init(restConfigurationInfo);
	}

	public boolean init(RestConfigurationInfo restConfigurationInfo) {

		logger.debug("create HttpRestClient: restConfigurationInfo= {}", restConfigurationInfo);
		boolean result = false;
		createHttpClient(restConfigurationInfo);
		result = true;
		
		logger.debug("Finish creating HttpRestClient. Result is {}", result);
		return result;
	}

	public void destroy() {

		try {
			httpClient.close();
			logger.debug("Http client closed");
		} catch (Exception e) {
			logger.trace("Failed to close http client", e);
		}

	}

	private void createHttpClient(RestConfigurationInfo restConfigurationInfo) {
		// throws KeyManagementException, NoSuchAlgorithmException {

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

		int connPoolSize = getConnectionPoolSize(restConfigurationInfo);
		connManager.setMaxTotal(connPoolSize);
		connManager.setDefaultMaxPerRoute(10);
		connManager.setValidateAfterInactivity(15000);

		// Create common default configuration
		int socketTimeout = getTimeout(restConfigurationInfo.getSocketTimeoutInSec(), DEFAULT_SOCKET_TIMEOUT);

		int connectTimeoutInSec = getTimeout(restConfigurationInfo.getConnectTimeoutInSec(), DEFAULT_CONNECT_TIMEOUT);

		int readTimeOut = getTimeout(restConfigurationInfo.getReadTimeoutInSec(), DEFAULT_READ_TIMEOUT_IN_SEC);

		RequestConfig clientConfig = RequestConfig.custom().setConnectTimeout(connectTimeoutInSec)
				.setSocketTimeout(socketTimeout).setConnectionRequestTimeout(readTimeOut).build();

		this.cm = connManager;

		this.httpClient = HttpClients.custom().setDefaultRequestConfig(clientConfig).setConnectionManager(connManager)
				.build();

	}

	private int getConnectionPoolSize(RestConfigurationInfo restConfigurationInfo) {
		Integer connPoolSizeObj = restConfigurationInfo.getConnectionPoolSize();
		int connPoolSize = DEFAULT_CONNECTION_POOL_SIZE;
		if (connPoolSizeObj != null) {
			connPoolSize = connPoolSizeObj.intValue();
			if (connPoolSize <= 0) {
				connPoolSize = DEFAULT_CONNECTION_POOL_SIZE;
			}
		}
		return connPoolSize;
	}

	private int getTimeout(Integer value, Integer defaultValue) {

		int defaultTimeout = defaultValue != null ? defaultValue.intValue() * 1000 : 0;

		int timeout = defaultTimeout;

		if (value != null) {
			timeout = defaultValue.intValue() * 1000;
			if (timeout <= 0) {
				timeout = defaultTimeout;
			}
		}

		return timeout;
	}

	/**
	 * Executes RS-GET to perform FIND.
	 * 
	 * @param headerParameterKey
	 *            String
	 * @param headerParameterValue
	 *            String
	 * @return String
	 */
	public RestResponse doGET(String uri, Properties headers) {

		logger.debug("Before executing uri {}. headers = {}", uri, headers);

		HttpGet httpGet = new HttpGet(uri);

		RestResponse response = execute(httpGet, headers);

		return response;
	}

	public RestResponse doPUT(String uri, Properties headers, String body) {

		logger.debug("Before executing uri {}. headers = {}.body = {}", uri, headers, body);

		HttpPut httpPut = new HttpPut(uri);
		StringEntity data = new StringEntity(body, ContentType.APPLICATION_JSON);
		httpPut.setEntity(data);
		RestResponse response = execute(httpPut, headers);

		return response;
	}

	public RestResponse doPOST(String uri, Properties headers, String body) {

		logger.debug("Before executing uri {}. headers = {}.body = {}", uri, headers, body);

		HttpPost httpPost = new HttpPost(uri);
		StringEntity data = new StringEntity(body, ContentType.APPLICATION_JSON);
		httpPost.setEntity(data);
		RestResponse response = execute(httpPost, headers);

		return response;
	}

	public RestResponseAsByteArray doGetAsByteArray(String uri, Properties headers) {

		logger.debug("Before executing uri {}. headers = {}", uri, headers);

		HttpGet httpGet = new HttpGet(uri);

		RestResponseAsByteArray response = executeAndReturnByteArray(httpGet, headers);

		return response;
	}

	private void addHeadersToRequest(HttpRequestBase httpRequestBase, Properties headers) {

		if (headers != null) {
			for (Entry<Object, Object> entry : headers.entrySet()) {
				httpRequestBase.addHeader(entry.getKey().toString(), entry.getValue().toString());
			}
		}

	}

	private RestResponse execute(HttpRequestBase httpRequestBase, Properties headers) {

		RestResponse restResponse = null;

		CloseableHttpResponse httpResponse = null;

		try {

			addHeadersToRequest(httpRequestBase, headers);

			httpResponse = this.httpClient.execute(httpRequestBase);

			restResponse = buildRestResponseFromResult(httpResponse);

			logger.debug("After executing uri {}. response = {}", httpRequestBase.getURI().toString(), restResponse);

		} catch (Exception exception) {
			httpRequestBase.abort();

			String description = "Failed executing http request " + httpRequestBase.getURI().toString() + "("
					+ httpRequestBase.getMethod() + ")";
			BeEcompErrorManager.getInstance().logInternalFlowError("ExecuteRestRequest", description,
					ErrorSeverity.ERROR);
			restResponse = errorRestResponse;
		} finally {
			// ensure the connection gets released to the manager
			releaseResource(httpResponse);
		}

		return restResponse;
	}

	private RestResponse buildRestResponseFromResult(CloseableHttpResponse httpResponse) throws IOException {

		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String statusDesc = httpResponse.getStatusLine().getReasonPhrase();

		HttpEntity entity = httpResponse.getEntity();
		String response = null;
		if (entity != null) {
			response = EntityUtils.toString(entity);
		}

		RestResponse restResponse = new RestResponse(response, statusDesc, statusCode);

		return restResponse;
	}

	private RestResponseAsByteArray buildRestResponseByteArrayFromResult(CloseableHttpResponse httpResponse)
			throws IOException {

		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String statusDesc = httpResponse.getStatusLine().getReasonPhrase();

		HttpEntity entity = httpResponse.getEntity();

		byte[] response = null;
		if (entity != null) {
			InputStream content = entity.getContent();
			if (content != null) {
				response = IOUtils.toByteArray(content);
			}
		}

		RestResponseAsByteArray restResponse = new RestResponseAsByteArray(response, statusDesc, statusCode);

		return restResponse;
	}

	private RestResponseAsByteArray executeAndReturnByteArray(HttpRequestBase httpRequestBase, Properties headers) {

		RestResponseAsByteArray restResponse = null;

		CloseableHttpResponse httpResponse = null;

		try {

			addHeadersToRequest(httpRequestBase, headers);

			httpResponse = this.httpClient.execute(httpRequestBase);

			restResponse = buildRestResponseByteArrayFromResult(httpResponse);

			if (restResponse != null) {
				logger.debug("After executing uri {}. Response: {}", httpRequestBase.getURI().toString(), restResponse.toPrettyString());
			}

		} catch (Exception exception) {
			httpRequestBase.abort();
			String description = "Failed executing http request " + httpRequestBase.getURI().toString() + "("
					+ httpRequestBase.getMethod() + ")";
			BeEcompErrorManager.getInstance().logInternalFlowError("ExecuteRestRequest", description,
					ErrorSeverity.ERROR);
			logger.debug(description, exception);
			restResponse = errorRestResponseAsByteArray;
		} finally {
			// ensure the connection gets released to the manager
			releaseResource(httpResponse);
		}

		return restResponse;
	}

	/**
	 * This method print the JSON response from the REST Server
	 * 
	 * @param response
	 *            the JSON response from the REST server
	 * @param method
	 *            name of method
	 */
	private void logResponse(String response, String method) {
		logger.trace("{} response = {}", method, response);
	}

	private void releaseResource(CloseableHttpResponse response) {
		if (response != null) {
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					EntityUtils.consume(entity);
				}
				response.close();
			} catch (Exception e) {
				logger.error("failed to close connection exception", e);
			}
		}
	}

	public boolean isInitialized() {
		return isInitialized;
	}

}
