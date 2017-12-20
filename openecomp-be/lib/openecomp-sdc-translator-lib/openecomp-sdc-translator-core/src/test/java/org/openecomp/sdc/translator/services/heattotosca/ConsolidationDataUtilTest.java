package org.openecomp.sdc.translator.services.heattotosca;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ConsolidationDataUtilTest {
    private static final String PORT_TYPE_FORMAT_1 = "a_11_network_port_22";
    private static final String PORT_TYPE_FORMAT_2 = "a_11_network_port22";
    private static final String PORT_TYPE_FORMAT_3 = "a_network_port_22";
    private static final String PORT_TYPE_FORMAT_4 = "a_network_port22";
    private static final String PORT_TYPE_FORMAT_5 = "network_port_22";
    private static final String PORT_TYPE_FORMAT_6 = "network_port22";
    private static final String PORT_TYPE_FORMAT_7 = "a_network_11_port22";
    private static final String PORT_TYPE_OUTPUT_1 = "a_network_port";
    private static final String PORT_TYPE_OUTPUT_2 = "network_port";
    private static final String PORT_TYPE_OUTPUT_3 = "a_network_11_port";

    @Test
    public void testGetPortType_Empty() throws Exception {
        String port = "";
        assertEquals(ConsolidationDataUtil.getPortType(port), port);
    }

    @Test
    public void testGetPortType_Spaces() throws Exception {
        String port = "   ";
        assertEquals(ConsolidationDataUtil.getPortType(port), port);
    }

    @Test
    public void testGetPortType_Null() throws Exception {
        String port = null;
        assertEquals(ConsolidationDataUtil.getPortType(port), port);
    }

    @Test
    public void testGetPortType_OnlyPortType() throws Exception {
        String port = "network";
        assertEquals(ConsolidationDataUtil.getPortType(port), port);
    }

    @Test
    public void testGetPortType_WithServerAndPortIndex() throws Exception {
        assertEquals(ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_1), PORT_TYPE_OUTPUT_1);
    }

    @Test
    public void testGetPortType_Input_WithServerAndPortIndexWithoutUnderscore() throws Exception {
        assertEquals(ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_2), PORT_TYPE_OUTPUT_1);
    }

    @Test
    public void testGetPortType_Input_WithoutServerIndexAndWithPortIndex() throws Exception {
        assertEquals(ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_3), PORT_TYPE_OUTPUT_1);
    }

    @Test
    public void testGetPortType_Input_WithoutServerIndexAndWithPortIndexWithoutUnderscore() throws Exception {
        assertEquals(ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_4), PORT_TYPE_OUTPUT_1);
    }

    @Test
    public void testGetPortType_Input_PortTypeWithIndex() throws Exception {
        assertEquals(ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_5), PORT_TYPE_OUTPUT_2);
    }

    @Test
    public void testGetPortType_Input_PortIndexWithoutUnderscore() throws Exception {
        assertEquals(ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_6), PORT_TYPE_OUTPUT_2);
    }

    @Test
    public void testGetPortType_Input_PortIndexAndDigitInBetween() throws Exception {
        assertEquals(ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_7), PORT_TYPE_OUTPUT_3);
    }
}
