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
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.MapUtils;

@Getter
public class ToscaTopolgyTemplate {

    @Setter
    private Map<String, ToscaProperty> inputs;
    @Setter
    private Map<String, ToscaProperty> outputs;
    @Setter
    private Map<String, ToscaNodeTemplate> node_templates;
    private Map<String, ToscaGroupTemplate> groups;
    private Map<String, ToscaPolicyTemplate> policies;
    @Setter
    private SubstitutionMapping substitution_mappings;
    @Setter
    private Map<String, ToscaRelationshipTemplate> relationshipTemplates;

    public void addGroups(Map<String, ToscaGroupTemplate> groups) {
        if (this.groups == null) {
            this.groups = new HashMap<>();
        }
        this.groups.putAll(groups);
    }

    public void addPolicies(Map<String, ToscaPolicyTemplate> policiesMap) {
        if (this.policies == null) {
            this.policies = new HashMap<>();
        }
        this.policies.putAll(policiesMap);
    }

    public boolean isEmpty() {
        return substitution_mappings == null &&
            MapUtils.isEmpty(inputs) &&
            MapUtils.isEmpty(outputs) &&
            MapUtils.isEmpty(node_templates) &&
            MapUtils.isEmpty(groups) &&
            MapUtils.isEmpty(policies) &&
            MapUtils.isEmpty(relationshipTemplates);
    }
}
