/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.core.impl;

import static org.openecomp.core.converter.datatypes.Constants.ONAP_INDEX;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.onap.sdc.tosca.datatypes.model.Import;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GlobalSubstitutionServiceTemplate extends ServiceTemplate {
    private static final Logger logger = LoggerFactory.getLogger(ServiceTemplate.class);

    public static final String GLOBAL_SUBSTITUTION_SERVICE_FILE_NAME =
        "GlobalSubstitutionTypesServiceTemplate.yaml";
    public static final String TEMPLATE_NAME_PROPERTY = "template_name";
    public static final String DEFININTION_VERSION = "tosca_simple_yaml_1_0_0";
    public static final String HEAT_INDEX = "openecomp_heat_index";
    public static final String HEAT_INDEX_IMPORT_FILE = "openecomp-heat/_index.yml";
    public static final String ONAP_INDEX_IMPORT_FILE = "onap/_index.yml";
    private static final Map<String, ServiceTemplate> globalServiceTemplates =
        GlobalTypesGenerator.getGlobalTypesServiceTemplate(OnboardingTypesEnum.CSAR);

    public GlobalSubstitutionServiceTemplate() {
        super();
        init();
    }


    public void appendNodes(Map<String, NodeType> nodes) {
        Optional<Map<String, NodeType>> nodeTypesToAdd =
            removeExistingGlobalTypes(nodes);

        nodeTypesToAdd.ifPresent(nodeTypes -> getNode_types().putAll(nodeTypes));
    }

    public void init()   {
        writeDefinitionSection();
        writeMetadataSection();
        writeImportsSection();
        setNode_types(new HashMap<>());
    }

    private void writeImportsSection() {
        List<Map<String, Import>> imports = new ArrayList<>();
        Map<String, Import> stringImportMap = new HashMap<>();
        imports.add(stringImportMap);
        setImports(imports);
        Import imprtObj = new Import();
        imprtObj.setFile(HEAT_INDEX_IMPORT_FILE);
        stringImportMap.put(HEAT_INDEX, imprtObj);
        Import onapDefinitionsImport = new Import();
        onapDefinitionsImport.setFile(ONAP_INDEX_IMPORT_FILE);
        stringImportMap.put(ONAP_INDEX, onapDefinitionsImport);
    }


    private void writeMetadataSection() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(TEMPLATE_NAME_PROPERTY, "GlobalSubstitutionTypes");
        setMetadata(metadata);
    }

    private void writeDefinitionSection() {
        setTosca_definitions_version(DEFININTION_VERSION);
    }

    private Optional<Map<String, NodeType>> removeExistingGlobalTypes(Map<String, NodeType> nodes){
        Map<String, NodeType> nodeTypesToAdd = new HashMap<>();
        ServiceTemplate serviceTemplate = globalServiceTemplates.get("openecomp/nodes.yml");

        if(Objects.isNull(serviceTemplate) || MapUtils.isEmpty(serviceTemplate.getNode_types())){
            return Optional.of(nodes);
        }

        Map<String, NodeType> globalNodeTypes = getAllGlobalNodeTypes();
        for(Map.Entry<String, NodeType> nodeTypeEntry : nodes.entrySet()){
            if(!globalNodeTypes.containsKey(nodeTypeEntry.getKey())){
                Optional<NodeType> nodeType =
                    ToscaConverterUtil
                        .createObjectFromClass(nodeTypeEntry.getKey(), nodeTypeEntry.getValue(), NodeType.class);

                nodeType
                    .ifPresent(nodeTypeValue -> nodeTypesToAdd.put(nodeTypeEntry.getKey(), nodeTypeValue));
            }
        }

        return Optional.of(nodeTypesToAdd);
    }

    private Map<String, NodeType> getAllGlobalNodeTypes(){
        Map<String, NodeType> globalNodeTypes = new HashMap<>();

        for(Map.Entry<String, ServiceTemplate> serviceTemplateEntry : globalServiceTemplates.entrySet()){
            if(isNodesServiceTemplate(serviceTemplateEntry.getKey())){
                globalNodeTypes.putAll(serviceTemplateEntry.getValue().getNode_types());
            }
        }

        return globalNodeTypes;
    }

    private boolean isNodesServiceTemplate(String filename) {
        return filename.endsWith("nodes.yml") || filename.endsWith("nodes.yaml");
    }
}
