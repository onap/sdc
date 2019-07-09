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

import java.util.Map;
import org.junit.Test;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDescriptionDto;
import org.openecomp.sdcrests.vsp.rest.services.VspItemProperty;

/**
 * This class was generated.
 */
public class MapVspDescriptionDtoToItemTest {

    @Test()
    public void testConversion() {

        final VspDescriptionDto source = new VspDescriptionDto();

        final String name = "992d877f-90c7-4d67-b431-e2b761ca954c";
        source.setName(name);

        final String description = "af946014-eb47-4c98-a9f8-e3b43bbfe4e8";
        source.setDescription(description);

        final String vendorId = "20f7944e-ae84-4604-b597-f4c14ee413cc";
        source.setVendorId(vendorId);

        final String vendorName = "8fac7a9d-b801-47d4-a482-e21ee6558873";
        source.setVendorName(vendorName);

        final Item target = new Item();

        final MapVspDescriptionDtoToItem mapper = new MapVspDescriptionDtoToItem();
        mapper.doMapping(source, target);

        assertEquals(name, target.getName());
        assertEquals(description, target.getDescription());
        Map<String, Object> properties = target.getProperties();
        assertEquals(vendorId, properties.get(VspItemProperty.VENDOR_ID));
        assertEquals(vendorName, properties.get(VspItemProperty.VENDOR_NAME));
    }
}
