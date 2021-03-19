/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.heat.datatypes.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

public class HeatResourcesTypesTest {

    @Test
    public void findByHeatResourceTest() {
        assertEquals(HeatResourcesTypes.findByHeatResource("OS::Nova::ServerGroup"),
                HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE);
    }

    @Test
    public void isResourceExpectedToBeExposedTest() {
        assertEquals(true, HeatResourcesTypes.isResourceExpectedToBeExposed("OS::Nova::ServerGroup"));
        assertEquals(true, HeatResourcesTypes.isResourceExpectedToBeExposed("OS::Contrail::VirtualNetwork"));
        assertEquals(true, HeatResourcesTypes.isResourceExpectedToBeExposed("OS::Neutron::Net"));
        assertEquals(true, HeatResourcesTypes.isResourceExpectedToBeExposed("OS::Cinder::Volume"));
        assertEquals(true, HeatResourcesTypes.isResourceExpectedToBeExposed("OS::Neutron::SecurityGroup"));
        assertEquals(false, HeatResourcesTypes.isResourceExpectedToBeExposed("OS::Nova::Server"));
    }

    @Test
    public void isResourceTypeValidTest() {
        assertEquals(true, HeatResourcesTypes.isResourceTypeValid("OS::Neutron::SecurityGroup"));
        assertEquals(false, HeatResourcesTypes.isResourceTypeValid("OS::Neutron::test"));
    }

    @Test
    public void getListForResourceTypeTest() {
        Map<HeatResourcesTypes, List<String>> res = HeatResourcesTypes.getListForResourceType(
                HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE, HeatResourcesTypes.HEAT_CLOUD_CONFIG_TYPE);

        assertNotNull(res.get(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE));
        assertNotNull(res.get(HeatResourcesTypes.HEAT_CLOUD_CONFIG_TYPE));
        assertNull(res.get(HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE));
    }

}
