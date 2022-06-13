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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.components.validation.NodeFilterValidator;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
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
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeFilterOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.user.Role;

@ExtendWith(MockitoExtension.class)
public class ComponentNodeFilterBusinessLogicTest extends BaseBusinessLogicMock {

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
    private RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition;
    private String constraint;
    private UIConstraint uiConstraint;

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
    public void createWhenNodeFilterExistsTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));

        final Optional<CINodeFilterDataDefinition> result = componentNodeFilterBusinessLogic
            .createNodeFilterIfNotExist(componentId, componentInstanceId, true, ComponentTypeEnum.RESOURCE);

        assertThat(result).isPresent();
        assertThat(result.get().getProperties()).isEqualTo(ciNodeFilterDataDefinition.getProperties());
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
    }

    @Test
    public void createNodeFilterFailTest() {
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
    public void createNodeFilterIfNotExist() throws BusinessLogicException {
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
    public void deleteNodeFilterIfExistsTest() throws BusinessLogicException {
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
    public void deleteWhenNodeFilterExistsTest() throws BusinessLogicException {
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
    public void deleteNodeFilterIfExistsFailTest() {
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
    public void addNodeFilterPropertiesTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator
            .validateFilter(resource, componentInstanceId,
                requirementNodeFilterPropertyDataDefinition.getConstraints(),
                NodeFilterConstraintAction.ADD, NodeFilterConstraintType.PROPERTIES, "")).thenReturn(Either.left(true));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterOperation.addNewProperty(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(RequirementNodeFilterPropertyDataDefinition.class))).thenReturn(Either.left(ciNodeFilterDataDefinition));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<CINodeFilterDataDefinition> result = componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstanceId, NodeFilterConstraintAction.ADD,
                "MyPropertyName", constraint, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, "");

        assertThat(result).isPresent();
        assertThat(result.get().getProperties().getListToscaDataDefinition()).hasSize(1);
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1)).validateFilter(resource, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.ADD, NodeFilterConstraintType.PROPERTIES, "");
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterOperation, times(1))
            .addNewProperty(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
                any(RequirementNodeFilterPropertyDataDefinition.class));
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    public void addNodeFilterCapabilitiesTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator
            .validateFilter(resource, componentInstanceId,
                requirementNodeFilterPropertyDataDefinition.getConstraints(),
                NodeFilterConstraintAction.ADD, NodeFilterConstraintType.CAPABILITIES, capabilityName)).thenReturn(Either.left(true));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterOperation.addNewCapabilities(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(RequirementNodeFilterCapabilityDataDefinition.class))).thenReturn(Either.left(ciNodeFilterDataDefinition));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<CINodeFilterDataDefinition> result = componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstanceId, NodeFilterConstraintAction.ADD,
                "MyPropertyName", constraint, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.CAPABILITIES, capabilityName);

        assertThat(result).isPresent();
        assertThat(result.get().getProperties().getListToscaDataDefinition()).hasSize(1);
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1)).validateFilter(resource, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.ADD, NodeFilterConstraintType.CAPABILITIES, capabilityName);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterOperation, times(1))
            .addNewCapabilities(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
                any(RequirementNodeFilterCapabilityDataDefinition.class));
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    public void addNodeFilterFailTest() {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator
            .validateFilter(resource, componentInstanceId,
                requirementNodeFilterPropertyDataDefinition.getConstraints(),
                NodeFilterConstraintAction.ADD, NodeFilterConstraintType.PROPERTIES, capabilityName)).thenReturn(Either.left(true));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        final List<String> constraints = requirementNodeFilterPropertyDataDefinition.getConstraints();
        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstanceId, NodeFilterConstraintAction.ADD,
                "MyPropertyName", constraint, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, capabilityName));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterValidator, times(1)).validateFilter(resource, componentInstanceId,
            constraints, NodeFilterConstraintAction.ADD, NodeFilterConstraintType.PROPERTIES, capabilityName);
        verify(nodeFilterOperation, times(0))
            .addNewProperty(componentId, componentInstanceId, ciNodeFilterDataDefinition,
                requirementNodeFilterPropertyDataDefinition);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    public void addNodeFilterFailFetchComponentTest() {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));

        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .addNodeFilter(componentId, componentInstanceId, NodeFilterConstraintAction.ADD,
                "MyPropertyName", constraint, true, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, capabilityName));
    }

    @Test
    public void deleteNodeFilterTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(nodeFilterValidator.validateFilter(resource, componentInstanceId, singletonList(constraint),
            NodeFilterConstraintAction.DELETE, NodeFilterConstraintType.PROPERTIES, "")).thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        when(nodeFilterOperation
            .deleteConstraint(componentId, componentInstanceId, ciNodeFilterDataDefinition, 0,
                NodeFilterConstraintType.PROPERTIES))
            .thenReturn(Either.left(ciNodeFilterDataDefinition));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<CINodeFilterDataDefinition> deleteNodeFilterResult = componentNodeFilterBusinessLogic
            .deleteNodeFilter(componentId, componentInstanceId, NodeFilterConstraintAction.DELETE, constraint,
                0, true, ComponentTypeEnum.RESOURCE, NodeFilterConstraintType.PROPERTIES);

        assertThat(deleteNodeFilterResult).isPresent();

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterValidator, times(1))
            .validateComponentInstanceExist(resource, componentInstanceId);
        verify(nodeFilterValidator, times(1))
            .validateFilter(resource, componentInstanceId, singletonList(constraint),
                NodeFilterConstraintAction.DELETE, NodeFilterConstraintType.PROPERTIES, "");
        verify(nodeFilterOperation, times(1))
            .deleteConstraint(componentId, componentInstanceId, ciNodeFilterDataDefinition, 0,
                NodeFilterConstraintType.PROPERTIES);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    public void deleteNodeFilterFailTest() {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(nodeFilterValidator.validateFilter(resource, componentInstanceId, singletonList(constraint),
            NodeFilterConstraintAction.DELETE, NodeFilterConstraintType.PROPERTIES, "")).thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        when(nodeFilterOperation
            .deleteConstraint(componentId, componentInstanceId, ciNodeFilterDataDefinition, 0,
                NodeFilterConstraintType.PROPERTIES))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .deleteNodeFilter(componentId, componentInstanceId, NodeFilterConstraintAction.DELETE, constraint,
                0, true, ComponentTypeEnum.RESOURCE, NodeFilterConstraintType.PROPERTIES));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Resource);
        verify(nodeFilterValidator, times(1))
            .validateFilter(resource, componentInstanceId, singletonList(constraint),
                NodeFilterConstraintAction.DELETE, NodeFilterConstraintType.PROPERTIES, "");
        verify(nodeFilterValidator, times(1))
            .validateComponentInstanceExist(resource, componentInstanceId);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Resource);
    }

    @Test
    public void deleteNodeFilterFailValidationTest() {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(nodeFilterValidator.validateFilter(resource, componentInstanceId, singletonList(constraint),
            NodeFilterConstraintAction.DELETE, NodeFilterConstraintType.PROPERTIES, "")).thenReturn(Either.left(true));

        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .deleteNodeFilter(componentId, componentInstanceId, NodeFilterConstraintAction.DELETE, constraint,
                0, true, ComponentTypeEnum.RESOURCE, NodeFilterConstraintType.PROPERTIES));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1))
            .validateFilter(resource, componentInstanceId, singletonList(constraint),
                NodeFilterConstraintAction.DELETE, NodeFilterConstraintType.PROPERTIES, "");
        verify(nodeFilterValidator, times(1))
            .validateComponentInstanceExist(resource, componentInstanceId);
    }

    @Test
    public void updateNodeFilterTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(nodeFilterValidator
            .validateFilter(ArgumentMatchers.any(Component.class), anyString(), anyList(),
                ArgumentMatchers.any(NodeFilterConstraintAction.class),
                ArgumentMatchers.any(NodeFilterConstraintType.class), anyString())).thenReturn(Either.left(true));
        
        when(nodeFilterValidator
                .validateFilter(ArgumentMatchers.any(Component.class), anyString(), anyList(),
                    ArgumentMatchers.any(NodeFilterConstraintAction.class),
                    ArgumentMatchers.any(NodeFilterConstraintType.class), isNull())).thenReturn(Either.left(true));

        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        when(nodeFilterOperation.deleteConstraint(componentId, componentInstanceId, ciNodeFilterDataDefinition,
            0, NodeFilterConstraintType.PROPERTIES)).thenReturn(Either.left(ciNodeFilterDataDefinition));

        when(nodeFilterOperation.addNewProperty(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(RequirementNodeFilterPropertyDataDefinition.class))).thenReturn(Either.left(ciNodeFilterDataDefinition));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<CINodeFilterDataDefinition> updateNodeFilterResult = componentNodeFilterBusinessLogic
            .updateNodeFilter(componentId, componentInstanceId, uiConstraint, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, 0);

        assertThat(updateNodeFilterResult).isPresent();
        assertThat(updateNodeFilterResult.get().getProperties().getListToscaDataDefinition()).hasSize(1);
    }

    @Test
    public void updateNodeFilterFailTest() throws BusinessLogicException {
        componentInstance.setNodeFilter(ciNodeFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(nodeFilterValidator
                .validateFilter(ArgumentMatchers.any(Component.class), anyString(), anyList(),
                    ArgumentMatchers.any(NodeFilterConstraintAction.class),
                    ArgumentMatchers.any(NodeFilterConstraintType.class), anyString())).thenReturn(Either.left(true));
        when(nodeFilterValidator
            .validateFilter(ArgumentMatchers.any(Component.class), anyString(), anyList(),
                ArgumentMatchers.any(NodeFilterConstraintAction.class),
                ArgumentMatchers.any(NodeFilterConstraintType.class), isNull())).thenReturn(Either.left(true));

        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        when(nodeFilterOperation.deleteConstraint(componentId, componentInstanceId, ciNodeFilterDataDefinition,
            0, NodeFilterConstraintType.PROPERTIES)).thenReturn(Either.left(ciNodeFilterDataDefinition));

        when(nodeFilterOperation.addNewProperty(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(RequirementNodeFilterPropertyDataDefinition.class))).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Resource))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
                .updateNodeFilter(componentId, componentInstanceId, uiConstraint, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, 0));
    }

    @Test
    public void updateNodeFilterFailValidationTest() {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterValidator.validateComponentInstanceExist(resource, componentInstanceId))
            .thenReturn(Either.left(true));
        when(nodeFilterValidator
            .validateFilter(ArgumentMatchers.any(Component.class), anyString(), anyList(),
                ArgumentMatchers.any(NodeFilterConstraintAction.class),
                ArgumentMatchers.any(NodeFilterConstraintType.class), anyString())).thenReturn(Either.left(true));

        assertThrows(BusinessLogicException.class, () -> componentNodeFilterBusinessLogic
            .updateNodeFilter(componentId, componentInstanceId, uiConstraint, ComponentTypeEnum.RESOURCE,
                NodeFilterConstraintType.PROPERTIES, 0));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
    }

    @Test
    public void testAssociateNodeFilterToComponentInstance() {
        CINodeFilterDataDefinition ciNodeFilterDataDefinition = new CINodeFilterDataDefinition();

        UploadNodeFilterInfo filter = new UploadNodeFilterInfo();
        UploadNodeFilterPropertyInfo propertyDataDefinition = new UploadNodeFilterPropertyInfo();
        propertyDataDefinition.setName("order");
        propertyDataDefinition.setValues(Collections.singletonList("order: {equal: 2"));
        List<UploadNodeFilterPropertyInfo> propertyList = new LinkedList<>();
        propertyList.add(propertyDataDefinition);
        filter.setProperties(Collections.singletonList(propertyDataDefinition));

        Map<String, UploadNodeFilterInfo> nodeFilterMap = new HashMap<>();
        nodeFilterMap.put(componentInstanceId, filter);

        when(nodeFilterOperation.createNodeFilter(componentId, componentInstanceId)).thenReturn(Either.left(ciNodeFilterDataDefinition));
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterOperation.addNewProperty(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(RequirementNodeFilterPropertyDataDefinition.class))).thenReturn(Either.left(ciNodeFilterDataDefinition));

        StorageOperationStatus status = componentNodeFilterBusinessLogic.associateNodeFilterToComponentInstance(componentId, nodeFilterMap);
        assertEquals(StorageOperationStatus.OK, status);
    }

    @Test
    public void testAssociateNodeFilterToComponentInstanceFail() {
        CINodeFilterDataDefinition ciNodeFilterDataDefinition = new CINodeFilterDataDefinition();

        UploadNodeFilterInfo filter = new UploadNodeFilterInfo();
        UploadNodeFilterPropertyInfo propertyDataDefinition = new UploadNodeFilterPropertyInfo();
        propertyDataDefinition.setName("order");
        propertyDataDefinition.setValues(Collections.singletonList("order: {equal: 2"));
        List<UploadNodeFilterPropertyInfo> propertyList = new LinkedList<>();
        propertyList.add(propertyDataDefinition);
        filter.setProperties(Collections.singletonList(propertyDataDefinition));

        Map<String, UploadNodeFilterInfo> nodeFilterMap = new HashMap<>();
        nodeFilterMap.put(componentInstanceId, filter);

        when(nodeFilterOperation.createNodeFilter(componentId, componentInstanceId)).thenReturn(Either.left(ciNodeFilterDataDefinition));
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(resource));
        when(nodeFilterOperation.addNewProperty(anyString(), anyString(), any(CINodeFilterDataDefinition.class),
            any(RequirementNodeFilterPropertyDataDefinition.class))).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));

        Assertions.assertThrows(ComponentException.class, () -> componentNodeFilterBusinessLogic.associateNodeFilterToComponentInstance(componentId,
            nodeFilterMap));
    }

    @Test
    public void validateUserTes() {
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

    public void initResource() {
        try {
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
            constraint = new ConstraintConvertor().convert(uiConstraint);

            requirementNodeFilterPropertyDataDefinition = new RequirementNodeFilterPropertyDataDefinition();
            requirementNodeFilterPropertyDataDefinition.setName(uiConstraint.getServicePropertyName());
            requirementNodeFilterPropertyDataDefinition.setConstraints(new LinkedList<>(Arrays.asList(constraint)));

            final ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> listDataDefinition =
                new ListDataDefinition<>(new LinkedList<>(Arrays.asList(requirementNodeFilterPropertyDataDefinition)));

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
            resource.setProperties(new LinkedList<>(Arrays.asList(property)));
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }
}
