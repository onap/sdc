package org.openecomp.sdc.action.types;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ActionTest {

    Action action = new Action();

    @Before
    public void setup() {
        action.setActionUuId("123");
        action.setActionInvariantUuId("123345");
        action.setData("long data");
        action.setName("Action Name");
    }

    @Test
    public void testActionUuid() {
        assertEquals(action.getActionUuId(), "123");
    }

    @Test
    public void testActionInvariantUuid() {
        assertEquals(action.getActionInvariantUuId(), "12345");
    }

    @Test
    public void testData() {
        assertEquals(action.getData(), "long data");
    }

    @Test
    public void testName() {
        assertEquals(action.getName(), "Action Name");
    }
}