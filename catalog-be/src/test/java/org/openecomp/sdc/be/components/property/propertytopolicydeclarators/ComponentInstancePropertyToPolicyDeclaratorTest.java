package org.openecomp.sdc.be.components.property.propertytopolicydeclarators;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class ComponentInstancePropertyToPolicyDeclaratorTest {

    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private PropertyBusinessLogic propertyBl;
    @Mock
    private ComponentInstanceBusinessLogic componentInstanceBl;
    @InjectMocks
    private ComponentInstancePropertyToPolicyDeclarator declarator;
    @Captor
    private ArgumentCaptor<ComponentInstanceProperty> captor;

    private static Service service;
    private static ComponentInstance componentInstance;
    private static PropertyDataDefinition prop1;
    private static final String PROP_1_NAME = "prop1";
    private static final String NON_EXIST_PROP_NAME = "prop1";
    private static final String COMPONENT_INSTANCE_ID = "ciId";
    private static final String NON_EXIST_ID = "nonExistId";


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        service = new Service();
        componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(COMPONENT_INSTANCE_ID);
        service.setComponentInstances(Collections.singletonList(componentInstance));

        prop1 = new PolicyDataDefinition();
        prop1.setName(PROP_1_NAME);
    }

    @Test
    public void testCreateDeclaredProperty_success() {
        ComponentInstanceProperty declaredProperty = declarator.createDeclaredProperty(prop1);
        Assert.assertEquals(prop1.getName(), declaredProperty.getName());
    }

    @Test
    public void testUpdatePropertiesValues_success() {
        List<ComponentInstanceProperty> properties = Arrays.asList(new ComponentInstanceProperty(prop1));
        Map<String, List<ComponentInstanceProperty>> expectedInstanceProperties =
                Collections.singletonMap(COMPONENT_INSTANCE_ID, properties);

        when(toscaOperationFacade
                     .addComponentInstancePropertiesToComponent(eq(service), eq(expectedInstanceProperties)))
                .thenReturn(Either.left(expectedInstanceProperties));


        Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> updateEither =
                (Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus>) declarator
                                                                                                       .updatePropertiesValues(
                                                                                                               service,
                                                                                                               COMPONENT_INSTANCE_ID,
                                                                                                               properties);

        Assert.assertTrue(updateEither.isLeft());
        Map<String, List<ComponentInstanceProperty>> actualInstanceProperties = updateEither.left().value();
        validateUpdateResult(properties, expectedInstanceProperties, actualInstanceProperties);
    }

    @Test
    public void testResolvePropertiesOwner_success() {
        Optional<ComponentInstance> componentInstanceCandidate =
                declarator.resolvePropertiesOwner(service, COMPONENT_INSTANCE_ID);

        Assert.assertTrue(componentInstanceCandidate.isPresent());
        Assert.assertEquals(componentInstanceCandidate.get(), componentInstance);
    }

    @Test
    public void testResolvePropertiesOwner_failure() {
        Optional<ComponentInstance> componentInstanceCandidate =
                declarator.resolvePropertiesOwner(service, NON_EXIST_ID);

        Assert.assertFalse(componentInstanceCandidate.isPresent());
    }

    @Test
    public void testUnDeclarePropertiesAsPolicies_success() {
        PolicyDefinition policyDefinition = new PolicyDefinition();
        policyDefinition.setName(PROP_1_NAME);

        when(componentInstanceBl.getComponentInstancePropertyByPolicyId(eq(service), eq(policyDefinition)))
                .thenReturn(Optional.of(new ComponentInstanceProperty(prop1)));
        when(toscaOperationFacade
                     .updateComponentInstanceProperty(any(), any(), captor.capture()))
                .thenReturn(StorageOperationStatus.OK);

        StorageOperationStatus status =
                declarator.unDeclarePropertiesAsPolicies(service, policyDefinition);

        Assert.assertEquals(status, StorageOperationStatus.OK);

        ComponentInstanceProperty actualProperty = captor.getValue();
        Assert.assertEquals(prop1.getName(), actualProperty.getName());
    }

    private void validateUpdateResult(List<ComponentInstanceProperty> properties,
            Map<String, List<ComponentInstanceProperty>> expectedInstanceProperties,
            Map<String, List<ComponentInstanceProperty>> actualInstanceProperties) {
        Assert.assertEquals(expectedInstanceProperties.size(), actualInstanceProperties.size());
        Assert.assertEquals(1, actualInstanceProperties.size());
        Assert.assertEquals(expectedInstanceProperties.keySet(), actualInstanceProperties.keySet());

        List<ComponentInstanceProperty> actualComponentInstanceProperties =
                actualInstanceProperties.get(COMPONENT_INSTANCE_ID);

        Assert.assertEquals(properties, actualComponentInstanceProperties);
    }
}