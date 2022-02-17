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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

@Getter
@Setter
@NoArgsConstructor
public class ToscaNodeTemplate {

    private String type;
    private List<Object> occurrences;
    private Map<String, String> instance_count;
    private List<String> directives;
    private Map<String, String> metadata;
    private String description;
    private Map<String, Object> properties;
    private Map<String, Object> attributes;
    private List<Map<String, ToscaTemplateRequirement>> requirements;
    private Map<String, ToscaTemplateCapability> capabilities;
    private Map<String, ToscaTemplateArtifact> artifacts;
    private NodeFilter node_filter;
    private Map<String, Object> interfaces;

    public void setDirectives(List<String> directives) {
        this.directives = CollectionUtils.isEmpty(directives) ? null : directives;
    }

    public void addInterface(String interfaceName, Object interfaceDataDefinition) {
        if (MapUtils.isEmpty(this.interfaces)) {
            this.interfaces = new HashMap<>();
        }
        this.interfaces.put(interfaceName, interfaceDataDefinition);
    }
}
