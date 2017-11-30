package org.openecomp.sdc.be.components.impl.generic;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.common.api.ResourceType;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class GenericTypeBusinessLogicTest {

    @InjectMocks
    private GenericTypeBusinessLogic testInstance;

    @Mock
    private ToscaOperationFacade toscaOperationFacadeMock;

    @Before
    public void setUp() throws Exception {
        testInstance = new GenericTypeBusinessLogic();
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void fetchDerivedFromGenericType_cvfv_getGenericResourceTypeFromDerivedFrom() throws Exception {
        Resource cvfc = new Resource();
        cvfc.setResourceType(ResourceTypeEnum.CVFC);
        cvfc.setDerivedFrom(Arrays.asList("genericType", "someOtherType"));
        Resource genericResource = new Resource();
        when(toscaOperationFacadeMock.getLatestCertifiedNodeTypeByToscaResourceName("genericType")).thenReturn(Either.left(genericResource));
        Either<Resource, ResponseFormat> fetchedGenericType = testInstance.fetchDerivedFromGenericType(cvfc);
        assertEquals(genericResource, fetchedGenericType.left().value());
    }

    @Test
    public void fetchDerivedFromGenericType_getGenericResourceTypeFromConfiguration() throws Exception {
        Resource resource = Mockito.mock(Resource.class);
        when(resource.getResourceType()).thenReturn(ResourceTypeEnum.VF);
        when(resource.fetchGenericTypeToscaNameFromConfig()).thenReturn("genericType");
        Resource genericResource = new Resource();
        when(toscaOperationFacadeMock.getLatestCertifiedNodeTypeByToscaResourceName("genericType")).thenReturn(Either.left(genericResource));
        Either<Resource, ResponseFormat> fetchedGenericType = testInstance.fetchDerivedFromGenericType(resource);
        assertEquals(genericResource, fetchedGenericType.left().value());
    }

    @Test
    public void generateInputsFromGenericTypeProperties() throws Exception {
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
    public void generateInputsFromGenericTypeProperties_genericHasNoProps() throws Exception {
        Resource genericNodeType = new Resource();
        assertTrue(testInstance.generateInputsFromGenericTypeProperties(genericNodeType).isEmpty());
    }

    private void assertInput(InputDefinition inputDefinition, PropertyDefinition propertyDefinition) {
        assertEquals(inputDefinition.getOwnerId(), "genericUid");
        assertEquals(inputDefinition.getValue(), propertyDefinition.getValue());
        assertEquals(inputDefinition.getName(), propertyDefinition.getName());
    }

    private PropertyDefinition generatePropDefinition(String name) {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(name);
        propertyDefinition.setValue(name + "value");
        return propertyDefinition;
    }


}