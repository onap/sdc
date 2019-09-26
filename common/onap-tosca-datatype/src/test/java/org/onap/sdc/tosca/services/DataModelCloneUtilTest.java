/*
 * Copyright © 2016-2018 European Support Limited
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

package org.onap.sdc.tosca.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.Constraint;
import org.onap.sdc.tosca.datatypes.model.Implementation;
import org.onap.sdc.tosca.datatypes.model.OperationDefinition;
import org.onap.sdc.tosca.datatypes.model.OperationDefinitionTemplate;
import org.onap.sdc.tosca.datatypes.model.OperationDefinitionType;
import org.onap.sdc.tosca.datatypes.model.PropertyDefinition;
import org.onap.sdc.tosca.datatypes.model.Status;

public class DataModelCloneUtilTest {

    private static final String KEY1 = "Key1";
    private static final String VAL1 = "Val1";
    private static final String KEY2 = "Key2";
    private static final String VAL2 = "Val2";
    private static final String KEY3 = "Key3";
    private static final String DESC1 = "Desc1";
    private static final String DESC2 = "Desc2";
    private static final String PRIMARY1 = "primary1";
    private static final String PARAM1 = "param1";
    private static final String INPUT_KEY1 = "inKey1";
    private static final String PRIMARY2 = "primary2";
    private static final String PARAM2 = "param2";
    private static final String INPUT_KEY2 = "inKey2";


    @Test
    public void cloneStringStringMapTest() {
        Map<String, String> originalMap = new HashMap<>();
        originalMap.put(KEY1, VAL1);
        originalMap.put(KEY2, VAL2);

        Map<String, String> cloneMap = DataModelCloneUtil.cloneStringStringMap(originalMap);
        Assert.assertEquals(originalMap.size(), cloneMap.size());
        Assert.assertEquals(originalMap.get(KEY1), cloneMap.get(KEY1));
        Assert.assertEquals(originalMap.get(KEY2), cloneMap.get(KEY2));
    }


    @Test
    public void cloneStringObjectMapTest() {
        Map<String, Object> originalMap = new HashMap<>();
        originalMap.put(KEY1, VAL1);
        ArrayList<Object> list = new ArrayList<>();
        list.add(VAL1);
        list.add(VAL2);
        originalMap.put(KEY2, list);
        HashMap<String, String> map = new HashMap<>();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        originalMap.put(KEY3, map);

        Map<String, Object> cloneMap = DataModelCloneUtil.cloneStringObjectMap(originalMap);
        Assert.assertEquals(originalMap.size(), cloneMap.size());
        Assert.assertEquals(originalMap.get(KEY1), cloneMap.get(KEY1));
        List originalListObj = (List) originalMap.get(KEY2);
        List cloneListObj = (List) cloneMap.get(KEY2);
        Assert.assertEquals(originalListObj.size(), cloneListObj.size());
        Assert.assertEquals(originalListObj.get(0), cloneListObj.get(0));
        Assert.assertEquals(originalListObj.get(1), cloneListObj.get(1));
        Map originalMapObj = (Map) originalMap.get(KEY3);
        Map cloneMapObj = (Map) cloneMap.get(KEY3);
        Assert.assertEquals(originalMapObj.size(), cloneMapObj.size());
        Assert.assertEquals(originalMapObj.get(KEY1), cloneMapObj.get(KEY1));
        Assert.assertEquals(originalMapObj.get(KEY2), cloneMapObj.get(KEY2));
    }


    @Test
    public void cloneStringOperationDefinitionMapsTest() {
        OperationDefinition operationDefinition1 = createOperationDefinition(DESC1);
        OperationDefinition operationDefinition2 = createOperationDefinition(DESC2);

        Map<String, OperationDefinition> originalMap = new HashMap<>();
        originalMap.put(KEY1, operationDefinition1);
        originalMap.put(KEY2, operationDefinition2);


        Map<String, OperationDefinition> cloneMap = DataModelCloneUtil.cloneStringOperationDefinitionMap(originalMap);

        Assert.assertEquals(originalMap.size(), cloneMap.size());
        Assert.assertEquals(originalMap.get(KEY1).getDescription(), cloneMap.get(KEY1).getDescription());
        Assert.assertEquals(originalMap.get(KEY2).getDescription(), cloneMap.get(KEY2).getDescription());

    }

    private OperationDefinition createOperationDefinition(String desc) {
        OperationDefinition operationDefinition = new OperationDefinition();
        operationDefinition.setDescription(desc);
        return operationDefinition;
    }

    @Test
    public void cloneStringOperationDefinitionTemplateMapsTest() {
        OperationDefinitionTemplate operationDefinitionTemp1 =
                createOperationDefinitionTemplate(DESC1, PRIMARY1, PARAM1, INPUT_KEY1);

        OperationDefinitionTemplate operationDefinitionTemp2 =
                createOperationDefinitionTemplate(DESC2, PRIMARY2, PARAM2, INPUT_KEY2);


        Map<String, OperationDefinitionTemplate> originalMap = new HashMap<>();
        originalMap.put(KEY1, operationDefinitionTemp1);
        originalMap.put(KEY2, operationDefinitionTemp2);


        Map<String, OperationDefinitionTemplate> cloneMap =
                DataModelCloneUtil.cloneStringOperationDefinitionMap(originalMap);

        Assert.assertEquals(originalMap.size(), cloneMap.size());
        Assert.assertEquals(originalMap.get(KEY1).getDescription(), cloneMap.get(KEY1).getDescription());
        Assert.assertEquals(originalMap.get(KEY2).getDescription(), cloneMap.get(KEY2).getDescription());
        Assert.assertEquals(originalMap.get(KEY1).getImplementation().getPrimary(),
                cloneMap.get(KEY1).getImplementation().getPrimary());
        Assert.assertEquals(originalMap.get(KEY2).getInputs().get(INPUT_KEY2).toString(),
                cloneMap.get(KEY2).getInputs().get(INPUT_KEY2).toString());


    }

    @Test
    public void cloneStringOperationDefinitionTypeMapsTest() {
        Map<String, PropertyDefinition> inputs = new HashMap<>();
        inputs.put(INPUT_KEY1, createPropertyDefinition());

        OperationDefinitionType operationDefinitionType1 = createOperationDefinitionType(DESC1, PRIMARY1, inputs);
        OperationDefinitionType operationDefinitionType2 =
                createOperationDefinitionType(DESC2, PRIMARY2, DataModelCloneUtil.clonePropertyDefinitions(inputs));

        Map<String, OperationDefinitionType> originalMap = new HashMap<>();
        originalMap.put(KEY1, operationDefinitionType1);
        originalMap.put(KEY2, operationDefinitionType2);

        Map<String, OperationDefinitionType> cloneMap =
                DataModelCloneUtil.cloneStringOperationDefinitionMap(originalMap);

        Assert.assertEquals(originalMap.size(), cloneMap.size());
        Assert.assertEquals(originalMap.get(KEY1).getDescription(), cloneMap.get(KEY1).getDescription());
        Assert.assertEquals(originalMap.get(KEY2).getDescription(), cloneMap.get(KEY2).getDescription());
        Assert.assertEquals(originalMap.get(KEY1).getImplementation(), cloneMap.get(KEY1).getImplementation());
        Assert.assertEquals(originalMap.get(KEY2).getInputs().get(INPUT_KEY1).getStatus(),
                cloneMap.get(DataModelCloneUtilTest.KEY2).getInputs().get(INPUT_KEY1).getStatus());
        Assert.assertEquals(originalMap.get(KEY2).getInputs().get(INPUT_KEY1).getConstraints().get(0).getEqual(),
                cloneMap.get(KEY2).getInputs().get(INPUT_KEY1).getConstraints().get(0).getEqual());
    }

    private PropertyDefinition createPropertyDefinition() {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setRequired(false);
        propertyDefinition.setStatus(Status.UNSUPPORTED.getName());
        Constraint constraint = new Constraint();
        constraint.setEqual("1234");
        ArrayList<Constraint> constraints = new ArrayList<>();
        constraints.add(constraint);
        propertyDefinition.setConstraints(constraints);
        return propertyDefinition;
    }

    private OperationDefinitionTemplate createOperationDefinitionTemplate(String desc, String primary,
            String inputParameterName, String inputKey) {
        OperationDefinitionTemplate operationDefinitionTemp = new OperationDefinitionTemplate();
        operationDefinitionTemp.setDescription(desc);
        Implementation implementation = new Implementation();
        implementation.setPrimary(primary);
        operationDefinitionTemp.setImplementation(implementation);
        HashMap<String, String> valueAssignment = new HashMap<>();
        valueAssignment.put("get_input", inputParameterName);
        HashMap<String, Object> inputs = new HashMap<>();
        inputs.put(inputKey, valueAssignment);
        operationDefinitionTemp.setInputs(inputs);
        return operationDefinitionTemp;
    }

    private OperationDefinitionType createOperationDefinitionType(String desc, String implementationValue,
            Map<String, PropertyDefinition> inputs) {
        OperationDefinitionType operationDefinitionType = new OperationDefinitionType();
        operationDefinitionType.setDescription(desc);
        operationDefinitionType.setImplementation(implementationValue);
        operationDefinitionType.setInputs(inputs);
        return operationDefinitionType;
    }

}
