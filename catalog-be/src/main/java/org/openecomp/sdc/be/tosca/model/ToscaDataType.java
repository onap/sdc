/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Limited. All rights reserved.
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

package org.openecomp.sdc.be.tosca.model;

import java.util.Map;

/**
 * Represents a data type (in data_types in TOSCA model).
 */
public class ToscaDataType {

    private String derived_from;
    private String version;
    private Map<String, String> metadata;
    private String description;
    //private List<ToscaConstraint> constraints;
    private Map<String, ToscaProperty> properties;

    /**
     * Gets derived_from.
     *
     * @return Current derived_from value.
     */
    public String getDerived_from() {
        return derived_from;
    }

    /**
     * Sets derived_from.
     *
     * @param derived_from New derived_from value.
     */
    public void setDerived_from(String derived_from) {
        this.derived_from = derived_from;
    }

    /**
     * Gets version.
     *
     * @return Current version value.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets version.
     *
     * @param version New version value.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets metadata map.
     *
     * @return Current metadata map.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets metadata map.
     *
     * @param metadata New metadata map.
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets description.
     *
     * @return Current description value.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description New description value.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets properties map.
     *
     * @return Current properties map.
     */
    public Map<String, ToscaProperty> getProperties() {
        return properties;
    }

    /**
     * Sets properties map.
     *
     * @param properties New properties map.
     */
    public void setProperties(Map<String, ToscaProperty> properties) {
        this.properties = properties;
    }
}
