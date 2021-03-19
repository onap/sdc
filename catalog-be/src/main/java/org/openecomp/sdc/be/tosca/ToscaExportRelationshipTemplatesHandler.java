/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.tosca;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaRelationship;
import org.openecomp.sdc.be.tosca.model.ToscaRelationshipTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateRequirement;

/**
 * Handles the relationship_templates in the TOSCA export
 */
public class ToscaExportRelationshipTemplatesHandler {

    /**
     * Creates the relationship_templates map based on the node_templates requirements.
     *
     * @param nodeTemplateMap the node template map
     * @return the relationship_templates map
     */
    public Map<String, ToscaRelationshipTemplate> createFrom(final Map<String, ToscaNodeTemplate> nodeTemplateMap) {
        if (MapUtils.isEmpty(nodeTemplateMap)) {
            return Collections.emptyMap();
        }
        final Map<String, ToscaRelationshipTemplate> relationshipTemplates = new HashMap<>();
        for (final Entry<String, ToscaNodeTemplate> nodeEntry : nodeTemplateMap.entrySet()) {
            final ToscaNodeTemplate nodeTemplate = nodeEntry.getValue();
            if (isEmpty(nodeTemplate.getRequirements())) {
                continue;
            }
            final AtomicInteger relationshipTemplateCount = new AtomicInteger(1);
            for (final Map<String, ToscaTemplateRequirement> requirementMap : nodeTemplate.getRequirements()) {
                requirementMap.entrySet().stream().filter(entry -> entry.getValue().isRelationshipComplexNotation()).forEach(requirementEntry -> {
                    final ToscaTemplateRequirement requirement = requirementEntry.getValue();
                    final ToscaRelationship relationship = requirement.getRelationshipAsComplexType();
                    final ToscaRelationshipTemplate relationshipTemplate = new ToscaRelationshipTemplate();
                    relationshipTemplate.setType(relationship.getType());
                    relationshipTemplate.setInterfaces(relationship.getInterfaces());
                    final String relationshipName = String
                        .format("%s.%s", ToscaRelationshipTemplate.createRelationshipName(nodeEntry.getKey(), requirementEntry.getKey()),
                            relationshipTemplateCount);
                    requirement.setRelationship(relationshipName);
                    relationshipTemplate.setName(relationshipName);
                    relationshipTemplates.put(relationshipName, relationshipTemplate);
                    relationshipTemplateCount.incrementAndGet();
                });
            }
        }
        return relationshipTemplates;
    }
}
