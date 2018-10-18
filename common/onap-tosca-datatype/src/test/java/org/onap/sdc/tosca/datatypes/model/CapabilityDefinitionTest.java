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
        assertEquals(cap2, capabilityDefinition);
    }
}