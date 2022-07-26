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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DEFAULT;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DESCRIPTION;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.REQUIRED;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.STATUS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.TYPE;

import com.google.gson.Gson;
import fj.data.Either;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
        final InterfaceDefinition actualInterfaceDefinition = interfaceDefinitionHandler.create(null, load, StringUtils.EMPTY);
        assertInterfaceDefinition(actualInterfaceDefinition);
    }

    @Test
    void testCreateWithOperationSuccess() throws FileNotFoundException {
        final Map<String, Object> load = loadYaml(Paths.get("interfaceDefinition-tosca1.3.yaml"));
        final InterfaceDefinition actualInterfaceDefinition = interfaceDefinitionHandler.create(null, load, StringUtils.EMPTY);
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

        final Map<String, Map<String, Object>> startOperationExpectedInputMap = createStartOperationInputMap();
        final OperationDataDefinition startOperation = actualInterfaceDefinition.getOperations().get(START_OPERATION);
        assertOperation("'camunda/executeAction'", startOperationExpectedInputMap, startOperation);
        final Map<String, Map<String, Object>> stopOperationExpectedInputMap = createStopOperationInputMap();
        final OperationDataDefinition stopOperation = actualInterfaceDefinition.getOperations().get(STOP_OPERATION);
        assertOperation("'camunda/executeAction'", stopOperationExpectedInputMap, stopOperation);
    }

    private Map<String, Map<String, Object>> createStopOperationInputMap() {
        final Map<String, Map<String, Object>> stopOperationExpectedInputMap = new HashMap<>();
        final Map<String, Object> actionInput = Map.of(
            "type", "org.openecomp.resource.datatypes.Action"
        );
        stopOperationExpectedInputMap.put("action", actionInput);
        return stopOperationExpectedInputMap;
    }

    private Map<String, Map<String, Object>> createStartOperationInputMap() {
        final Map<String, Map<String, Object>> startOperationExpectedInputMap = new HashMap<>();
        final Map<String, Object> actionInput = Map.of(
            "type", "org.openecomp.resource.datatypes.Action"
        );
        startOperationExpectedInputMap.put("action", actionInput);
        final Map<String, Object> stringInput = Map.of(
            "type", "string",
            "default", "this is a string"
        );
        startOperationExpectedInputMap.put("stringInput", stringInput);
        final Map<String, Object> booleanInput = Map.of(
            "type", "boolean",
            "default", true
        );
        startOperationExpectedInputMap.put("booleanInput", booleanInput);
        final Map<String, Object> integerInput = Map.of(
            "type", "integer",
            "description", "an integer",
            "status", "supported",
            "required", true,
            "default", 11
        );
        startOperationExpectedInputMap.put("integerInput", integerInput);
        final Map<String, Object> floatInput = Map.of(
            "type", "float",
            "required", false,
            "default", 11.1
        );
        startOperationExpectedInputMap.put("floatInput", floatInput);

        final LinkedHashMap<String, Object> complexInputDefault = new LinkedHashMap<>();
        complexInputDefault.put("dsl_stability_profile", "dsl_stability_profile_value");
        complexInputDefault.put("central_splitter", false);
        complexInputDefault.put("service_restoration_sla", "service_restoration_sla_value");
        complexInputDefault.put("battery_backup", true);
        complexInputDefault.put("partner_priorty_assist", false);
        final Map<String, Object> complexInput = Map.of(
            "type", "onap.datatypes.partner.access_details",
            "status", "experimental",
            "default", complexInputDefault
        );
        startOperationExpectedInputMap.put("complexInput", complexInput);
        return startOperationExpectedInputMap;
    }

    private void assertOperation(final String implementation, final OperationDataDefinition actualOperation) {
        assertOperation(implementation, Collections.emptyMap(), actualOperation);
    }

    private void assertOperation(final String implementation, final Map<String, Map<String, Object>> inputNameTypeMap,
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

        for (final Entry<String, Map<String, Object>> inputEntry : inputNameTypeMap.entrySet()) {
            final String expectedInputName = inputEntry.getKey();
            final Optional<OperationInputDefinition> inputDefinitionOptional = inputList.stream()
                .filter(operationInputDefinition -> operationInputDefinition.getName().equals(expectedInputName)).findFirst();
            assertTrue(inputDefinitionOptional.isPresent(), String.format("Input '%s' should be present", expectedInputName));
            final OperationInputDefinition actualInputDefinition = inputDefinitionOptional.get();
            final Map<String, Object> expectedInput = inputEntry.getValue();

            assertEquals(expectedInput.get(STATUS.getElementName()), actualInputDefinition.getStatus(),
                String.format("%s attribute of input %s should be as expected", STATUS.getElementName(), expectedInputName)
            );
            assertEquals(expectedInput.get(TYPE.getElementName()), actualInputDefinition.getType(),
                String.format("%s attribute of input %s should be as expected", TYPE.getElementName(), expectedInputName)
            );
            assertEquals(expectedInput.get(DESCRIPTION.getElementName()), actualInputDefinition.getDescription(),
                String.format("%s attribute of input %s should be as expected", DESCRIPTION.getElementName(), expectedInputName)
            );
            final Object expectedRequired =
                expectedInput.get(REQUIRED.getElementName()) == null ? false : expectedInput.get(REQUIRED.getElementName());
            assertEquals(expectedRequired, actualInputDefinition.getRequired(),
                String.format("%s attribute of input %s should be as expected", REQUIRED.getElementName(), expectedInputName)
            );

            String expectedJson = null;
            if (expectedInput.get(DEFAULT.getElementName()) != null) {
                expectedJson = new Gson().toJson(expectedInput.get(DEFAULT.getElementName()));
            }
            assertEquals(expectedJson, actualInputDefinition.getToscaDefaultValue(),
                String.format("%s of input %s should be as expected", DEFAULT.getElementName(), expectedInputName)
            );
        }
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
        return new Yaml().load(fileInputStream);
    }
}