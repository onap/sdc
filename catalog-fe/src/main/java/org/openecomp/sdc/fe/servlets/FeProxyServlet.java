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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.api.Response;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.Configuration.CatalogFacadeMsConfig;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.FeEcompErrorManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;
import org.openecomp.sdc.fe.config.PluginsConfiguration.Plugin;
import org.openecomp.sdc.fe.impl.LogHandler;
import org.openecomp.sdc.fe.utils.BeProtocol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;

import static org.apache.commons.lang3.StringUtils.isEmpty;


public class FeProxyServlet extends SSLProxyServlet {
	private static final long serialVersionUID = 1L;
	private static final String URL = "%s://%s%s";
	private static final String MS_URL = "%s://%s:%s";
	private static final String ONBOARDING_CONTEXT = "/onboarding-api";
	private static final String DCAED_CONTEXT = "/dcae-api";
	private static final String WORKFLOW_CONTEXT = "/wf";
	private static final String SDC1_FE_PROXY = "/sdc1/feProxy";
	private static final String PLUGIN_ID_WORKFLOW = "WORKFLOW";
	public static final String UUID = "uuid";
	public static final String TRANSACTION_START_TIME = "transactionStartTime";
	private static Logger log = Logger.getLogger(FeProxyServlet.class.getName());

	private static String msUrl;
	private static final String FACADE_PATH_IDENTIFIER = "uicache";
	private static final String CATALOG_REQUEST_IDENTIFIER = "/v1/catalog";
	private static final String ARCHIVE_PATH_IDENTIFIER = String.format("%s/archive/", CATALOG_REQUEST_IDENTIFIER);
	private static final String HOME_REQUEST_IDENTIFIER = "/v1/followed";
	@Override
	protected String rewriteTarget(HttpServletRequest request) {
		String originalUrl="";
		String redirectedUrl = "";

		try {
			logFeRequest(request);
			originalUrl = request.getRequestURL().toString();

			Configuration config = getConfiguration(request);
			if (config == null) {
				log.error("failed to retrieve configuration.");
			}
			if (isMsRequest(request.getRequestURL().toString())) {
				redirectedUrl = redirectMsRequestToMservice(request, config);
			} else {
				redirectedUrl = getModifiedUrl(config, getPluginConfiguration(request), request.getRequestURI(), getQueryString(request));
			}
		}
		catch (MalformedURLException mue) {
			FeEcompErrorManager.getInstance().logFeHttpLoggingError("FE Request");
			log.error(EcompLoggerErrorCode.DATA_ERROR, "FeProxyServlet rewriteTarget", "sdc-FE", "Malformed URL Exception: ", mue);
		}
		catch (Exception e) {
            log.error(EcompLoggerErrorCode.UNKNOWN_ERROR,"FeProxyServlet rewriteTarget", "sdc-FE", "Unexpected FE request processing error: ", e);
		}
		if (log.isDebugEnabled()) {
			log.debug("FeProxyServlet Redirecting request from: {} , to: {}", originalUrl, redirectedUrl);
		}

		return redirectedUrl;
	}

	@Override
	protected void onProxyResponseSuccess(HttpServletRequest request, HttpServletResponse proxyResponse, Response response) {
		try {
			logFeResponse(request, response);
		} catch (Exception e) {
			FeEcompErrorManager.getInstance().logFeHttpLoggingError("FE Response");
            log.error(EcompLoggerErrorCode.UNKNOWN_ERROR,"FeProxyServlet onProxyResponseSuccess", "sdc-FE", "Unexpected FE response logging error: ", e);
		}
		super.onProxyResponseSuccess(request, proxyResponse, response);
	}

	private void logFeRequest(HttpServletRequest httpRequest){
		LogHandler.logFeRequest(httpRequest);
		inHttpRequest(httpRequest);
	}

	private void logFeResponse(HttpServletRequest request, Response proxyResponse){
		LogHandler.logFeResponse(request);
		outHttpResponse(proxyResponse);
	}

	// Extracted for purpose of clear method name, for logback %M parameter
	private void inHttpRequest(HttpServletRequest httpRequest) {
		log.info("{} {} {}", httpRequest.getMethod(), httpRequest.getRequestURI(), httpRequest.getProtocol());
	}

