/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InputDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.yaml.snakeyaml.Yaml;

import fj.data.Either;

@ExtendWith(MockitoExtension.class)
class InterfaceDefinitionHandlerTest {

    @Mock
    private InterfaceOperationBusinessLogic interfaceOperationBusinessLogic;
    private InterfaceDefinitionHandler interfaceDefinitionHandler;
    private InterfaceDefinition interfaceLifecyleStandard;
    private static final Path TEST_RESOURCE_PATH = Paths.get("src/test/resources/interfaceDefinition");
    private static final String CREATE_OPERATION = "create";
    private static final String DELETE_OPERATION = "delete";
    private static final String START_OPERATION = "start";
    private static final String STOP_OPERATION = "stop";
    private static final String INTERFACE_TYPE = "tosca.interfaces.node.lifecycle.Standard";

    @BeforeEach
    void setUp() {
        interfaceDefinitionHandler = new InterfaceDefinitionHandler(interfaceOperationBusinessLogic);
        mockAllInterfacesLifecycle();
    }

    private void mockAllInterfacesLifecycle() {
        final Map<String, InterfaceDefinition> interfaceTypes = new HashMap<>();
        interfaceLifecyleStandard = new InterfaceDefinition();
        interfaceLifecyleStandard.setType(INTERFACE_TYPE);
        final Map<String, OperationDataDefinition> operations = new HashMap<>();
        operations.put(CREATE_OPERATION, new OperationDataDefinition());
        operations.put(START_OPERATION, new OperationDataDefinition());
        operations.put(STOP_OPERATION, new OperationDataDefinition());
        operations.put(DELETE_OPERATION, new OperationDataDefinition());
        interfaceLifecyleStandard.setOperations(operations);
        interfaceTypes.put(INTERFACE_TYPE, interfaceLifecyleStandard);
        when(interfaceOperationBusinessLogic.getAllInterfaceLifecycleTypes(StringUtils.EMPTY)).thenReturn(Either.left(interfaceTypes));
    }

    @Test
    void testCreateWithLegacyOperationDeclarationSuccess() throws FileNotFoundException {
        final Map<String, Object> load = loadYaml(Paths.get("interfaceDefinition-legacy.yaml"));
        final InterfaceDefinition actualInterfaceDefinition = interfaceDefinitionHandler.create(load, StringUtils.EMPTY);
        assertInterfaceDefinition(actualInterfaceDefinition);
    }

    @Test
    void testCreateWithOperationSuccess() throws FileNotFoundException {
        final Map<String, Object> load = loadYaml(Paths.get("interfaceDefinition-tosca1.3.yaml"));
        final InterfaceDefinition actualInterfaceDefinition = interfaceDefinitionHandler.create(load, StringUtils.EMPTY);
        assertInterfaceDefinition(actualInterfaceDefinition);
    }

    private void assertInterfaceDefinition(final InterfaceDefinition actualInterfaceDefinition) {
        interfaceLifecyleStandard.getOperations().keySet().forEach(operation ->
            assertTrue(actualInterfaceDefinition.hasOperation(operation)));
        assertThat("Interface type should be as expected", actualInterfaceDefinition.getType(),
            equalTo(actualInterfaceDefinition.getType()));
        assertThat("Interface should contain 2 inputs", actualInterfaceDefinition.getInputs(), aMapWithSize(2));
        assertThat("Interface inputs should be as expected", actualInterfaceDefinition.getInputs().keySet(),
            containsInAnyOrder("stringInput", "actionInput"));

        final InputDataDefinition stringInput = actualInterfaceDefinition.getInputs().get("stringInput");
        assertInput("string", "stringInput description", true, "defaultValue", "aStatus", stringInput);
        final InputDataDefinition actionInput = actualInterfaceDefinition.getInputs().get("actionInput");
        assertInput("org.openecomp.resource.datatypes.Action", null, false, null, null, actionInput);

        final OperationDataDefinition createOperation = actualInterfaceDefinition.getOperations().get(CREATE_OPERATION);
        assertOperation("'camunda/serviceSelect'", createOperation);

        final OperationDataDefinition deleteOperation = actualInterfaceDefinition.getOperations().get(DELETE_OPERATION);
        assertOperation("'camunda/serviceDeselect'", deleteOperation);

        final Map<String, String> expectedInputMap = new HashMap<>();
        expectedInputMap.put("action", "org.openecomp.resource.datatypes.Action");
        final OperationDataDefinition startOperation = actualInterfaceDefinition.getOperations().get(START_OPERATION);
        assertOperation("'camunda/executeAction'", expectedInputMap, startOperation);
        final OperationDataDefinition stopOperation = actualInterfaceDefinition.getOperations().get(STOP_OPERATION);
        assertOperation("'camunda/executeAction'", expectedInputMap, stopOperation);
    }

    private void assertOperation(final String implementation, final OperationDataDefinition actualOperation) {
        assertOperation(implementation, Collections.emptyMap(), actualOperation);
    }

    private void assertOperation(final String implementation, final Map<String, String> inputNameTypeMap,
                                 final OperationDataDefinition actualOperation) {
        final ArtifactDataDefinition artifactDefinition = actualOperation.getImplementation();
        assertThat("Implementation should be as expected", artifactDefinition.getArtifactName(), equalTo(implementation));
        final ListDataDefinition<OperationInputDefinition> inputListDataDef = actualOperation.getInputs();
        if (inputListDataDef == null) {
            if (MapUtils.isNotEmpty(inputNameTypeMap)) {
                final String expectedInputNames = String.join(",", inputNameTypeMap.keySet());
                fail(String.format("No inputs were found, but some inputs are expected: %s", expectedInputNames));
            }
            return;
        }

        final String msgFormat = "Operation should have %s";
        final List<OperationInputDefinition> inputList = inputListDataDef.getListToscaDataDefinition();
        assertThat(String.format(msgFormat, "the expected quantity of inputs"), inputList, hasSize(inputNameTypeMap.size()));

        final List<String> inputNames = inputList.stream()
            .map(OperationInputDefinition::getName).collect(Collectors.toList());

        assertThat(String.format(msgFormat, "the expected inputs"), inputNames,
            hasItems(inputNameTypeMap.keySet().toArray(new String[0])));
    }

    private void assertInput(final String type, final String description, final Boolean required,
                             final String defaultValue, final String status, final InputDataDefinition actualInput) {
        assertThat("Input type should be as expected", type, equalTo(actualInput.getType()));
        assertThat("Input description should be as expected", description, equalTo(actualInput.getDescription()));
        assertThat("Input required should be as expected", required, equalTo(required != null && required));
        assertThat("Input default should be as expected", defaultValue, equalTo(actualInput.getDefaultValue()));
        assertThat("Input status should be as expected", status, equalTo(actualInput.getStatus()));
    }

    private Map<String, Object> loadYaml(final Path filePathFromResource) throws FileNotFoundException {
        final Path filePath = Paths.get(TEST_RESOURCE_PATH.toString(), filePathFromResource.toString());
        final FileInputStream fileInputStream = new FileInputStream(filePath.toString());
        return (Map<String, Object>) new Yaml().load(fileInputStream);
    }
}