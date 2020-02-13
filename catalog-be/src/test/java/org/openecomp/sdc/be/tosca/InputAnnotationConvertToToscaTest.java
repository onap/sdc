/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.tosca;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.tosca.model.ToscaInput;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.utils.InputConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InputAnnotationConvertToToscaTest {
    private InputConverter inputConverter;
    private PropertyDefinition property , property1 , property2,property3;
    private InputDefinition inputDefinition;
    Map<String, DataTypeDefinition> dataTypes;

    @Before
    public void setUp(){
        property = new PropertyDefinition();
        property.setName("myProperty");
        property.setType(ToscaPropertyType.STRING.getType());
        property.setValue("this is property string");
        property.setDescription("propertyDescription");
        SchemaDefinition schemaDefinition = new SchemaDefinition();
        schemaDefinition.setProperty(property);


        property1 = new PropertyDefinition();
        property1.setName("otherProperty");
        property1.setType(ToscaPropertyType.INTEGER.getType());
        property1.setValue("2");

        property1.setSchema(schemaDefinition);


        property2 = new PropertyDefinition();
        property2.setName("annotationProperty");
        property2.setType(ToscaPropertyType.FLOAT.getType());
        property2.setValue("3.14");

        property3 = new PropertyDefinition();
        property3.setName("anotherAnnotationProperty");
        property3.setType(ToscaPropertyType.BOOLEAN.getType());
        property3.setValue("True");

        dataTypes = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();

        List<PropertyDefinition> properties = new ArrayList<>();
        properties.add(property1);

        dataTypeDefinition.setProperties(properties);
        dataTypes.put("nameProperty", dataTypeDefinition);

        List<Annotation> annotationList = new ArrayList<>();
        Annotation annotation = new Annotation();
        annotation.setName("Annotation1");
        annotation.setDescription("description1");

        List<PropertyDataDefinition> propertiesAnnotation = new ArrayList<>();
        propertiesAnnotation.add(property2);
        propertiesAnnotation.add(property3);
        annotation.setProperties(propertiesAnnotation);
        annotationList.add(annotation);
        inputDefinition = new InputDefinition();
        inputDefinition.setName("inputName1");
        inputDefinition.setSchema(schemaDefinition);
        inputDefinition.setAnnotations(annotationList);

    }
    @Test
    public void ConvertAnnotationParseOneInput(){

        ArrayList<InputDefinition> inputDefList = new ArrayList<> ();
        inputDefList.add(inputDefinition);
        inputConverter = new InputConverter(new PropertyConvertor());
        Map<String, ToscaProperty> resultInputs ;
        resultInputs = inputConverter.convertInputs(inputDefList,dataTypes);
        //verify one Input only
        assertEquals(1,resultInputs.size());
        ToscaInput toscaInput =(ToscaInput) resultInputs.get("inputName1");
        Map<String, Object> propertyMap = toscaInput.getAnnotations().get("Annotation1").getProperties();
        assertEquals(2,propertyMap.size());
        double pi = (double)propertyMap.get("annotationProperty");
        Assert.assertEquals(3.14,pi,0.01);
        boolean annotationVal = (boolean)propertyMap.get("anotherAnnotationProperty");
        assertTrue(annotationVal);
        assertEquals("propertyDescription", toscaInput.getEntry_schema().getDescription());
        assertEquals("string", toscaInput.getEntry_schema().getType() );
    }


}
