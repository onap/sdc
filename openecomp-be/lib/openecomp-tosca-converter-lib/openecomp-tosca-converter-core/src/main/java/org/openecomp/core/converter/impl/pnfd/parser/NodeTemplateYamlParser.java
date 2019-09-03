/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.core.converter.impl.pnfd.parser;

import java.util.List;
import java.util.Map;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;

/**
 * Handles YAML from/to {@link NodeTemplate} conversions
 */
public class NodeTemplateYamlParser {

    private NodeTemplateYamlParser() {
    }

    /**
     * Parses the given a YAML object to a {@link NodeTemplate} instance.
     * @param nodeTemplateYaml    the YAML object representing a TOSCA Node Template
     * @return
     *  A new instance of {@link NodeTemplate}.
     */
    public static NodeTemplate parse(final Map<String, Object> nodeTemplateYaml) {
        final NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setType((String) nodeTemplateYaml.get("type"));
        nodeTemplate.setDescription((String) nodeTemplateYaml.get("description"));
        nodeTemplate.setCopy((String) nodeTemplateYaml.get("copy"));
        nodeTemplate.setProperties((Map<String, Object>) nodeTemplateYaml.get("properties"));
        nodeTemplate.setAttributes((Map<String, Object>) nodeTemplateYaml.get("attributes"));
        nodeTemplate.setDirectives((List<String>) nodeTemplateYaml.get("directives"));
        nodeTemplate.setMetadata((Map<String, String>) nodeTemplateYaml.get("metadata"));
        nodeTemplate.setInterfaces((Map<String, Object>) nodeTemplateYaml.get("interfaces"));

        return nodeTemplate;
    }


}
