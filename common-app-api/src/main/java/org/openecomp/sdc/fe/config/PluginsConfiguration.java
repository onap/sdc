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
        private String pluginHost;
        private String pluginPort;
        private String pluginPath;
        private String pluginStateUrl;
        private String pluginProtocol;
        private Map<String, PluginDisplayOptions> pluginDisplayOptions;

        public Map<String, PluginDisplayOptions> getPluginDisplayOptions() {
            return pluginDisplayOptions;
        }

        public void setPluginDisplayOptions(Map<String, PluginDisplayOptions> pluginDisplayOptions) {
            this.pluginDisplayOptions = pluginDisplayOptions;
        }

        public String getPluginStateUrl() {
            return pluginStateUrl;
        }

        public void setPluginStateUrl(String pluginStateUrl) {
            this.pluginStateUrl = pluginStateUrl;
        }

        public String getPluginProtocol() {
            return pluginProtocol;
        }

        public void setPluginProtocol(String pluginProtocol) {
            this.pluginProtocol = pluginProtocol;
        }

        public String getPluginId() {
            return pluginId;
        }

        public void setPluginId(String pluginId) {
            this.pluginId = pluginId;
        }

        public String getPluginHost() {
            return pluginHost;
        }

        public void setPluginHost(String pluginHost) {
            this.pluginHost = pluginHost;
        }

        public String getPluginPort() {
            return pluginPort;
        }

        public void setPluginPort(String pluginPort) {
            this.pluginPort = pluginPort;
        }

        public String getPluginPath() {
            return pluginPath;
        }

        public void setPluginPath(String pluginPath) {
            this.pluginPath = pluginPath;
        }

    }

    public static class PluginDisplayOptions {

        private String displayName;
        private List<String> displayContext;

        public List<String> getDisplayContext() {
            return displayContext;
        }

        public void setDisplayContext(List<String> displayContext) {
            this.displayContext = displayContext;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

    }

}


