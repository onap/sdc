package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.merge.input.DeclaredInputsResolver;
import org.openecomp.sdc.be.components.merge.input.InputsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.components.utils.AnnotationBuilder;
import org.openecomp.sdc.be.components.utils.InputsBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.utils.Conditions.hasPropertiesWithNames;

@RunWith(MockitoJUnitRunner.class)
public class ComponentInstanceInputsRedeclareHandlerTest {

    private static final String RESOURCE_ID = "resourceID";
    private ComponentInstanceInputsRedeclareHandler testInstance;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private DeclaredInputsResolver declaredInputsResolver;
    @Mock
    private InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogic;
    @Captor
    private ArgumentCaptor<Map<String, List<PropertyDataDefinition>>> getInputPropertiesCaptor;
    private Resource  currContainer;
    private List<InputDefinition> prevDeclaredInputs;
    private Annotation annotation1, annotation2, annotation3;

    @Before
    public void setUp() throws Exception {
        testInstance = new ComponentInstanceInputsRedeclareHandler(declaredInputsResolver, toscaOperationFacade, new ComponentsUtils(mock(AuditingManager.class)), inputsValuesMergingBusinessLogic);
        currContainer = new ResourceBuilder()
                .addInstanceProperty("inst1", "prop1")
                .addInstanceProperty("inst1", "prop2")
                .addInstanceInput("inst1", "prop1", Collections.singletonList(new GetInputValueDataDefinition()))
                .addInstanceInput("inst1", "prop2", Collections.singletonList(new GetInputValueDataDefinition()))
                .setUniqueId(RESOURCE_ID)
                .build();

        annotation1 = AnnotationBuilder.create()
                .setName("annotation1")
                .build();

        annotation2 = AnnotationBuilder.create()
                .setName("annotation2")
                .build();

        annotation3 = AnnotationBuilder.create()
                .setName("annotation3")
                .build();

        InputDefinition declaredInput1 = InputsBuilder.create()
                .setPropertyId("prop1")
                .setName("input1")
                .addAnnotation(annotation1)
                .addAnnotation(annotation2)
                .build();

        InputDefinition declaredInput2 = InputsBuilder.create()
                .setPropertyId("prop2")
                .setName("input2")
                .addAnnotation(annotation3)
                .build();

        prevDeclaredInputs = asList(declaredInput1, declaredInput2);
    }

    @Test
    public void redeclareOnlyPropertiesForGivenInstance() {
        Resource originInstanceType = new Resource();
        when(declaredInputsResolver.getPreviouslyDeclaredInputsToMerge(anyList(), eq(currContainer), getInputPropertiesCaptor.capture())).thenReturn(prevDeclaredInputs);
        when(toscaOperationFacade.updateInputsToComponent(prevDeclaredInputs, RESOURCE_ID)).thenReturn(Either.left(null));
        ActionStatus actionStatus = testInstance.redeclareComponentInputsForInstance(currContainer, "inst1", originInstanceType, Collections.emptyList());
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyInstanceSpecificPropertiesPassedToDeclaredInputsResolver();
    }

    @Test
    public void updateInputsWithAnnotationsFromOriginInstanceType() {
        InputDefinition input1 = InputsBuilder.create()
                .addAnnotation(annotation2)
                .addAnnotation(annotation3)
                .setName("prop1")
                .build();

        InputDefinition input2 = InputsBuilder.create()
                .setName("prop2")
                .build();
        Resource originInstanceType = new ResourceBuilder()
                .addInput(input1)
                .addInput(input2)
                .build();

        when(declaredInputsResolver.getPreviouslyDeclaredInputsToMerge(anyList(), eq(currContainer), getInputPropertiesCaptor.capture())).thenReturn(prevDeclaredInputs);
        when(toscaOperationFacade.updateInputsToComponent(prevDeclaredInputs, RESOURCE_ID)).thenReturn(Either.left(null));
        ActionStatus actionStatus = testInstance.redeclareComponentInputsForInstance(currContainer, "inst1", originInstanceType, Collections.emptyList());
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        assertThat(prevDeclaredInputs)
                .extracting("annotations")
                .containsExactlyInAnyOrder(asList(annotation1, annotation3, annotation2), asList(annotation3));
    }

    private void verifyInstanceSpecificPropertiesPassedToDeclaredInputsResolver() {
        Map<String, List<PropertyDataDefinition>> allResourceProps = getInputPropertiesCaptor.getValue();
        assertThat(allResourceProps)
                .hasEntrySatisfying("inst1", hasPropertiesWithNames("prop1", "prop2"));
    }
}