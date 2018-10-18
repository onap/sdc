package org.onap.sdc.tosca.datatypes.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class CapabilityDefinitionTest {


    CapabilityDefinition capabilityDefinition;

    @Before
    public void initialize() {
        capabilityDefinition = new CapabilityDefinition();
    }

    @Test
    public void testClone() {
        CapabilityDefinition cap2 = capabilityDefinition.clone();
        assertIfObjectsAreSame(cap2, capabilityDefinition);
    }

    private void assertIfObjectsAreSame(CapabilityDefinition expected, CapabilityDefinition actual) {
        assertEquals(expected.getAttributes(), actual.getAttributes());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getOccurrences(), actual.getOccurrences());
        assertEquals(expected.getProperties(), actual.getProperties());
        assertEquals(expected.getType(), actual.getType());
    }
}