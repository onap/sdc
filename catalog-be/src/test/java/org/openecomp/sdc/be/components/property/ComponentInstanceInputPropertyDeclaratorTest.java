package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.utils.AnnotationBuilder;
import org.openecomp.sdc.be.components.utils.InputsBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.MockGenerator.mockComponentUtils;
import static org.openecomp.sdc.be.MockGenerator.mockExceptionUtils;

@RunWith(MockitoJUnitRunner.class)
public class ComponentInstanceInputPropertyDeclaratorTest extends PropertyDeclaratorTestBase {


    private ComponentInstanceInputPropertyDeclarator testInstance;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Captor
    private ArgumentCaptor<ComponentParametersView> inputsFilterCaptor;

    private Annotation annotation1, annotation2;

    private static final String PROPERTY_UID = "propertyUid";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testInstance = new ComponentInstanceInputPropertyDeclarator(mockComponentUtils(), null,
                toscaOperationFacade, null, mockExceptionUtils());
        annotation1 =  AnnotationBuilder.create()
                .setType("annotationType1")
                .setName("annotation1")
                .addProperty("prop1")
                .addProperty("prop2")
                .build();

        annotation2 =  AnnotationBuilder.create()
                .setType("annotationType2")
                .setName("annotation2")
                .addProperty("prop3")
                .build();
    }

    @Test
    public void whenDeclaredPropertyOriginalInputContainsAnnotation_createNewInputWithSameAnnotations() {
        List<PropertyDataDefinition> properties = Collections.singletonList(prop1);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        Component originInstanceNodeType = createComponentWithInputAndAnnotation(prop1.getName());
        when(toscaOperationFacade.addComponentInstanceInputsToComponent(eq(resource), anyMap())).thenReturn(Either.left(Collections.emptyMap()));
        when(toscaOperationFacade.getToscaElement(eq(ORIGIN_INSTANCE_ID), inputsFilterCaptor.capture())).thenReturn(Either.left(originInstanceNodeType));
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = testInstance.declarePropertiesAsInputs(resource, "inst1", propsToDeclare);
        List<InputDefinition> inputs = createdInputs.left().value();
        assertThat(inputs).hasSize(1);
        verifyInputAnnotations(inputs.get(0));
        assertThat(inputsFilterCaptor.getValue().isIgnoreInputs()).isFalse();
    }

    @Test
    public void throwExceptionWhenFailingToGetInstanceOriginType() {
        List<PropertyDataDefinition> properties = Collections.singletonList(prop1);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        when(toscaOperationFacade.getToscaElement(eq(ORIGIN_INSTANCE_ID), inputsFilterCaptor.capture())).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        assertThatExceptionOfType(StorageException.class).isThrownBy(() -> testInstance.declarePropertiesAsInputs(resource, "inst1", propsToDeclare));
    }

    @Test
    public void testCreateDeclaredProperty() {
        ComponentInstanceInput declaredProperty = testInstance.createDeclaredProperty(prop1);
        assertThat(declaredProperty.getUniqueId()).isEqualTo(prop1.getUniqueId());
    }

    @Test
    public void testUpdateDeclaredProperties() {
        List<ComponentInstanceInput> expectedProperties = Collections.singletonList(new ComponentInstanceInput(prop1));
        Map<String, List<ComponentInstanceInput>> expectedMap = new HashMap<>();
        expectedMap.put(prop1.getName(), expectedProperties);

        when(toscaOperationFacade.addComponentInstanceInputsToComponent(eq(resource), anyMap()))
                .thenReturn(Either.left(expectedMap));

        Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> updateEither =
                (Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus>) testInstance.updatePropertiesValues(resource, resource.getUniqueId(), expectedProperties);

        assertThat(updateEither.isLeft());
        Map<String, List<ComponentInstanceInput>> actualProperties = updateEither.left().value();
        assertThat(actualProperties.values().size()).isEqualTo(expectedProperties.size());
        assertThat(actualProperties.values().iterator().next()).isEqualTo(expectedProperties);
    }

    @Test
    public void testResolvePropertiesOwner() {
        Optional<ComponentInstance> componentInstance = testInstance.resolvePropertiesOwner(resource, INSTANCE_ID);

        assertThat(componentInstance.isPresent());
        assertThat(componentInstance.get().getUniqueId()).isEqualTo(INSTANCE_ID);
    }

    private void verifyInputAnnotations(InputDefinition inputDefinition) {
        List<Annotation> annotations = inputDefinition.getAnnotations();
        assertThat(annotations)
                .containsExactlyInAnyOrder(annotation1, annotation2);
    }

    private Component createComponentWithInputAndAnnotation(String inputName) {
        InputDefinition input = InputsBuilder.create()
                .setName(inputName)
                .addAnnotation(annotation1)
                .addAnnotation(annotation2)
                .build();
        return new ResourceBuilder()
                .addInput(input)
                .build();
    }

}