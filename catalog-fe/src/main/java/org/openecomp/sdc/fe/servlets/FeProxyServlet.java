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

import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.api.Response;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.FeEcompErrorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class FeProxyServlet extends SSLProxyServlet {
	private static final long serialVersionUID = 1L;
	private static final String URL = "%s://%s:%s%s";
	private static Logger log = LoggerFactory.getLogger(FeProxyServlet.class.getName());
	private static Cache<String, MdcData> mdcDataCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

	@Override
	public URI rewriteURI(HttpServletRequest request) {
		try {
			logFeRequest(request);
		} catch (Exception e) {
			FeEcompErrorManager.getInstance().processEcompError(EcompErrorName.FeHttpLoggingError, "FE Request");
			FeEcompErrorManager.getInstance().logFeHttpLoggingError("FE Request");
			log.error("Unexpected FE request logging error :", e);
		}
		String originalUrl = request.getRequestURL().toString();
		String redirectedUrl = getModifiedUrl(request);

		log.debug("FeProxyServlet Redirecting request from: {} , to: {}", originalUrl, redirectedUrl);

		return URI.create(redirectedUrl);
	}

	@Override
	protected void onResponseSuccess(HttpServletRequest request, HttpServletResponse response, Response proxyResponse) {
		try {
			logFeResponse(request, proxyResponse);
		} catch (Exception e) {
			FeEcompErrorManager.getInstance().processEcompError(EcompErrorName.FeHttpLoggingError, "FE Response");
			FeEcompErrorManager.getInstance().logFeHttpLoggingError("FE Response");
			log.error("Unexpected FE response logging error :", e);
		}
		super.onResponseSuccess(request, response, proxyResponse);
	}

	private void logFeRequest(HttpServletRequest httpRequest) {

		MDC.clear();

		Long transactionStartTime = System.currentTimeMillis();
		// UUID - In FE, we are supposed to get the below header from UI.
		// We do not generate it if it's missing - BE does.
		String uuid = httpRequest.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER);
		String serviceInstanceID = httpRequest.getHeader(Constants.X_ECOMP_SERVICE_ID_HEADER);

		if (uuid != null && uuid.length() > 0) {
			// UserId for logging
			String userId = httpRequest.getHeader(Constants.USER_ID_HEADER);

			String remoteAddr = httpRequest.getRemoteAddr();
			String localAddr = httpRequest.getLocalAddr();

			mdcDataCache.put(uuid, new MdcData(serviceInstanceID, userId, remoteAddr, localAddr, transactionStartTime));

			updateMdc(uuid, serviceInstanceID, userId, remoteAddr, localAddr, null);
		}
		inHttpRequest(httpRequest);
	}

	private void logFeResponse(HttpServletRequest request, Response proxyResponse) {
		String uuid = request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER);
		String transactionRoundTime = null;

		if (uuid != null) {
			MdcData mdcData = mdcDataCache.getIfPresent(uuid);
			if (mdcData != null) {
				Long transactionStartTime = mdcData.getTransactionStartTime();
				if (transactionStartTime != null) {// should'n ever be null, but
													// just to be defensive
					transactionRoundTime = Long.toString(System.currentTimeMillis() - transactionStartTime);
				}
				updateMdc(uuid, mdcData.getServiceInstanceID(), mdcData.getUserId(), mdcData.getRemoteAddr(), mdcData.getLocalAddr(), transactionRoundTime);
			}
		}
		outHttpResponse(proxyResponse);

		MDC.clear();
	}

	// Extracted for purpose of clear method name, for logback %M parameter
	private void inHttpRequest(HttpServletRequest httpRequest) {
		log.info("{} {} {}", httpRequest.getMethod(), httpRequest.getRequestURI(), httpRequest.getProtocol());
	}

	// Extracted for purpose of clear method name, for logback %M parameter
	private void outHttpResponse(Response proxyResponse) {
		log.info("SC=\"{}\"", proxyResponse.getStatus());
	}

	private void updateMdc(String uuid, String serviceInstanceID, String userId, String remoteAddr, String localAddr, String transactionStartTime) {
		MDC.put("uuid", uuid);
		MDC.put("serviceInstanceID", serviceInstanceID);
		MDC.put("userId", userId);
		MDC.put("remoteAddr", remoteAddr);
		MDC.put("localAddr", localAddr);
		MDC.put("timer", transactionStartTime);
	}

	private class MdcData {
		private String serviceInstanceID;
		private String userId;
		private String remoteAddr;
		private String localAddr;
		private Long transactionStartTime;

		public MdcData(String serviceInstanceID, String userId, String remoteAddr, String localAddr, Long transactionStartTime) {
			super();
			this.serviceInstanceID = serviceInstanceID;
			this.userId = userId;
			this.remoteAddr = remoteAddr;
			this.localAddr = localAddr;
			this.transactionStartTime = transactionStartTime;
		}

		public Long getTransactionStartTime() {
			return transactionStartTime;
		}

		public String getUserId() {
			return userId;
		}

		public String getRemoteAddr() {
			return remoteAddr;
		}

		public String getLocalAddr() {
			return localAddr;
		}

		public String getServiceInstanceID() {
			return serviceInstanceID;
		}
	}

	public String getModifiedUrl(HttpServletRequest request) {

		Configuration config = getConfiguration(request);
		if (config == null) {
			log.error("failed to retrive configuration.");
		}
		String scheme = config.getBeProtocol();
		String uri = request.getRequestURI().toString();
		StringBuilder url = new StringBuilder();
		url.append(scheme).append("://").append(config.getBeHost());
		url.append(":");
		if (config.getBeProtocol().equals(BE_PROTOCOL.HTTP.getProtocolName())) {
			url.append(config.getBeHttpPort());
		} else {
			url.append(config.getBeSslPort());
		}
		url.append(uri);
		String queryString = request.getQueryString(); // d=789
		if (queryString != null) {
			url.append("?").append(queryString);
		}

		String redirectedUrl = url.toString();
		String onboardingForwardContext = config.getOnboardingForwardContext();
		if (onboardingForwardContext == null || onboardingForwardContext.isEmpty()) {
			onboardingForwardContext = "/onboarding-api";
		}
		redirectedUrl = redirectedUrl.replace("/sdc1/feProxy/dcae-api", "/dcae");
		redirectedUrl = redirectedUrl.replace("/sdc1/feProxy/onboarding-api", onboardingForwardContext);
		redirectedUrl = redirectedUrl.replace("/sdc1/feProxy", "/sdc2");
		return redirectedUrl;

	}

	private Configuration getConfiguration(HttpServletRequest request) {
		Configuration config = ((ConfigurationManager) request.getSession().getServletContext().getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).getConfiguration();
		return config;
	}
}
