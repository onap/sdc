/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.config.CategoryBaseTypeConfig;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.exception.ResponseFormat;

class GenericTypeBusinessLogicTest {

    @InjectMocks
    private GenericTypeBusinessLogic testInstance;

    @Mock
    private ToscaOperationFacade toscaOperationFacadeMock;

    @Mock
    private ComponentsUtils componentsUtils;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testInstance = new GenericTypeBusinessLogic(componentsUtils, toscaOperationFacadeMock);
        final var configurationManager = new ConfigurationManager();

        final var configuration = new Configuration();
        CategoryBaseTypeConfig categoryBaseTypeConfig1 = new CategoryBaseTypeConfig();
        categoryBaseTypeConfig1.setRequired(true);
        CategoryBaseTypeConfig categoryBaseTypeConfig2 = new CategoryBaseTypeConfig();
        categoryBaseTypeConfig2.setRequired(false);
        Map<String, CategoryBaseTypeConfig> serviceBaseNodeTypeMap = Map.of(
            "category1", categoryBaseTypeConfig1,
            "category2", categoryBaseTypeConfig2
        );
        configuration.setServiceBaseNodeTypes(serviceBaseNodeTypeMap);
        configurationManager.setConfiguration(configuration);
    }

    @Test
    void fetchDerivedFromGenericType_cvfv_getGenericResourceTypeFromDerivedFrom() {
        Resource cvfc = new Resource();
        cvfc.setResourceType(ResourceTypeEnum.CVFC);
        cvfc.setDerivedFrom(Arrays.asList("genericType", "someOtherType"));
        cvfc.setDerivedFromGenericType("genericType");
        Resource genericResource = new Resource();
        when(toscaOperationFacadeMock.getLatestCertifiedNodeTypeByToscaResourceName(cvfc.getDerivedFromGenericType()))
            .thenReturn(Either.left(genericResource));
        Either<Resource, ResponseFormat> fetchedGenericType = testInstance.fetchDerivedFromGenericType(cvfc);
        assertEquals(genericResource, fetchedGenericType.left().value());
    }

    @Test
    void fetchDerivedFromGenericType_getGenericResourceTypeFromConfiguration() {
        Resource resource = mock(Resource.class);
        when(resource.getResourceType()).thenReturn(ResourceTypeEnum.VF);
        final var genericType = "genericType";
        when(resource.fetchGenericTypeToscaNameFromConfig()).thenReturn(genericType);
        Resource genericResource = new Resource();
        when(toscaOperationFacadeMock.getLatestCertifiedNodeTypeByToscaResourceName(genericType)).thenReturn(Either.left(genericResource));
        Either<Resource, ResponseFormat> fetchedGenericType = testInstance.fetchDerivedFromGenericType(resource);
        assertEquals(genericResource, fetchedGenericType.left().value());
    }

    @Test
    void generateInputsFromGenericTypeProperties() {
        Resource genericNodeType = new Resource();
        genericNodeType.setUniqueId("genericUid");
        PropertyDefinition propertyDefinition = generatePropDefinition("prop1");
        PropertyDefinition propertyDefinition2 = generatePropDefinition("prop2");

        genericNodeType.setProperties(Arrays.asList(propertyDefinition, propertyDefinition2));

        List<InputDefinition> genericInputs = testInstance.generateInputsFromGenericTypeProperties(genericNodeType);
        assertEquals(2, genericInputs.size());
        assertInput(genericInputs.get(0), propertyDefinition);
        assertInput(genericInputs.get(1), propertyDefinition2);
    }

    @Test
    void generateInputsFromGenericTypeProperties_genericHasNoProps() {
        Resource genericNodeType = new Resource();
        assertTrue(testInstance.generateInputsFromGenericTypeProperties(genericNodeType).isEmpty());
    }

    @Test
    void hasMandatorySubstitutionForServiceTest() {
        final var component = mock(Component.class);
        when(component.isService()).thenReturn(true);
        final var categoryDefinition = new CategoryDefinition();
        categoryDefinition.setName("category1");
        when(component.getCategories()).thenReturn(List.of(categoryDefinition));
        assertTrue(testInstance.hasMandatorySubstitutionType(component));
    }

    @Test
    void hasMandatorySubstitutionForServiceWithNotRequiredCategoryTest() {
        final var component = mock(Component.class);
        when(component.isService()).thenReturn(true);
        final var categoryDefinition = new CategoryDefinition();
        categoryDefinition.setName("category2");
        when(component.getCategories()).thenReturn(List.of(categoryDefinition));
        assertFalse(testInstance.hasMandatorySubstitutionType(component));
    }

    @Test
    void hasMandatorySubstitutionForServiceWithNotConfiguredCategoryTest() {
        final var component = mock(Component.class);
        when(component.isService()).thenReturn(true);
        final var categoryDefinition = new CategoryDefinition();
        categoryDefinition.setName("category3");
        when(component.getCategories()).thenReturn(List.of(categoryDefinition));
        assertTrue(testInstance.hasMandatorySubstitutionType(component));
    }

    @Test
    void hasMandatorySubstitutionForServiceWithNonExistentConfigTest() {
        final var configurationManager = new ConfigurationManager();
        configurationManager.setConfiguration(new Configuration());
        final var component = mock(Component.class);
        when(component.isService()).thenReturn(true);
        assertTrue(testInstance.hasMandatorySubstitutionType(component));
    }

    @Test
    void hasMandatorySubstitutionServiceMissingCategoryTest() {
        final Component component = mock(Component.class);
        when(component.isService()).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> testInstance.hasMandatorySubstitutionType(component));
    }

    @Test
    void hasMandatorySubstitutionTypeForNonServiceComponentTest() {
        final Component component = mock(Component.class);
        when(component.isService()).thenReturn(false);
        assertTrue(testInstance.hasMandatorySubstitutionType(component));
    }

    private void assertInput(InputDefinition inputDefinition, PropertyDefinition propertyDefinition) {
        assertEquals("genericUid", inputDefinition.getOwnerId());
        assertEquals(inputDefinition.getName(), propertyDefinition.getName());
        assertNull(inputDefinition.getValue());
        assertNull(inputDefinition.getDefaultValue());
    }

    private PropertyDefinition generatePropDefinition(String name) {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(name);
        propertyDefinition.setValue(name + "value");
        return propertyDefinition;
    }

}
