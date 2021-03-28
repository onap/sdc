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
package org.onap.sdc.tosca.datatypes.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TopologyTemplate {

    private String description;
    private Map<String, ParameterDefinition> inputs;
    private Map<String, NodeTemplate> node_templates;
    private Map<String, RelationshipTemplate> relationship_templates;
    private Map<String, GroupDefinition> groups;
    private Map<String, ParameterDefinition> outputs;
    private SubstitutionMapping substitution_mappings;
    private Map<String, PolicyDefinition> policies;
    private List<WorkflowDefinition> workflows;

    /**
     * Add group.
     *
     * @param groupKey        the group key
     * @param groupDefinition the group definition
     */
    public void addGroup(String groupKey, GroupDefinition groupDefinition) {
        if (Objects.isNull(this.groups)) {
            this.groups = new HashMap<>();
        }
        this.groups.put(groupKey, groupDefinition);
    }

}
