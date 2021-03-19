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
package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;

public class ToscaPolicyTemplate {

    private String type;
    private IToscaMetadata metadata;
    private Map<String, Object> properties;
    private List<String> targets;

    public ToscaPolicyTemplate(String type, IToscaMetadata metadata, Map<String, Object> properties, List<String> targets) {
        this.type = type;
        this.metadata = metadata;
        this.properties = properties;
        this.targets = targets;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public IToscaMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(IToscaMetadata metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public List<String> getTargets() {
        return targets;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }
}
