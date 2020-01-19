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

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
public class ComponentInstancePropsAndInputsMergeTest {

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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        resourceToUpdate =  new ResourceBuilder().addInstanceInput(INSTANCE_ID1, "instInput1")
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
    }

    @Test
    public void mergeDataAfterCreate() throws Exception {
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

        when(toscaOperationFacade.getToscaElement(Mockito.eq("resourceId"), parametersViewCaptor.capture())).thenReturn(Either.left(resourceToUpdate));
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(oldInstInputs, oldInputs, resourceToUpdate, INSTANCE_ID1)).thenReturn(ActionStatus.OK);
        when(componentInstancePropertiesMergeBL.mergeComponentInstanceProperties(oldInstProps, oldInputs, resourceToUpdate, INSTANCE_ID1)).thenReturn(ActionStatus.OK);
        when(componentInstanceInputsRedeclareHandler.redeclareComponentInputsForInstance(resourceToUpdate, INSTANCE_ID1, currInstanceOriginType, oldInputs)).thenReturn(ActionStatus.OK);
        Component mergeResult = testInstance.mergeDataAfterCreate(USER, dataForMergeHolder, resourceToUpdate, INSTANCE_ID1);
        assertEquals(mergeResult, resourceToUpdate);
        assertComponentFilter(parametersViewCaptor.getValue());
    }

    @Test(expected = ComponentException.class)
    public void mergeDataAfterCreate_failedToMergeComponentInstanceInputs() throws Exception {
        ResponseFormat errorResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(errorResponse);
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(anyList(), anyList(), any(Component.class), anyString())).thenReturn(ActionStatus.GENERAL_ERROR);
        testInstance.mergeDataAfterCreate(USER, new DataForMergeHolder(), new Service(), "inst1");
        verifyZeroInteractions(componentInstanceInputsRedeclareHandler, componentInstancePropertiesMergeBL, toscaOperationFacade);
    }

    @Test(expected = ComponentException.class)
    public void mergeDataAfterCreate_failedToMergeComponentInstProps() throws Exception {
        ResponseFormat errorResponse = new ResponseFormat();
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(anyList(), anyList(), any(Component.class), anyString())).thenReturn(ActionStatus.OK);
        when(componentInstancePropertiesMergeBL.mergeComponentInstanceProperties(anyList(), anyList(), any(Component.class), anyString())).thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(errorResponse);
        testInstance.mergeDataAfterCreate(USER, new DataForMergeHolder(), new Service(), "inst1");
        verifyZeroInteractions(componentInstanceInputsRedeclareHandler, toscaOperationFacade);
    }


    @Test(expected = ComponentException.class)
    public void mergeDataAfterCreate_mergeInputs_FailedToFetchResource() throws Exception {
        ResponseFormat errorResponse = new ResponseFormat();
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(anyList(), anyList(), any(Component.class), anyString())).thenReturn(ActionStatus.OK);
        when(componentInstancePropertiesMergeBL.mergeComponentInstanceProperties(anyList(), anyList(), any(Component.class), anyString())).thenReturn(ActionStatus.OK);
        when(toscaOperationFacade.getToscaElement(any(), any(ComponentParametersView.class))).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(errorResponse);
        DataForMergeHolder dataHolder = new DataForMergeHolder();
        dataHolder.setOrigComponentInputs(ObjectGenerator.buildInputs("input1", "input2"));
        testInstance.mergeDataAfterCreate(USER, dataHolder, new Service(), "inst1");
        verifyZeroInteractions(componentInstanceInputsRedeclareHandler);
    }

    private void assertComponentFilter(ComponentParametersView value) {
        assertFalse(value.isIgnoreComponentInstances());
        assertFalse(value.isIgnoreComponentInstancesProperties());
        assertFalse(value.isIgnoreComponentInstancesInputs());
        assertFalse(value.isIgnoreArtifacts());
    }
}
