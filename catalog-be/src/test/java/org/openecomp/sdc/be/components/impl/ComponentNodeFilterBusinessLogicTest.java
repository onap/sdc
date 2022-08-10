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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.validation.NodeFilterValidator;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterConstraintDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeFilterConstraintType;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterPropertyInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.dto.FilterConstraintDto;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeFilterOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.ui.mapper.FilterConstraintMapper;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;

@ExtendWith(MockitoExtension.class)
class ComponentNodeFilterBusinessLogicTest extends BaseBusinessLogicMock {

    private static final String servicePropertyName = "resourceType";
    private static final String constraintOperator = "equal";
    private static final String sourceType = "static";
    private static final String sourceName = sourceType;
    private static final String propertyValue = "resourceTypeValue";
    private static final String componentId = "dac65869-dfb4-40d2-aa20-084324659ec1";
    private static final String componentInstanceId = "dac65869-dfb4-40d2-aa20-084324659ec1.resource0";
    private static final String capabilityName = "MyCapabilityName";

    @InjectMocks
    private ComponentNodeFilterBusinessLogic componentNodeFilterBusinessLogic;
    @Mock
    private NodeFilterValidator nodeFilterValidator;
    @Mock
    private NodeFilterOperation nodeFilterOperation;
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

    private Resource resource;
    private ComponentInstance componentInstance;
    private CINodeFilterDataDefinition ciNodeFilterDataDefinition;
    private UIConstraint uiConstraint;
    private FilterConstraintDto filterConstraintDto;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        componentNodeFilterBusinessLogic =
            new ComponentNodeFilterBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
                groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation,
                nodeFilterOperation, nodeFilterValidator);
        componentNodeFilterBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
        componentNodeFilterBusinessLogic.setGraphLockOperation(graphLockOperation);
        componentNodeFilterBusinessLogic.setComponentsUtils(componentsUtils);
        componentNodeFilterBusinessLogic.setUserValidations(userValidations);
        componentNodeFilterBusinessLogic.setJanusGraphGenericDao(janusGraphGenericDao);
        componentNodeFilterBusinessLogic.setJanusGraphDao(janusGraphDao);

        initResource();
    }

    @Test
    void createWhenNodeFilterExistsTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));

        final Optional<CINodeFilterDataDefinition> result = componentNodeFilterBusinessLogic
            .createNodeFilterIfNotExist(componentId, componentInstanceId, true, ComponentTypeEnum.RESOURCE);

        assertThat(result).isPresent();
        assertThat(result.get().getProperties()).isEqualTo(ciNodeFilterDataDefinition.getProperties());
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
    }

    @Test
    void createNodeFilterFailTest() {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);
        when(componentsUtils.convertFromStorageResponse(any())).thenReturn(ActionStatus.GENERAL_ERROR);
        when(nodeFilterOperation.createNodeFilter(componentId, componentInstanceId))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .createNodeFilterIfNotExist(componentId, componentInstanceId, true, ComponentTypeEnum.RESOURCE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterOperation, times(1)).createNodeFilter(componentId, componentInstanceId);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    void createNodeFilterIfNotExist() throws BusinessLogicException {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterOperation.createNodeFilter(componentId, componentInstanceId))
            .thenReturn(Either.left(ciNodeFilterDataDefinition));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<CINodeFilterDataDefinition> result = componentNodeFilterBusinessLogic
            .createNodeFilterIfNotExist(componentId, componentInstanceId, true, ComponentTypeEnum.RESOURCE);

        assertThat(result).isPresent();
        assertThat(result.get().getProperties()).isEqualTo(ciNodeFilterDataDefinition.getProperties());

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterOperation, times(1)).createNodeFilter(componentId, componentInstanceId);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    void deleteNodeFilterIfExistsTest() throws BusinessLogicException {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));

        final Optional<String> result = componentNodeFilterBusinessLogic
            .deleteNodeFilterIfExists(componentId, componentInstanceId, true, ComponentTypeEnum.RESOURCE);

        assertThat(result).isPresent();
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1)).validateComponentInstanceExist(resource, componentInstanceId);
    }

    @Test
    void deleteWhenNodeFilterExistsTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(nodeFilterOperation.deleteNodeFilter(resource, componentInstanceId))
            .thenReturn(Either.left(componentInstanceId));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<String> result = componentNodeFilterBusinessLogic
            .deleteNodeFilterIfExists(componentId, componentInstanceId, true, ComponentTypeEnum.RESOURCE);

        assertThat(result).isPresent();
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterValidator, times(1)).validateComponentInstanceExist(resource, componentInstanceId);
        verify(nodeFilterOperation, times(1)).deleteNodeFilter(resource, componentInstanceId);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    void deleteNodeFilterIfExistsFailTest() {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(nodeFilterOperation.deleteNodeFilter(resource, componentInstanceId))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .deleteNodeFilterIfExists(componentId, componentInstanceId, true, ComponentTypeEnum.RESOURCE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterValidator, times(1)).validateComponentInstanceExist(resource, componentInstanceId);
        verify(nodeFilterOperation, times(1)).deleteNodeFilter(resource, componentInstanceId);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    void addNodeFilterPropertiesTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateFilter(resource, componentInstanceId, filterConstraintDto))
            .thenReturn(Either.left(true));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterOperation.addPropertyFilter(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(PropertyFilterDataDefinition.class))).thenReturn(Either.left(ciNodeFilterDataDefinition));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<CINodeFilterDataDefinition> result = componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstanceId, filterConstraintDto, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, null);

        assertThat(result).isPresent();
        assertThat(result.get().getProperties().getListToscaDataDefinition()).hasSize(1);
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1))
            .validateFilter(resource, componentInstanceId, filterConstraintDto);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterOperation, times(1))
            .addPropertyFilter(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
                any(PropertyFilterDataDefinition.class));
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    void addNodeFilterCapabilitiesTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateFilter(resource, componentInstanceId, filterConstraintDto))
            .thenReturn(Either.left(true));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterOperation.addCapabilities(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(RequirementNodeFilterCapabilityDataDefinition.class))).thenReturn(Either.left(ciNodeFilterDataDefinition));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<CINodeFilterDataDefinition> result = componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstanceId, filterConstraintDto, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.CAPABILITIES, capabilityName
            );

        assertThat(result).isPresent();
        assertThat(result.get().getProperties().getListToscaDataDefinition()).hasSize(1);
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1))
            .validateFilter(resource, componentInstanceId, filterConstraintDto);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterOperation, times(1))
            .addCapabilities(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
                any(RequirementNodeFilterCapabilityDataDefinition.class));
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    void addNodeFilterFailTest() {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator
            .validateFilter(resource, componentInstanceId, filterConstraintDto)).thenReturn(Either.left(true));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterOperation
            .addPropertyFilter(eq(componentId), eq(componentInstanceId), eq(ciNodeFilterDataDefinition), any(PropertyFilterDataDefinition.class)))
            .thenReturn(Either.right(StorageOperationStatus.COMPONENT_IS_IN_USE));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.COMPONENT_IS_IN_USE)).thenReturn(ActionStatus.COMPONENT_IN_USE);
        final ResponseFormat expectedResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormatByResource(ActionStatus.COMPONENT_IN_USE, resource.getSystemName())).thenReturn(expectedResponse);

        final BusinessLogicException businessLogicException = assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstanceId, filterConstraintDto, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, capabilityName));

        assertEquals(expectedResponse, businessLogicException.getResponseFormat());
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);

        verify(nodeFilterValidator, times(1))
            .validateFilter(resource, componentInstanceId, filterConstraintDto);
        verify(nodeFilterOperation, times(1))
            .addPropertyFilter(eq(componentId), eq(componentInstanceId), eq(ciNodeFilterDataDefinition),
                any(PropertyFilterDataDefinition.class));
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
        verify(janusGraphDao, times(1)).rollback();
        verify(janusGraphDao, never()).commit();
    }

    @Test
    void addNodeFilterFailFetchComponentTest() {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId))
            .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND)).thenReturn(ActionStatus.COMPONENT_NOT_FOUND);
        final ResponseFormat expectedResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NOT_FOUND)).thenReturn(expectedResponse);

        final BusinessLogicException businessLogicException = assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstanceId, filterConstraintDto, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, capabilityName));
        assertEquals(expectedResponse, businessLogicException.getResponseFormat());
    }

    @Test
    void deleteNodeFilterTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        when(nodeFilterOperation
            .deleteConstraint(componentId, componentInstanceId, ciNodeFilterDataDefinition, 0,
                NodeFilterConstraintType.PROPERTIES))
            .thenReturn(Either.left(ciNodeFilterDataDefinition));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<CINodeFilterDataDefinition> deleteNodeFilterResult = componentNodeFilterBusinessLogic
            .deleteNodeFilter(componentId, componentInstanceId, 0, true, ComponentTypeEnum.RESOURCE, NodeFilterConstraintType.PROPERTIES);

        assertThat(deleteNodeFilterResult).isPresent();

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterValidator, times(1))
            .validateComponentInstanceExist(resource, componentInstanceId);
        verify(nodeFilterOperation, times(1))
            .deleteConstraint(componentId, componentInstanceId, ciNodeFilterDataDefinition, 0,
                NodeFilterConstraintType.PROPERTIES);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    void deleteNodeFilterFailTest() {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        when(nodeFilterOperation
            .deleteConstraint(componentId, componentInstanceId, ciNodeFilterDataDefinition, 0,
                NodeFilterConstraintType.PROPERTIES))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .deleteNodeFilter(componentId, componentInstanceId, 0, true, ComponentTypeEnum.RESOURCE, NodeFilterConstraintType.PROPERTIES));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterValidator, times(1))
            .validateComponentInstanceExist(resource, componentInstanceId);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    void deleteNodeFilterFailValidationTest() {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));

        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .deleteNodeFilter(componentId, componentInstanceId, 0, true, ComponentTypeEnum.RESOURCE, NodeFilterConstraintType.PROPERTIES));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1))
            .validateComponentInstanceExist(resource, componentInstanceId);
    }

    @Test
    void updateNodeFilterTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(nodeFilterValidator
            .validateFilter(any(Component.class), anyString(), any(FilterConstraintDto.class))
        ).thenReturn(Either.left(true));
        
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        when(nodeFilterOperation.deleteConstraint(componentId, componentInstanceId, ciNodeFilterDataDefinition,
            0, NodeFilterConstraintType.PROPERTIES)).thenReturn(Either.left(ciNodeFilterDataDefinition));

        when(nodeFilterOperation.addPropertyFilter(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(PropertyFilterDataDefinition.class))).thenReturn(Either.left(ciNodeFilterDataDefinition));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<CINodeFilterDataDefinition> updateNodeFilterResult = componentNodeFilterBusinessLogic
            .updateNodeFilter(componentId, componentInstanceId, uiConstraint, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, 0);

        assertThat(updateNodeFilterResult).isPresent();
        assertThat(updateNodeFilterResult.get().getProperties().getListToscaDataDefinition()).hasSize(1);
    }

    @Test
    void updateNodeFilterFailTest() {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(nodeFilterValidator
            .validateFilter(any(Component.class), anyString(), any(FilterConstraintDto.class))
        ).thenReturn(Either.left(true));


        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        when(nodeFilterOperation.deleteConstraint(componentId, componentInstanceId, ciNodeFilterDataDefinition,
            0, NodeFilterConstraintType.PROPERTIES)).thenReturn(Either.left(ciNodeFilterDataDefinition));

        when(nodeFilterOperation.addPropertyFilter(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(PropertyFilterDataDefinition.class))).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
                .updateNodeFilter(componentId, componentInstanceId, uiConstraint, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, 0));
    }

    @Test
    void updateNodeFilterFailValidationTest() {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));

        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .updateNodeFilter(componentId, componentInstanceId, uiConstraint, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, 0));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
    }

    @Test
    void testAssociateNodeFilterToComponentInstance() {
        CINodeFilterDataDefinition ciNodeFilterDataDefinition = new CINodeFilterDataDefinition();

        UploadNodeFilterInfo filter = new UploadNodeFilterInfo();
        UploadNodeFilterPropertyInfo propertyDataDefinition = new UploadNodeFilterPropertyInfo();
        propertyDataDefinition.setName("order");
        propertyDataDefinition.setValues(Collections.singletonList("order: {equal: 2}"));
        filter.setProperties(Collections.singletonList(propertyDataDefinition));

        Map<String, UploadNodeFilterInfo> nodeFilterMap = new HashMap<>();
        nodeFilterMap.put(componentInstanceId, filter);

        when(nodeFilterOperation.createNodeFilter(componentId, componentInstanceId)).thenReturn(Either.left(ciNodeFilterDataDefinition));
        when(nodeFilterOperation.addPropertyFilter(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(PropertyFilterDataDefinition.class))).thenReturn(Either.left(ciNodeFilterDataDefinition));

        StorageOperationStatus status = componentNodeFilterBusinessLogic.associateNodeFilterToComponentInstance(componentId, nodeFilterMap);
        assertEquals(StorageOperationStatus.OK, status);
    }

    @Test
    void testAssociateNodeFilterToComponentInstanceFail() {
        CINodeFilterDataDefinition ciNodeFilterDataDefinition = new CINodeFilterDataDefinition();

        UploadNodeFilterInfo filter = new UploadNodeFilterInfo();
        UploadNodeFilterPropertyInfo propertyDataDefinition = new UploadNodeFilterPropertyInfo();
        propertyDataDefinition.setName("order");
        propertyDataDefinition.setValues(Collections.singletonList("order: {equal: 2}"));
        filter.setProperties(Collections.singletonList(propertyDataDefinition));

        Map<String, UploadNodeFilterInfo> nodeFilterMap = new HashMap<>();
        nodeFilterMap.put(componentInstanceId, filter);

        when(nodeFilterOperation.createNodeFilter(componentId, componentInstanceId)).thenReturn(Either.left(ciNodeFilterDataDefinition));
        when(nodeFilterOperation.addPropertyFilter(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(PropertyFilterDataDefinition.class))).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));

        assertThrows(ComponentException.class, () -> componentNodeFilterBusinessLogic.associateNodeFilterToComponentInstance(componentId,
            nodeFilterMap));
    }

    @Test
    void validateUserTes() {
        final String USER_ID = "jh0003";
        final User user = new User();
        user.setUserId(USER_ID);
        user.setRole(Role.ADMIN.name());
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user).thenReturn(user);
        final User result = componentNodeFilterBusinessLogic.validateUser(USER_ID);
        assertNotNull(result);
        assertTrue(USER_ID.equalsIgnoreCase(result.getUserId()));
        assertTrue(Role.ADMIN.name().equalsIgnoreCase(result.getRole()));
    }

    private void initResource() {
        resource = new Resource();
        resource.setName("MyResource");
        resource.setUniqueId(componentId);
        resource.setToscaResourceName("My_Resource_Tosca_Name");
        resource.addCategory("Network Layer 2-3", "Router");
        resource.setDescription("My short description");

        componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(componentInstanceId);
        componentInstance.setName("myComponentInstance");
        componentInstance.setDirectives(ConfigurationManager.getConfigurationManager().getConfiguration()
            .getDirectives());

        uiConstraint = new UIConstraint(servicePropertyName, constraintOperator, sourceType, sourceName, propertyValue);
        final FilterConstraintMapper filterConstraintMapper = new FilterConstraintMapper();
        filterConstraintDto = filterConstraintMapper.mapFrom(uiConstraint);

        PropertyFilterDataDefinition propertyFilterDataDefinition = new PropertyFilterDataDefinition();
        propertyFilterDataDefinition.setName(uiConstraint.getServicePropertyName());
        final PropertyFilterConstraintDataDefinition propertyFilterConstraint =
            filterConstraintMapper.mapTo(filterConstraintDto);
        propertyFilterDataDefinition.setConstraints(new LinkedList<>(List.of(propertyFilterConstraint)));

        final ListDataDefinition<PropertyFilterDataDefinition> listDataDefinition =
            new ListDataDefinition<>(new LinkedList<>(singletonList(propertyFilterDataDefinition)));

        ciNodeFilterDataDefinition = new CINodeFilterDataDefinition();
        ciNodeFilterDataDefinition.setProperties(listDataDefinition);
        ciNodeFilterDataDefinition.setID("NODE_FILTER_UID");

        resource.setComponentInstances(singletonList(componentInstance));

        final PropertyDefinition property = new PropertyDefinition();
        property.setName(uiConstraint.getServicePropertyName());

        final List<ComponentInstanceProperty> origProperties = new ArrayList<>();
        final ComponentInstanceProperty origProperty = new ComponentInstanceProperty();
        origProperty.setName(uiConstraint.getServicePropertyName());
        origProperty.setValue(propertyValue);
        origProperty.setType(uiConstraint.getSourceType());
        origProperties.add(origProperty);

        final Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put(componentInstanceId, origProperties);

        resource.setComponentInstancesProperties(componentInstanceProps);
        resource.setProperties(new LinkedList<>(List.of(property)));
    }

}
