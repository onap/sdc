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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.NetworkType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicRequestDto;

/**
 * This class was generated.
 */
public class MapNicRequestDtoToNicEntityTest {

    @Test()
    public void testConversion() {

        final NicRequestDto source = new NicRequestDto();
        final String name = "a15dd471-bd14865f646f";
        source.setName(name);

        final String description = "bf3b2713-0248c69a0da3";
        source.setDescription(description);

        final String networkId = "c381c91c-3f15170693b9";
        source.setNetworkId(networkId);

        final NetworkType networkType = NetworkType.Internal;
        source.setNetworkType(networkType.name());

        final String networkDescription = "0fa4629d-0d6353b49857";
        source.setNetworkDescription(networkDescription);

        final NicEntity target = new NicEntity();
        final MapNicRequestDtoToNicEntity mapper = new MapNicRequestDtoToNicEntity();
        mapper.doMapping(source, target);

        Nic nicCompositionData = target.getNicCompositionData();
        assertEquals(name, nicCompositionData.getName());
        assertEquals(description, nicCompositionData.getDescription());
        assertEquals(networkId, nicCompositionData.getNetworkId());
        assertEquals(networkType, nicCompositionData.getNetworkType());
        assertEquals(networkDescription, nicCompositionData.getNetworkDescription());
    }
}
