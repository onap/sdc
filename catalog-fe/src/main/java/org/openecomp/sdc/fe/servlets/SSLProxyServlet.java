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

package org.openecomp.sdc.fe.servlets;

import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SSLProxyServlet extends ProxyServlet {

	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(SSLProxyServlet.class.getName());

	public enum BE_PROTOCOL {
		HTTP("http"), SSL("ssl");
		private String protocolName;

		public String getProtocolName() {
			return protocolName;
		}

		BE_PROTOCOL(String protocolName) {
			this.protocolName = protocolName;
		}
	};

	@Override
	public void customizeProxyRequest(Request proxyRequest, HttpServletRequest request) {
		super.customizeProxyRequest(proxyRequest, request);
		// Add Missing Headers to proxy request
		@SuppressWarnings("unchecked")
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			if (!proxyRequest.getHeaders().containsKey(headerName)) {
				String headerVal = request.getHeader(headerName);
				log.debug("Adding missing header to request,  header name: {} , header value: {}", headerName,
						headerVal);
				proxyRequest.header(headerName, headerVal);
			}
		}
		proxyRequest.getHeaders().remove(HttpHeader.HOST);

	}

	@Override
	protected HttpClient createHttpClient() throws ServletException {
		Configuration config = ((ConfigurationManager) getServletConfig().getServletContext()
				.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).getConfiguration();
		boolean isSecureClient = !config.getBeProtocol().equals(BE_PROTOCOL.HTTP.getProtocolName());
		HttpClient client = (isSecureClient) ? getSecureHttpClient() : super.createHttpClient();
		setTimeout(600000);
		client.setIdleTimeout(600000);
		client.setStopTimeout(600000);

		return client;
	}

	private HttpClient getSecureHttpClient() throws ServletException {
		// Instantiate and configure the SslContextFactory
		SslContextFactory sslContextFactory = new SslContextFactory(true);

		// Instantiate HttpClient with the SslContextFactory
		HttpClient httpClient = new HttpClient(sslContextFactory);

		// Configure HttpClient, for example:
		httpClient.setFollowRedirects(false);

		// Start HttpClient
		try {
			httpClient.start();

			return httpClient;
		} catch (Exception x) {
			throw new ServletException(x);
		}
	}
}
