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
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.InvalidArgumentException;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;
import org.openecomp.sdc.fe.config.PluginsConfiguration.Plugin;

import java.io.IOException;

public class PluginStatusBL {

    private static final Logger log = Logger.getLogger(PluginStatusBL.class.getName());
    private final Gson gson;
    private final CloseableHttpClient client;
    private final PluginsConfiguration pluginsConfiguration;
    private RequestConfig requestConfig;

    public PluginStatusBL() {
        this.pluginsConfiguration = ConfigurationManager.getConfigurationManager().getPluginsConfiguration();
        this.client = HttpClients.createDefault();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public PluginStatusBL(CloseableHttpClient client) {
        this.pluginsConfiguration = ConfigurationManager.getConfigurationManager().getPluginsConfiguration();
        this.client = client;

        this.gson = new GsonBuilder().setPrettyPrinting().create();

    }

    public String getPluginsList() {
        String result = null;

        if (pluginsConfiguration == null || pluginsConfiguration.getPluginsList() == null) {
            log.warn("Configuration of type {} was not found", PluginsConfiguration.class);
            throw new InvalidArgumentException("the plugin configuration was not read successfully.");

        } else {
            log.debug("The value returned from getConfig is {}", pluginsConfiguration);

            result = gson.toJson(pluginsConfiguration.getPluginsList());
        }
        return result;
    }

    public String getPluginAvailability(String pluginId) {
        String result = null;

        if (pluginsConfiguration == null || pluginsConfiguration.getPluginsList() == null) {
            log.warn("Configuration of type {} was not found", PluginsConfiguration.class);
            throw new InvalidArgumentException("the plugin configuration was not read successfully.");

        } else {
            log.debug("The value returned from getConfig is {}", pluginsConfiguration);
            Integer connectionTimeout = pluginsConfiguration.getConnectionTimeout();
            this.requestConfig = RequestConfig.custom()
                    .setSocketTimeout(connectionTimeout)
                    .setConnectTimeout(connectionTimeout)
                    .setConnectionRequestTimeout(connectionTimeout).build();


            Plugin wantedPlugin = pluginsConfiguration.getPluginsList().stream()
                    .filter(plugin -> plugin.getPluginId().equals(pluginId))
                    .findAny()
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
