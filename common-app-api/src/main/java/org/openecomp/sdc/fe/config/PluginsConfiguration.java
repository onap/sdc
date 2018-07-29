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

        public boolean isOnline() {
            return isOnline;
        }

        public void setOnline(boolean online) {
            isOnline = online;
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


