package org.openecomp.sdc.be.components.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.test.utils.InterfaceOperationTestUtils;

public class InterfaceOperationUtilsTest {

    private static final String TEST_RESOURCE_NAME = "TestResource";
    private static final String operationId = "operationId";
    private static final String operationName = "createOperation";
    private static final String interfaceId = "interfaceId";
    private static final String operationId1 = "operationId1";
    private static final String interfaceId1 = "interfaceId1";
    private static Resource resource;

    @Before
    public void setup() {
        resource =
                new ResourceBuilder().setComponentType(ComponentTypeEnum.RESOURCE).setName(TEST_RESOURCE_NAME).build();
        resource.setInterfaces(InterfaceOperationTestUtils.createMockInterfaceDefinitionMap(interfaceId, operationId,
                operationName));
    }

    @Test
    public void testGetInterfaceDefinitionFromComponentByInterfaceTypeSuccess() {
        Assert.assertTrue(
                InterfaceOperationUtils.getInterfaceDefinitionFromComponentByInterfaceType(resource, interfaceId)
                        .isPresent());
    }

    @Test
    public void testGetInterfaceDefinitionFromComponentByInterfaceTypeFailure() {
        Assert.assertFalse(
                InterfaceOperationUtils.getInterfaceDefinitionFromComponentByInterfaceType(resource, TEST_RESOURCE_NAME)
                        .isPresent());
    }

    @Test
    public void testGetInterfaceDefinitionFromComponentByInterfaceTypeNoInterface() {
        resource.getInterfaces().clear();
        Assert.assertFalse(
                InterfaceOperationUtils.getInterfaceDefinitionFromComponentByInterfaceType(resource, interfaceId)
                        .isPresent());
    }

    @Test
    public void testGetInterfaceDefinitionFromComponentByInterfaceIdSuccess() {
        Assert.assertTrue(
                InterfaceOperationUtils.getInterfaceDefinitionFromComponentByInterfaceId(resource, interfaceId)
                        .isPresent());
    }

    @Test
    public void testGetInterfaceDefinitionFromComponentByInterfaceIdFailure() {
        Assert.assertFalse(
                InterfaceOperationUtils.getInterfaceDefinitionFromComponentByInterfaceId(resource, TEST_RESOURCE_NAME)
                        .isPresent());
    }

    @Test
    public void testGetInterfaceDefinitionFromComponentByInterfaceIdNoInterface() {
        resource.getInterfaces().clear();
        Assert.assertFalse(
                InterfaceOperationUtils.getInterfaceDefinitionFromComponentByInterfaceId(resource, interfaceId)
                        .isPresent());
    }

    @Test
    public void testGetOperationFromInterfaceDefinitionSuccess() {
        Assert.assertTrue(InterfaceOperationUtils.getOperationFromInterfaceDefinition(
                InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName), operationId).isPresent());
    }

    @Test
    public void testGetOperationFromInterfaceDefinitionFailure() {
        Assert.assertFalse(InterfaceOperationUtils.getOperationFromInterfaceDefinition(
                InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName), TEST_RESOURCE_NAME)
                                   .isPresent());
    }

    @Test
    public void testGetOperationFromInterfaceDefinitionNoOperationMap() {
        InterfaceDefinition interfaceDefinition =
                InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName);
        interfaceDefinition.getOperations().clear();
        Optional<Map.Entry<String, Operation>> operationEntry =
                InterfaceOperationUtils.getOperationFromInterfaceDefinition(interfaceDefinition, TEST_RESOURCE_NAME);
        Assert.assertFalse(operationEntry.isPresent());
    }

    @Test
    public void testGetInterfaceDefinitionFromOperationIdSuccess() {
        List<InterfaceDefinition> interfaces = new ArrayList<>();
        interfaces.add(InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName));
        interfaces.add(InterfaceOperationTestUtils.createMockInterface(interfaceId1, operationId1, operationName));
        Assert.assertTrue(InterfaceOperationUtils.getInterfaceDefinitionFromOperationId(interfaces, operationId)
                                  .isPresent());
    }

    @Test
    public void testGetInterfaceDefinitionFromOperationIdFailure() {
        List<InterfaceDefinition> interfaces = new ArrayList<>();
        interfaces.add(InterfaceOperationTestUtils.createMockInterface(interfaceId1, operationId1, operationName));
        Assert.assertFalse(InterfaceOperationUtils.getInterfaceDefinitionFromOperationId(interfaces, operationId)
                                  .isPresent());
    }

    @Test
    public void testGetInterfaceDefinitionFromOperationIdFailureInterfacesEmpty() {
        List<InterfaceDefinition> interfaces = new ArrayList<>();
        Assert.assertFalse(InterfaceOperationUtils.getInterfaceDefinitionFromOperationId(interfaces, operationId)
                                   .isPresent());
    }

    @Test
    public void testIsArtifactInUse() {
        Assert.assertTrue(InterfaceOperationUtils.isArtifactInUse(resource, operationId1, "uniqId"));
        Assert.assertFalse(InterfaceOperationUtils.isArtifactInUse(resource, operationId1, "uniqId1"));
    }

}