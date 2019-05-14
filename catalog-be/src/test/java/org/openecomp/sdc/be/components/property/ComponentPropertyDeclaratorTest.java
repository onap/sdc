/*
 * ============LICENSE_START=============================================================================================================
 * Copyright (c) 2019 <Company or Individual>.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 * ============LICENSE_END===============================================================================================================
 *
 */
package org.openecomp.sdc.be.components.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.utils.InputsBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ServiceBuilder;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

@RunWith(MockitoJUnitRunner.class)
public class ComponentPropertyDeclaratorTest {

    @InjectMocks
    private ComponentPropertyDeclarator testSubject;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    private static final String PROPERTY_UID = "propertyUid";
    private static final String SERVICE_UID = "serviceUid";
    private Service service;

    @Before
    public void init() {
        service = new ServiceBuilder().setUniqueId(SERVICE_UID).build();
    }


    @Test
    public void createDeclaredProperty_success() {
        PropertyDataDefinition propertyDataDefinition = getPropertyForDeclaration();
        PropertyDataDefinition declaredProperty = testSubject.createDeclaredProperty(propertyDataDefinition);

        assertTrue(Objects.nonNull(declaredProperty));
        assertEquals(propertyDataDefinition.getUniqueId(), declaredProperty.getUniqueId());
    }

    @Test
    public void updatePropertiesValues_success() {
        PropertyDataDefinition propertyForDeclaration = getPropertyForDeclaration();
        when(toscaOperationFacade.updatePropertyOfComponent(any(Component.class), any(PropertyDefinition.class)))
                .thenReturn(Either.left(new PropertyDefinition(propertyForDeclaration)));

        Either<List<PropertyDataDefinition>, StorageOperationStatus> updateEither =
                (Either<List<PropertyDataDefinition>, StorageOperationStatus>) testSubject
                                                                                       .updatePropertiesValues(service,
                                                                                               SERVICE_UID, Collections
                                                                                                                    .singletonList(
                                                                                                                            propertyForDeclaration));

        assertTrue(updateEither.isLeft());

        List<PropertyDataDefinition> properties = updateEither.left().value();
        assertTrue(CollectionUtils.isNotEmpty(properties));
        assertEquals(1, properties.size());
        assertEquals(propertyForDeclaration, properties.get(0));
    }

    @Test
    public void resolvePropertiesOwner_success() {
        Optional<Component> ownerCandidate = testSubject.resolvePropertiesOwner(service, SERVICE_UID);

        assertTrue(ownerCandidate.isPresent());
        assertEquals(service, ownerCandidate.get());
    }

    @Test
    public void addPropertiesListToInput_success() {
        InputDefinition input = InputsBuilder.create().setPropertyId(PROPERTY_UID).build();
        PropertyDataDefinition propertyForDeclaration = getPropertyForDeclaration();

        testSubject.addPropertiesListToInput(propertyForDeclaration, input);

        List<ComponentInstanceProperty> inputProperties = input.getProperties();
        assertTrue(CollectionUtils.isNotEmpty(inputProperties));
        assertEquals(1, inputProperties.size());
        assertEquals(propertyForDeclaration.getUniqueId(), inputProperties.get(0).getUniqueId());
    }

    private PropertyDataDefinition getPropertyForDeclaration() {
        return new PropertyDataDefinitionBuilder().setUniqueId(PROPERTY_UID).build();
    }
}