	// Extracted for purpose of clear method name, for logback %M parameter
	private void outHttpResponse(Response proxyResponse) {
		log.info("SC=\"{}\"", proxyResponse.getStatus());
	}

	private String getModifiedUrl(Configuration config, PluginsConfiguration pluginConf, String uri, String queryString) throws MalformedURLException{
		if (config == null) {
            log.error(EcompLoggerErrorCode.UNKNOWN_ERROR,"FeProxyServlet getModifiedUrl", "sdc-FE", "failed to retrieve configuration.");
			throw new RuntimeException("failed to read FE configuration");
		}
		String protocol;
		String host;
		String port;
		if (uri.contains(ONBOARDING_CONTEXT)){
			uri = uri.replace(SDC1_FE_PROXY+ONBOARDING_CONTEXT,ONBOARDING_CONTEXT);
			protocol = config.getOnboarding().getProtocolBe();
			host = config.getOnboarding().getHostBe();
			port = config.getOnboarding().getPortBe().toString();		
		}else if(uri.contains(DCAED_CONTEXT)){
			uri = uri.replace(SDC1_FE_PROXY+DCAED_CONTEXT,DCAED_CONTEXT);
			protocol = config.getBeProtocol();
			host = config.getBeHost();
			if (config.getBeProtocol().equals(BeProtocol.HTTP.getProtocolName())) {
				port = config.getBeHttpPort().toString();
			} else {
				port = config.getBeSslPort().toString();
			}
		}
		else if (uri.contains(WORKFLOW_CONTEXT)){
			uri = uri.replace(SDC1_FE_PROXY +WORKFLOW_CONTEXT,WORKFLOW_CONTEXT);
			String workflowPluginURL = pluginConf.getPluginsList()
					.stream()
					.filter(plugin -> plugin.getPluginId().equalsIgnoreCase(PLUGIN_ID_WORKFLOW))
					.map(Plugin::getPluginDiscoveryUrl)
					.findFirst().orElse(null);

			java.net.URL workflowURL = new URL(workflowPluginURL);
			protocol = workflowURL.getProtocol();
			host = workflowURL.getHost();
			port = String.valueOf(workflowURL.getPort());
		}
		else{
			uri = uri.replace(SDC1_FE_PROXY,"/sdc2");
			protocol = config.getBeProtocol();
			host = config.getBeHost();
			if (config.getBeProtocol().equals(BeProtocol.HTTP.getProtocolName())) {
				port = config.getBeHttpPort().toString();
			} else {
				port = config.getBeSslPort().toString();
			}
		}	

		String authority = getAuthority(host, port);
		String modifiedUrl = String.format(URL,protocol,authority,uri);
		if( !StringUtils.isEmpty(queryString)){
			modifiedUrl += "?" + queryString;
		}
		 
		return  modifiedUrl;

	}
	@VisibleForTesting
	String redirectMsRequestToMservice(HttpServletRequest request, Configuration config) throws MalformedURLException {

		boolean isMsToggledOn = isMsToggleOn(config);
		String redirectValue;
		if (isMsToggledOn) {
			redirectValue = handleMsToggleOnRedirect(request, config);
		} else {
			redirectValue = handleMsToggleOffRedirect(request, config);
		}
		return redirectValue;
	}
private PluginsConfiguration getPluginConfiguration(HttpServletRequest request) {
		return ((ConfigurationManager) request.getSession().getServletContext().getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).getPluginsConfiguration();
  }
	private boolean isMsToggleOn(Configuration config) {
		boolean toggleOn = true;
		final CatalogFacadeMsConfig catalogFacadeMs = config.getCatalogFacadeMs();
		if (catalogFacadeMs == null) {
			toggleOn = false;
			;
		} else if (isEmpty(catalogFacadeMs.getHealthCheckUri())) {
			toggleOn = false;
		} else if (isEmpty(catalogFacadeMs.getHost())) {
			toggleOn = false;
		} else if (isEmpty(catalogFacadeMs.getPath())) {
			toggleOn = false;
		} else if (isEmpty(catalogFacadeMs.getProtocol())) {
			toggleOn = false;
		} else if (catalogFacadeMs.getPort() == null) {
			toggleOn = false;
		}
		return toggleOn;
	}
	private String handleMsToggleOffRedirect(HttpServletRequest request, Configuration config) throws MalformedURLException {
		String redirectValue;
		String currentURI = request.getRequestURI();
		if (isEmpty(request.getQueryString())) {
			// Catalog
			if (currentURI.endsWith(CATALOG_REQUEST_IDENTIFIER)) {
				String facadeSuffix = String.format("%s%s", FACADE_PATH_IDENTIFIER, CATALOG_REQUEST_IDENTIFIER);
				String nonFacadeUrl = currentURI.replace(facadeSuffix, "rest/v1/screen");
				redirectValue = getModifiedUrl(config, getPluginConfiguration(request), nonFacadeUrl, "excludeTypes=VFCMT&excludeTypes=Configuration");
			}
			// Home
			else if (currentURI.endsWith(HOME_REQUEST_IDENTIFIER)){
				redirectValue = getModifiedUrl(config, getPluginConfiguration(request), currentURI, getQueryString(request));
			}
			// Archive
			else if (currentURI.endsWith(ARCHIVE_PATH_IDENTIFIER)) {
				redirectValue = getModifiedUrl(config, getPluginConfiguration(request), currentURI, getQueryString(request));
			} else {
				String message = String.format("facade is toggled off, Could not rediret url %s", currentURI);
				log.error(message);
				throw new NotImplementedException(message);
			}
		} else {
			// Left Pallet
			if (currentURI.contains("/latestversion/notabstract/metadata")) {
				String nonFacadeUrl = currentURI.replace(FACADE_PATH_IDENTIFIER, "rest");
				redirectValue = getModifiedUrl(config, getPluginConfiguration(request), nonFacadeUrl, getQueryString(request));
			}
			// Catalog with Query Params
			else if (currentURI.endsWith(CATALOG_REQUEST_IDENTIFIER)) {
				String facadeSuffix = String.format("%s%s", FACADE_PATH_IDENTIFIER, CATALOG_REQUEST_IDENTIFIER);
				String nonFacadeUrl = currentURI.replace(facadeSuffix, "rest/v1/screen");
				redirectValue = getModifiedUrl(config, getPluginConfiguration(request), nonFacadeUrl, "excludeTypes=VFCMT&excludeTypes=Configuration");
			} else {
				String message = String.format("facade is toggled off, Could not rediret url %s with query params %s",
						currentURI, getQueryString(request));
				log.error(message);
				throw new NotImplementedException(message);
			}
		}

		return redirectValue;
	}

