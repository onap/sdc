/*
 * Copyright © 2016-2019 European Support Limited
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


package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.property.PropertyDeclarationOrchestrator;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstListInput;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.exception.ResponseFormat;

public class InputsBusinessLogicTest {

    private static final String COMPONENT_INSTANCE_ID = "instanceId";
    private static final String COMPONENT_ID = "componentId";
    private static final String USER_ID = "userId";
    private static final String INSTANCE_INPUT_ID = "inputId";
    private static final String LISTINPUT_NAME = "listInput";
    private static final String LISTINPUT_SCHEMA_TYPE = "org.onap.datatypes.listinput";
    private static final String LISTINPUT_PROP1_NAME = "prop1";
    private static final String LISTINPUT_PROP1_TYPE = "string";
    private static final String LISTINPUT_PROP2_NAME = "prop2";
    private static final String LISTINPUT_PROP2_TYPE = "integer";

    @Mock
    private ComponentsUtils componentsUtilsMock;

    @Mock
    private IUserBusinessLogic userAdminMock;

    @Mock
    private ToscaOperationFacade toscaOperationFacadeMock;

    @Mock
    private UserValidations userValidations;

    @Mock
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @Mock
    private IGraphLockOperation graphLockOperation;

    @Mock
    private PropertyDeclarationOrchestrator propertyDeclarationOrchestrator;

    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;

    @Mock
    private PropertyOperation propertyOperation;

    @Mock
    private TitanDao titanDao;

    @Mock
    private DataTypeBusinessLogic dataTypeBusinessLogic;

    @InjectMocks
    private InputsBusinessLogic testInstance;

    private Service service;

    @Captor
    ArgumentCaptor<Map<String, DataTypeDefinition>> dataTypesMapCaptor;

    private Map<String, List<ComponentInstanceInput>> instanceInputMap;
    private List<ComponentInstanceInput> inputsList;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = new Service();
        service.setUniqueId(COMPONENT_ID);
        service.setLastUpdaterUserId(USER_ID);
        service.setIsDeleted(false);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        // add a ComponentInstance
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(COMPONENT_INSTANCE_ID);
        service.setComponentInstances(Collections.singletonList(componentInstance));

        instanceInputMap = new HashMap<>();
        ComponentInstanceInput componentInstanceInput = new ComponentInstanceInput();
        componentInstanceInput.setInputId(INSTANCE_INPUT_ID);
        componentInstanceInput.setName(INSTANCE_INPUT_ID);
        inputsList = Collections.singletonList(componentInstanceInput);
        instanceInputMap.put(COMPONENT_INSTANCE_ID, inputsList);
        instanceInputMap.put("someInputId", Collections.singletonList(new ComponentInstanceInput()));
        service.setComponentInstancesInputs(instanceInputMap);

        when(userValidations.validateUserExists(eq(USER_ID), anyString(), eq(false))).thenReturn(new User());
        when(userAdminMock.getUser(USER_ID, false)).thenReturn(Either.left(new User()));
    }

    @Test
    public void getComponentInstanceInputs_ComponentInstanceNotExist() throws Exception {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        Either<List<ComponentInstanceInput>, ResponseFormat> componentInstanceInputs = testInstance.getComponentInstanceInputs(USER_ID, COMPONENT_ID, "nonExisting");
        assertTrue(componentInstanceInputs.isRight());
        verify(componentsUtilsMock).getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
    }

    @Test
    public void getComponentInstanceInputs_emptyInputsMap() throws Exception {
        service.setComponentInstancesInputs(Collections.emptyMap());
        getComponents_emptyInputs(service);
    }

    @Test
    public void getComponentInstanceInputs_nullInputsMap() throws Exception {
        service.setComponentInstancesInputs(null);
        getComponents_emptyInputs(service);
    }

    @Test
    public void getComponentInstanceInputs_instanceHasNoInputs() throws Exception {
        service.setComponentInstancesInputs(Collections.singletonMap("someInputId", new ArrayList<>()));
        getComponents_emptyInputs(service);
    }

    @Test
    public void getComponentInstanceInputs() throws Exception {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        Either<List<ComponentInstanceInput>, ResponseFormat> componentInstanceInputs = testInstance.getComponentInstanceInputs(USER_ID, COMPONENT_ID, COMPONENT_INSTANCE_ID);
        assertEquals("inputId", componentInstanceInputs.left().value().get(0).getInputId());
    }

    @Test
    public void testGetInputs() {
        String userId = "userId";
        String componentId = "compId";
        Component component = new Resource();
        when(toscaOperationFacadeMock.getToscaElement(any(String.class), any(ComponentParametersView.class))).thenReturn(Either.left(component));
        testInstance.getInputs(userId, componentId);
        assertEquals(null, component.getInputs());
    }

    @Test
    public void testGetCIPropertiesByInputId() {
        Either<List<ComponentInstanceProperty>, ResponseFormat> result;
        String userId = "userId";
        String componentId = "compId";
        Component component = new Resource();
        List<InputDefinition> listDef = new ArrayList<>();
        InputDefinition inputDef = new InputDefinition();
        inputDef.setUniqueId(componentId);
        listDef.add(inputDef);
        component.setInputs(listDef);
        when(toscaOperationFacadeMock.getToscaElement(any(String.class), any(ComponentParametersView.class))).thenReturn(Either.left(component));
        result = testInstance.getComponentInstancePropertiesByInputId(userId, componentId, componentId, componentId);
        assertTrue(result.isLeft());
    }

    @Test
    public void testDeclareProperties() {
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        service.setLastUpdaterUserId(USER_ID);
        ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
        Map<String, List<ComponentInstancePropInput>> propertiesForDeclaration = new HashMap<>();
        propertiesForDeclaration.put(COMPONENT_ID, getPropertiesListForDeclaration());
        componentInstInputsMap.setServiceProperties(propertiesForDeclaration);

        List<InputDefinition> declaredPropertiesToInputs = getDeclaredProperties();
        initMockitoStubbings(declaredPropertiesToInputs);

        Either<List<InputDefinition>, ResponseFormat> declaredPropertiesEither =
                testInstance.declareProperties(USER_ID, COMPONENT_ID, ComponentTypeEnum.SERVICE, componentInstInputsMap);

        assertTrue(declaredPropertiesEither.isLeft());

        List<InputDefinition> declaredProperties = declaredPropertiesEither.left().value();
        assertTrue(CollectionUtils.isNotEmpty(declaredProperties));
        assertEquals(1, declaredProperties.size());
        assertEquals(declaredProperties, declaredPropertiesToInputs);
    }

    private void initMockitoStubbings(List<InputDefinition> declaredPropertiesToInputs) {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(
                Either.left(service));
        when(propertyDeclarationOrchestrator.declarePropertiesToInputs(any(), any())).thenReturn(Either.left(
                declaredPropertiesToInputs));
        when(toscaOperationFacadeMock.addInputsToComponent(any(), any())).thenReturn(Either.left(declaredPropertiesToInputs));
        when(titanDao.commit()).thenReturn(TitanOperationStatus.OK);
        when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.unlockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
        when(componentInstanceBusinessLogic.setInputConstraint(any())).thenReturn(Collections.emptyList());
    }

    private void getComponents_emptyInputs(Service service) {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        Either<List<ComponentInstanceInput>, ResponseFormat> componentInstanceInputs = testInstance.getComponentInstanceInputs(USER_ID, COMPONENT_ID, COMPONENT_INSTANCE_ID);
        assertEquals(Collections.emptyList(), componentInstanceInputs.left().value());
    }

    @Test
    public void testgetInputs_ARTIFACT_NOT_FOUND() throws Exception {

        when(componentsUtilsMock.convertFromStorageResponse(StorageOperationStatus.ARTIFACT_NOT_FOUND)).thenReturn(ActionStatus.ARTIFACT_NOT_FOUND);
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
       Either<List<InputDefinition>, ResponseFormat> responseFormatEither = testInstance.getInputs("USR01", COMPONENT_ID);
        assertEquals(true,responseFormatEither.isRight());

    }

    @Test
    public void testgetInputs_SUCCESS() throws Exception {
        Component component = new Service();
        InputDefinition input= new InputDefinition();
        List inputlist = new ArrayList<>();
        inputlist.add(input);
        component.setInputs(inputlist);
        when(componentsUtilsMock.convertFromStorageResponse(StorageOperationStatus.ARTIFACT_NOT_FOUND)).thenReturn(ActionStatus.ARTIFACT_NOT_FOUND);
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(component));
        Either<List<InputDefinition>, ResponseFormat> responseFormatEither = testInstance.getInputs("USR01", COMPONENT_ID);
        assertEquals(inputlist,responseFormatEither.left().value());
    }

    @Test
    public void testgetComponentInstancePropertiesByInputId_Artifactnotfound() throws Exception
    {
        Component component = new Service();
        InputDefinition input= new InputDefinition();
        List inputlist = new ArrayList<>();
        inputlist.add(input);
        component.setInputs(inputlist);
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
        Either<List<ComponentInstanceProperty>, ResponseFormat> responseFormatEither = testInstance.getComponentInstancePropertiesByInputId("USR01", COMPONENT_ID,"INST0.1", "INPO1");
        assertEquals(true,responseFormatEither.isRight());
    }

    @Test
    public void testgetComponentInstancePropertiesByInputId_PARENT_ARTIFACT_NOT_FOUND() throws Exception
    {
        Component component = new Service();
        InputDefinition input= new InputDefinition();
        List inputlist = new ArrayList<>();
        inputlist.add(input);
        component.setInputs(inputlist);
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId("INST0.1");
        componentInstance.setComponentUid("RES0.1");
        List compinstancelist = new ArrayList<>();
        compinstancelist.add(componentInstance);
        component.setComponentInstances(compinstancelist);
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(component));
        when(toscaOperationFacadeMock.getToscaElement(eq("RES0.1"), any(ComponentParametersView.class))).thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
        Either<List<ComponentInstanceProperty>, ResponseFormat> responseFormatEither = testInstance.getComponentInstancePropertiesByInputId("USR01", COMPONENT_ID,"INST0.1", "INPO1");
        assertEquals(true,responseFormatEither.isRight());
    }

    @Test
    public void testgetComponentInstancePropertiesByInputId() throws Exception
    {
        Component component = new Service();
        InputDefinition input= new InputDefinition();
        input.setUniqueId("INPO1");
        List inputlist = new ArrayList<>();
        inputlist.add(input);
        component.setInputs(inputlist);
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId("INST0.1");
        componentInstance.setComponentUid("RES0.1");
        List compinstancelist = new ArrayList<>();
        compinstancelist.add(componentInstance);
        component.setComponentInstances(compinstancelist);
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(component));
        when(componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(any(Component.class),eq("INPO1"))).thenReturn(compinstancelist);
        //when(toscaOperationFacadeMock.getToscaElement(eq("RES0.1"), Mockito.any(ComponentParametersView.class))).thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
        when(toscaOperationFacadeMock.getToscaElement(eq("RES0.1"), any(ComponentParametersView.class))).thenReturn(Either.left(component));
        Either<List<ComponentInstanceProperty>, ResponseFormat> responseFormatEither = testInstance.getComponentInstancePropertiesByInputId("USR01", COMPONENT_ID,"INST0.1", "INPO1");
        assertEquals(compinstancelist,responseFormatEither.left().value());
    }

    @Test
    public void testgetInputsForComponentInput_ARTIFACT_NOT_FOUND() throws Exception
    {
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
        Either<List<ComponentInstanceInput>, ResponseFormat> result = testInstance.getInputsForComponentInput("USR01", COMPONENT_ID,"INPO1");
        assertEquals(true,result.isRight());
    }

    @Test
    public void testgetInputsForComponentInput() throws Exception
    {
        Component component = new Service();
        InputDefinition input= new InputDefinition();
        input.setUniqueId("INPO1");
        List inputlist = new ArrayList<>();
        inputlist.add(input);
        component.setInputs(inputlist);
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId("INST0.1");
        componentInstance.setComponentUid("RES0.1");
        List compinstancelist = new ArrayList<>();
        compinstancelist.add(componentInstance);
        component.setComponentInstances(compinstancelist);
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(component));
        when(componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(any(Component.class),eq("INPO1"))).thenReturn(compinstancelist);
        Either<List<ComponentInstanceInput>, ResponseFormat> result = testInstance.getInputsForComponentInput("USR01", COMPONENT_ID,"INPO1");
        assertEquals(true,result.isLeft());
    }

    private List<ComponentInstancePropInput> getPropertiesListForDeclaration() {
        return inputsList.stream().map(this::getPropertyForDeclaration).collect(Collectors.toList());
    }

    private ComponentInstancePropInput getPropertyForDeclaration(ComponentInstanceInput componentInstanceInput) {
        ComponentInstancePropInput propInput = new ComponentInstancePropInput();
        propInput.setInput(componentInstanceInput);
        propInput.setPropertiesName(componentInstanceInput.getName());

        return propInput;
    }

    private List<InputDefinition> getDeclaredProperties() {
        return inputsList.stream().map(InputDefinition::new).collect(Collectors.toList());
    }


    private InputDefinition setUpListInput() {
        InputDefinition listInput = new InputDefinition();
        listInput.setName(LISTINPUT_NAME);
        listInput.setType("list");
        SchemaDefinition listInputSchema = new SchemaDefinition();
        listInputSchema.setProperty(new PropertyDataDefinitionBuilder()
                .setType(LISTINPUT_SCHEMA_TYPE)
                .setIsRequired(false)
                .build()
        );
        listInput.setSchema(listInputSchema);
        return listInput;
    }

    private ComponentInstListInput setUpCreateListInputParams() {
        ComponentInstListInput componentInstListInput = new ComponentInstListInput();

        // Create a "list input"
        InputDefinition listInput = setUpListInput();
        componentInstListInput.setListInput(listInput);

        // Create ComponentInstancePropInputs
        // for inputs in the ComponentInstance
        Map<String, List<ComponentInstancePropInput>> propInputsListMap = new HashMap<>();
        // Add 2 PropInputs. property owner is COMPONENT_INSTANCE_ID
        List<ComponentInstancePropInput> propInputsList = new ArrayList<>();
        ComponentInstancePropInput propInput = new ComponentInstancePropInput();
        propInput.setName(LISTINPUT_PROP1_NAME);
        propInput.setType(LISTINPUT_PROP1_TYPE);
        propInput.setUniqueId(COMPONENT_INSTANCE_ID+"."+LISTINPUT_PROP1_NAME);
        propInputsList.add(propInput);
        propInput = new ComponentInstancePropInput();
        propInput.setName(LISTINPUT_PROP2_NAME);
        propInput.setType(LISTINPUT_PROP2_TYPE);
        propInput.setUniqueId(COMPONENT_INSTANCE_ID+"."+LISTINPUT_PROP2_NAME);
        propInputsList.add(propInput);
        propInputsListMap.put(COMPONENT_INSTANCE_ID, propInputsList);
        ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
        componentInstInputsMap.setComponentInstanceInputsMap(propInputsListMap);
        componentInstListInput.setComponentInstInputsMap(componentInstInputsMap);

        return componentInstListInput;
    }

    @Test
    public void test_createListInput_success() throws Exception {
        ComponentInstListInput createListInputParams = setUpCreateListInputParams();
        ComponentInstInputsMap componentInstInputsMap = createListInputParams.getComponentInstInputsMap();
        List<ComponentInstancePropInput> propInputsList = componentInstInputsMap.getComponentInstanceInputsMap().get(COMPONENT_INSTANCE_ID);
        InputDefinition listInput = createListInputParams.getListInput();

        // set up mock returns
        // for get component object:
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        // for data type creation (use captor):
        when(toscaOperationFacadeMock.addDataTypesToComponent(dataTypesMapCaptor.capture(), eq(COMPONENT_ID))).thenReturn(Either.left(new ArrayList<>()));
        when(propertyDeclarationOrchestrator.getPropOwnerId(componentInstInputsMap)).thenReturn(COMPONENT_INSTANCE_ID);
        when(propertyDeclarationOrchestrator.declarePropertiesToListInput(service, componentInstInputsMap, listInput)).thenReturn(Either.left(listInput));
        // for BaseOperation.getAllDataTypes:
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>())); // don't use Collections.emptyMap
        // for BaseOperation.validatePropertyDefaultValue:
        when(propertyOperation.isPropertyTypeValid(any())).thenReturn(true);
        when(propertyOperation.isPropertyInnerTypeValid(any(),any())).thenReturn(new ImmutablePair<>(listInput.getSchemaType(), true));
        when(propertyOperation.isPropertyDefaultValueValid(any(), any())).thenReturn(true);
        // for createListInputsInGraph:
        when(toscaOperationFacadeMock.addInputsToComponent(anyMap(), eq(COMPONENT_ID))).thenReturn(Either.left(Arrays.asList(listInput)));
        // for rollback/commit:
        when(titanDao.commit()).thenReturn(TitanOperationStatus.OK);
        // for unlock resource
        when(graphLockOperation.unlockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);

        Either<List<InputDefinition>, ResponseFormat> result =
                testInstance.createListInput(USER_ID, COMPONENT_ID, ComponentTypeEnum.SERVICE, createListInputParams, true, false);
        // validate result
        assertEquals(true, result.isLeft());
        List<InputDefinition> resultInputList = result.left().value();
        assertEquals(1, resultInputList.size());
        //InputDefinition resultInput = resultInputList.get(0);
        Map<String, DataTypeDefinition> captoredDataTypeMap = dataTypesMapCaptor.getValue();
        assertEquals(1, captoredDataTypeMap.size());
        assertEquals(true, captoredDataTypeMap.containsKey(LISTINPUT_SCHEMA_TYPE));
        DataTypeDefinition captoredDataType = captoredDataTypeMap.get(LISTINPUT_SCHEMA_TYPE);
        assertEquals("tosca.datatypes.Root", captoredDataType.getDerivedFromName());
        assertEquals( propInputsList.size(), captoredDataType.getProperties().size());
        // confirm if corresponding property exists in data type
        captoredDataType.getProperties().forEach(dataTypeProp -> {
            Optional<ComponentInstancePropInput> find = propInputsList.stream()
                    .filter(propInput -> propInput.getName().equals(dataTypeProp.getName())).findAny();
            assertEquals(true, find.isPresent());
        });
    }

    @Test
    public void test_createListInput_fail_getComponent() throws Exception {
        ComponentInstListInput createListInputParams = setUpCreateListInputParams();
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<List<InputDefinition>, ResponseFormat> result =
                testInstance.createListInput(USER_ID, COMPONENT_ID, ComponentTypeEnum.SERVICE, createListInputParams, true, false);
        assertEquals(true, result.isRight());
    }


    @Test
    public void test_createListInput_fail_lockComponent() throws Exception {
        ComponentInstListInput createListInputParams = setUpCreateListInputParams();
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT);
        Either<List<InputDefinition>, ResponseFormat> result =
                testInstance.createListInput(USER_ID, COMPONENT_ID, ComponentTypeEnum.SERVICE, createListInputParams, true, false);
        assertEquals(true, result.isRight());
    }

    @Test
    public void test_createListInput_fail_getAllDataTypes() throws Exception {
        ComponentInstListInput createListInputParams = setUpCreateListInputParams();
        ComponentInstInputsMap componentInstInputsMap = createListInputParams.getComponentInstInputsMap();

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacadeMock.addDataTypesToComponent(dataTypesMapCaptor.capture(), eq(COMPONENT_ID))).thenReturn(Either.left(new ArrayList<>()));
        when(propertyDeclarationOrchestrator.getPropOwnerId(componentInstInputsMap)).thenReturn(COMPONENT_INSTANCE_ID);
        when(applicationDataTypeCache.getAll()).thenReturn(Either.right(TitanOperationStatus.NOT_FOUND));
        when(componentsUtilsMock.getResponseFormat(ActionStatus.DATA_TYPE_CANNOT_BE_EMPTY)).thenReturn(new ResponseFormat());

        Either<List<InputDefinition>, ResponseFormat> result =
                testInstance.createListInput(USER_ID, COMPONENT_ID, ComponentTypeEnum.SERVICE, createListInputParams, true, false);
        assertEquals(true, result.isRight());
        verify(applicationDataTypeCache, times(1)).getAll();
        verify(componentsUtilsMock, times(1)).getResponseFormat(ActionStatus.DATA_TYPE_CANNOT_BE_EMPTY);
    }

    @Test
    public void test_createListInput_fail_prepareAndValidateInput() throws Exception {
        ComponentInstListInput createListInputParams = setUpCreateListInputParams();
        ComponentInstInputsMap componentInstInputsMap = createListInputParams.getComponentInstInputsMap();
        InputDefinition listInput = createListInputParams.getListInput();

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacadeMock.addDataTypesToComponent(dataTypesMapCaptor.capture(), eq(COMPONENT_ID))).thenReturn(Either.left(new ArrayList<>()));
        when(propertyDeclarationOrchestrator.getPropOwnerId(componentInstInputsMap)).thenReturn(COMPONENT_INSTANCE_ID);
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>())); // don't use Collections.emptyMap
        // for BaseOperation.validatePropertyDefaultValue:
        when(propertyOperation.isPropertyTypeValid(any())).thenReturn(false);

        Either<List<InputDefinition>, ResponseFormat> result =
                testInstance.createListInput(USER_ID, COMPONENT_ID, ComponentTypeEnum.SERVICE, createListInputParams, true, false);
        assertEquals(true, result.isRight());
        verify(propertyOperation, times(1)).isPropertyTypeValid(any());
    }

    @Test
    public void test_createListInput_fail_addInputsToComponent() throws Exception {
        ComponentInstListInput createListInputParams = setUpCreateListInputParams();
        ComponentInstInputsMap componentInstInputsMap = createListInputParams.getComponentInstInputsMap();
        InputDefinition listInput = createListInputParams.getListInput();

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacadeMock.addDataTypesToComponent(dataTypesMapCaptor.capture(), eq(COMPONENT_ID))).thenReturn(Either.left(new ArrayList<>()));
        when(propertyDeclarationOrchestrator.getPropOwnerId(componentInstInputsMap)).thenReturn(COMPONENT_INSTANCE_ID);
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(new HashMap<>())); // don't use Collections.emptyMap
        // for BaseOperation.validatePropertyDefaultValue:
        when(propertyOperation.isPropertyTypeValid(any())).thenReturn(true);
        when(propertyOperation.isPropertyInnerTypeValid(any(),any())).thenReturn(new ImmutablePair<>(listInput.getSchemaType(), true));
        when(propertyOperation.isPropertyDefaultValueValid(any(), any())).thenReturn(true);
        when(toscaOperationFacadeMock.addInputsToComponent(anyMap(), eq(COMPONENT_ID))).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));

        Either<List<InputDefinition>, ResponseFormat> result =
                testInstance.createListInput(USER_ID, COMPONENT_ID, ComponentTypeEnum.SERVICE, createListInputParams, true, false);
        assertEquals(true, result.isRight());
        verify(toscaOperationFacadeMock, times(1)).addInputsToComponent(anyMap(), eq(COMPONENT_ID));
    }

    @Test
    public void test_deleteInput_listInput_fail_getComponent() throws Exception {
        //ComponentInstListInput createListInputParams = setUpCreateListInputParams();
        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(componentsUtilsMock.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());

        Either<InputDefinition, ResponseFormat> result = testInstance.deleteInput(COMPONENT_ID, USER_ID, LISTINPUT_NAME);
        assertEquals(true, result.isRight());
        verify(toscaOperationFacadeMock, times(1)).getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class));
    }


    @Test
    public void test_deleteInput_listInput_fail_validateInput() throws Exception {
        InputDefinition listInput = setUpListInput();
        String inputId = COMPONENT_ID + "." + listInput.getName();
        listInput.setUniqueId(inputId);
        service.setInputs(Collections.singletonList(listInput));
        //ComponentInstListInput createListInputParams = setUpCreateListInputParams();
        final String NONEXIST_INPUT_NAME = "myInput";

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
                .thenReturn(Either.left(service));
        when(componentsUtilsMock.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());

        Either<InputDefinition, ResponseFormat> result = testInstance.deleteInput(COMPONENT_ID, USER_ID, NONEXIST_INPUT_NAME);
        assertEquals(true, result.isRight());
        verify(toscaOperationFacadeMock, times(1)).getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class));
    }


    @Test
    public void test_deleteInput_listInput_fail_lockComponent() throws Exception {
        InputDefinition listInput = setUpListInput();
        String inputId = COMPONENT_ID + "." + listInput.getName();
        listInput.setUniqueId(inputId);
        service.setInputs(Collections.singletonList(listInput));

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
                .thenReturn(Either.left(service));
        //when(componentsUtilsMock.getResponseFormat(any())).thenReturn(new ResponseFormat());
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.NOT_FOUND);

        Either<InputDefinition, ResponseFormat> result = testInstance.deleteInput(COMPONENT_ID, USER_ID, inputId);
        assertEquals(true, result.isRight());
        verify(toscaOperationFacadeMock, times(1)).getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class));
        verify(graphLockOperation, times(1)).lockComponent(COMPONENT_ID, NodeTypeEnum.Service);
    }


    @Test
    public void test_deleteInput_listInput_fail_deleteInput() throws Exception {
        InputDefinition listInput = setUpListInput();
        String inputId = COMPONENT_ID + "." + listInput.getName();
        listInput.setUniqueId(inputId);
        service.setInputs(Collections.singletonList(listInput));

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
                .thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacadeMock.deleteInputOfResource(service, listInput.getName())).thenReturn(StorageOperationStatus.BAD_REQUEST);
        when(componentsUtilsMock.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());

        Either<InputDefinition, ResponseFormat> result = testInstance.deleteInput(COMPONENT_ID, USER_ID, inputId);
        assertEquals(true, result.isRight());
        verify(toscaOperationFacadeMock, times(1)).getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class));
        verify(graphLockOperation, times(1)).lockComponent(COMPONENT_ID, NodeTypeEnum.Service);
        verify(toscaOperationFacadeMock, times(1)).deleteInputOfResource(service, listInput.getName());
    }


    @Test
    public void test_deleteInput_listInput_success() throws Exception {
        InputDefinition listInput = setUpListInput();
        String inputId = COMPONENT_ID + "." + listInput.getName();
        listInput.setUniqueId(inputId);
        listInput.setIsDeclaredListInput(true);
        service.setInputs(Collections.singletonList(listInput));
        ArgumentCaptor<String> schemaTypeCaptor = ArgumentCaptor.forClass(String.class);

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
                .thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacadeMock.deleteInputOfResource(service, listInput.getName())).thenReturn(StorageOperationStatus.OK);
        //when(componentsUtilsMock.getResponseFormat(any())).thenReturn(new ResponseFormat());
        when(propertyDeclarationOrchestrator.unDeclarePropertiesAsListInputs(service, listInput)).thenReturn(StorageOperationStatus.OK);
        when(dataTypeBusinessLogic.deletePrivateDataType(eq(service), schemaTypeCaptor.capture()))
                .thenReturn(Either.left(new DataTypeDefinition())); // TODO: replace to return proper datatype
        //when(propertyDeclarationOrchestrator.unDeclarePropertiesAsInputs(service, listInput)).thenReturn(StorageOperationStatus.OK);

        Either<InputDefinition, ResponseFormat> result = testInstance.deleteInput(COMPONENT_ID, USER_ID, inputId);
        assertEquals(true, result.isLeft());
        verify(propertyDeclarationOrchestrator, times(1)).unDeclarePropertiesAsListInputs(service, listInput);
        verify(dataTypeBusinessLogic, times(1)).deletePrivateDataType(service, listInput.getSchemaType());
        assertEquals(listInput.getSchemaType(), schemaTypeCaptor.getValue());
    }


    @Test
    public void test_deleteInput_input_fail_unDeclare() throws Exception {
        InputDefinition listInput = setUpListInput();
        String inputId = COMPONENT_ID + "." + listInput.getName();
        listInput.setUniqueId(inputId);
        listInput.setIsDeclaredListInput(false);
        service.setInputs(Collections.singletonList(listInput));

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
                .thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacadeMock.deleteInputOfResource(service, listInput.getName())).thenReturn(StorageOperationStatus.OK);
        //when(componentsUtilsMock.getResponseFormat(any())).thenReturn(new ResponseFormat());
        when(propertyDeclarationOrchestrator.unDeclarePropertiesAsInputs(service, listInput)).thenReturn(StorageOperationStatus.BAD_REQUEST);

        Either<InputDefinition, ResponseFormat> result = testInstance.deleteInput(COMPONENT_ID, USER_ID, inputId);
        assertEquals(true, result.isRight());
        verify(propertyDeclarationOrchestrator, times(1)).unDeclarePropertiesAsInputs(service, listInput);
    }


    @Test
    public void test_deleteInput_input_success() throws Exception {
        InputDefinition listInput = setUpListInput();
        String inputId = COMPONENT_ID + "." + listInput.getName();
        listInput.setUniqueId(inputId);
        listInput.setIsDeclaredListInput(false);
        service.setInputs(Collections.singletonList(listInput));

        when(toscaOperationFacadeMock.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class)))
                .thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(COMPONENT_ID, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacadeMock.deleteInputOfResource(service, listInput.getName())).thenReturn(StorageOperationStatus.OK);
        //when(componentsUtilsMock.getResponseFormat(any())).thenReturn(new ResponseFormat());
        when(propertyDeclarationOrchestrator.unDeclarePropertiesAsInputs(service, listInput)).thenReturn(StorageOperationStatus.OK);

        Either<InputDefinition, ResponseFormat> result = testInstance.deleteInput(COMPONENT_ID, USER_ID, inputId);
        assertEquals(true, result.isLeft());
        verify(propertyDeclarationOrchestrator, times(1)).unDeclarePropertiesAsInputs(service, listInput);
    }

}
