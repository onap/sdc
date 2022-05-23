/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import fj.data.Either;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
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
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

class ComponentInstanceAttributesAndOutputsMergeTest {

    private static final String INSTANCE_ID1 = "inst1";
    private static final User USER = new User();

    @InjectMocks
    private ComponentInstanceAttributesAndOutputsMerge testInstance;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ComponentsUtils componentsUtils;

    @Mock
    private ComponentInstanceAttributesMergeBL componentInstanceAttributesMergeBL;

    @Mock
    private ComponentInstanceOutputsRedeclareHandler componentInstanceOutputsRedeclareHandler;

    private Resource resourceToUpdate;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        resourceToUpdate = new ResourceBuilder()
            .addInstanceAttribute(INSTANCE_ID1, "instAttribute1")
            .addInstanceAttribute(INSTANCE_ID1, "instAttribute2")
            .addOutput("output1")
            .addOutput("output2")
            .setUniqueId("resourceId").build();

        List<OutputDefinition> oldOutputs = ObjectGenerator.buildOutputs("output1");
        List<ComponentInstanceAttribute> oldInstAttribute = ObjectGenerator.buildInstanceAttributes("instAttribute1", "instAttribute3");

        DataForMergeHolder oldDataHolder = new DataForMergeHolder();
        oldDataHolder.setOrigComponentOutputs(oldOutputs);
        oldDataHolder.setOrigComponentInstanceAttributes(oldInstAttribute);
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Test
    void mergeDataAfterCreate() {
        List<OutputDefinition> oldOutputs = ObjectGenerator.buildOutputs("output1");
        List<ComponentInstanceAttribute> oldInstAttributes = ObjectGenerator.buildInstanceAttributes("instAttribute1", "instAttribute3");
        List<ComponentInstanceAttribute> commonInstAttributes = ObjectGenerator.buildInstanceAttributes("instAttribute1");

        DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
        dataForMergeHolder.setOrigComponentOutputs(oldOutputs);
        dataForMergeHolder.setOrigComponentInstanceAttributes(oldInstAttributes);

        ArgumentCaptor<ComponentParametersView> parametersViewCaptor = ArgumentCaptor.forClass(ComponentParametersView.class);

       when(componentInstanceAttributesMergeBL.mergeComponentInstanceAttributes(anyList(), eq(resourceToUpdate), eq(INSTANCE_ID1)))
                .thenReturn(ActionStatus.OK);
        when(componentInstanceOutputsRedeclareHandler.redeclareComponentOutputsForInstance(any(), any(), anyList()))
                .thenReturn(ActionStatus.OK);
        when(toscaOperationFacade.getToscaElement(Mockito.eq("resourceId"), parametersViewCaptor.capture()))
                .thenReturn(Either.left(resourceToUpdate));
        Component mergeResult = testInstance.mergeDataAfterCreate(USER, dataForMergeHolder, resourceToUpdate, INSTANCE_ID1);
        assertEquals(mergeResult, resourceToUpdate);
        assertComponentFilter(parametersViewCaptor.getValue());
    }

    @Test
    void mergeDataAfterCreate_failedToMergeComponentInstanceOutputs() {
        List<OutputDefinition> oldOutputs = ObjectGenerator.buildOutputs("output1");
        List<ComponentInstanceAttribute> oldInstAttributes = ObjectGenerator.buildInstanceAttributes("instAttribute1", "instAttribute3");

        DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
        dataForMergeHolder.setOrigComponentOutputs(oldOutputs);
        dataForMergeHolder.setOrigComponentInstanceAttributes(oldInstAttributes);

        ArgumentCaptor<ComponentParametersView> parametersViewCaptor = ArgumentCaptor.forClass(ComponentParametersView.class);

        when(componentInstanceAttributesMergeBL.mergeComponentInstanceAttributes(anyList(), eq(resourceToUpdate), eq(INSTANCE_ID1)))
                .thenReturn(ActionStatus.OK);
        when(toscaOperationFacade.getToscaElement(Mockito.eq("resourceId"), parametersViewCaptor.capture()))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        assertThrows(ComponentException.class, () -> {
            testInstance.mergeDataAfterCreate(USER, dataForMergeHolder, resourceToUpdate, "inst1");
        });
        verifyNoInteractions(componentInstanceOutputsRedeclareHandler);
    }

    @Test
    void mergeDataAfterCreate_failedToMergeComponentInstAttributes() {
        final ResponseFormat errorResponse = new ResponseFormat();
        when(componentInstanceAttributesMergeBL.mergeComponentInstanceAttributes(anyList(), any(Component.class), anyString()))
            .thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(errorResponse);
        final DataForMergeHolder dataHolder = new DataForMergeHolder();
        final Service service = new Service();
        assertThrows(ComponentException.class, () -> {
            testInstance.mergeDataAfterCreate(USER, dataHolder, service, "inst1");
        });
        verifyNoInteractions(componentInstanceOutputsRedeclareHandler, toscaOperationFacade);
    }

    @Test
    void mergeDataAfterCreate_mergeInputs_FailedToFetchResource() {
        final ResponseFormat errorResponse = new ResponseFormat();
        when(componentInstanceAttributesMergeBL.mergeComponentInstanceAttributes(anyList(), any(Component.class), anyString()))
            .thenReturn(ActionStatus.OK);
        when(toscaOperationFacade.getToscaElement(any(), any(ComponentParametersView.class)))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(errorResponse);
        final DataForMergeHolder dataHolder = new DataForMergeHolder();
        dataHolder.setOrigComponentOutputs(ObjectGenerator.buildOutputs("output1", "output2"));
        final Service service = new Service();
        assertThrows(ComponentException.class, () -> {
            testInstance.mergeDataAfterCreate(USER, dataHolder, service, "inst1");
        });
        verifyNoInteractions(componentInstanceOutputsRedeclareHandler);
    }

    private void assertComponentFilter(ComponentParametersView value) {
        assertFalse(value.isIgnoreComponentInstances());
        assertFalse(value.isIgnoreComponentInstancesAttributes());
        assertFalse(value.isIgnoreOutputs());
        assertFalse(value.isIgnoreArtifacts());
    }
}
