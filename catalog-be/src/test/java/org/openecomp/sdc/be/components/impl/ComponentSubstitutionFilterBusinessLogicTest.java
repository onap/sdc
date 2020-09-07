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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.components.validation.NodeFilterValidator;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementSubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.SubstitutionFilterOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.exception.ResponseFormat;

@ExtendWith(MockitoExtension.class)
public class ComponentSubstitutionFilterBusinessLogicTest extends BaseBusinessLogicMock {

    private static final String servicePropertyName = "controller_actor";
    private static final String constraintOperator = "equal";
    private static final String sourceType = "static";
    private static final String sourceName = sourceType;
    private static final String propertyValue = "constraintValue";
    private static final String componentId = "dac65869-dfb4-40d2-aa20-084324659ec1";
    private static final String componentInstanceId = "dac65869-dfb4-40d2-aa20-084324659ec1.service0";

    @InjectMocks
    private ComponentSubstitutionFilterBusinessLogic componentSubstitutionFilterBusinessLogic;
    @Mock
    private NodeFilterValidator nodeFilterValidator;
    @Mock
    private SubstitutionFilterOperation substitutionFilterOperation;
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
    private ResponseFormat responseFormat;

    private Service service;
    private ComponentInstance componentInstance;
    private SubstitutionFilterDataDefinition substitutionFilterDataDefinition;
    private RequirementSubstitutionFilterPropertyDataDefinition requirementSubstitutionFilterPropertyDataDefinition;
    private String constraint;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        componentSubstitutionFilterBusinessLogic =
            new ComponentSubstitutionFilterBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
                groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation,
                substitutionFilterOperation, nodeFilterValidator);
        componentSubstitutionFilterBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
        componentSubstitutionFilterBusinessLogic.setGraphLockOperation(graphLockOperation);
        componentSubstitutionFilterBusinessLogic.setComponentsUtils(componentsUtils);
        componentSubstitutionFilterBusinessLogic.setUserValidations(userValidations);
        componentSubstitutionFilterBusinessLogic.setJanusGraphGenericDao(janusGraphGenericDao);
        componentSubstitutionFilterBusinessLogic.setJanusGraphDao(janusGraphDao);

        initResource();
    }

    @Test
    public void doNotCreateSubstitutionFilterAsExistsTest() throws BusinessLogicException {
        componentInstance.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));

        final Optional<SubstitutionFilterDataDefinition> result = componentSubstitutionFilterBusinessLogic
            .createSubstitutionFilterIfNotExist(componentId, componentInstanceId, true, ComponentTypeEnum.SERVICE);
        assertThat(result).isPresent();
        assertThat(result.get().getProperties()).isEqualTo(substitutionFilterDataDefinition.getProperties());
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
    }

    @Test
    public void createSubstitutionFilterIfNotExistTest() throws BusinessLogicException {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(substitutionFilterOperation.createSubstitutionFilter(componentId, componentInstanceId))
            .thenReturn(Either.left(substitutionFilterDataDefinition));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<SubstitutionFilterDataDefinition> result = componentSubstitutionFilterBusinessLogic
            .createSubstitutionFilterIfNotExist(componentId, componentInstanceId, true, ComponentTypeEnum.SERVICE);
        assertThat(result).isPresent();
        assertThat(result.get().getProperties()).isEqualTo(substitutionFilterDataDefinition.getProperties());
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(substitutionFilterOperation, times(1)).createSubstitutionFilter(componentId, componentInstanceId);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    @Test
    public void createSubstitutionFilterIfNotExistFailTest() {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(substitutionFilterOperation.createSubstitutionFilter(componentId, componentInstanceId))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentSubstitutionFilterBusinessLogic
            .createSubstitutionFilterIfNotExist(componentId, componentInstanceId, true, ComponentTypeEnum.SERVICE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(substitutionFilterOperation, times(1)).createSubstitutionFilter(componentId, componentInstanceId);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    @Test
    public void addSubstitutionFilterTest() throws BusinessLogicException {
        componentInstance.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));
        when(nodeFilterValidator.validateFilter(service, componentInstanceId, Collections.singletonList(constraint),
            NodeFilterConstraintAction.ADD)).thenReturn(Either.left(true));
        when(nodeFilterValidator.validateComponentInstanceExist(service, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        when(substitutionFilterOperation
            .addNewProperty(anyString(), anyString(), any(SubstitutionFilterDataDefinition.class),
                any(RequirementSubstitutionFilterPropertyDataDefinition.class)))
            .thenReturn(Either.left(substitutionFilterDataDefinition));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<SubstitutionFilterDataDefinition> result = componentSubstitutionFilterBusinessLogic
            .addSubstitutionFilter(componentId, componentInstanceId, NodeFilterConstraintAction.ADD,
                servicePropertyName, constraint, true, ComponentTypeEnum.SERVICE);

        assertThat(result).isPresent();
        assertThat(result.get().getProperties().getListToscaDataDefinition()).hasSize(1);
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(nodeFilterValidator, times(1)).validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.ADD);
        verify(substitutionFilterOperation, times(0))
            .addNewProperty(componentId, componentInstanceId, substitutionFilterDataDefinition,
                requirementSubstitutionFilterPropertyDataDefinition);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);

    }

    @Test
    public void addSubstitutionFilterFailTest() {
        componentInstance.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));
        when(nodeFilterValidator.validateFilter(service, componentInstanceId, Collections.singletonList(constraint),
            NodeFilterConstraintAction.ADD)).thenReturn(Either.left(true));
        when(nodeFilterValidator.validateComponentInstanceExist(service, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        when(substitutionFilterOperation
            .addNewProperty(anyString(), anyString(), any(SubstitutionFilterDataDefinition.class),
                any(RequirementSubstitutionFilterPropertyDataDefinition.class)))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentSubstitutionFilterBusinessLogic
            .addSubstitutionFilter(componentId, componentInstanceId, NodeFilterConstraintAction.ADD,
                servicePropertyName, constraint, true, ComponentTypeEnum.SERVICE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(nodeFilterValidator, times(1)).validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.ADD);
        verify(substitutionFilterOperation, times(0))
            .addNewProperty(componentId, componentInstanceId, substitutionFilterDataDefinition,
                requirementSubstitutionFilterPropertyDataDefinition);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    @Test
    public void updateSubstitutionFilterTest() throws BusinessLogicException {
        componentInstance.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));
        when(nodeFilterValidator.validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.UPDATE)).thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        when(substitutionFilterOperation.updateSubstitutionFilter(anyString(), anyString(),
            any(SubstitutionFilterDataDefinition.class), anyList()))
            .thenReturn(Either.left(substitutionFilterDataDefinition));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<SubstitutionFilterDataDefinition> result = componentSubstitutionFilterBusinessLogic
            .updateSubstitutionFilter(componentId, componentInstanceId, Collections.singletonList(constraint),
                true, ComponentTypeEnum.SERVICE);

        assertThat(result).isPresent();

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1)).validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.UPDATE);
    }

    @Test
    public void updateSubstitutionFilterFailTest() {
        componentInstance.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));
        when(nodeFilterValidator.validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.UPDATE)).thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        when(substitutionFilterOperation.updateSubstitutionFilter(anyString(), anyString(),
            any(SubstitutionFilterDataDefinition.class), anyList()))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        final List<String> constraints = requirementSubstitutionFilterPropertyDataDefinition.getConstraints();
        assertThrows(BusinessLogicException.class, () -> componentSubstitutionFilterBusinessLogic
            .updateSubstitutionFilter(componentId, componentInstanceId, constraints, true, ComponentTypeEnum.SERVICE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1)).validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.UPDATE);
    }

    @Test
    public void updateSubstitutionFilterFailWithFilterNotFoundTest() {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));
        when(nodeFilterValidator.validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.UPDATE)).thenReturn(Either.left(true));

        assertThrows(BusinessLogicException.class, () -> componentSubstitutionFilterBusinessLogic
            .updateSubstitutionFilter(componentId, componentInstanceId,
                requirementSubstitutionFilterPropertyDataDefinition.getConstraints(), true,
                ComponentTypeEnum.SERVICE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1)).validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.UPDATE);
    }

    @Test
    public void updateSubstitutionFilterFailValidationTest() {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));

        final UIConstraint uiConstraint =
            new UIConstraint("invalidProperty", constraintOperator, sourceType, sourceName, propertyValue);
        constraint = new ConstraintConvertor().convert(uiConstraint);
        requirementSubstitutionFilterPropertyDataDefinition.setConstraints(Collections.singletonList(constraint));

        when(responseFormat.getFormattedMessage()).thenReturn(ActionStatus.SELECTED_PROPERTY_NOT_PRESENT.name());

        when(nodeFilterValidator.validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.UPDATE))
            .thenReturn(Either.right(responseFormat));

        assertThrows(BusinessLogicException.class, () -> componentSubstitutionFilterBusinessLogic
            .updateSubstitutionFilter(componentId, componentInstanceId,
                requirementSubstitutionFilterPropertyDataDefinition.getConstraints(), true,
                ComponentTypeEnum.SERVICE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1)).validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.UPDATE);
    }

    @Test
    public void deleteSubstitutionFilterTest() throws BusinessLogicException {
        substitutionFilterDataDefinition.setProperties(new ListDataDefinition<>());
        componentInstance.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));
        when(nodeFilterValidator.validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.DELETE)).thenReturn(Either.left(true));

        when(nodeFilterValidator.validateComponentInstanceExist(service, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(substitutionFilterOperation.deleteConstraint(anyString(), anyString(),
            any(SubstitutionFilterDataDefinition.class), anyInt()))
            .thenReturn(Either.left(substitutionFilterDataDefinition));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<SubstitutionFilterDataDefinition> result = componentSubstitutionFilterBusinessLogic
            .deleteSubstitutionFilter(componentId, componentInstanceId, NodeFilterConstraintAction.DELETE, constraint,
                0, true, ComponentTypeEnum.SERVICE);

        assertThat(result).isPresent();
        assertThat(result.get().getProperties().getListToscaDataDefinition()).hasSize(0);
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1)).validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.DELETE);
        verify(nodeFilterValidator, times(1)).validateComponentInstanceExist(service, componentInstanceId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(substitutionFilterOperation, times(1)).deleteConstraint(componentId, componentInstanceId,
            substitutionFilterDataDefinition, 0);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    @Test
    public void deleteSubstitutionFilterFailTest() {
        componentInstance.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));
        when(nodeFilterValidator.validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.DELETE)).thenReturn(Either.left(true));

        when(nodeFilterValidator.validateComponentInstanceExist(service, componentInstanceId))
            .thenReturn(Either.left(true));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(substitutionFilterOperation.deleteConstraint(anyString(), anyString(),
            any(SubstitutionFilterDataDefinition.class), anyInt()))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentSubstitutionFilterBusinessLogic
            .deleteSubstitutionFilter(componentId, componentInstanceId, NodeFilterConstraintAction.DELETE, constraint,
                0, true, ComponentTypeEnum.SERVICE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1)).validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.DELETE);
        verify(nodeFilterValidator, times(1)).validateComponentInstanceExist(service, componentInstanceId);
        verify(substitutionFilterOperation, times(1)).deleteConstraint(componentId, componentInstanceId,
            substitutionFilterDataDefinition, 0);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    @Test
    public void deleteSubstitutionFilterFailValidationTest() {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(service));
        when(nodeFilterValidator.validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.DELETE)).thenReturn(Either.left(true));

        when(nodeFilterValidator.validateComponentInstanceExist(service, componentInstanceId))
            .thenReturn(Either.left(true));

        assertThrows(BusinessLogicException.class, () -> componentSubstitutionFilterBusinessLogic
            .deleteSubstitutionFilter(componentId, componentInstanceId, NodeFilterConstraintAction.DELETE, constraint,
                0, true, ComponentTypeEnum.SERVICE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(nodeFilterValidator, times(1)).validateFilter(service, componentInstanceId,
            Collections.singletonList(constraint), NodeFilterConstraintAction.DELETE);
    }

    public void initResource() {
        try {
            service = new Service();
            service.setName("MyTestService");
            service.setUniqueId(componentId);

            componentInstance = new ComponentInstance();
            componentInstance.setUniqueId(componentInstanceId);
            componentInstance.setName("myComponentInstance");
            componentInstance.setDirectives(ConfigurationManager.getConfigurationManager().getConfiguration()
                .getDirectives());

            final UIConstraint uiConstraint =
                new UIConstraint(servicePropertyName, constraintOperator, sourceType, sourceName, propertyValue);
            constraint = new ConstraintConvertor().convert(uiConstraint);

            requirementSubstitutionFilterPropertyDataDefinition = new RequirementSubstitutionFilterPropertyDataDefinition();
            requirementSubstitutionFilterPropertyDataDefinition.setName(uiConstraint.getServicePropertyName());
            requirementSubstitutionFilterPropertyDataDefinition
                .setConstraints(Collections.singletonList(constraint));

            final ListDataDefinition<RequirementSubstitutionFilterPropertyDataDefinition> listDataDefinition =
                new ListDataDefinition<>(
                    Collections.singletonList(requirementSubstitutionFilterPropertyDataDefinition));

            substitutionFilterDataDefinition = new SubstitutionFilterDataDefinition();
            substitutionFilterDataDefinition.setProperties(listDataDefinition);
            substitutionFilterDataDefinition.setID("SUBSTITUTION_FILTER_UID");

            service.setComponentInstances(singletonList(componentInstance));

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

            service.setComponentInstancesProperties(componentInstanceProps);
            service.setProperties(new LinkedList<>(Arrays.asList(property)));
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }
}
