package org.openecomp.sdc.logging.api;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author EVITALIY
 * @since 08 Jan 18
 */
public class ServiceBinderTest {

    @Test
    public void testGetContextServiceBinding() {
        assertFalse(ServiceBinder.getContextServiceBinding().isPresent());
    }

    @Test
    public void testGetCreationServiceBinding() {
        assertFalse(ServiceBinder.getCreationServiceBinding().isPresent());
    }
}