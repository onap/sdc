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

package org.openecomp.sdc.fe.config;

import org.openecomp.sdc.common.api.BasicConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PluginsConfiguration extends BasicConfiguration {

    private List<Plugin> pluginsList;
    private Integer connectionTimeout;

    public List<Plugin> getPluginsList() {
        return pluginsList;
    }

    public void setPluginsList(List<Plugin> pluginsList) {
        this.pluginsList = pluginsList;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public PluginsConfiguration() {
        this.pluginsList = new ArrayList<>();
    }

    public static class Plugin {

        private String pluginId;
        private String pluginDiscoveryUrl;
        private String pluginSourceUrl;
        private String pluginStateUrl;
        private Map<String, PluginDisplayOptions> pluginDisplayOptions;
        private boolean isOnline;

        public String getPluginId() {
            return pluginId;
        }

        public void setPluginId(String pluginId) {
            this.pluginId = pluginId;
        }

        public String getPluginDiscoveryUrl() {
            return pluginDiscoveryUrl;
        }

        public void setPluginDiscoveryUrl(String pluginDiscoveryUrl) {
            this.pluginDiscoveryUrl = pluginDiscoveryUrl;
        }

        public String getPluginSourceUrl() {
            return pluginSourceUrl;
        }

        public void setPluginSourceUrl(String pluginSourceUrl) {
            this.pluginSourceUrl = pluginSourceUrl;
        }

        public String getPluginStateUrl() {
            return pluginStateUrl;
        }

        public void setPluginStateUrl(String pluginStateUrl) {
            this.pluginStateUrl = pluginStateUrl;
        }

        public Map<String, PluginDisplayOptions> getPluginDisplayOptions() {
            return pluginDisplayOptions;
        }

        public void setPluginDisplayOptions(Map<String, PluginDisplayOptions> pluginDisplayOptions) {
            this.pluginDisplayOptions = pluginDisplayOptions;
        }

    }

    public static class PluginDisplayOptions {

        private String displayName;
        private List<String> displayContext;
        private List<String> displayRoles;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public List<String> getDisplayContext() {
            return displayContext;
        }

        public void setDisplayContext(List<String> displayContext) {
            this.displayContext = displayContext;
        }

        public List<String> getDisplayRoles() {
            return displayRoles;
        }

        public void setDisplayRoles(List<String> displayRoles) {
            this.displayRoles = displayRoles;
        }

        @Override
        public String toString() {
            return "PluginDisplayOptions[" +
                    "displayName='" + displayName +
                    ", displayContext=" + displayContext +
                    ", displayRoles=" + displayRoles +
                    ']';
        }
    }

    @Override
    public String toString() {
        return "PluginsConfiguration[" + "pluginsList=" + pluginsList + ", connectionTimeout=" + connectionTimeout +']';
    }
}


