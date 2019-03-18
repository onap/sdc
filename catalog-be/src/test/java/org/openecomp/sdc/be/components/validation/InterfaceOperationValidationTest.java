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
import static org.openecomp.sdc.test.utils.InterfaceOperationTestUtils.createMockOperation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.test.utils.InterfaceOperationTestUtils;

public class InterfaceOperationValidationTest {

    private static final String resourceId = "resourceId";
    private static final String operationId = "operationId";
    private static final String operationId2 = "operationId2";
    private static final String interfaceType1 = "org.test.lifecycle.standard.interfaceType.first";
    private static final String interfaceType2 = "org.test.lifecycle.standard.interfaceType.second";
    private static final String interfaceType3 = "org.test.lifecycle.standard.interfaceType.third";
    private static final String operationType1 = "createOperation";
    private static final String operationType2 = "updateOperation";
    private static final String inputName1 = "Input1";
    private static final String outputName1 = "Output1";
    private static final String outputName2 = "Output2";

    private final InterfaceOperationValidationUtilTest interfaceOperationValidationUtilTest =
            new InterfaceOperationValidationUtilTest();
    private final ListDataDefinition<OperationInputDefinition> operationInputDefinitionList =
            new ListDataDefinition<>();
    private final ListDataDefinition<OperationOutputDefinition> operationOutputDefinitionList =
            new ListDataDefinition<>();

    private Component component;
    private ResponseFormatManager responseFormatManagerMock;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        responseFormatManagerMock = Mockito.mock(ResponseFormatManager.class);
        when(responseFormatManagerMock.getResponseFormat(any())).thenReturn(new ResponseFormat());
        when(responseFormatManagerMock.getResponseFormat(any(), any())).thenReturn(new ResponseFormat());
        when(responseFormatManagerMock.getResponseFormat(any(), any(), any())).thenReturn(new ResponseFormat());

