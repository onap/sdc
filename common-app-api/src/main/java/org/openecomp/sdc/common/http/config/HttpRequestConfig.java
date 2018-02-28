package org.openecomp.sdc.common.http.config;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestConfig {

    private String serverRootUrl;
    private Map<String, String> resourceNamespaces;
    
    public String getServerRootUrl() {
        return serverRootUrl;
    }
    
    public void setServerRootUrl(String serverRootUrl) {
        this.serverRootUrl = serverRootUrl;
    }
    
    public Map<String, String> getResourceNamespaces() {
        if(resourceNamespaces == null) {
            resourceNamespaces = new HashMap<>();
        }
        return resourceNamespaces;
    }
    
    public void setResourceNamespaces(Map<String, String> resourceNamespaces) {
        this.resourceNamespaces = resourceNamespaces;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HttpRequestConfig [serverRootUrl=");
        builder.append(serverRootUrl);
        builder.append(", resourceNamespaces=");
        builder.append(resourceNamespaces);
        builder.append("]");
        return builder.toString();
    }
}
