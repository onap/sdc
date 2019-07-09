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
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicDto;

/**
 * This class was generated.
 */
public class MapNicToNicDtoTest {

    @Test()
    public void testConversion() {

        final Nic source = new Nic();

        final String name = "a15dd471-62cf-4702-841b-bd14865f646f";
        source.setName(name);

        final String description = "bf3b2713-5f3d-40f9-abf1-0248c69a0da3";
        source.setDescription(description);

        final String networkId = "c381c91c-d872-4a95-8f63-3f15170693b9";
        source.setNetworkId(networkId);

        final String networkName = "58efd7dd-3142-4fbe-8a67-ac136057c177";
        source.setNetworkName(networkName);

        final String networkDescription = "0fa4629d-bdd1-407e-b12c-0d6353b49857";
        source.setNetworkDescription(networkDescription);

        final NicDto target = new NicDto();
        final MapNicToNicDto mapper = new MapNicToNicDto();
        mapper.doMapping(source, target);

        assertEquals(name, target.getName());
        assertEquals(description, target.getDescription());
        assertEquals(networkId, target.getNetworkId());
        assertEquals(networkName, target.getNetworkName());
        assertEquals(networkDescription, target.getNetworkDescription());
    }
}
