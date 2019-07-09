/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.translator.services.heattotosca;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConsolidationDataUtilTest {
    private static final String PORT_TYPE_FORMAT_1 = "a_11_network_port_22";
    private static final String PORT_TYPE_FORMAT_2 = "a_11_network_port22";
    private static final String PORT_TYPE_FORMAT_3 = "a_network_port_22";
    private static final String PORT_TYPE_FORMAT_4 = "a_network_port22";
    private static final String PORT_TYPE_FORMAT_5 = "network_port_22";
    private static final String PORT_TYPE_FORMAT_6 = "network_port22";
    private static final String PORT_TYPE_FORMAT_7 = "a_network_11_port22";
    private static final String PORT_TYPE_OUTPUT_1 = "a_network_port_22";
    private static final String PORT_TYPE_OUTPUT_2 =  "a_network_port22";
    private static final String PORT_TYPE_OUTPUT_3 = "network_port_22";
    private static final String PORT_TYPE_OUTPUT_4 = "network_port22";
    private static final String PORT_TYPE_OUTPUT_5 = "a_network_11_port22";

    private static final String VM_TYPE = "a";

    @Test
    public void testGetPortType_Empty() {
        String port = "";
        assertEquals(ConsolidationDataUtil.getPortType(port, VM_TYPE), port);
    }

    @Test
    public void testGetPortType_Spaces() {
        String port = "   ";
        assertEquals(ConsolidationDataUtil.getPortType(port, VM_TYPE), port);
    }

    @Test
    public void testGetPortType_Null() {
        String port = null;
        assertEquals(ConsolidationDataUtil.getPortType(port, VM_TYPE), port);
    }

    @Test
    public void testGetPortType_OnlyPortType() {
        String port = "network";
        assertEquals(ConsolidationDataUtil.getPortType(port, VM_TYPE), port);
    }

    @Test
    public void testGetPortType_WithServerAndPortIndex() {
        assertEquals(PORT_TYPE_OUTPUT_1, ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_1, VM_TYPE));
    }

    @Test
    public void testGetPortType_Input_WithServerAndPortIndexWithoutUnderscore() {
        assertEquals(PORT_TYPE_OUTPUT_2, ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_2, VM_TYPE));
    }

    @Test
    public void testGetPortType_Input_WithoutServerIndexAndWithPortIndex() {
        assertEquals(PORT_TYPE_OUTPUT_1, ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_3, VM_TYPE));
    }

    @Test
    public void testGetPortType_Input_WithoutServerIndexAndWithPortIndexWithoutUnderscore() {
        assertEquals(PORT_TYPE_OUTPUT_2, ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_4, VM_TYPE));
    }

    @Test
    public void testGetPortType_Input_PortTypeWithIndex() {
        assertEquals(PORT_TYPE_OUTPUT_3, ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_5, VM_TYPE));
    }

    @Test
    public void testGetPortType_Input_PortIndexWithoutUnderscore() {
        assertEquals(PORT_TYPE_OUTPUT_4, ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_6, VM_TYPE));
    }

    @Test
    public void testGetPortType_Input_PortIndexAndDigitInBetween() {
        assertEquals(PORT_TYPE_OUTPUT_5, ConsolidationDataUtil.getPortType(PORT_TYPE_FORMAT_7, VM_TYPE));
    }
}