  private String handleMsToggleOnRedirect(HttpServletRequest request, Configuration config) {
		String currentUrl = request.getRequestURL()
				.toString();
		if (StringUtils.isEmpty(msUrl)) {
			// do that only once
			msUrl = String.format(MS_URL, config.getCatalogFacadeMs()
					.getProtocol(),
					config.getCatalogFacadeMs().getHost(),
					config.getCatalogFacadeMs().getPort());
		}
		StringBuilder url;
		String queryString;
		String msPath = config.getCatalogFacadeMs().getPath();
		if (currentUrl.endsWith(ARCHIVE_PATH_IDENTIFIER)) {
			url = new StringBuilder(msUrl + msPath + CATALOG_REQUEST_IDENTIFIER);
			queryString = "arc=true";
		} else {
			url = new StringBuilder(msUrl + currentUrl.substring(currentUrl.indexOf(msPath)));
			queryString = request.getQueryString();
		}
		if (queryString != null) {
			url.append("?").append(queryString);
		}
		if (log.isDebugEnabled()) {
			log.debug("Redirect catalog request to {}", url.toString());
		}
		return url.toString();
	}

	@VisibleForTesting
	boolean isMsRequest(String currentUrl) {
		return currentUrl.contains(FACADE_PATH_IDENTIFIER) || currentUrl.endsWith(ARCHIVE_PATH_IDENTIFIER);
	}
	private Configuration getConfiguration(HttpServletRequest request) {
		return ((ConfigurationManager) request.getSession().getServletContext().getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).getConfiguration();
	}

	private String getAuthority(String host, String port) {
		String authority;
		if (port==null){
			authority=host;
		}
		else{
			authority=host+":"+port;
		}
		return authority;
	}
	
	private String getQueryString(HttpServletRequest request){
		final String queryString = request.getQueryString();
		return StringUtils.isEmpty(queryString) ? StringUtils.EMPTY : queryString;
	}

}
