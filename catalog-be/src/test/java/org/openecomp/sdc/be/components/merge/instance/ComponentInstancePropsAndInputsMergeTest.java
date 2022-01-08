/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.merge.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

class ComponentInstancePropsAndInputsMergeTest {

    private static final String INSTANCE_ID1 = "inst1";
    private static final User USER = new User();

    @InjectMocks
    private ComponentInstancePropsAndInputsMerge testInstance;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ComponentsUtils componentsUtils;

    @Mock
    private ComponentInstancePropertiesMergeBL componentInstancePropertiesMergeBL;

    @Mock
    private ComponentInstanceInputsMergeBL componentInstanceInputsMergeBL;

    @Mock
    private ComponentInstanceInputsRedeclareHandler componentInstanceInputsRedeclareHandler;

    private Resource resourceToUpdate;

    private DataForMergeHolder oldDataHolder;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        resourceToUpdate = new ResourceBuilder().addInstanceInput(INSTANCE_ID1, "instInput1")
            .addInstanceInput(INSTANCE_ID1, "instInput2")
            .addInstanceProperty(INSTANCE_ID1, "instProp1")
            .addInstanceProperty(INSTANCE_ID1, "instProp2")
            .addInput("input1")
            .addInput("input2")
            .setUniqueId("resourceId").build();

        List<InputDefinition> oldInputs = ObjectGenerator.buildInputs("input1");
        List<ComponentInstanceProperty> oldInstProps = ObjectGenerator.buildInstanceProperties("instProp1", "instProp3");
        List<ComponentInstanceInput> oldInstInputs = ObjectGenerator.buildInstanceInputs("instInput1", "instInput3");

        oldDataHolder = new DataForMergeHolder();
        oldDataHolder.setOrigComponentInputs(oldInputs);
        oldDataHolder.setOrigComponentInstanceProperties(oldInstProps);
        oldDataHolder.setOrigComponentInstanceInputs(oldInstInputs);
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Test
    void mergeDataAfterCreate() {
        List<InputDefinition> oldInputs = ObjectGenerator.buildInputs("input1");
        List<ComponentInstanceProperty> oldInstProps = ObjectGenerator.buildInstanceProperties("instProp1", "instProp3");
        List<ComponentInstanceInput> oldInstInputs = ObjectGenerator.buildInstanceInputs("instInput1", "instInput3");

        DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
        dataForMergeHolder.setOrigComponentInputs(oldInputs);
        dataForMergeHolder.setOrigComponentInstanceProperties(oldInstProps);
        dataForMergeHolder.setOrigComponentInstanceInputs(oldInstInputs);
        Resource currInstanceOriginType = new Resource();
        dataForMergeHolder.setCurrInstanceNode(currInstanceOriginType);

        ArgumentCaptor<ComponentParametersView> parametersViewCaptor = ArgumentCaptor.forClass(ComponentParametersView.class);

        when(toscaOperationFacade.getToscaElement(Mockito.eq("resourceId"), parametersViewCaptor.capture()))
            .thenReturn(Either.left(resourceToUpdate));
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(oldInstInputs, oldInputs, resourceToUpdate, INSTANCE_ID1))
            .thenReturn(ActionStatus.OK);
        when(componentInstancePropertiesMergeBL.mergeComponentInstanceProperties(oldInstProps, oldInputs, resourceToUpdate, INSTANCE_ID1))
            .thenReturn(ActionStatus.OK);
        when(componentInstanceInputsRedeclareHandler
            .redeclareComponentInputsForInstance(resourceToUpdate, INSTANCE_ID1, currInstanceOriginType, oldInputs)).thenReturn(ActionStatus.OK);
        Component mergeResult = testInstance.mergeDataAfterCreate(USER, dataForMergeHolder, resourceToUpdate, INSTANCE_ID1);
        assertEquals(mergeResult, resourceToUpdate);
        assertComponentFilter(parametersViewCaptor.getValue());
    }

    @Test
    void mergeDataAfterCreate_failedToMergeComponentInstanceInputs() {
        final ResponseFormat errorResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(errorResponse);
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(anyList(), anyList(), any(Component.class), anyString()))
            .thenReturn(ActionStatus.GENERAL_ERROR);
        final DataForMergeHolder dataHolder = new DataForMergeHolder();
        final Service service = new Service();
        assertThrows(ComponentException.class, () -> {
            testInstance.mergeDataAfterCreate(USER, dataHolder, service, "inst1");
        });
        verifyZeroInteractions(componentInstanceInputsRedeclareHandler, componentInstancePropertiesMergeBL, toscaOperationFacade);
    }

    @Test
    void mergeDataAfterCreate_failedToMergeComponentInstProps() {
        final ResponseFormat errorResponse = new ResponseFormat();
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(anyList(), anyList(), any(Component.class), anyString()))
            .thenReturn(ActionStatus.OK);
        when(componentInstancePropertiesMergeBL.mergeComponentInstanceProperties(anyList(), anyList(), any(Component.class), anyString()))
            .thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(errorResponse);
        final DataForMergeHolder dataHolder = new DataForMergeHolder();
        final Service service = new Service();
        assertThrows(ComponentException.class, () -> {
            testInstance.mergeDataAfterCreate(USER, dataHolder, service, "inst1");
        });
        verifyZeroInteractions(componentInstanceInputsRedeclareHandler, toscaOperationFacade);
    }

    @Test
    void mergeDataAfterCreate_mergeInputs_FailedToFetchResource() {
        final ResponseFormat errorResponse = new ResponseFormat();
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(anyList(), anyList(), any(Component.class), anyString()))
            .thenReturn(ActionStatus.OK);
        when(componentInstancePropertiesMergeBL.mergeComponentInstanceProperties(anyList(), anyList(), any(Component.class), anyString()))
            .thenReturn(ActionStatus.OK);
        when(toscaOperationFacade.getToscaElement(any(), any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(errorResponse);
        final DataForMergeHolder dataHolder = new DataForMergeHolder();
        dataHolder.setOrigComponentInputs(ObjectGenerator.buildInputs("input1", "input2"));
        final Service service = new Service();
        assertThrows(ComponentException.class, () -> {
            testInstance.mergeDataAfterCreate(USER, dataHolder, service, "inst1");
        });
        verifyZeroInteractions(componentInstanceInputsRedeclareHandler);
    }

    private void assertComponentFilter(ComponentParametersView value) {
        assertFalse(value.isIgnoreComponentInstances());
        assertFalse(value.isIgnoreComponentInstancesProperties());
        assertFalse(value.isIgnoreComponentInstancesInputs());
        assertFalse(value.isIgnoreArtifacts());
    }
}
