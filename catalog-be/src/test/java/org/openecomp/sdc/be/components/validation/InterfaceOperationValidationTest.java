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
    private static final String interfaceId = "interfaceId";
    private static final String operationId = "operationId";
    private static final String interfaceType = "interfaceType";
    private static final String operationType = "operationType";
    private static final String operationId2 = "operationId2";
    private static final String inputId = "inputId";
    private static final String outputId = "outputId";
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

        component = new ResourceBuilder().setComponentType(ComponentTypeEnum.RESOURCE).setUniqueId(resourceId)
                            .setName(resourceId).build();
        component.setInterfaces(InterfaceOperationTestUtils.createMockInterfaceDefinitionMap(interfaceId, operationId));
        component.setInputs(createInputsForComponent());
    }

    private List<InputDefinition> createInputsForComponent() {
        InputDefinition inputDefinition1 = new InputDefinition();
        inputDefinition1.setName(inputId);
        inputDefinition1.setInputId(inputId);
        inputDefinition1.setUniqueId(inputId);
        inputDefinition1.setValue(inputId);
        inputDefinition1.setDefaultValue(inputId);

        InputDefinition inputDefinition2 = new InputDefinition();
        inputDefinition2.setName(outputId);
        inputDefinition2.setInputId(outputId);
        inputDefinition2.setUniqueId(outputId);
        inputDefinition2.setValue(outputId);
        inputDefinition2.setDefaultValue(outputId);

        return Arrays.asList(inputDefinition1, inputDefinition2);
    }

    @Test
    public void shouldPassOperationValidationForHappyScenario() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "upgrade");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isLeft());
    }

    private InterfaceDefinition createInterfaceOperationData(String uniqueId, String description,
            ArtifactDefinition artifactDefinition, ListDataDefinition<OperationInputDefinition> inputs,
            ListDataDefinition<OperationOutputDefinition> outputs, String name) {
        Operation operation = new Operation();
        operation.setUniqueId(uniqueId);
        operation.setDescription(description);
        operation.setImplementation(artifactDefinition);
        operation.setInputs(inputs);
        operation.setOutputs(outputs);
        operation.setName(name);
        Map<String, Operation> operationMap = new HashMap<>();
        operationMap.put(operation.getUniqueId(), operation);
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setOperationsMap(operationMap);
        return interfaceDefinition;
    }

    @Test
    public void shouldFailWhenOperationNameIsEmpty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isRight());
    }

    @Test
    public void shouldFailWhenOperationNamesAreNotUniqueForCreate() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, operationId);
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isRight());
    }

    @Test
    public void shouldNotFailWhenOperationNamesAreNotUniqueForUpdate() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, operationId);
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), true)
                                  .isLeft());
    }

    @Test
    public void shouldFailWhenOperationNameLengthIsInvalid() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList,
                        "interface operation2 -  The Spring Initializer provides a project generator to make you "
                                + "productive with the certain technology stack from the beginning. "
                                + "You can create a skeleton project"
                                + "with web, data access (relational and NoSQL data stores), "
                                + "cloud, or messaging support");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces()
                                                  .get(interfaceId), Collections.emptyMap(), false)
                                  .isRight());
    }

    @Test
    public void shouldFailWhenOperationInputParamNamesAreNotUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(outputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "create");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isRight());
    }

    @Test
    public void shouldPassWhenOperationInputParamNamesAreUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(outputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isLeft());
    }

    @Test
    public void shouldPassWhenOperationInputParamNamesHasSubProperty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        operationInputDefinitionList.getListToscaDataDefinition().get(0).setInputId(
                operationInputDefinitionList.getListToscaDataDefinition().get(0).getInputId().concat(".subproperty"));
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isLeft());
    }

    @Test
    public void shouldFailWhenOperationInputParamNameEmpty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("  "));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isRight());
    }

    @Test
    public void shouldFailWhenOperationOutputParamNameEmpty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(" "));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isRight());
    }

    @Test
    public void shouldPassWhenInterfaceOperationOutputParamNamesUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isLeft());
    }

    @Test
    public void shouldFailWhenOperationOutputParamNamesAreNotUnique() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isRight());
    }

    @Test
    public void shouldPassWhenOperationInputParamExistInComponentProperty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isLeft());
    }

    @Test
    public void shouldFailWhenOperationInputParamDoesntExistInComponentProperty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(inputId));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition(operationId));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition(outputId));
        InterfaceDefinition interfaceDefinition =
                createInterfaceOperationData(operationId2, operationId2, new ArtifactDefinition(),
                        operationInputDefinitionList, operationOutputDefinitionList, "update");
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(interfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId), Collections.emptyMap(), false)
                                  .isRight());
    }

    @Test
    public void shouldFailValidateAllowedOperationCountOnLocalInterfaceType() {
        InterfaceDefinition inputInterfaceDefinition =
                InterfaceOperationTestUtils.createMockInterface(interfaceId, operationType);
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(inputInterfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId),
                                          InterfaceOperationTestUtils.createMockInterfaceTypeMap(interfaceType,
                                                  operationType),
                                          false).isRight());
    }

    @Test
    public void shouldFailValidateAllowedOperationsOnGlobalInterfaceType() {
        InterfaceDefinition inputInterfaceDefinition =
                InterfaceOperationTestUtils.createMockInterface(interfaceType, operationId);
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(inputInterfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId),
                                          InterfaceOperationTestUtils.createMockInterfaceTypeMap(interfaceType,
                                                  operationType),
                                          false).isRight());
    }

    @Test
    public void shouldPassValidateAllowedOperationsOnGlobalInterfaceType() {
        InterfaceDefinition inputInterfaceDefinition =
                InterfaceOperationTestUtils.createMockInterface(interfaceType, operationType);
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(inputInterfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId),
                                          InterfaceOperationTestUtils.createMockInterfaceTypeMap(interfaceType,
                                                  operationType),
                                          false).isLeft());
    }

    @Test
    public void shouldFailValidateOperationNameUniquenessInCollection() {
        InterfaceDefinition inputInterfaceDefinition =
                InterfaceOperationTestUtils.createMockInterface(interfaceType, operationType);
        inputInterfaceDefinition.getOperations()
                .put(operationId, InterfaceOperationTestUtils.createMockOperation(operationType));
        Assert.assertTrue(interfaceOperationValidationUtilTest
                                  .validateInterfaceOperations(inputInterfaceDefinition, component,
                                          component.getInterfaces().get(interfaceId),
                                          InterfaceOperationTestUtils.createMockInterfaceTypeMap(interfaceType,
                                                  operationType),
                                          false).isRight());
    }

    private class InterfaceOperationValidationUtilTest extends InterfaceOperationValidation {

        protected ResponseFormatManager getResponseFormatManager() {
            return responseFormatManagerMock;
        }
    }
}
