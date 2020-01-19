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

package org.openecomp.sdc.be.components.merge.input;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.model.InputDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
public class InputsValuesMergingBusinessLogicTest {

    private static final String INPUT_DEFUALT_TYPE = "string";
    private static final String INPUT1_ID = "input1";
    private static final String INPUT2_ID = "input2";
    private static final String INPUT3_ID = "input3";
    private static final String INPUT4_ID = "input4";
    private InputsValuesMergingBusinessLogic testInstance;

    @Before
    public void setUp() throws Exception {
        testInstance = new InputsValuesMergingBusinessLogic();
    }

    @Test
    public void testMergeInputs_inputsOfDifferentType_dontCopyOldValue() {
        InputDefinition oldInput = createUserDefinedInputDefinition(INPUT1_ID, "oldVal1");

        InputDefinition newInput = createInputDefinition(INPUT1_ID, null);
        newInput.setType("int");


        Map<String, InputDefinition> updatedInputs = Collections.singletonMap(newInput.getName(), newInput);
        Map<String, InputDefinition> oldInputs = Collections.singletonMap(oldInput.getName(), oldInput);
        testInstance.mergeComponentInputs(oldInputs, updatedInputs);

        assertNull(updatedInputs.get(INPUT1_ID).getDefaultValue());
    }

    @Test
    public void testMergeInputs_newInputsHaveNoValue_copyOldValues() {
        InputDefinition oldInputWithCsarDefaultValue = createInputDefinition(INPUT1_ID, "oldVal1");
        InputDefinition oldInputWithUserDefinedValue = createUserDefinedInputDefinition(INPUT2_ID, "oldVal2");
        InputDefinition oldInputNotExistOnNew = createUserDefinedInputDefinition(INPUT3_ID, null);

        InputDefinition newInput1 = createInputDefinition(INPUT1_ID, "");
        InputDefinition newInput2 = createUserDefinedInputDefinition(INPUT2_ID, null);

        Map<String, InputDefinition> updatedInputs = mapInputsByName(Arrays.asList(newInput1, newInput2));
        Map<String, InputDefinition> oldInputs = mapInputsByName(Arrays.asList(oldInputWithCsarDefaultValue, oldInputWithUserDefinedValue, oldInputNotExistOnNew));
        testInstance.mergeComponentInputs(oldInputs, updatedInputs);

        assertEquals(oldInputWithCsarDefaultValue.getDefaultValue(), updatedInputs.get(INPUT1_ID).getDefaultValue());
        assertEquals(oldInputWithUserDefinedValue.getDefaultValue(), updatedInputs.get(INPUT2_ID).getDefaultValue());
        assertNull(updatedInputs.get(INPUT3_ID));
    }

    @Test
    public void testMergeInputs_newInputsHaveValue_dontOverrideNewValue() {
        InputDefinition oldInputWithCsarDefaultValue = createInputDefinition(INPUT1_ID, "oldVal1");
        InputDefinition oldInputWithUserDefinedValue = createUserDefinedInputDefinition(INPUT2_ID, "oldVal2");
        InputDefinition oldInputWithNoValue = createUserDefinedInputDefinition(INPUT3_ID, null);

        InputDefinition newInput1 = createInputDefinition(INPUT1_ID, "newVal1");
        InputDefinition newInput2 = createUserDefinedInputDefinition(INPUT2_ID, "newVal2");
        InputDefinition newInput3 = createUserDefinedInputDefinition(INPUT3_ID, "newVal3");
        InputDefinition newInput4 = createUserDefinedInputDefinition(INPUT4_ID, "newVal4");

        Map<String, InputDefinition> updatedInputs = mapInputsByName(Arrays.asList(newInput1, newInput2, newInput3, newInput4));
        Map<String, InputDefinition> oldInputs = mapInputsByName(Arrays.asList(oldInputWithCsarDefaultValue, oldInputWithUserDefinedValue, oldInputWithNoValue));
        testInstance.mergeComponentInputs(oldInputs, updatedInputs);

        assertEquals(updatedInputs.get(INPUT1_ID).getDefaultValue(), newInput1.getDefaultValue());
        assertEquals(updatedInputs.get(INPUT2_ID).getDefaultValue(), newInput2.getDefaultValue());
        assertEquals(updatedInputs.get(INPUT3_ID).getDefaultValue(), newInput3.getDefaultValue());
        assertEquals(updatedInputs.get(INPUT4_ID).getDefaultValue(), newInput4.getDefaultValue());
    }


    private Map<String, InputDefinition> mapInputsByName(List<InputDefinition> inputs) {
        return MapUtil.toMap(inputs, InputDefinition::getName);
    }

    private InputDefinition createInputDefinition(String name, String value) {
        InputDefinition inputDef = new InputDefinition();
        inputDef.setName(name);
        inputDef.setDefaultValue(value);
        inputDef.setType(INPUT_DEFUALT_TYPE);
        return inputDef;
    }

    private InputDefinition createUserDefinedInputDefinition(String name, String value) {
        InputDefinition inputDef = createInputDefinition(name, value);
        inputDef.setOwnerId("owner");
        return inputDef;
    }
}
