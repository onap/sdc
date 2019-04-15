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

import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToscaNodeTemplate {

    private String type;
    private List<String> directives;
    private ToscaMetadata metadata;
    private String description;
    private Map<String, Object> properties;
    private List<Map<String, ToscaTemplateRequirement>> requirements;
    private Map<String, ToscaTemplateCapability> capabilities;
    private Map<String, ToscaTemplateArtifact> artifacts;
    private NodeFilter node_filter;
    private Map<String, Object> interfaces;

    public void setDirectives(List<String> directives) {
        if (CollectionUtils.isEmpty(directives)) {
            this.directives = null;
            return;
        }
        this.directives = directives;
    }

    public void addInterface(String interfaceName, Object interfaceDataDefinition) {
        if (MapUtils.isEmpty(this.interfaces)) {
            this.interfaces = new HashMap<>();
        }

        this.interfaces.put(interfaceName, interfaceDataDefinition);
    }
}

