/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019  Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.components.impl.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.model.UploadNodeFilterCapabilitiesInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterPropertyInfo;


class CINodeFilterUtilsTest {

    private final CINodeFilterUtils ciNodeFilterUtils = new CINodeFilterUtils();

    @Test
    void testNodeFilterDataDefinition() {
        UploadNodeFilterInfo uNodeFilterInfo = new UploadNodeFilterInfo();

        UploadNodeFilterPropertyInfo propertyInfo = new UploadNodeFilterPropertyInfo();
        propertyInfo.setName("prop1");
        List<UploadNodeFilterPropertyInfo> properties = new ArrayList<>();
        properties.add(propertyInfo);

        UploadNodeFilterCapabilitiesInfo capabilitiesInfo = new UploadNodeFilterCapabilitiesInfo();
        capabilitiesInfo.setName("cap1");
        capabilitiesInfo.setProperties(properties);

        Map<String, UploadNodeFilterCapabilitiesInfo> capabilities = new HashMap<>();
        capabilities.put("test", capabilitiesInfo);
        uNodeFilterInfo.setCapabilities(capabilities);

        CINodeFilterDataDefinition dataDefinition = ciNodeFilterUtils.getNodeFilterDataDefinition(uNodeFilterInfo, "id");
        assertEquals("id", dataDefinition.getID());
    }
}