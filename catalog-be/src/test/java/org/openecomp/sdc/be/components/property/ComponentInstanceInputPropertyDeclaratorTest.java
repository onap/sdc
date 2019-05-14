/*
 * Copyright © 2016-2019 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.components.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.MockGenerator.mockComponentUtils;
import static org.openecomp.sdc.be.MockGenerator.mockExceptionUtils;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
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
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;

@RunWith(MockitoJUnitRunner.class)
public class ComponentInstanceInputPropertyDeclaratorTest extends PropertyDeclaratorTestBase {


    private ComponentInstanceInputPropertyDeclarator testInstance;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @Mock
    private PropertyOperation propertyOperation;

    @Captor
    private ArgumentCaptor<ComponentParametersView> inputsFilterCaptor;

    private Annotation annotation1;

    private Annotation annotation2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testInstance = new ComponentInstanceInputPropertyDeclarator(mockComponentUtils(), propertyOperation,
                toscaOperationFacade, componentInstanceBusinessLogic, mockExceptionUtils());
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

    @Test
    public void unDeclarePropertiesAsListInputsTest() {
        InputDefinition inputToDelete = new InputDefinition();
        inputToDelete.setUniqueId(INPUT_ID);
        inputToDelete.setName(INPUT_ID);
        inputToDelete.setIsDeclaredListInput(true);

        Component component = createComponentWithListInput(INPUT_ID, "innerPropName");
        PropertyDefinition prop = new PropertyDataDefinitionBuilder()
                .setName("propName")
                .setValue(generateGetInputValueAsListInput(INPUT_ID, "innerPropName"))
                .setType("list")
                .setUniqueId("propName")
                .addGetInputValue(INPUT_ID)
                .build();
        component.setProperties(Collections.singletonList(prop));

        List<ComponentInstanceInput> ciPropList = new ArrayList<>();
        ComponentInstanceInput ciProp = new ComponentInstanceInput();
        List<String> pathOfComponentInstances = new ArrayList<>();
        pathOfComponentInstances.add("pathOfComponentInstances");
        ciProp.setPath(pathOfComponentInstances);
        ciProp.setUniqueId("componentInstanceId");
        ciProp.setDefaultValue("default value");
        ciProp.setComponentInstanceId("componentInstanceId");
        ciProp.setComponentInstanceName("componentInstanceName");
        ciProp.setValue(generateGetInputValueAsListInput(INPUT_ID, "innerPropName"));
        ciPropList.add(ciProp);

        when(componentInstanceBusinessLogic.getComponentInstanceInputsByInputId(eq(component), eq(INPUT_ID))).thenReturn(ciPropList);
        when(propertyOperation.findDefaultValueFromSecondPosition(eq(pathOfComponentInstances), eq(ciProp.getUniqueId()), eq(ciProp.getDefaultValue()))).thenReturn(Either.left(ciProp.getDefaultValue()));
        when(toscaOperationFacade.updateComponentInstanceInputs(eq(component), eq(ciProp.getComponentInstanceId()), eq(ciPropList))).thenReturn(StorageOperationStatus.OK);
        StorageOperationStatus storageOperationStatus = testInstance.unDeclarePropertiesAsListInputs(component, inputToDelete);

        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
    }

    @Test
    public void unDeclarePropertiesAsListInputsTest_whenNoListInput_returnOk() {
        InputDefinition input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");
        List<ComponentInstanceInput> resList = new ArrayList<>();
        when(componentInstanceBusinessLogic.getComponentInstanceInputsByInputId(eq(resource), eq(INPUT_ID))).thenReturn(resList);
        StorageOperationStatus status = testInstance.unDeclarePropertiesAsListInputs(resource, input);
        Assert.assertEquals(status, StorageOperationStatus.OK);
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

    private Component createComponentWithListInput(String inputName, String propName) {
        InputDefinition input = InputsBuilder.create()
                .setName(inputName)
                .build();

        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setDefaultValue("defaultValue");
        input.setValue(generateGetInputValueAsListInput(inputName, propName));

        return new ResourceBuilder()
                .setUniqueId(RESOURCE_ID)
                .addInput(input)
                .build();
    }
}