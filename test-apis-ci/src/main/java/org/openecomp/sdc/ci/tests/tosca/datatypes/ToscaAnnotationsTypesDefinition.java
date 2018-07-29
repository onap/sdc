package org.openecomp.sdc.ci.tests.tosca.datatypes;

import org.openecomp.sdc.ci.tests.datatypes.enums.ToscaKeysEnum;
import org.yaml.snakeyaml.TypeDescription;

import java.util.HashMap;
import java.util.Map;

public class ToscaAnnotationsTypesDefinition extends ToscaTypesDefinition {

    public static final String SOURCE_ANNOTATION = "org.openecomp.annotations.Source";

    private Map<String, ToscaTypeDefinition> annotation_types = new HashMap<>();

    public Map<String, ToscaTypeDefinition> getAnnotation_types() {
        return annotation_types;
    }

    public void setAnnotation_types(Map<String, ToscaTypeDefinition> annotation_types) {
        this.annotation_types = annotation_types;
    }

    public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaAnnotationsTypesDefinition.class);
        typeDescription.putListPropertyType(ToscaKeysEnum.IMPORTS.getToscaKey(), String.class);
        typeDescription.putMapPropertyType(ToscaKeysEnum.ANNOTATION_TYPES.getToscaKey(), String.class, ToscaTypeDefinition.class);
        return typeDescription;
    }
}
