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

package org.openecomp.sdc.fe.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.ResponseInfo;
import org.openecomp.sdc.common.api.ResponseInfo.ResponseStatusEnum;
import org.openecomp.sdc.fe.impl.Audit;
import org.openecomp.sdc.fe.impl.HttpRequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendClient {

	private static Logger log = LoggerFactory.getLogger(BackendClient.class.getName());

	private HostnameVerifier hostnameVerifier = null;

	private CloseableHttpClient backendHttpClient;
	private String backendHost;
	private String backendContext;

	public BackendClient(String protocol, String backendHost, String backendContext) {

		this.backendContext = backendContext;
		hostnameVerifier = new HostnameVerifier() {

			public boolean verify(String hostname, SSLSession session) {

				return true;
			}
		};

		if (protocol == null || protocol.isEmpty() || protocol.equals(Constants.HTTP)) {
			backendHttpClient = HttpClients.createDefault();
			this.backendHost = Constants.HTTP + "://" + backendHost;
		} else {
			// NULL can be returned in case of error
			backendHttpClient = getSslClient();
			this.backendHost = Constants.HTTPS + "://" + backendHost;
		}

	}

	public HostnameVerifier getHostnameVerifier() {
		return hostnameVerifier;
	}

	private CloseableHttpClient getSslClient() {

		CloseableHttpClient httpClient = null;
		try {

			// SSLContextBuilder is not thread safe
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLContext sslContext = builder.build();

			httpClient = HttpClientBuilder.create().setSSLHostnameVerifier(hostnameVerifier).setSslcontext(sslContext)
					.build();

		} catch (Exception e) {
			log.error("Failed to create https client", e);
			return null;
		}

		return httpClient;

	}

	public ResponseInfo forwardRequestToBackend(HttpRequestInfo requestInfo, List<String> requiredHeaders,
			AsyncResponse asyncResponse) {

		ResponseInfo responseInfo = null;
		log.debug("forwardRequestToBackend");
		if (backendHttpClient == null) {
			responseInfo = new ResponseInfo(ResponseStatusEnum.INTERNAL_ERROR, "Failed to create https client");
			Audit.error(log, requestInfo, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			asyncResponse.resume(
					Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(responseInfo.toString()).build());
			return responseInfo;
		}

		CloseableHttpResponse response = null;
		int status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
		HttpPost httpPost = new HttpPost(backendHost + backendContext);
		try {

			log.debug("Executing request {}", httpPost.getRequestLine());
			httpPost.setEntity(new InputStreamEntity(requestInfo.getRequestData()));
			boolean allHeadersAreSet = copyHeadersToRequest(requiredHeaders, requestInfo, httpPost);
			if (!allHeadersAreSet) {
				responseInfo = new ResponseInfo(ResponseStatusEnum.MISSING_HEADERS, "Required headers are missing");
				asyncResponse
						.resume(Response.status(HttpStatus.SC_BAD_REQUEST).entity(responseInfo.toString()).build());
				Audit.error(log, requestInfo, HttpStatus.SC_BAD_REQUEST);
			} else {
				response = backendHttpClient.execute(httpPost);
				status = response.getStatusLine().getStatusCode();
				asyncResponse.resume(Response.status(status).entity(response.getEntity()).build());
			}
			Audit.info(log, requestInfo, status);

		} catch (IOException e) {
			log.error("connection with backend failed with exception", e);
			responseInfo = new ResponseInfo(ResponseStatusEnum.INTERNAL_ERROR, e.getMessage());
			Audit.error(log, requestInfo, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			asyncResponse.resume(
					Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(responseInfo.toString()).build());
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				backendHttpClient.close();
			} catch (IOException e) {
				log.error("failed to close httpClient: {}", e.getMessage());
			}

		}

		return responseInfo;

	}

	private boolean copyHeadersToRequest(List<String> requiredHeaders, HttpRequestInfo requestInfo, HttpPost httpPost) {
		boolean allHeadersAreSet = false;
		Map<String, String> originalHeaders = requestInfo.getHeaders();
		for (String headerName : requiredHeaders) {
			String headerValue = originalHeaders.get(headerName);
			if (headerValue != null) {
				httpPost.setHeader(headerName, headerValue);
			} else {
				log.error("missing required header {}", headerName);
				return allHeadersAreSet;
			}
		}
		allHeadersAreSet = true;
		return allHeadersAreSet;
	}

}
