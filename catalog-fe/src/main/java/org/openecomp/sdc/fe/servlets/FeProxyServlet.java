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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.eclipse.jetty.client.api.Response;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.FeEcompErrorManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;
import org.openecomp.sdc.fe.config.PluginsConfiguration.Plugin;
import org.openecomp.sdc.fe.impl.MdcData;
import org.openecomp.sdc.fe.utils.BeProtocol;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class FeProxyServlet extends SSLProxyServlet {
    private static final long serialVersionUID = 1L;
    private static final String URL = "%s://%s%s%s";
    private static final String ONBOARDING_CONTEXT = "/onboarding-api";
    private static final String DCAED_CONTEXT = "/dcae-api";
    private static final String SDC1_FE_PROXY = "/sdc1/feProxy";
    private static final String SDC1_PLUGIN_REDIRECT = SDC1_FE_PROXY + "/plugin";

    private static final Logger LOGGER = Logger.getLogger(FeProxyServlet.class);
    private static final int EXPIRE_DURATION = 10;
    private static Cache<String, MdcData> mdcDataCache = CacheBuilder.newBuilder().expireAfterWrite(EXPIRE_DURATION, TimeUnit.SECONDS).build();


    @Override
    protected String rewriteTarget(HttpServletRequest request) {
        String originalUrl = "";
        String redirectedUrl = "";

        try {
            logFeRequest(request);

            originalUrl = request.getRequestURL().toString();
            redirectedUrl = getModifiedUrl(request);

        } catch (MalformedURLException mue) {
            FeEcompErrorManager.getInstance().logFeHttpLoggingError("FE Request");
            LOGGER.error(EcompLoggerErrorCode.DATA_ERROR, "FeProxyServlet rewriteTarget", "sdc-FE", "Malformed URL Exception: ", mue);
        } catch (Exception e) {
            FeEcompErrorManager.getInstance().logFeHttpLoggingError("FE Request");
            LOGGER.error(EcompLoggerErrorCode.UNKNOWN_ERROR, "FeProxyServlet rewriteTarget", "sdc-FE", "Unexpected FE request processing error: ", e);
        }

        LOGGER.debug("FeProxyServlet Redirecting request from: {} , to: {}", originalUrl, redirectedUrl);

        return redirectedUrl;
    }

    @Override
    protected void onProxyResponseSuccess(HttpServletRequest request, HttpServletResponse proxyResponse, Response response) {
        try {
            logFeResponse(request, response);
        } catch (Exception e) {
            FeEcompErrorManager.getInstance().logFeHttpLoggingError("FE Response");
            LOGGER.error(EcompLoggerErrorCode.UNKNOWN_ERROR, "FeProxyServlet onProxyResponseSuccess", "sdc-FE", "Unexpected FE response logging error: ", e);
        }
        super.onProxyResponseSuccess(request, proxyResponse, response);
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
                if (transactionStartTime != null) { // should'n ever be null, but
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
        LOGGER.info("{} {} {}", httpRequest.getMethod(), httpRequest.getRequestURI(), httpRequest.getProtocol());
    }

    // Extracted for purpose of clear method name, for logback %M parameter
    private void outHttpResponse(Response proxyResponse) {
        LOGGER.info("SC=\"{}\"", proxyResponse.getStatus());
    }

    private void updateMdc(String uuid, String serviceInstanceID, String userId, String remoteAddr, String localAddr, String transactionStartTime) {
        MDC.put("uuid", uuid);
        MDC.put("serviceInstanceID", serviceInstanceID);
        MDC.put("userId", userId);
        MDC.put("remoteAddr", remoteAddr);
        MDC.put("localAddr", localAddr);
        MDC.put("timer", transactionStartTime);
    }


    /****
     * scan all the plugins from the configuration against the URL and the redicert path
     * @param request
     * @return
     */
    private Plugin getPluginProxyForRequest(HttpServletRequest request) {
        return getPluginConfiguration(request).getPluginsList()
                .stream()
                .filter(plugin -> {
                    if (plugin.getPluginProxyRedirectPath() != null && !plugin.getPluginProxyRedirectPath().isEmpty()) {
                        return request.getRequestURI().contains(SDC1_PLUGIN_REDIRECT + plugin.getPluginProxyRedirectPath());
                    } else {
                        return false;
                    }
                })
                .findFirst().orElse(null);
    }

    private String getModifiedUrl(HttpServletRequest request) throws MalformedURLException {
        Configuration config = getConfiguration(request);
        if (config == null) {
            LOGGER.error(EcompLoggerErrorCode.UNKNOWN_ERROR, "FeProxyServlet getModifiedUrl", "sdc-FE", "failed to retrieve configuration.");
            throw new RuntimeException("failed to read FE configuration");
        }
        String uri = request.getRequestURI();

        // the modify logic is as follows:
        // - proxy ONBOARDING to the onboarding context. this is not a plugin and hardcoded
        // - proxy DCAE to the correct context. also - not a plugin but hardcoded
        // - proxy to the plugin according to configuration if the path is found in the plugin patterns
        // - proxy to the catalog backend if no other proxy was found

        if (uri.contains(ONBOARDING_CONTEXT)) {
            uri = uri.replace(SDC1_FE_PROXY + ONBOARDING_CONTEXT, ONBOARDING_CONTEXT);
            return getModifiedUrlString(
                    request,
                    uri,
                    config.getOnboarding().getHostBe(),
                    config.getOnboarding().getPortBe().toString(),
                    config.getOnboarding().getProtocolBe());
        }
        if (uri.contains(DCAED_CONTEXT)) {
            uri = uri.replace(SDC1_FE_PROXY + DCAED_CONTEXT, DCAED_CONTEXT);
            return getModifiedUrlString(
                    request,
                    uri,
                    config.getBeHost(),
                    getCatalogBePort(config),
                    config.getBeProtocol());
        }

        if (uri.contains(SDC1_PLUGIN_REDIRECT)) {
            Plugin proxyPlugin = getPluginProxyForRequest(request);
            if (proxyPlugin != null) {
                String proxyUrlStr = (proxyPlugin.getPluginFeProxyUrl() != null) ? proxyPlugin.getPluginFeProxyUrl() : proxyPlugin.getPluginSourceUrl();
                URL proxyUrl = new URL(proxyUrlStr);
                uri = uri.replace(SDC1_PLUGIN_REDIRECT + proxyPlugin.getPluginProxyRedirectPath(), proxyUrl.getPath());
                return getModifiedUrlString(request, uri, proxyUrl);
            }
        }

        Plugin proxyPlugin = getPluginProxyForRequest(request);
        if (proxyPlugin != null) {
            String proxyUrlStr = (proxyPlugin.getPluginFeProxyUrl() != null) ? proxyPlugin.getPluginFeProxyUrl() : proxyPlugin.getPluginSourceUrl();
            URL proxyUrl = new URL(proxyUrlStr);
            uri = uri.replace(SDC1_FE_PROXY + proxyPlugin.getPluginProxyRedirectPath(), proxyUrl.getPath());
            return getModifiedUrlString(request, uri, proxyUrl);
        }

        uri = uri.replace(SDC1_FE_PROXY, "/sdc2");
        return getModifiedUrlString(
                request,
                uri,
                config.getBeHost(),
                getCatalogBePort(config),
                config.getBeProtocol());
    }


    private String getCatalogBePort(Configuration config) {
        if (config.getBeProtocol().equals(BeProtocol.HTTP.getProtocolName())) {
            return config.getBeHttpPort().toString();
        } else {
            return config.getBeSslPort().toString();
        }
    }

    private String getModifiedUrlString(HttpServletRequest request, String uri, URL url) {
        String queryString = getQueryString(request);
        return String.format(URL, url.getProtocol(), url.getAuthority(), uri, queryString);
    }

    private String getModifiedUrlString(HttpServletRequest request, String uri, String host, String port, String protocol) {
        String authority = getAuthority(host, port);
        String queryString = getQueryString(request);
        return String.format(URL, protocol, authority, uri, queryString);
    }

    private PluginsConfiguration getPluginConfiguration(HttpServletRequest request) {
        return ((ConfigurationManager) request.getSession().getServletContext().getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).getPluginsConfiguration();
    }

    private Configuration getConfiguration(HttpServletRequest request) {
        return ((ConfigurationManager) request.getSession().getServletContext().getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).getConfiguration();
    }

    private String getAuthority(String host, String port) {
        String authority;
        if (port == null) {
            authority = host;
        } else {
            authority = host + ":" + port;
        }
        return authority;
    }

    private String getQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString != null) {
            queryString = "?" + queryString;
        } else {
            queryString = "";
        }
        return queryString;
    }
}
