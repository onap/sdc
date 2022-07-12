/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.components.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.attribute.AttributeDeclarationOrchestrator;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.ComponentInstOutputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribOutput;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.AttributeOperation;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

class OutputsBusinessLogicTest {

    private static final String COMPONENT_INSTANCE_ID = "instanceId";
    private static final String COMPONENT_ID = "componentId";
    private static final String USER_ID = "userId";
    private static final String OUTPUT_ID = "outputId";
    private static final String OUTPUT_TYPE = "string";
    private static final String LISTOUTPUT_NAME = "listOutput";
    private static final String LISTOUTPUT_SCHEMA_TYPE = "org.onap.datatypes.listoutput";
    private static final String LISTOUTPUT_PROP1_NAME = "prop1";
    private static final String LISTOUTPUT_PROP1_TYPE = "string";
    private static final String LISTOUTPUT_PROP2_NAME = "prop2";
    private static final String LISTOUTPUT_PROP2_TYPE = "integer";
    private static final String OLD_VALUE = "old value";
    private static final String NEW_VALUE = "new value";
    private final ConfigurationManager configurationManager =
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));

    @Mock
    private ComponentsUtils componentsUtilsMock;
    @Mock
    private UserBusinessLogic userAdminMock;
    @Mock
    private ToscaOperationFacade toscaOperationFacadeMock;
    @Mock
    private UserValidations userValidations;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    private AttributeDeclarationOrchestrator attributeDeclarationOrchestrator;
    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;
    @Mock
    private AttributeOperation attributeOperation;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private ResponseFormatManager responseFormatManager;
    @InjectMocks
    private OutputsBusinessLogic testInstance;

    private Service service;

    private Map<String, List<ComponentInstanceOutput>> instanceOutputMap;
    private List<ComponentInstanceOutput> outputsList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new Service();
        service.setUniqueId(COMPONENT_ID);
        service.setLastUpdaterUserId(USER_ID);
        service.setIsDeleted(false);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        testInstance.setUserValidations(userValidations);
        testInstance.setToscaOperationFacade(toscaOperationFacadeMock);
        testInstance.setGraphLockOperation(graphLockOperation);
        testInstance.setComponentsUtils(componentsUtilsMock);
        testInstance.setJanusGraphDao(janusGraphDao);
        testInstance.setApplicationDataTypeCache(applicationDataTypeCache);
        testInstance.setAttributeOperation(attributeOperation);

        // add a ComponentInstance
        final ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(COMPONENT_INSTANCE_ID);
        componentInstance.setName(COMPONENT_INSTANCE_ID);
        final AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setName("attribName");
        componentInstance.setAttributes(Collections.singletonList(attributeDefinition));
        service.setComponentInstances(Collections.singletonList(componentInstance));

        instanceOutputMap = new HashMap<>();
        final ComponentInstanceOutput componentInstanceOutput = new ComponentInstanceOutput();
        componentInstanceOutput.setOutputId(OUTPUT_ID);
        componentInstanceOutput.setName(OUTPUT_ID);
        outputsList = Collections.singletonList(componentInstanceOutput);
        instanceOutputMap.put(COMPONENT_INSTANCE_ID, outputsList);
        instanceOutputMap.put("someOutputId", Collections.singletonList(new ComponentInstanceOutput()));
        service.setComponentInstancesOutputs(instanceOutputMap);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(new User());
        when(userAdminMock.getUser(USER_ID, false)).thenReturn(new User());
    }

    @Test
    void getComponentInstanceOutputs_ComponentInstanceNotExist() {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        final Either<List<ComponentInstanceOutput>, ResponseFormat> componentInstanceOutputs = testInstance
            .getComponentInstanceOutputs(USER_ID, COMPONENT_ID, "nonExisting");
        assertThat(componentInstanceOutputs.isRight()).isTrue();
        verify(componentsUtilsMock).getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
    }

    @Test
    void getComponentInstanceOutputs_emptyOutputsMap() {
        service.setComponentInstancesOutputs(Collections.emptyMap());
        getComponents_emptyOutputs(service);
    }

    @Test
    void getComponentInstanceOutputs_nullOutputsMap() {
        service.setComponentInstancesOutputs(null);
        getComponents_emptyOutputs(service);
    }

    @Test
    void getComponentInstanceOutputs_instanceHasNoOutputs() {
        service.setComponentInstancesOutputs(Collections.singletonMap("someOutputId", new ArrayList<>()));
        getComponents_emptyOutputs(service);
    }

    @Test
    void getComponentInstanceOutputs() {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        final Either<List<ComponentInstanceOutput>, ResponseFormat> componentInstanceOutputs = testInstance
            .getComponentInstanceOutputs(USER_ID, COMPONENT_ID, COMPONENT_INSTANCE_ID);
        assertEquals("outputId", componentInstanceOutputs.left().value().get(0).getOutputId());
    }

    @Test
    void testDeclareAttributes() {
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        service.setLastUpdaterUserId(USER_ID);
        final ComponentInstOutputsMap componentInstOutputsMap = new ComponentInstOutputsMap();
        final Map<String, List<ComponentInstanceAttribOutput>> propertiesForDeclaration = new HashMap<>();
        propertiesForDeclaration.put(COMPONENT_ID, getPropertiesListForDeclaration());

        final List<OutputDefinition> declaredPropertiesToOutputs = getDeclaredProperties();
        initMockitoStubbings(declaredPropertiesToOutputs);

        final Either<List<OutputDefinition>, ResponseFormat> declaredPropertiesEither =
            testInstance.declareAttributes(USER_ID, COMPONENT_ID, ComponentTypeEnum.SERVICE, componentInstOutputsMap);

        assertThat(declaredPropertiesEither.isLeft()).isTrue();

        final List<OutputDefinition> declaredProperties = declaredPropertiesEither.left().value();
        assertThat(CollectionUtils.isNotEmpty(declaredProperties)).isTrue();
        assertEquals(1, declaredProperties.size());
        assertEquals(declaredProperties, declaredPropertiesToOutputs);
    }

    @Test
    void testDeclareAttributes_fail() {
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        service.setLastUpdaterUserId(USER_ID);
        final ComponentInstOutputsMap componentInstOutputsMap = new ComponentInstOutputsMap();
        final Map<String, List<ComponentInstanceAttribOutput>> propertiesForDeclaration = new HashMap<>();
        propertiesForDeclaration.put(COMPONENT_ID, getPropertiesListForDeclaration());

        final List<OutputDefinition> declaredPropertiesToOutputs = getDeclaredProperties();
        initMockitoStubbings(declaredPropertiesToOutputs);
        when(attributeDeclarationOrchestrator.declareAttributesToOutputs(any(), any())).thenThrow(ByResponseFormatComponentException.class);
        final Either<List<OutputDefinition>, ResponseFormat> declaredPropertiesEither =
            testInstance.declareAttributes(USER_ID, COMPONENT_ID, ComponentTypeEnum.SERVICE, componentInstOutputsMap);

        assertThat(declaredPropertiesEither.isRight()).isTrue();

    }

    private void initMockitoStubbings(List<OutputDefinition> declaredPropertiesToOutputs) {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(attributeDeclarationOrchestrator.declareAttributesToOutputs(any(), any())).thenReturn(Either.left(declaredPropertiesToOutputs));
        when(toscaOperationFacadeMock.addOutputsToComponent(any(), any())).thenReturn(Either.left(declaredPropertiesToOutputs));
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.unlockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
    }

    private void getComponents_emptyOutputs(Service service) {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        final Either<List<ComponentInstanceOutput>, ResponseFormat> componentInstanceOutputs = testInstance
            .getComponentInstanceOutputs(USER_ID, COMPONENT_ID, COMPONENT_INSTANCE_ID);
        assertEquals(Collections.emptyList(), componentInstanceOutputs.left().value());
    }

    private List<ComponentInstanceAttribOutput> getPropertiesListForDeclaration() {
        return outputsList.stream().map(this::getPropertyForDeclaration).collect(Collectors.toList());
    }

    private ComponentInstanceAttribOutput getPropertyForDeclaration(ComponentInstanceOutput componentInstanceOutput) {
        final ComponentInstanceAttribOutput propOutput = new ComponentInstanceAttribOutput();
        propOutput.setOutput(componentInstanceOutput);
        propOutput.setAttributesName(componentInstanceOutput.getName());

        return propOutput;
    }

    private List<OutputDefinition> getDeclaredProperties() {
        return outputsList.stream().map(OutputDefinition::new).collect(Collectors.toList());
    }

    private OutputDefinition setUpListOutput() {
        final OutputDefinition listOutput = new OutputDefinition();
        listOutput.setName(LISTOUTPUT_NAME);
        listOutput.setType("list");
        return listOutput;
    }

    @Test
    void test_deleteOutput_listOutput_fail_getComponent() throws Exception {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(componentsUtilsMock.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND)).thenReturn(ActionStatus.RESOURCE_NOT_FOUND);

        try {
            testInstance.deleteOutput(COMPONENT_ID, USER_ID, LISTOUTPUT_NAME);
        } catch (ComponentException e) {
            assertEquals(ActionStatus.RESOURCE_NOT_FOUND, e.getActionStatus());
            verify(toscaOperationFacadeMock, times(1)).getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class));
            return;
        }
        fail();
    }

    @Test
    void test_deleteOutput_listOutput_fail_validateOutput() throws Exception {
        final OutputDefinition listOutput = setUpListOutput();
        final String outputId = COMPONENT_ID + "." + listOutput.getName();
        listOutput.setUniqueId(outputId);
        service.setOutputs(Collections.singletonList(listOutput));
        final String NONEXIST_OUTPUT_NAME = "myOutput";

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));

        try {
            testInstance.deleteOutput(COMPONENT_ID, USER_ID, NONEXIST_OUTPUT_NAME);
        } catch (ComponentException e) {
            assertEquals(ActionStatus.OUTPUT_IS_NOT_CHILD_OF_COMPONENT, e.getActionStatus());
            verify(toscaOperationFacadeMock, times(1)).getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class));
            return;
        }
        fail();
    }

    @Test
    void test_deleteOutput_listOutput_fail_lockComponent() throws Exception {
        final OutputDefinition listOutput = setUpListOutput();
        final String outputId = COMPONENT_ID + "." + listOutput.getName();
        listOutput.setUniqueId(outputId);
        service.setOutputs(Collections.singletonList(listOutput));

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.NOT_FOUND);
        when(componentsUtilsMock.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND, ComponentTypeEnum.SERVICE))
            .thenReturn(ActionStatus.SERVICE_NOT_FOUND);

        try {
            testInstance.deleteOutput(COMPONENT_ID, USER_ID, outputId);
        } catch (ComponentException e) {
            assertEquals(ActionStatus.SERVICE_NOT_FOUND, e.getActionStatus());
            verify(toscaOperationFacadeMock, times(1)).getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class));
            verify(graphLockOperation, times(1)).lockComponent(COMPONENT_ID, NodeTypeEnum.Service);
            return;
        }
        fail();
    }

    @Test
    void test_deleteOutput_listOutput_fail_deleteOutput() throws Exception {
        final OutputDefinition listOutput = setUpListOutput();
        final String outputId = COMPONENT_ID + "." + listOutput.getName();
        listOutput.setUniqueId(outputId);
        service.setOutputs(Collections.singletonList(listOutput));

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacadeMock.deleteOutputOfResource(service, listOutput.getName())).thenReturn(StorageOperationStatus.BAD_REQUEST);
        when(componentsUtilsMock.convertFromStorageResponse(StorageOperationStatus.BAD_REQUEST)).thenReturn(ActionStatus.INVALID_CONTENT);

        try {
            testInstance.deleteOutput(COMPONENT_ID, USER_ID, outputId);
        } catch (ComponentException e) {
            assertEquals(ActionStatus.INVALID_CONTENT, e.getActionStatus());
            verify(toscaOperationFacadeMock, times(1)).getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class));
            verify(graphLockOperation, times(1)).lockComponent(COMPONENT_ID, NodeTypeEnum.Service);
            verify(toscaOperationFacadeMock, times(1)).deleteOutputOfResource(service, listOutput.getName());
            return;
        }
        fail();
    }

    @Test
    void test_deleteOutput_output_fail_unDeclare() throws Exception {
        final OutputDefinition listOutput = setUpListOutput();
        final String outputId = COMPONENT_ID + "." + listOutput.getName();
        listOutput.setUniqueId(outputId);
        service.setOutputs(Collections.singletonList(listOutput));

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacadeMock.deleteOutputOfResource(service, listOutput.getName())).thenReturn(StorageOperationStatus.OK);
        when(attributeDeclarationOrchestrator.unDeclareAttributesAsOutputs(service, listOutput)).thenReturn(StorageOperationStatus.BAD_REQUEST);
        when(componentsUtilsMock.convertFromStorageResponse(StorageOperationStatus.BAD_REQUEST)).thenReturn(ActionStatus.INVALID_CONTENT);

        try {
            testInstance.deleteOutput(COMPONENT_ID, USER_ID, outputId);
        } catch (ComponentException e) {
            assertEquals(ActionStatus.INVALID_CONTENT, e.getActionStatus());
            verify(attributeDeclarationOrchestrator, times(1)).unDeclareAttributesAsOutputs(service, listOutput);
            return;
        }
        fail();
    }

    @Test
    void test_deleteOutput_output_success() throws Exception {
        final OutputDefinition listOutput = setUpListOutput();
        final String outputId = COMPONENT_ID + "." + listOutput.getName();
        listOutput.setUniqueId(outputId);
        service.setOutputs(Collections.singletonList(listOutput));

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacadeMock.deleteOutputOfResource(service, listOutput.getName())).thenReturn(StorageOperationStatus.OK);
        when(attributeDeclarationOrchestrator.unDeclareAttributesAsOutputs(service, listOutput)).thenReturn(StorageOperationStatus.OK);

        testInstance.deleteOutput(COMPONENT_ID, USER_ID, outputId);
        verify(attributeDeclarationOrchestrator, times(1)).unDeclareAttributesAsOutputs(service, listOutput);
    }

    @Test
    void testCreateOutputsInGraph_OK() {
        final Map<String, OutputDefinition> outputs = new HashMap<>();
        final var out_1 = new OutputDefinition();
        out_1.setName("out-1");
        out_1.setValue("{ get_attribute: [ instanceId, attribName ] }");
        final var out_2 = new OutputDefinition();
        out_2.setName("out-2");
        out_2.setValue("{ get_attribute: [ SELF, oneMoreAttribute ] }");
        outputs.put(out_1.getName(), out_1);
        outputs.put(out_2.getName(), out_2);

        final List<OutputDefinition> serviceOutputs = new ArrayList<>();
        final var out_3 = new OutputDefinition();
        out_3.setName("out-3");
        serviceOutputs.add(out_3);
        service.setOutputs(serviceOutputs);

        final List<OutputDefinition> list = Arrays.asList(out_1, out_2, out_3);
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(attributeDeclarationOrchestrator.declareAttributesToOutputs(eq(service), any(ComponentInstOutputsMap.class)))
            .thenReturn(Either.left(list));
        when(toscaOperationFacadeMock.addOutputsToComponent(anyMap(), anyString())).thenReturn(Either.left(list));

        final var result = testInstance.createOutputsInGraph(outputs, service, USER_ID);
        assertTrue(result.isLeft());
        assertEquals(3, result.left().value().size());
        assertEquals(list, result.left().value());
    }

    @Test
    void testCreateOutputsInGraph_NegativeCreateAndAssociateOutputsStatus() {
        final Map<String, OutputDefinition> outputs = new HashMap<>();
        final var out_1 = new OutputDefinition();
        out_1.setName("out-1");
        out_1.setValue("{ get_attribute: [ instanceId, attribName ] }");
        final var out_2 = new OutputDefinition();
        out_2.setName("out-2");
        out_2.setValue("{ get_attribute: [ SELF, oneMoreAttribute ] }");
        outputs.put(out_1.getName(), out_1);
        outputs.put(out_2.getName(), out_2);

        final List<OutputDefinition> serviceOutputs = new ArrayList<>();
        final var out_3 = new OutputDefinition();
        out_3.setName("out-3");
        serviceOutputs.add(out_3);
        service.setOutputs(serviceOutputs);

        final var list = Arrays.asList(out_1, out_2, out_3);
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(attributeDeclarationOrchestrator.declareAttributesToOutputs(eq(service), any(ComponentInstOutputsMap.class)))
            .thenReturn(Either.left(list));
        when(toscaOperationFacadeMock.addOutputsToComponent(anyMap(), eq(COMPONENT_ID)))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtilsMock.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        when(responseFormatManager.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(new ResponseFormat(Status.BAD_REQUEST.getStatusCode()));

        final var result = testInstance.createOutputsInGraph(outputs, service, USER_ID);
        assertTrue(result.isRight());
        assertNull(result.right().value());
    }
}
