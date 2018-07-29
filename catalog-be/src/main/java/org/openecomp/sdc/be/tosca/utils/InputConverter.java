package org.openecomp.sdc.be.tosca.utils;

import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.tosca.PropertyConvertor;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.model.ToscaAnnotation;
import org.openecomp.sdc.be.tosca.model.ToscaInput;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
public class InputConverter {
    private PropertyConvertor propertyConvertor;
    private static final Logger log = Logger.getLogger(ToscaExportHandler.class);


    public InputConverter() {
        this.propertyConvertor = PropertyConvertor.getInstance();

    }
    /**
     * This is the converter made for input
     * input is derived from properties and  is similar to properties
     * now that it was added annotations , we created a new convertetor for it
     * Input
     *    List of annotation
     *          Annotation:
     *              name
     *              type
     *              description
     *              list of properties */
    public Map<String, ToscaProperty>  convertInputs( List<InputDefinition> inputDef,Map<String, DataTypeDefinition> dataTypes) {
        log.debug("convert inputs to to tosca  ");

        Map<String, ToscaProperty> inputs = new HashMap<>();
        if (inputDef != null) {
            inputDef.forEach(i -> {
                //Extract input the same as property
                ToscaProperty toscaProperty = propertyConvertor.convertProperty(dataTypes, i, false);
                //now that we have Tosca property we create new object called tosca input which drives from it
                ToscaInput toscaInput = new ToscaInput(toscaProperty);
                List<Annotation> annotations = i.getAnnotations();
                extractAnnotations(dataTypes, toscaInput, annotations);
                inputs.put(i.getName(), toscaInput);
            });
        }
        return inputs;
    }

    private void extractAnnotations(Map<String, DataTypeDefinition> dataTypes, ToscaInput toscaInput, List<Annotation> annotationsList) {
        if (annotationsList != null) {
            annotationsList.forEach(inputAnnotation -> {
                ToscaAnnotation annotation = new ToscaAnnotation();
                if ((inputAnnotation.getType()) != null) {
                    annotation.setType(inputAnnotation.getType());
                }
                if (inputAnnotation.getDescription() != null) {
                    annotation.setDescription(inputAnnotation.getDescription());
                }
                if (inputAnnotation.getProperties() != null) {
                    Map<String, Object> properties = new HashMap<>();
                    inputAnnotation.getProperties().forEach(k -> {
                        propertyConvertor.convertAndAddValue(dataTypes,properties,k, k::getValue);
                    });
                    annotation.setProperties(properties);
                }
                toscaInput.addAnnotation(inputAnnotation.getName(), annotation);
            });
        }
    }
}



