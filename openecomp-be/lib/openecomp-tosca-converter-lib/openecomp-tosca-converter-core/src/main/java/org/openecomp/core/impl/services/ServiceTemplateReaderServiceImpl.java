/*-
 * ============LICENSE_START=======================================================
 *  Modification Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.openecomp.core.impl.services;

import static org.openecomp.core.converter.datatypes.Constants.INPUTS;
import static org.openecomp.core.converter.datatypes.Constants.METADATA;
import static org.openecomp.core.converter.datatypes.Constants.NODE_TEMPLATES;
import static org.openecomp.core.converter.datatypes.Constants.NODE_TYPES;
import static org.openecomp.core.converter.datatypes.Constants.OUTPUTS;
import static org.openecomp.core.converter.datatypes.Constants.POLICIES;
import static org.openecomp.core.converter.datatypes.Constants.SUBSTITUTION_MAPPINGS;
import static org.openecomp.core.converter.datatypes.Constants.TOPOLOGY_TEMPLATE;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DATA_TYPES;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.IMPORTS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.converter.ServiceTemplateReaderService;

public class ServiceTemplateReaderServiceImpl implements ServiceTemplateReaderService {

    private final Map<String, Object> readServiceTemplate;

    public ServiceTemplateReaderServiceImpl(byte[] serviceTemplateContent) {
        this.readServiceTemplate = readServiceTemplate(serviceTemplateContent);
    }

    @Override
    public Map<String, Object> readServiceTemplate(byte[] serviceTemplateContent) {
        return new YamlUtil().yamlToObject(new String(serviceTemplateContent), Map.class);
    }

    @Override
    public List<Object> getImports() {
        final String importsElementName = IMPORTS.getElementName();
        return Objects.isNull(this.readServiceTemplate.get(importsElementName)) ? new ArrayList<>()
            : (List<Object>) this.readServiceTemplate.get(importsElementName);
    }

    @Override
    public List<Object> getPolicies() {
        List<Object> policiesList = new ArrayList<>();
        if (Objects.nonNull(this.getTopologyTemplate()) && Objects.nonNull(((Map<String, Object>) this.getTopologyTemplate()).get(POLICIES))) {
            policiesList = (List<Object>) ((Map<String, Object>) this.getTopologyTemplate()).get(POLICIES);
        }
        return policiesList;
    }

    @Override
    public Object getMetadata() {
        return this.readServiceTemplate.get(METADATA);
    }

    @Override
    public Object getToscaVersion() {
        return this.readServiceTemplate.get(TOSCA_VERSION.getElementName());
    }

    @Override
    public Map<String, Object> getNodeTypes() {
        return Objects.isNull(this.readServiceTemplate.get(NODE_TYPES)) ? new HashMap<>()
            : (Map<String, Object>) this.readServiceTemplate.get(NODE_TYPES);
    }

    @Override
    public Object getTopologyTemplate() {
        return this.readServiceTemplate.get(TOPOLOGY_TEMPLATE);
    }

    @Override
    public Map<String, Object> getNodeTemplates() {
        return Objects.isNull(this.getTopologyTemplate()) ? new HashMap<>()
            : (Map<String, Object>) ((Map<String, Object>) this.getTopologyTemplate()).get(NODE_TEMPLATES);
    }

    @Override
    public Map<String, Object> getInputs() {
        return Objects.isNull(this.getTopologyTemplate()) ? new HashMap<>()
            : (Map<String, Object>) ((Map<String, Object>) this.getTopologyTemplate()).get(INPUTS);
    }

    @Override
    public Map<String, Object> getOutputs() {
        return Objects.isNull(this.getTopologyTemplate()) ? new HashMap<>()
            : (Map<String, Object>) ((Map<String, Object>) this.getTopologyTemplate()).get(OUTPUTS);
    }

    @Override
    public Map<String, Object> getSubstitutionMappings() {
        return Objects.isNull(this.getTopologyTemplate()) ? new HashMap<>()
            : (Map<String, Object>) ((Map<String, Object>) this.getTopologyTemplate()).get(SUBSTITUTION_MAPPINGS);
    }

    @Override
    public Map<String, Object> getDataTypes() {
        final String dataTypesElementName = DATA_TYPES.getElementName();
        return Objects.isNull(this.readServiceTemplate.get(dataTypesElementName)) ? new HashMap<>()
            : (Map<String, Object>) this.readServiceTemplate.get(dataTypesElementName);
    }
}
