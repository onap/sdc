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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.validation.NodeFilterValidator;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.dto.FilterConstraintDto;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.SubstitutionFilterOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.ui.mapper.FilterConstraintMapper;
import org.openecomp.sdc.be.ui.model.UIConstraint;

@ExtendWith(MockitoExtension.class)
class ComponentSubstitutionFilterBusinessLogicTest extends BaseBusinessLogicMock {

    private static final String servicePropertyName = "controller_actor";
    private static final String constraintOperator = "equal";
    private static final String sourceType = "static";
    private static final String sourceName = sourceType;
    private static final String propertyValue = "constraintValue";
    private static final String componentId = "dac65869-dfb4-40d2-aa20-084324659ec1";

    @InjectMocks
    private ComponentSubstitutionFilterBusinessLogic componentSubstitutionFilterBusinessLogic;
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
    private NodeFilterValidator nodeFilterValidator;

    private Component component;
    private SubstitutionFilterDataDefinition substitutionFilterDataDefinition;
    private SubstitutionFilterPropertyDataDefinition substitutionFilterPropertyDataDefinition;
    private String constraint;
    private FilterConstraintDto filterConstraintDto;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
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
    void doNotCreateSubstitutionFilterAsExistsTest() throws BusinessLogicException {
        component.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));

        final Optional<SubstitutionFilterDataDefinition> result = componentSubstitutionFilterBusinessLogic
                .createSubstitutionFilterIfNotExist(componentId, true, ComponentTypeEnum.SERVICE);
        assertThat(result).isPresent();
        assertThat(result.get().getProperties()).isEqualTo(substitutionFilterDataDefinition.getProperties());
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
    }

    @Test
    void createSubstitutionFilterIfNotExistTest() throws BusinessLogicException {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
                .thenReturn(StorageOperationStatus.OK);
        when(substitutionFilterOperation.createSubstitutionFilter(componentId))
                .thenReturn(Either.left(substitutionFilterDataDefinition));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
                .thenReturn(StorageOperationStatus.OK);

        final Optional<SubstitutionFilterDataDefinition> result = componentSubstitutionFilterBusinessLogic
                .createSubstitutionFilterIfNotExist(componentId, true, ComponentTypeEnum.SERVICE);
        assertThat(result).isPresent();
        assertThat(result.get().getProperties()).isEqualTo(substitutionFilterDataDefinition.getProperties());
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(substitutionFilterOperation, times(1)).createSubstitutionFilter(componentId);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    @Test
    void createSubstitutionFilterIfNotExistFailTest() {
        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
                .thenReturn(StorageOperationStatus.OK);
        when(substitutionFilterOperation.createSubstitutionFilter(componentId))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
                .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentSubstitutionFilterBusinessLogic
                .createSubstitutionFilterIfNotExist(componentId, true, ComponentTypeEnum.SERVICE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(substitutionFilterOperation, times(1)).createSubstitutionFilter(componentId);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    @Test
    void addSubstitutionFilterTest() throws BusinessLogicException {
        component.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterValidator.validateSubstitutionFilter(component, filterConstraintDto)).thenReturn(Either.left(true));
        when(substitutionFilterOperation
            .addPropertyFilter(anyString(), any(SubstitutionFilterDataDefinition.class),
                any(SubstitutionFilterPropertyDataDefinition.class)))
            .thenReturn(Either.left(substitutionFilterDataDefinition));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<SubstitutionFilterDataDefinition> result = componentSubstitutionFilterBusinessLogic
                .addSubstitutionFilter(componentId, filterConstraintDto, true, ComponentTypeEnum.SERVICE);

        assertThat(result).isPresent();
        assertThat(result.get().getProperties().getListToscaDataDefinition()).hasSize(1);
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(nodeFilterValidator, times(1)).validateSubstitutionFilter(component, filterConstraintDto);
        verify(substitutionFilterOperation, times(1))
            .addPropertyFilter(anyString(), any(SubstitutionFilterDataDefinition.class),
                    any(SubstitutionFilterPropertyDataDefinition.class));
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);

    }

    @Test
    void addSubstitutionFilterFailTest() {
        component.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterValidator.validateSubstitutionFilter(component, filterConstraintDto)).thenReturn(Either.left(true));
        when(substitutionFilterOperation
            .addPropertyFilter(componentId, substitutionFilterDataDefinition,
                substitutionFilterPropertyDataDefinition))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));

        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentSubstitutionFilterBusinessLogic
                .addSubstitutionFilter(componentId, filterConstraintDto, true, ComponentTypeEnum.SERVICE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(nodeFilterValidator, times(1)).validateSubstitutionFilter(component, filterConstraintDto);
        verify(substitutionFilterOperation, times(0))
            .addPropertyFilter(componentId, substitutionFilterDataDefinition,
                substitutionFilterPropertyDataDefinition);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    @Test
    void updateSubstitutionFilterTest() throws BusinessLogicException {
        component.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterValidator.validateSubstitutionFilter(component, List.of(filterConstraintDto))).thenReturn(Either.left(true));
        when(substitutionFilterOperation.updatePropertyFilters(anyString(), any(SubstitutionFilterDataDefinition.class), anyList()))
                .thenReturn(Either.left(substitutionFilterDataDefinition));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<SubstitutionFilterDataDefinition> result = componentSubstitutionFilterBusinessLogic
            .updateSubstitutionFilter(componentId, List.of(filterConstraintDto), true, ComponentTypeEnum.SERVICE);

        assertThat(result).isPresent();
        assertThat(result.get().getProperties().getListToscaDataDefinition()).hasSize(1);
        verify(substitutionFilterOperation, times(1))
                .updatePropertyFilters(anyString(), any(SubstitutionFilterDataDefinition.class), anyList());
        verify(nodeFilterValidator, times(1)).validateSubstitutionFilter(component, List.of(filterConstraintDto));
        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    @Test
    void updateSubstitutionFilterFailTest() {
        component.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(nodeFilterValidator.validateSubstitutionFilter(component, List.of(filterConstraintDto))).thenReturn(Either.left(true));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentSubstitutionFilterBusinessLogic
            .updateSubstitutionFilter(componentId, List.of(filterConstraintDto), true, ComponentTypeEnum.SERVICE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(nodeFilterValidator, times(1)).validateSubstitutionFilter(component, List.of(filterConstraintDto));
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    @Test
    void deleteSubstitutionFilterTest() throws BusinessLogicException {
        substitutionFilterDataDefinition.setProperties(new ListDataDefinition<>());
        component.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(substitutionFilterOperation.deleteConstraint(anyString(), any(SubstitutionFilterDataDefinition.class), anyInt()))
            .thenReturn(Either.left(substitutionFilterDataDefinition));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        final Optional<SubstitutionFilterDataDefinition> result = componentSubstitutionFilterBusinessLogic
                .deleteSubstitutionFilter(componentId, anyInt(), true, ComponentTypeEnum.SERVICE);

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(substitutionFilterOperation, times(1)).deleteConstraint(componentId,
                substitutionFilterDataDefinition, 0);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    @Test
    void deleteSubstitutionFilterFailTest() {
        component.setSubstitutionFilter(substitutionFilterDataDefinition);

        when(toscaOperationFacade.getToscaElement(componentId)).thenReturn(Either.left(component));
        when(graphLockOperation.lockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);
        when(substitutionFilterOperation.deleteConstraint(anyString(),
            any(SubstitutionFilterDataDefinition.class), anyInt()))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(graphLockOperation.unlockComponent(componentId, NodeTypeEnum.Service))
            .thenReturn(StorageOperationStatus.OK);

        assertThrows(BusinessLogicException.class, () -> componentSubstitutionFilterBusinessLogic
                .deleteSubstitutionFilter(componentId, anyInt(),true, ComponentTypeEnum.SERVICE));

        verify(toscaOperationFacade, times(1)).getToscaElement(componentId);
        verify(graphLockOperation, times(1)).lockComponent(componentId, NodeTypeEnum.Service);
        verify(substitutionFilterOperation, times(1)).deleteConstraint(componentId,
                substitutionFilterDataDefinition, 0);
        verify(graphLockOperation, times(1)).unlockComponent(componentId, NodeTypeEnum.Service);
    }

    public void initResource() {
        try {
            component = new Service();
            component.setName("MyTestService");
            component.setUniqueId(componentId);

            final UIConstraint uiConstraint =
                new UIConstraint(servicePropertyName, constraintOperator, sourceType, sourceName, propertyValue);
            final FilterConstraintMapper filterConstraintMapper = new FilterConstraintMapper();
            filterConstraintDto = filterConstraintMapper.mapFrom(uiConstraint);
            constraint = new ConstraintConvertor().convert(uiConstraint);

            substitutionFilterPropertyDataDefinition = new SubstitutionFilterPropertyDataDefinition();
            substitutionFilterPropertyDataDefinition.setName(uiConstraint.getServicePropertyName());
            substitutionFilterPropertyDataDefinition
                .setConstraints(List.of(filterConstraintMapper.mapTo(filterConstraintDto)));

            final ListDataDefinition<SubstitutionFilterPropertyDataDefinition> listDataDefinition =
                new ListDataDefinition<>(
                    Collections.singletonList(substitutionFilterPropertyDataDefinition));

            substitutionFilterDataDefinition = new SubstitutionFilterDataDefinition();
            substitutionFilterDataDefinition.setProperties(listDataDefinition);
            substitutionFilterDataDefinition.setID("SUBSTITUTION_FILTER_UID");

            final PropertyDefinition property = new PropertyDefinition();
            property.setName(uiConstraint.getServicePropertyName());

            component.setProperties(new LinkedList<>(Arrays.asList(property)));
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }
}
