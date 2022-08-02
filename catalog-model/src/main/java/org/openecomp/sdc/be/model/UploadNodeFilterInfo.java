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
package org.openecomp.sdc.be.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadNodeFilterInfo {

    private String name;
    private Object tosca_id;
    private List<UploadNodeFilterPropertyInfo> properties = new ArrayList<>();
    private List<Map<String, UploadNodeFilterCapabilitiesInfo>> capabilities = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UploadNodeFilterPropertyInfo> getProperties() {
        return properties;
    }

    public void setProperties(List<UploadNodeFilterPropertyInfo> properties) {
        this.properties = properties;
    }

    public List<Map<String, UploadNodeFilterCapabilitiesInfo>> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Map<String, UploadNodeFilterCapabilitiesInfo>> capabilities) {
        this.capabilities = capabilities;
    }

    public Object getTosca_id() {
        return tosca_id;
    }

    public void setTosca_id(Object tosca_id) {
        this.tosca_id = tosca_id;
    }
}
