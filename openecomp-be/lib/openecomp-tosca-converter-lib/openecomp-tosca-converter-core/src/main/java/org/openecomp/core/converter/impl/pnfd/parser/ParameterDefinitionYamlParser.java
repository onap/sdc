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

import java.util.Map;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.Status;

/**
 * Handles YAML from/to {@link ParameterDefinition} conversions
 */
public class ParameterDefinitionYamlParser {

    private ParameterDefinitionYamlParser() {
    }

    /**
     * Parses the given a YAML object to a {@link ParameterDefinition} instance.
     * @param parameterDefinitionYaml    the YAML object representing a TOSCA Parameter Definition
     * @return
     *  A new instance of {@link ParameterDefinition}.
     */
    public static ParameterDefinition parse(final Map<String, Object> parameterDefinitionYaml) {
        final ParameterDefinition parameterDefinition = new ParameterDefinition();
        parameterDefinition.set_default(parameterDefinitionYaml.get("default"));
        parameterDefinition.setDescription((String) parameterDefinitionYaml.get("description"));
        final Map<String, Object> entrySchemaYaml = (Map<String, Object>) parameterDefinitionYaml.get("entry_schema");
        if (entrySchemaYaml != null) {
            final EntrySchema entrySchema = new EntrySchema();
            entrySchema.setType((String) entrySchemaYaml.get("type"));
            parameterDefinition.setEntry_schema(entrySchema);
        }
        parameterDefinition.setRequired((Boolean) parameterDefinitionYaml.get("required"));
        parameterDefinition.setType((String) parameterDefinitionYaml.get("type"));
        parameterDefinition.setStatus((Status) parameterDefinitionYaml.get("status"));

        return parameterDefinition;
    }
}
