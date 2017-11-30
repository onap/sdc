package org.openecomp.sdc.be.components.merge.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.merge.input.ComponentInputsMergeBL;
import org.openecomp.sdc.be.components.merge.property.ComponentInstanceInputsMergeBL;
import org.openecomp.sdc.be.components.merge.property.ComponentInstancePropertiesMergeBL;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
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
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

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
    private ComponentInputsMergeBL componentInputsMergeBL;

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

        ArgumentCaptor<ComponentParametersView> parametersViewCaptor = ArgumentCaptor.forClass(ComponentParametersView.class);

        when(toscaOperationFacade.getToscaElement(Mockito.eq("resourceId"), parametersViewCaptor.capture())).thenReturn(Either.left(resourceToUpdate));
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(oldInstInputs, oldInputs, resourceToUpdate, INSTANCE_ID1)).thenReturn(ActionStatus.OK);
        when(componentInstancePropertiesMergeBL.mergeComponentInstanceProperties(oldInstProps, oldInputs, resourceToUpdate, INSTANCE_ID1)).thenReturn(ActionStatus.OK);
        when(componentInputsMergeBL.redeclareComponentInputsForInstance(oldInputs, resourceToUpdate, INSTANCE_ID1)).thenReturn(ActionStatus.OK);
        Either<Component, ResponseFormat> mergeResult = testInstance.mergeDataAfterCreate(USER, dataForMergeHolder, resourceToUpdate, INSTANCE_ID1);
        assertEquals(mergeResult.left().value(), resourceToUpdate);
        assertComponentFilter(parametersViewCaptor.getValue());
    }

    @Test
    public void mergeDataAfterCreate_failedToMergeComponentInstanceInputs() throws Exception {
        ResponseFormat errorResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(errorResponse);
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(anyListOf(ComponentInstanceInput.class), anyListOf(InputDefinition.class), any(Component.class), anyString())).thenReturn(ActionStatus.GENERAL_ERROR);
        Either<Component, ResponseFormat> mergeResult = testInstance.mergeDataAfterCreate(USER, new DataForMergeHolder(), new Service(), "inst1");
        assertEquals(errorResponse, mergeResult.right().value());
        verifyZeroInteractions(componentInputsMergeBL, componentInstancePropertiesMergeBL, toscaOperationFacade);
    }

    @Test
    public void mergeDataAfterCreate_failedToMergeComponentInstProps() throws Exception {
        ResponseFormat errorResponse = new ResponseFormat();
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(anyList(), anyList(), any(Component.class), anyString())).thenReturn(ActionStatus.OK);
        when(componentInstancePropertiesMergeBL.mergeComponentInstanceProperties(anyList(), anyList(), any(Component.class), anyString())).thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(errorResponse);
        Either<Component, ResponseFormat> mergeResult = testInstance.mergeDataAfterCreate(USER, new DataForMergeHolder(), new Service(), "inst1");
        assertEquals(errorResponse, mergeResult.right().value());
        verifyZeroInteractions(componentInputsMergeBL, toscaOperationFacade);
    }

    @Test
    public void mergeDataAfterCreate_mergeInputs_FailedToFetchResource() throws Exception {
        ResponseFormat errorResponse = new ResponseFormat();
        when(componentInstanceInputsMergeBL.mergeComponentInstanceInputs(anyList(), anyList(), any(Component.class), anyString())).thenReturn(ActionStatus.OK);
        when(componentInstancePropertiesMergeBL.mergeComponentInstanceProperties(anyList(), anyList(), any(Component.class), anyString())).thenReturn(ActionStatus.OK);
        when(toscaOperationFacade.getToscaElement(any(), any(ComponentParametersView.class))).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(errorResponse);
        DataForMergeHolder dataHolder = new DataForMergeHolder();
        dataHolder.setOrigComponentInputs(ObjectGenerator.buildInputs("input1", "input2"));
        Either<Component, ResponseFormat> mergeResult = testInstance.mergeDataAfterCreate(USER, dataHolder, new Service(), "inst1");
        assertEquals(errorResponse, mergeResult.right().value());
        verifyZeroInteractions(componentInputsMergeBL);
    }

    private void assertComponentFilter(ComponentParametersView value) {
        assertFalse(value.isIgnoreComponentInstances());
        assertFalse(value.isIgnoreComponentInstancesProperties());
        assertFalse(value.isIgnoreComponentInstancesInputs());
        assertFalse(value.isIgnoreArtifacts());
    }
}