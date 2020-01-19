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
