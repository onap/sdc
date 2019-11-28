/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.onap.config.type;

import org.onap.config.Constants;

public class ConfigurationQuery {

    private String tenant = Constants.DEFAULT_TENANT;
    private String namespace = Constants.DEFAULT_NAMESPACE;
    private String key;
    private boolean fallback;
    private boolean externalLookup;
    private boolean latest;
    private boolean nodeSpecific;

    public ConfigurationQuery fallback(boolean fallback) {
        this.fallback = fallback;
        return this;
    }

    public ConfigurationQuery latest(boolean val) {
        this.latest = val;
        return this;
    }

    public ConfigurationQuery nodeSpecific(boolean val) {
        this.nodeSpecific = val;
        return this;
    }

    public ConfigurationQuery externalLookup(boolean val) {
        this.externalLookup = val;
        return this;
    }

    public ConfigurationQuery tenant(String id) {
        if (id != null) {
            tenant = id;
        }
        return this;
    }


    public ConfigurationQuery namespace(String id) {
        if (id != null) {
            namespace = id;
        }
        return this;
    }

    public ConfigurationQuery key(String id) {
        key = id;
        return this;
    }

    public String getTenant() {
        return tenant.toUpperCase();
    }

    public String getNamespace() {
        return namespace.toUpperCase();
    }

    public String getKey() {
        return key;
    }

    public boolean isFallback() {
        return fallback;
    }

    public boolean isNodeSpecific() {
        return nodeSpecific;
    }

    public boolean isExternalLookup() {
        return externalLookup;
    }

    public boolean isLatest() {
        return latest;
    }

    @Override
    public String toString() {
        return "ConfigurationQuery{" +
                "tenant='" + tenant + '\'' +
                ", namespace='" + namespace + '\'' +
                ", key='" + key + '\'' +
                ", fallback=" + fallback +
                ", externalLookup=" + externalLookup +
                ", latest=" + latest +
                ", nodeSpecific=" + nodeSpecific +
                '}';
    }
}
