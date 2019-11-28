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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.common.api.BasicConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class PluginsConfiguration extends BasicConfiguration {

    private List<Plugin> pluginsList;
    private Integer connectionTimeout;

    public PluginsConfiguration() {
        this.pluginsList = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Plugin {
        private String pluginId;
        private String pluginDiscoveryUrl;
        private String pluginSourceUrl;
        private String pluginStateUrl;
        private String pluginFeProxyUrl; // this is optional in case it is different from the source url.
        private String pluginProxyRedirectPath;
        private Map<String, PluginDisplayOptions> pluginDisplayOptions;
        private boolean isOnline;

    }

    @Getter
    @Setter
    @ToString
    public static class PluginDisplayOptions {
        private String displayName;
        private List<String> displayContext;
        private List<String> displayRoles;
    }

}


