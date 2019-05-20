package org.openecomp.sdc.be.components.property;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.property.propertytopolicydeclarators.ComponentPropertyToPolicyDeclarator;
import org.openecomp.sdc.be.datatypes.elements.GetPolicyValueDataDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

@RunWith(MockitoJUnitRunner.class)
public class ComponentPropertyToPolicyDeclaratorTest extends PropertyDeclaratorTestBase {

    @InjectMocks
    ComponentPropertyToPolicyDeclarator declarator;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Captor
    ArgumentCaptor<PropertyDefinition> propertyCaptor;

    private static final String OWNER_ID = "ownerId";
    private static final String SERVICE_ID = "serviceId";
    private static final String PROPERTY_ID = "propertyId";
    private static final String POLICY = "policy";
    private static final String TEST_VALUE = "testValue";


    @Test
    public void testDeclarePropertyToPolicy_success() {
        Service service = new Service();
        service.setUniqueId(SERVICE_ID);

        when(toscaOperationFacade.updatePropertyOfComponent(eq(service), Mockito.any())).thenReturn(Either.left(new PropertyDefinition()));
        Either<List<PolicyDefinition>, StorageOperationStatus> declareEither = declarator.declarePropertiesAsPolicies(
                service, OWNER_ID, createInstancePropInputList(Collections.singletonList(prop1)));

        Assert.assertTrue(declareEither.isLeft());
    }

    @Test
    public void testUndeclarePolicy_success() {
        Service service = new Service();
        service.setUniqueId(SERVICE_ID);

        PolicyDefinition policyDefinition = createPolicyDefinition(PROPERTY_ID);

        PropertyDefinition expectedProperty = createPropertyWithDeclaredPolicy(getPolicyId(PROPERTY_ID));
        service.addProperty(expectedProperty);

        when(toscaOperationFacade.updatePropertyOfComponent(eq(service), propertyCaptor.capture())).thenReturn(Either.left(new PropertyDefinition()));

        StorageOperationStatus storageOperationStatus =
                declarator.unDeclarePropertiesAsPolicies(service, policyDefinition);

        PropertyDefinition actualProperty = propertyCaptor.getValue();

        Assert.assertEquals(storageOperationStatus, storageOperationStatus.OK);
        Assert.assertEquals(expectedProperty, actualProperty);

    }

    @Test
    public void shouldReturnOriginalPropertyValueAfterUndeclaring() {
        Service service = new Service();
        service.setUniqueId(SERVICE_ID);

        PropertyDefinition expectedProperty = new PropertyDefinition(prop1);
        addGetPolicyValueToProperty(getPolicyId(prop1.getUniqueId()), expectedProperty);
        service.addProperty(expectedProperty);

        when(toscaOperationFacade.updatePropertyOfComponent(eq(service), propertyCaptor.capture())).thenReturn(Either.left(new PropertyDefinition()));

        Either<List<PolicyDefinition>, StorageOperationStatus> declareEither = declarator.declarePropertiesAsPolicies(
                service, OWNER_ID, createInstancePropInputList(Collections.singletonList(prop1)));

        Assert.assertTrue(declareEither.isLeft());

        PolicyDefinition policyDefinition = createPolicyDefinition(prop1.getUniqueId());
        StorageOperationStatus storageOperationStatus =
                declarator.unDeclarePropertiesAsPolicies(service, policyDefinition);

        List<PropertyDefinition> actualProperties = propertyCaptor.getAllValues();

        Assert.assertEquals(storageOperationStatus, storageOperationStatus.OK);
        Assert.assertEquals(actualProperties.size(), 2);
        Assert.assertEquals(prop1.getValue(), actualProperties.get(1).getValue());
    }

    private PropertyDefinition createPropertyWithDeclaredPolicy(String policyId) {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setUniqueId(PROPERTY_ID);

        addGetPolicyValueToProperty(policyId, propertyDefinition);
        return propertyDefinition;
    }

    private void addGetPolicyValueToProperty(String policyId, PropertyDefinition propertyDefinition) {
        GetPolicyValueDataDefinition getPolicyValueDataDefinition = new GetPolicyValueDataDefinition();
        getPolicyValueDataDefinition.setPolicyId(policyId);
        getPolicyValueDataDefinition.setPropertyName(propertyDefinition.getUniqueId());
        getPolicyValueDataDefinition.setOrigPropertyValue(propertyDefinition.getValue());

        List<GetPolicyValueDataDefinition> getPolicyList = new ArrayList<>();
        getPolicyList.add(getPolicyValueDataDefinition);
        propertyDefinition.setGetPolicyValues(getPolicyList);
    }

    private PolicyDefinition createPolicyDefinition(String propertyId) {
        PolicyDefinition policyDefinition = new PolicyDefinition();
        String policyId = getPolicyId(propertyId);
        policyDefinition.setUniqueId(policyId);

        return policyDefinition;
    }

    private String getPolicyId(String propertyId) {
        return SERVICE_ID + "." + propertyId + "." + POLICY;
    }

}