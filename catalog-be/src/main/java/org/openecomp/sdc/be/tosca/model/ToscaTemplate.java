/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.apache.commons.lang3.tuple.Triple;
import org.openecomp.sdc.be.model.Component;

import java.util.List;
import java.util.Map;

public class ToscaTemplate {

    private String tosca_definitions_version;
    private ToscaMetadata metadata;
    private List<Map<String, Map<String, String>>> imports;
    private Map<String, Object> interface_types;
    private Map<String, ToscaDataType> data_types;
    private Map<String, ToscaNodeType> node_types;
    private ToscaTopolgyTemplate topology_template;

    private List<Triple<String, String, Component>> dependencies;

    public ToscaTemplate(String tosca_definitions_version) {
        this.tosca_definitions_version = tosca_definitions_version;
    }

    public Map<String, ToscaNodeType> getNode_types() {
        return node_types;
    }

    public void setNode_types(Map<String, ToscaNodeType> node_types) {
        this.node_types = node_types;
    }


    public List<Map<String, Map<String, String>>> getImports() {
        return imports;
    }

    public void setImports(List<Map<String, Map<String, String>>> imports) {
        this.imports = imports;
    }

    public String getTosca_definitions_version() {
        return tosca_definitions_version;
    }

    public void setTosca_definitions_version(String tosca_definitions_version) {
        this.tosca_definitions_version = tosca_definitions_version;
    }

    public ToscaMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ToscaMetadata metadata) {
        this.metadata = metadata;
    }

    public ToscaTopolgyTemplate getTopology_template() {
        return topology_template;
    }

    public void setTopology_template(ToscaTopolgyTemplate topology_template) {
        this.topology_template = topology_template;
    }

    public List<Triple<String, String, Component>> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Triple<String, String, Component>> dependencies) {
        this.dependencies = dependencies;
    }

    public Map<String, Object> getInterface_types() {
        return interface_types;
    }

    //    public void setInterface_types(Map<String, Object> interface_types) {
    //        this.interface_types = interface_types;
    //    }

    public void setInterface_types(Map<String, Object> interface_types) {
        this.interface_types = interface_types;

    }

    /**
     * Gets data_types map.
     * @return Current data_types map.
     */
    public Map<String, ToscaDataType> getData_types() {
        return data_types;
    }

    /**
     * Sets data_types map.
     * @param data_types New data_types map.
     */
    public void setData_types(Map<String, ToscaDataType> data_types) {
        this.data_types = data_types;
    }
}

