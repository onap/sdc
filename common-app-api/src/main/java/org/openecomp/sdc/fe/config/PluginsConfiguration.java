package org.openecomp.sdc.fe.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.common.api.BasicConfiguration;

public class PluginsConfiguration extends BasicConfiguration {

    private List<Plugin> pluginsList;

    public List<Plugin> getPluginsList() {
        return pluginsList;
    }

    public void setPluginsList(List<Plugin> pluginsList) {
        this.pluginsList = pluginsList;
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



    }

}