        component = new ResourceBuilder()
                .setComponentType(ComponentTypeEnum.RESOURCE)
                .setUniqueId(resourceId)
                .setName(resourceId)
                .build();
        component.setInterfaces(InterfaceOperationTestUtils.createMockInterfaceDefinitionMap(interfaceType1,
                operationId, operationType1));
        component.setInputs(createInputsForComponent());
    }

    @Test
    public void shouldPassOperationValidationForHappyScenario() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "upgrade");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false)
                                  .isLeft());
    }

    @Test
    public void shouldFailWhenOperationNameIsEmpty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isRight());
    }

    @Test
    public void shouldFailWhenOperationNamesAreNotUniqueForCreate() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition = createInterfaceOperationData(operationId, operationId2,
                new ArtifactDefinition(), operationInputDefinitionList, operationOutputDefinitionList, operationType1);
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isRight());
    }

    @Test
    public void shouldNotFailWhenOperationNamesAreNotUniqueForUpdate() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, operationId);
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), true).isLeft());
    }

    @Test
    public void shouldFailWhenOperationNameLengthIsInvalid() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList,operationOutputDefinitionList,

                        "interface operation2 -  The Spring Initializer provides a project generator to make you "
                                + "productive with the certain technology stack from the beginning. "
                                + "You can create a "
                                + "skeleton project with web, data access (relational and NoSQL data stores), "
                                + "cloud, "
                                + "or messaging support");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isRight());
    }

    @Test
    public void shouldFailWhenOperationInputParamNamesAreNotUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationInputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationInputDefinition(outputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "create");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isRight());
    }

    @Test
    public void shouldPassWhenOperationInputParamNamesAreUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationInputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationInputDefinition(outputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isLeft());
    }

    @Test
    public void shouldPassWhenOperationInputParamNamesHasSubProperty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        operationInputDefinitionList.getListToscaDataDefinition().get(0).setInputId(
                operationInputDefinitionList.getListToscaDataDefinition().get(0).getInputId().concat(".subproperty"));
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isLeft());
    }

    @Test
    public void shouldFailWhenOperationInputParamNameEmpty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("  ", 1));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isRight());
    }

    @Test
    public void shouldFailWhenOperationOutputParamNameEmpty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(" "));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isRight());
    }

    @Test
    public void shouldPassWhenInterfaceOperationOutputParamNamesUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(inputName1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isLeft());
    }

    @Test
    public void shouldFailWhenOperationOutputParamNamesAreNotUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(inputName1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isRight());
    }

    @Test
    public void shouldPassWhenOperationInputParamExistInComponentProperty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isLeft());
    }

    @Test
    public void shouldFailWhenOperationInputParamDoesntExistInComponentProperty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 2));
        operationOutputDefinitionList
                .add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputName1));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(interfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1), Collections.emptyMap(), false).isRight());
    }

    @Test
    public void shouldFailValidateAllowedOperationCountOnLocalInterfaceType() {
        InterfaceDefinition inputInterfaceDefinition =
                InterfaceOperationTestUtils.createMockInterface(interfaceType1
                , operationId, operationType1);
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(inputInterfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1),
                        InterfaceOperationTestUtils.createMockInterfaceTypeMap(
                                interfaceType2, operationType1), false).isRight());
    }

    @Test
    public void shouldFailValidateAllowedOperationsOnGlobalInterfaceType() {
        InterfaceDefinition inputInterfaceDefinition =
                InterfaceOperationTestUtils.createMockInterface(interfaceType1
                , operationId, operationType1);
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(inputInterfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1),
                        InterfaceOperationTestUtils.createMockInterfaceTypeMap(
                                interfaceType1, operationType1), false).isRight());
    }

    @Test
    public void shouldPassValidateAllowedOperationsOnGlobalInterfaceType() {
        InterfaceDefinition inputInterfaceDefinition =
                InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId2, operationType2);
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(inputInterfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1),
                        InterfaceOperationTestUtils.createMockInterfaceTypeMap(
                                interfaceType2, operationType2), false).isLeft());
    }

    @Test
    public void shouldFailValidateOperationNameUniquenessInCollection() {
        InterfaceDefinition inputInterfaceDefinition = InterfaceOperationTestUtils.createMockInterface(interfaceType1,
                operationId, operationType1);
        inputInterfaceDefinition.getOperations().put(operationId,
                createMockOperation(operationId, operationType1));
        Assert.assertTrue(interfaceOperationValidationUtilTest
                .validateInterfaceOperations(inputInterfaceDefinition, component,
                        component.getInterfaces().get(interfaceType1),
                        InterfaceOperationTestUtils.createMockInterfaceTypeMap(
                                interfaceType1, operationType1 ), false).isRight());
    }

    @Test
    public void shouldPassValidateWhenInputIsMappedToValidOutput() {
        InterfaceDefinition inputInterfaceDefinition = InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1);
        inputInterfaceDefinition.getOperationsMap().values()
                .forEach(operation -> operation.getInputs().getListToscaDataDefinition()
                        .forEach(operationInputDefinition -> operationInputDefinition.setInputId(interfaceType1 +
                                "." + operationType1 + "." + outputName1)));
        Assert.assertTrue(interfaceOperationValidationUtilTest.validateInterfaceOperations(inputInterfaceDefinition,
                component, null, Collections.emptyMap(), false).isLeft());
    }

    @Test
    public void shouldPassValidateWhenOutputNameIsUnchanged() {
        InterfaceDefinition inputInterfaceDefinition = InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1);
        InterfaceDefinition inputParamOutputMappedInterface = InterfaceOperationTestUtils.createMockInterface(
                interfaceType2, operationId, operationType1);
        inputParamOutputMappedInterface.getOperationsMap().values()
                .forEach(operation -> operation.getInputs().getListToscaDataDefinition()
                        .forEach(operationInputDefinition -> operationInputDefinition.setInputId(interfaceType2 +
                                "." + operationType2 + "." + outputName1)));
        component.getInterfaces().put(interfaceType2, inputParamOutputMappedInterface);
        component.getInterfaces().put(interfaceType3, InterfaceOperationTestUtils.createMockInterface(interfaceType3,
                operationId, operationType2));
        Assert.assertTrue(interfaceOperationValidationUtilTest.validateInterfaceOperations(inputInterfaceDefinition,
                component, component.getInterfaces().get(interfaceType1),
                Collections.emptyMap(), true).isLeft());
    }

    @Test
    public void shouldPassValidateWhenDeletedOutputIsUnMapped() {
        InterfaceDefinition inputInterfaceDefinition = InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1);
        inputInterfaceDefinition.getOperationsMap().values()
                .forEach(operation -> operation.getOutputs().getListToscaDataDefinition()
                        .forEach(operationOutputDefinition -> operationOutputDefinition.setName(outputName2)));
        component.getInterfaces().put(interfaceType3, InterfaceOperationTestUtils.createMockInterface(
                interfaceType3, operationId, operationType2));
        component.getInterfaces().put(interfaceType2, InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1));
        Assert.assertTrue(interfaceOperationValidationUtilTest.validateInterfaceOperations(inputInterfaceDefinition,
                component, component.getInterfaces().get(interfaceType2),
                Collections.emptyMap(), true).isLeft());
    }

    @Test
    public void shouldPassValidateNoOutputsInExistingInterfaceOperation() {
        InterfaceDefinition inputInterfaceDefinition = InterfaceOperationTestUtils.createMockInterface(interfaceType1,
                operationId, operationType1);
        inputInterfaceDefinition.getOperationsMap().values()
                .forEach(operation -> operation.getOutputs().getListToscaDataDefinition()
                        .forEach(operationOutputDefinition -> operationOutputDefinition.setName(outputName2)));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputName1, 1));
        InterfaceDefinition noOutputInterface = createInterfaceOperationData(operationId, "desc",
                new ArtifactDefinition(), operationInputDefinitionList, null, operationType1);
        component.getInterfaces().put(interfaceType1, noOutputInterface);
        component.getInterfaces().put(interfaceType3, InterfaceOperationTestUtils.createMockInterface(
                interfaceType3, operationId, operationType2));
        Assert.assertTrue(interfaceOperationValidationUtilTest.validateInterfaceOperations(inputInterfaceDefinition,
                component, component.getInterfaces().get(interfaceType1),
                Collections.emptyMap(), true).isLeft());
    }

    @Test
    public void shouldFailValidateMappedOutputDoesNotExistInComponent() {
        //Input parameter is mapped to invalid output (which does not exist in any of the other operations)
        InterfaceDefinition inputInterfaceDefinition = InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1);
        inputInterfaceDefinition.getOperationsMap().values()
                .forEach(operation -> operation.getInputs().getListToscaDataDefinition()
                        .forEach(operationInputDefinition -> operationInputDefinition.setInputId(outputName2)));

        Assert.assertTrue(interfaceOperationValidationUtilTest.validateInterfaceOperations(inputInterfaceDefinition,
                component, null, Collections.emptyMap(), false).isRight());
    }

    @Test
    public void shouldFailValidateComponentFirstInterfaceInvalidInputMapping() {
        //Input parameter is mapped to invalid output (which does not exist in any of the other operations)
        InterfaceDefinition inputInterfaceDefinition = InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1);
        inputInterfaceDefinition.getOperationsMap().values()
                .forEach(operation -> operation.getInputs().getListToscaDataDefinition()
                        .forEach(operationInputDefinition -> operationInputDefinition.setInputId(outputName2)));
        component.setInterfaces(null);
        Assert.assertTrue(interfaceOperationValidationUtilTest.validateInterfaceOperations(inputInterfaceDefinition,
                component, null, Collections.emptyMap(), false).isRight());
    }


    @Test
    public void shouldFailValidateMappedOutputDeletion() {
        //Input interface from user with new output name (Output2)
        InterfaceDefinition inputInterfaceDefinition = InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1);
        inputInterfaceDefinition.getOperationsMap().values()
                .forEach(operation -> operation.getOutputs().getListToscaDataDefinition()
                        .forEach(operationOutputDefinition -> operationOutputDefinition.setName(outputName2)));

        InterfaceDefinition inputParamOutputMappedInterface = InterfaceOperationTestUtils.createMockInterface(
                interfaceType3, operationId, operationType2);
        inputParamOutputMappedInterface.getOperationsMap().values()
                .forEach(operation -> operation.getInputs().getListToscaDataDefinition()
                        .forEach(operationInputDefinition -> operationInputDefinition.setInputId(interfaceType2 +
                                "." + operationType1 + "." + outputName1)));
        component.getInterfaces().put(interfaceType3, inputParamOutputMappedInterface);
        component.getInterfaces().put(interfaceType2, InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1));
        Assert.assertTrue(interfaceOperationValidationUtilTest.validateInterfaceOperations(inputInterfaceDefinition,
                component, component.getInterfaces().get(interfaceType2),
                Collections.emptyMap(), true).isRight());
    }

    @Test
    public void shouldFailValidateAllMappedOutputsDeleted() {
        //Input interface from user with all outputs deleted
        InterfaceDefinition inputInterfaceDefinition = InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1);
        Map<String, Operation> operationsMap = inputInterfaceDefinition.getOperationsMap();
        for (Map.Entry<String, Operation> operationEntry : operationsMap.entrySet()) {
            operationEntry.getValue().setOutputs(null);
        }
        inputInterfaceDefinition.setOperationsMap(operationsMap);

        InterfaceDefinition inputParamOutputMappedInterface = InterfaceOperationTestUtils.createMockInterface(
                interfaceType3, operationId, operationType2);
        inputParamOutputMappedInterface.getOperationsMap().values()
                .forEach(operation -> operation.getInputs().getListToscaDataDefinition()
                        .forEach(operationInputDefinition -> operationInputDefinition.setInputId(interfaceType2 +
                                "." + operationType1 + "." + outputName1)));
        component.getInterfaces().put(interfaceType3, inputParamOutputMappedInterface);
        component.getInterfaces().put(interfaceType2, InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1));
        Assert.assertTrue(interfaceOperationValidationUtilTest.validateInterfaceOperations(inputInterfaceDefinition,
                component, component.getInterfaces().get(interfaceType2),
                Collections.emptyMap(), true).isRight());
    }


    @Test
    public void shouldFailValidateDeleteOperationOperationWithMappedOutput() {
        InterfaceDefinition inputInterfaceDefinition = InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1);
        InterfaceDefinition inputParamOutputMappedInterface = InterfaceOperationTestUtils.createMockInterface(
                interfaceType3, operationId, operationType2);
        inputParamOutputMappedInterface.getOperationsMap().values()
                .forEach(operation -> operation.getInputs().getListToscaDataDefinition()
                        .forEach(operationInputDefinition -> operationInputDefinition.setInputId(interfaceType2 +
                                "." + operationType1 + "." + outputName1)));
        component.getInterfaces().put(interfaceType3, inputParamOutputMappedInterface);
        component.getInterfaces().put(interfaceType2, InterfaceOperationTestUtils.createMockInterface(interfaceType2,
                operationId, operationType1));
        Assert.assertTrue(interfaceOperationValidationUtilTest.validateDeleteOperationContainsNoMappedOutput(
                inputInterfaceDefinition.getOperationsMap().get(operationId), component,
                inputInterfaceDefinition).isRight());
    }

    private InterfaceDefinition createInterfaceOperationData(String uniqueID, String description,
                                                             ArtifactDefinition artifactDefinition,
                                                             ListDataDefinition<OperationInputDefinition> inputs,
                                                             ListDataDefinition<OperationOutputDefinition> outputs,
                                                             String name) {
        Operation operation = new Operation();
        operation.setUniqueId(uniqueID);
        operation.setDescription(description);
        operation.setImplementation(artifactDefinition);
        operation.setInputs(inputs);
        operation.setOutputs(outputs);
        operation.setName(name);
        Map<String, Operation> operationMap = new HashMap<>();
        operationMap.put(operation.getUniqueId(), operation);
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType(interfaceType2);
        interfaceDefinition.setOperationsMap(operationMap);
        return interfaceDefinition;
    }

    private List<InputDefinition> createInputsForComponent() {
        String componentInputName = "ComponentInput1";
        InputDefinition inputDefinition1 = new InputDefinition();
        inputDefinition1.setName(componentInputName);
        inputDefinition1.setInputId(componentInputName + "_inputId");
        inputDefinition1.setUniqueId(componentInputName + "_uniqueId");
        inputDefinition1.setValue(componentInputName + "_value");
        inputDefinition1.setDefaultValue(componentInputName + "_defaultValue");

        String componentInputName2 = "ComponentInput2";
        InputDefinition inputDefinition2 = new InputDefinition();
        inputDefinition2.setName(componentInputName2);
        inputDefinition2.setInputId(componentInputName2 + "_inputId");
        inputDefinition2.setUniqueId(componentInputName2 + "_uniqueId");
        inputDefinition2.setValue(componentInputName2 + "_value");
        inputDefinition2.setDefaultValue(componentInputName2 + "_defaultValue");

        return Arrays.asList(inputDefinition1, inputDefinition2);
    }

    private class InterfaceOperationValidationUtilTest extends InterfaceOperationValidation {

        protected ResponseFormatManager getResponseFormatManager() {
            return responseFormatManagerMock;
        }
    }
}
