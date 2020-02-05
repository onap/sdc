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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.DataType;
import org.onap.sdc.tosca.datatypes.model.Import;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;

public class GlobalSubstitutionServiceTemplate extends ServiceTemplate {

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

    public void init()   {
        writeDefinitionSection();
        writeMetadataSection();
        writeImportsSection();
        setNode_types(new HashMap<>());
        setData_types(new HashMap<>());
    }

    public void appendNodes(final Map<String, NodeType> nodes) {
        final Optional<Map<String, NodeType>> nodeTypesToAdd = findNonGlobalTypesNodes(nodes);
        nodeTypesToAdd.ifPresent(nodeTypes -> getNode_types().putAll(nodeTypes));
    }

    public void appendDataTypes(final Map<String, DataType> dataTypeMap) {
        if (MapUtils.isEmpty(dataTypeMap)) {
            return;
        }
        dataTypeMap.entrySet().stream()
            .filter(dataTypeEntry -> !isGlobalDataType(dataTypeEntry.getKey()))
            .forEach(dataTypeEntry -> {
                final Optional<DataType> dataType = parseDataTypeToYamlObject(dataTypeEntry);
                dataType.ifPresent(dataType1 -> getData_types().put(dataTypeEntry.getKey(), dataType1));
            });
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

    private Optional<Map<String, NodeType>> findNonGlobalTypesNodes(final Map<String, NodeType> nodes){
        final Map<String, NodeType> globalNodeTypes = getAllGlobalNodeTypes();
        if (MapUtils.isEmpty(globalNodeTypes)) {
            return Optional.of(nodes);
        }

        final Map<String, NodeType> nodeTypesToAdd = new HashMap<>();

        for(Map.Entry<String, NodeType> nodeTypeEntry : nodes.entrySet()){
            if(!globalNodeTypes.containsKey(nodeTypeEntry.getKey())){
                Optional<NodeType> nodeType = parseNodeTypeToYamlObject(nodeTypeEntry);
                nodeType
                    .ifPresent(nodeTypeValue -> nodeTypesToAdd.put(nodeTypeEntry.getKey(), nodeTypeValue));
            }
        }

        return Optional.of(nodeTypesToAdd);
    }

    private boolean isGlobalDataType(final String dataType) {
        final Map<String, DataType> allGlobalDataTypes = getAllGlobalDataTypes();
        if (MapUtils.isEmpty(allGlobalDataTypes)) {
            return false;
        }

        return allGlobalDataTypes.containsKey(dataType);
    }

    private Optional<NodeType> parseNodeTypeToYamlObject(final Entry<String, NodeType> nodeTypeEntry) {
        return ToscaConverterUtil
            .createObjectFromClass(nodeTypeEntry.getKey(), nodeTypeEntry.getValue(), NodeType.class);
    }

    private Optional<DataType> parseDataTypeToYamlObject(final Entry<String, DataType> dataTypeEntry) {
        return ToscaConverterUtil
            .createObjectFromClass(dataTypeEntry.getKey(), dataTypeEntry.getValue(), DataType.class);
    }

    private Map<String, DataType> getAllGlobalDataTypes(){
        final Map<String, DataType> globalDataTypeMap = new HashMap<>();

        for (Map.Entry<String, ServiceTemplate> serviceTemplateEntry : globalServiceTemplates.entrySet()) {
            final Map<String, DataType> dataTypeMap = serviceTemplateEntry.getValue().getData_types();
            if (MapUtils.isNotEmpty(dataTypeMap)) {
                globalDataTypeMap.putAll(dataTypeMap);
            }
        }

        return globalDataTypeMap;
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
