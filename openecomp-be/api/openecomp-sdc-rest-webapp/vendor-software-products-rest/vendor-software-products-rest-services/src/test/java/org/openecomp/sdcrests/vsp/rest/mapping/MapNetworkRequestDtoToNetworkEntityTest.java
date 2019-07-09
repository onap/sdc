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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NetworkRequestDto;

/**
 * This class was generated.
 */
public class MapNetworkRequestDtoToNetworkEntityTest {

    @Test()
    public void testConversion() {

        final NetworkRequestDto source = new NetworkRequestDto();

        final String name = "a15dd471-62cf-4702-841b-bd14865f646f";
        source.setName(name);

        final boolean dhcp = true;
        source.setDhcp(dhcp);

        final NetworkEntity target = new NetworkEntity();
        final MapNetworkRequestDtoToNetworkEntity mapper = new MapNetworkRequestDtoToNetworkEntity();
        mapper.doMapping(source, target);

        Network network = target.getNetworkCompositionData();
        assertEquals(name, network.getName());
        assertEquals(dhcp, network.isDhcp());
    }
}
