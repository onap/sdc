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

package org.openecomp.sdc.be.components.validation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import fj.data.Either;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.test.utils.InterfaceOperationTestUtils;

public class InterfaceOperationValidationTest {

    private final Component component = setUpComponentMock();
    private ResponseFormatManager responseFormatManagerMock;

    private final InterfaceOperationValidationUtilTest interfaceOperationValidationUtilTest = new InterfaceOperationValidationUtilTest();
    private final ListDataDefinition<OperationInputDefinition> operationInputDefinitionList = new ListDataDefinition<>();
    private final ListDataDefinition<OperationOutputDefinition> operationOutputDefinitionList = new ListDataDefinition<>();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        responseFormatManagerMock = Mockito.mock(ResponseFormatManager.class);
        when(responseFormatManagerMock.getResponseFormat(any())).thenReturn(new ResponseFormat());
        when(responseFormatManagerMock.getResponseFormat(any(), any())).thenReturn(new ResponseFormat());
        when(responseFormatManagerMock.getResponseFormat(any(), any(), any())).thenReturn(new ResponseFormat());
    }

    @Test
    public void shouldPassOperationValidationForHappyScenario() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                        operationOutputDefinitionList,"upgrade");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isLeft());
    }

    @Test
    public void shouldFailWhenOperationOperationDescriptionLengthInvalid() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2 -  The Spring Initializer provides a project generator to make you " +
                        "productive with the certain technology stack from the beginning. You can create a skeleton project" +
                        "with web, data access (relational and NoSQL data stores), cloud, or messaging support",
                new ArtifactDefinition(), operationInputDefinitionList, operationOutputDefinitionList,"update");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }



    @Test
    public void shouldFailWhenOperationNameIsEmpty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList, "");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void shouldFailWhenOperationNamesAreNotUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"CREATE");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void shouldFailWhenOperationNameLengthIsInvalid() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,
                "interface operation2 -  The Spring Initializer provides a project generator to make you " +
                        "productive with the certain technology stack from the beginning. You can create a skeleton project" +
                        "with web, data access (relational and NoSQL data stores), cloud, or messaging support");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }


    @Test
    public void shouldFailWhenOperationInputParamNamesAreNotUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label2"));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label2"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"create");

        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void shouldPassWhenOperationInputParamNamesAreUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label2"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"update");

        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isLeft());
    }

    @Test
    public void shouldPassWhenOperationInputParamNamesHasSubProperty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label2"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"update");
        operationInputDefinitionList.getListToscaDataDefinition().get(0).setInputId(operationInputDefinitionList
                .getListToscaDataDefinition().get(0).getInputId().concat(".subproperty"));
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isLeft());
    }

    @Test
    public void shouldFailWhenOperationInputParamNameEmpty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("  "));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"update");

        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void shouldFailWhenOperationOutputParamNameEmpty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("inputParam"));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(" "));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"update");

        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void shouldPassWhenInterfaceOperationOutputParamNamesUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label2"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"update");

        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isLeft());
    }

    @Test
    public void shouldFailWhenOperationOutputParamNamesAreNotUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("inputParam1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("outParam1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("outParam2"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("outParam2"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"update");

        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, component, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    private Set<Operation> createInterfaceOperationData( String uniqueID, String description, ArtifactDefinition artifactDefinition,
                                                         ListDataDefinition<OperationInputDefinition> inputs,
                                                         ListDataDefinition<OperationOutputDefinition> outputs, String name) {
        return Sets.newHashSet(InterfaceOperationTestUtils.createInterfaceOperation(uniqueID, description, artifactDefinition, inputs, outputs, name));
    }

    private Component setUpComponentMock(){
        Component component = new Resource();

        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        InputDefinition inputDefinition1 = new InputDefinition();

        List<ComponentInstanceInput> componentInstanceInputs = new ArrayList<>();
        ComponentInstanceInput componentInstanceInput1 = new ComponentInstanceInput();
        componentInstanceInput1.setComponentInstanceName("componentInstance1");
        componentInstanceInput1.setUniqueId("inputId1");
        ComponentInstanceInput componentInstanceInput2 = new ComponentInstanceInput();
        componentInstanceInput2.setComponentInstanceName("componentInstance2");
        componentInstanceInput2.setUniqueId("inputId2");

        componentInstanceInputs.add(componentInstanceInput1);
        componentInstanceInputs.add(componentInstanceInput2);

        inputDefinition.setUniqueId("inputId1");
        inputDefinition.setInputs(componentInstanceInputs);
        inputDefinition1.setUniqueId("uniqueId3");

        inputs.add(inputDefinition);
        inputs.add(inputDefinition1);
        component.setInputs(inputs);
        component.setInterfaces(createMockInterfaceDefinition());
        return  component;
    }

    private  Map<String, InterfaceDefinition> createMockInterfaceDefinition() {
        Map<String, Operation> operationMap = InterfaceOperationTestUtils.createMockOperationMap();
        Map<String, InterfaceDefinition> interfaceDefinitionMap = new HashMap<>();
        interfaceDefinitionMap.put("int1", InterfaceOperationTestUtils.createInterface("int1", "Interface 1",
                "lifecycle", "tosca", operationMap));

        return interfaceDefinitionMap;
    }

    private class InterfaceOperationValidationUtilTest extends InterfaceOperationValidation {

        protected ResponseFormatManager getResponseFormatManager() {
            return responseFormatManagerMock;
        }
    }
}
