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
package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;

public class NodeFilter {

    Object tosca_id;
    List<Map<String, List<Object>>> properties;
    List<Map<String, CapabilityFilter>> capabilities;

    public Object getTosca_id() {
        return tosca_id;
    }

    public void setTosca_id(Object tosca_id) {
        this.tosca_id = tosca_id;
    }

    public List<Map<String, CapabilityFilter>> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Map<String, CapabilityFilter>> capabilities) {
        this.capabilities = capabilities;
    }

    public List<Map<String, List<Object>>> getProperties() {
        return properties;
    }

    public void setProperties(List<Map<String, List<Object>>> properties) {
        this.properties = properties;
    }
}
