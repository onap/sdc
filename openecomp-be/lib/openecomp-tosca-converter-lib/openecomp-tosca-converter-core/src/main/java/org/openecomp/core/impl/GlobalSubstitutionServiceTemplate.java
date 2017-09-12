package org.openecomp.core.impl;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalSubstitutionServiceTemplate extends ServiceTemplate {
    private static final Logger logger = LoggerFactory.getLogger(ServiceTemplate.class);

    public static final String GLOBAL_SUBSTITUTION_SERVICE_FILE_NAME = "GlobalSubstitutionServiceTemplate.yaml";
    public static final String TEMPLATE_NAME_PROPERTY = "template_name";
    public static final String DEFININTION_VERSION = "tosca_simple_yaml_1_0_0";
    public static final String HEAT_INDEX = "openecomp_heat_index";

    public GlobalSubstitutionServiceTemplate() {
        super();
        init();
    }


    public void appendNodes(Map<String, NodeType> nodes) {
        getNode_types().putAll(nodes);
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
        imprtObj.setFile("openecomp-heat/_index.yml");
        stringImportMap.put("openecomp_heat_index", imprtObj);
    }


    private void writeMetadataSection() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(TEMPLATE_NAME_PROPERTY, "GlobalSubstitutionTypes");
        setMetadata(metadata);
    }

    private void writeDefinitionSection() {
        setTosca_definitions_version(DEFININTION_VERSION);
    }
}
