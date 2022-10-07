/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.tosca.constraints.ValidValuesConstraint;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;

@ExtendWith(MockitoExtension.class)
class ComponentInterfaceOperationBusinessLogicTest extends BaseBusinessLogicMock {

    @InjectMocks
    private ComponentInterfaceOperationBusinessLogic componentInterfaceOperationBusinessLogic;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private GraphLockOperation graphLockOperation;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private JanusGraphGenericDao janusGraphGenericDao;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private UserValidations userValidations;
    @Mock
    private ComponentValidations componentValidations;
    @Mock
    private PropertyBusinessLogic propertyBusinessLogic;
    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;
    @Mock
    private ArtifactsBusinessLogic artifactsBusinessLogic;

    private Component component;
    private ComponentInstance componentInstance;
    private ComponentParametersView componentFilter;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        componentInterfaceOperationBusinessLogic =
            new ComponentInterfaceOperationBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
                groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation,
                componentValidations, propertyBusinessLogic, artifactsBusinessLogic);
        componentInterfaceOperationBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
        componentInterfaceOperationBusinessLogic.setGraphLockOperation(graphLockOperation);
        componentInterfaceOperationBusinessLogic.setComponentsUtils(componentsUtils);
        componentInterfaceOperationBusinessLogic.setUserValidations(userValidations);
        componentInterfaceOperationBusinessLogic.setJanusGraphGenericDao(janusGraphGenericDao);
        componentInterfaceOperationBusinessLogic.setJanusGraphDao(janusGraphDao);
        componentInterfaceOperationBusinessLogic.setApplicationDataTypeCache(applicationDataTypeCache);
        initComponentData();
    }

    @Test
    void updateComponentInstanceInterfaceOperationTest() throws BusinessLogicException {
        final String componentId = component.getUniqueId();
        final String componentInstanceId = componentInstance.getUniqueId();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setUniqueId(UUID.randomUUID().toString());
        interfaceDefinition.setType("tosca.interfaces.node.lifecycle.Standard");
        final Map<String, OperationDataDefinition> operations = new HashMap<>();
        final OperationDataDefinition operationDataDefinition = new OperationDataDefinition();
        operationDataDefinition.setUniqueId(UUID.randomUUID().toString());
        final ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
        artifactDataDefinition.setArtifactName("EO Implementation info");
        operationDataDefinition.setImplementation(artifactDataDefinition);
        operations.put("configure", operationDataDefinition);
        interfaceDefinition.setOperations(operations );

        final ComponentInstanceInterface componentInstanceInterface =
            new ComponentInstanceInterface("interfaceId", interfaceDefinition);
        Map<String, List<ComponentInstanceInterface>> componentInstancesInterfacesMap = new HashMap<>();
        componentInstancesInterfacesMap.put(componentInstanceId, Collections.singletonList(componentInstanceInterface));
        component.setComponentInstancesInterfaces(componentInstancesInterfacesMap);
        componentInstance.setInterfaces(
            (Map<String, Object>) new HashMap<>().put(componentInstanceId, interfaceDefinition));
        component.setComponentInstances(Collections.singletonList(componentInstance));

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(componentValidations.getComponentInstance(component, componentInstanceId))
            .thenReturn(Optional.of(componentInstance));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateComponentInstanceInterfaces(component, componentInstanceId))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade
            .updateComponentInstanceMetadataOfTopologyTemplate(any(Service.class), any(ComponentParametersView.class)))
            .thenReturn(Either.left(component));
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(new HashMap<>()));

        final Optional<ComponentInstance> result = componentInterfaceOperationBusinessLogic
            .updateComponentInstanceInterfaceOperation(componentId, componentInstanceId, interfaceDefinition,
                ComponentTypeEnum.SERVICE, new Wrapper<>(), true);
        assertThat(result).isPresent();
    }

    @Test
    void valueForInputFailsConstraintsValidation() throws BusinessLogicException {
        final String inputType = "myType";
        final String componentId = component.getUniqueId();
        final String componentInstanceId = componentInstance.getUniqueId();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setUniqueId(UUID.randomUUID().toString());
        interfaceDefinition.setType("tosca.interfaces.node.lifecycle.Standard");
        final Map<String, OperationDataDefinition> operations = new HashMap<>();
        final OperationDataDefinition operationDataDefinition = new OperationDataDefinition();
        operationDataDefinition.setUniqueId(UUID.randomUUID().toString());
        final ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
        final ListDataDefinition<OperationInputDefinition> inputsDefinitionListData = new ListDataDefinition<>();
        OperationInputDefinition input1 = new OperationInputDefinition();
        input1.setName("input_1");
        input1.setType(inputType);
        input1.setValue("{\"input_range\": \"invalid\"}");
        inputsDefinitionListData.add(input1);
        artifactDataDefinition.setArtifactName("EO Implementation info");
        operationDataDefinition.setImplementation(artifactDataDefinition);
        operationDataDefinition.setInputs(inputsDefinitionListData);
        operations.put("configure", operationDataDefinition);
        interfaceDefinition.setOperations(operations );

        DataTypeDefinition myType = new DataTypeDefinition();
        myType.setName(inputType);
        PropertyDefinition input_range = new PropertyDefinition();
        input_range.setName("input_range");
        input_range.setType("string");
        PropertyConstraint constraint1 = new ValidValuesConstraint(Arrays.asList("value1", "value2", "value3"));
        input_range.setConstraints(List.of(constraint1));
        myType.setProperties(List.of(input_range));
        Map<String, DataTypeDefinition> dataTypes = Collections.singletonMap(myType.getName(), myType);
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();

        final ComponentInstanceInterface componentInstanceInterface =
            new ComponentInstanceInterface("interfaceId", interfaceDefinition);
        Map<String, List<ComponentInstanceInterface>> componentInstancesInterfacesMap = new HashMap<>();
        componentInstancesInterfacesMap.put(componentInstanceId, Collections.singletonList(componentInstanceInterface));
        component.setComponentInstancesInterfaces(componentInstancesInterfacesMap);
        componentInstance.setInterfaces(
            (Map<String, Object>) new HashMap<>().put(componentInstanceId, interfaceDefinition));
        component.setComponentInstances(Collections.singletonList(componentInstance));

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(componentValidations.getComponentInstance(component, componentInstanceId))
            .thenReturn(Optional.of(componentInstance));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(dataTypes));

        final Optional<ComponentInstance> result = componentInterfaceOperationBusinessLogic
            .updateComponentInstanceInterfaceOperation(componentId, componentInstanceId, interfaceDefinition,
                ComponentTypeEnum.SERVICE, errorWrapper, false);
        assertThat(result).isNotPresent();
        assertTrue(errorWrapper.getInnerElement().getStatus() == 400);
        assertTrue(errorWrapper.getInnerElement().getRequestError().getRequestError().getServiceException().getText()
            .contains("Error: Invalid property values provided"));
    }

    @Test
    void valueForInputSucceedsConstraintsValidation() throws BusinessLogicException {
        final String inputType = "myType";
        final String componentId = component.getUniqueId();
        final String componentInstanceId = componentInstance.getUniqueId();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setUniqueId(UUID.randomUUID().toString());
        interfaceDefinition.setType("tosca.interfaces.node.lifecycle.Standard");
        final Map<String, OperationDataDefinition> operations = new HashMap<>();
        final OperationDataDefinition operationDataDefinition = new OperationDataDefinition();
        operationDataDefinition.setUniqueId(UUID.randomUUID().toString());
        final ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
        final ListDataDefinition<OperationInputDefinition> inputsDefinitionListData = new ListDataDefinition<>();
        OperationInputDefinition input1 = new OperationInputDefinition();
        input1.setName("input_1");
        input1.setType(inputType);
        input1.setValue("{\"input_range\": \"value1\"}");
        inputsDefinitionListData.add(input1);
        artifactDataDefinition.setArtifactName("EO Implementation info");
        operationDataDefinition.setImplementation(artifactDataDefinition);
        operationDataDefinition.setInputs(inputsDefinitionListData);
        operations.put("configure", operationDataDefinition);
        interfaceDefinition.setOperations(operations );

        DataTypeDefinition myType = new DataTypeDefinition();
        myType.setName(inputType);
        PropertyDefinition input_range = new PropertyDefinition();
        input_range.setName("input_range");
        input_range.setType("string");
        PropertyConstraint constraint1 = new ValidValuesConstraint(Arrays.asList("value1", "value2", "value3"));
        input_range.setConstraints(List.of(constraint1));
        myType.setProperties(List.of(input_range));
        Map<String, DataTypeDefinition> dataTypes = Collections.singletonMap(myType.getName(), myType);
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();

        final ComponentInstanceInterface componentInstanceInterface =
            new ComponentInstanceInterface("interfaceId", interfaceDefinition);
        Map<String, List<ComponentInstanceInterface>> componentInstancesInterfacesMap = new HashMap<>();
        componentInstancesInterfacesMap.put(componentInstanceId, Collections.singletonList(componentInstanceInterface));
        component.setComponentInstancesInterfaces(componentInstancesInterfacesMap);
        componentInstance.setInterfaces(
            (Map<String, Object>) new HashMap<>().put(componentInstanceId, interfaceDefinition));
        component.setComponentInstances(Collections.singletonList(componentInstance));

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(componentValidations.getComponentInstance(component, componentInstanceId))
            .thenReturn(Optional.of(componentInstance));
        when(toscaOperationFacade.updateComponentInstanceInterfaces(component, componentInstanceId))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade
            .updateComponentInstanceMetadataOfTopologyTemplate(any(Service.class), any(ComponentParametersView.class)))
            .thenReturn(Either.left(component));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(dataTypes));

        final Optional<ComponentInstance> result = componentInterfaceOperationBusinessLogic
            .updateComponentInstanceInterfaceOperation(componentId, componentInstanceId, interfaceDefinition,
                ComponentTypeEnum.SERVICE, errorWrapper, false);
        assertThat(result).isPresent();
    }

    @Test
    void valueForArtifactInputFailsConstraintsValidation() throws BusinessLogicException {
        final String inputType = "myType";
        final String artifactType = "artifactType";
        final String componentId = component.getUniqueId();
        final String componentInstanceId = componentInstance.getUniqueId();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setUniqueId(UUID.randomUUID().toString());
        interfaceDefinition.setType("tosca.interfaces.node.lifecycle.Standard");
        final Map<String, OperationDataDefinition> operations = new HashMap<>();
        final OperationDataDefinition operationDataDefinition = new OperationDataDefinition();
        operationDataDefinition.setUniqueId(UUID.randomUUID().toString());
        final ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();

        artifactDataDefinition.setArtifactName("EO Implementation info");
        artifactDataDefinition.setArtifactType(artifactType);
        PropertyDataDefinition propertyDataDefinition = new PropertyDataDefinition();
        propertyDataDefinition.setName("input_range");
        propertyDataDefinition.setType(inputType);
        propertyDataDefinition.setValue("{\"input_range\": \"invalid\"}");
        artifactDataDefinition.addProperty(propertyDataDefinition);

        operationDataDefinition.setImplementation(artifactDataDefinition);
        operations.put("configure", operationDataDefinition);
        interfaceDefinition.setOperations(operations );

        DataTypeDefinition myType = new DataTypeDefinition();
        ArtifactTypeDefinition myArtifactType = new ArtifactTypeDefinition();
        myArtifactType.setName(UniqueIdBuilder.buildArtifactTypeUid(null, artifactType));
        myType.setName(inputType);
        PropertyDefinition input_range = new PropertyDefinition();
        input_range.setName("input_range");
        input_range.setType("string");
        PropertyConstraint constraint1 = new ValidValuesConstraint(Arrays.asList("value1", "value2", "value3"));
        input_range.setConstraints(List.of(constraint1));
        myArtifactType.setProperties(List.of(input_range));
        myType.setProperties(List.of(input_range));
        final Map<String, ArtifactTypeDefinition> artifactTypeDefinitionCache = Collections.singletonMap(myArtifactType.getName(), myArtifactType);
        Map<String, DataTypeDefinition> dataTypes = Collections.singletonMap(myType.getName(), myType);
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();

        final ComponentInstanceInterface componentInstanceInterface =
            new ComponentInstanceInterface("interfaceId", interfaceDefinition);
        Map<String, List<ComponentInstanceInterface>> componentInstancesInterfacesMap = new HashMap<>();
        componentInstancesInterfacesMap.put(componentInstanceId, Collections.singletonList(componentInstanceInterface));
        component.setComponentInstancesInterfaces(componentInstancesInterfacesMap);
        componentInstance.setInterfaces(
            (Map<String, Object>) new HashMap<>().put(componentInstanceId, interfaceDefinition));
        component.setComponentInstances(Collections.singletonList(componentInstance));

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(componentValidations.getComponentInstance(component, componentInstanceId))
            .thenReturn(Optional.of(componentInstance));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(dataTypes));
        when(artifactsBusinessLogic.getAllToscaArtifacts(null)).thenReturn(artifactTypeDefinitionCache);

        final Optional<ComponentInstance> result = componentInterfaceOperationBusinessLogic
            .updateComponentInstanceInterfaceOperation(componentId, componentInstanceId, interfaceDefinition,
                ComponentTypeEnum.SERVICE, errorWrapper, false);
        assertThat(result).isNotPresent();
        assertTrue(errorWrapper.getInnerElement().getStatus() == 400);
        assertTrue(errorWrapper.getInnerElement().getRequestError().getRequestError().getServiceException().getText()
            .contains("Error: Invalid property values provided"));
    }

    @Test
    void valueForArtifactInputSucceedsConstraintsValidation() throws BusinessLogicException {
        final String inputType = "myType";
        final String artifactType = "artifactType";
        final String componentId = component.getUniqueId();
        final String componentInstanceId = componentInstance.getUniqueId();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setUniqueId(UUID.randomUUID().toString());
        interfaceDefinition.setType("tosca.interfaces.node.lifecycle.Standard");
        final Map<String, OperationDataDefinition> operations = new HashMap<>();
        final OperationDataDefinition operationDataDefinition = new OperationDataDefinition();
        operationDataDefinition.setUniqueId(UUID.randomUUID().toString());
        final ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();

        artifactDataDefinition.setArtifactName("EO Implementation info");
        artifactDataDefinition.setArtifactType(artifactType);
        PropertyDataDefinition propertyDataDefinition = new PropertyDataDefinition();
        propertyDataDefinition.setName("input_range");
        propertyDataDefinition.setType(inputType);
        propertyDataDefinition.setValue("{\"input_range\": \"value2\"}");
        artifactDataDefinition.addProperty(propertyDataDefinition);

        operationDataDefinition.setImplementation(artifactDataDefinition);
        operations.put("configure", operationDataDefinition);
        interfaceDefinition.setOperations(operations );

        DataTypeDefinition myType = new DataTypeDefinition();
        ArtifactTypeDefinition myArtifactType = new ArtifactTypeDefinition();
        myArtifactType.setName(UniqueIdBuilder.buildArtifactTypeUid(null, artifactType));
        myType.setName(inputType);
        PropertyDefinition input_range = new PropertyDefinition();
        input_range.setName("input_range");
        input_range.setType("string");
        PropertyConstraint constraint1 = new ValidValuesConstraint(Arrays.asList("value1", "value2", "value3"));
        input_range.setConstraints(List.of(constraint1));
        myArtifactType.setProperties(List.of(input_range));
        myType.setProperties(List.of(input_range));
        final Map<String, ArtifactTypeDefinition> artifactTypeDefinitionCache = Collections.singletonMap(myArtifactType.getName(), myArtifactType);
        Map<String, DataTypeDefinition> dataTypes = Collections.singletonMap(myType.getName(), myType);
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();

        final ComponentInstanceInterface componentInstanceInterface =
            new ComponentInstanceInterface("interfaceId", interfaceDefinition);
        Map<String, List<ComponentInstanceInterface>> componentInstancesInterfacesMap = new HashMap<>();
        componentInstancesInterfacesMap.put(componentInstanceId, Collections.singletonList(componentInstanceInterface));
        component.setComponentInstancesInterfaces(componentInstancesInterfacesMap);
        componentInstance.setInterfaces(
            (Map<String, Object>) new HashMap<>().put(componentInstanceId, interfaceDefinition));
        component.setComponentInstances(Collections.singletonList(componentInstance));

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(componentValidations.getComponentInstance(component, componentInstanceId))
            .thenReturn(Optional.of(componentInstance));
        when(toscaOperationFacade.updateComponentInstanceInterfaces(component, componentInstanceId))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade
            .updateComponentInstanceMetadataOfTopologyTemplate(any(Service.class), any(ComponentParametersView.class)))
            .thenReturn(Either.left(component));
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(dataTypes));
        when(artifactsBusinessLogic.getAllToscaArtifacts(null)).thenReturn(artifactTypeDefinitionCache);

        final Optional<ComponentInstance> result = componentInterfaceOperationBusinessLogic
            .updateComponentInstanceInterfaceOperation(componentId, componentInstanceId, interfaceDefinition,
                ComponentTypeEnum.SERVICE, errorWrapper, false);
        assertThat(result).isPresent();
    }

    private void initComponentData() {
        try {
            component = new Service();
            component.setName("MyTestService");
            component.setUniqueId("dac65869-dfb4-40d2-aa20-084324659ec1");

            componentInstance = new ComponentInstance();
            componentInstance.setUniqueId("dac65869-dfb4-40d2-aa20-084324659ec1.resource0");
            componentInstance.setOriginType(OriginTypeEnum.VFC);
            componentInstance.setName("My VFC Instance");

            componentFilter = new ComponentParametersView();
            componentFilter.disableAll();
            componentFilter.setIgnoreUsers(false);
            componentFilter.setIgnoreComponentInstances(false);
            componentFilter.setIgnoreInterfaces(false);
            componentFilter.setIgnoreComponentInstancesInterfaces(false);

        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }
}
