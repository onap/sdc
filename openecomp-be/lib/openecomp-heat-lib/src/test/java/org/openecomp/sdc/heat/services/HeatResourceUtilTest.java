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

/*
 *
 *  Copyright © 2017-2018 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 *
 */

package org.openecomp.sdc.heat.services;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;

public class HeatResourceUtilTest {

    private static final String ROLE = "role";

    @Test
    public void testEvaluateNetworkRoleFromResourceIdEmpty() {
        Assert.assertFalse(HeatResourceUtil.evaluateNetworkRoleFromResourceId(null, null).isPresent());
    }

    @Test
    public void testEvaluateNetworkRoleFromResourceIdNeutronExternal() {
        Optional<String> networkRole = HeatResourceUtil.evaluateNetworkRoleFromResourceId(
                "vm_type_1_role_port_1", HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource());

        Assert.assertTrue(networkRole.isPresent());
        Assert.assertEquals(networkRole.get(), ROLE);
    }

    @Test
    public void testEvaluateNetworkRoleFromResourceIdNeutronInternal() {
        Optional<String> networkRole = HeatResourceUtil.evaluateNetworkRoleFromResourceId(
                "vm_type_1_int_role_port_1", HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource());

        Assert.assertTrue(networkRole.isPresent());
        Assert.assertEquals(networkRole.get(), ROLE);
    }

    @Test
    public void testEvaluateNetworkRoleFromResourceIdVMIExternal() {
        Optional<String> networkRole = HeatResourceUtil.evaluateNetworkRoleFromResourceId(
                "vm_type_1_role_vmi_1", HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE
                        .getHeatResource());

        Assert.assertTrue(networkRole.isPresent());
        Assert.assertEquals(networkRole.get(), ROLE);
    }

    @Test
    public void testEvaluateNetworkRoleFromResourceIdVMIInternal() {
        Optional<String> networkRole = HeatResourceUtil.evaluateNetworkRoleFromResourceId(
                "vm_type_1_int_role_vmi_1", HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE
                        .getHeatResource());

        Assert.assertTrue(networkRole.isPresent());
        Assert.assertEquals(networkRole.get(), ROLE);
    }

    @Test
    public void testExtractNetworkRoleFromSubInterfaceIdEmpty() {
        Assert.assertFalse(HeatResourceUtil.extractNetworkRoleFromSubInterfaceId(null, null).isPresent());
    }

    @Test
    public void testExtractNetworkRoleFromSubInterfaceId() {
        Optional<String> networkRole = HeatResourceUtil.extractNetworkRoleFromSubInterfaceId(
                "vm_type_1_subint_role_vmi_1", HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE
                        .getHeatResource());

        Assert.assertTrue(networkRole.isPresent());
        Assert.assertEquals(networkRole.get(), ROLE);
    }
}
