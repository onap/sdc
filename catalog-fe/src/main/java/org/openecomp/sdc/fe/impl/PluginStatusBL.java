/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.InvalidArgumentException;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;
import org.openecomp.sdc.fe.config.PluginsConfiguration.Plugin;
import org.openecomp.sdc.fe.utils.JettySSLUtils;

public class PluginStatusBL {

    private static final Logger log = Logger.getLogger(PluginStatusBL.class.getName());
    private static final String MAX_CONNECTION_POOL = "maxOutgoingConnectionPoolTotal";
    private static final String MAX_ROUTE_POOL = "maxOutgoingPerRoute";
    private final Gson gson;
    private final PluginsConfiguration pluginsConfiguration;
    private CloseableHttpClient client;
    private RequestConfig requestConfig;

    public PluginStatusBL() {
        this.pluginsConfiguration = ConfigurationManager.getConfigurationManager().getPluginsConfiguration();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        // check if we have secure connections in the plugin list, if not - we won't bother with it
        try {
            this.client = getPooledClient(this.hasSecuredPlugins());
        } catch (Exception e) {
            log.error("Could not initialize the Https client: {}", e.getMessage());
            log.debug("Exception:", e);
        }
    }

    public PluginStatusBL(CloseableHttpClient client) {
        this.pluginsConfiguration = ConfigurationManager.getConfigurationManager().getPluginsConfiguration();
        this.client = client;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private boolean hasSecuredPlugins() {
        if (this.getPluginsList() != null) {
            return pluginsConfiguration.getPluginsList().stream()
                .anyMatch(plugin -> plugin.getPluginDiscoveryUrl().toLowerCase().startsWith("https"));
        }
        return false;
    }

    private CloseableHttpClient getPooledClient(boolean isSecured) throws GeneralSecurityException, IOException {
        final PoolingHttpClientConnectionManager poolingConnManager;
        if (!isSecured) {
            poolingConnManager = new PoolingHttpClientConnectionManager();
        } else {
            SSLConnectionSocketFactory s = new SSLConnectionSocketFactory(JettySSLUtils.getSslContext(), new NoopHostnameVerifier());
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory()).register("https", s).build();
            poolingConnManager = new PoolingHttpClientConnectionManager(registry);
        }
        int maxTotal = System.getProperties().containsKey(MAX_CONNECTION_POOL) ? Integer.parseInt(System.getProperty(MAX_CONNECTION_POOL)) : 5;
        int routeMax = System.getProperties().containsKey(MAX_ROUTE_POOL) ? Integer.parseInt(System.getProperty(MAX_ROUTE_POOL)) : 20;
        poolingConnManager.setMaxTotal(maxTotal);
        poolingConnManager.setDefaultMaxPerRoute(routeMax);
        return HttpClients.custom().setConnectionManager(poolingConnManager).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
    }

    public String getPluginsList() {
        if (pluginsConfiguration == null || pluginsConfiguration.getPluginsList() == null) {
            log.warn("Configuration of type {} was not found", PluginsConfiguration.class);
            throw new InvalidArgumentException("the plugin configuration was not read successfully.");
        } else {
            log.debug("The value returned from getConfig is {}", pluginsConfiguration);
            return gson.toJson(pluginsConfiguration.getPluginsList());
        }
    }

    public String getPluginAvailability(String pluginId) {
        String result = null;
        if (pluginsConfiguration == null || pluginsConfiguration.getPluginsList() == null) {
            log.warn("Configuration of type {} was not found", PluginsConfiguration.class);
            throw new InvalidArgumentException("the plugin configuration was not read successfully.");
        } else {
            log.debug("The value returned from getConfig is {}", pluginsConfiguration);
            Integer connectionTimeout = pluginsConfiguration.getConnectionTimeout();
            this.requestConfig = RequestConfig.custom().setSocketTimeout(connectionTimeout).setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(connectionTimeout).build();
            Plugin wantedPlugin = pluginsConfiguration.getPluginsList().stream().filter(plugin -> plugin.getPluginId().equals(pluginId)).findAny()
                .orElse(null);
            if (wantedPlugin != null) {
                result = gson.toJson(checkPluginAvailability(wantedPlugin));
            }
        }
        return result;
    }

    private boolean checkPluginAvailability(Plugin plugin) {
        boolean result = false;
        log.debug("sending head request to id:{} url:{}", plugin.getPluginId(), plugin.getPluginDiscoveryUrl());
        HttpHead head = new HttpHead(plugin.getPluginDiscoveryUrl());
        head.setConfig(this.requestConfig);
        if (this.client == null) {
            log.debug("The plugin {} will not run because https is not configured on the FE server", plugin.getPluginId());
            return false;
        }
        try (CloseableHttpResponse response = this.client.execute(head)) {
            result = response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
            log.debug("The plugin {} is {} with result {}", plugin.getPluginId(), (result ? "online" : "offline"), result);
        } catch (IOException e) {
            log.debug("The plugin {} is offline", plugin.getPluginId());
            log.debug("Exception:", e);
        }
        return result;
    }
}
