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

package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.junit.Test;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.be.model.UploadNodeFilterCapabilitiesInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterPropertyInfo;

public class NodeFilterUploadCreatorTest {

    @Test
    public void testSampleNodeFiler4Properties() {
        String nodeFilterStr = "        properties:\n        - str:\n            equal: str\n"
                                       + "        - int:\n            equal: 3\n        - str:\n            equal:\n"
                                       + "              get_property:\n              - yyyyy_proxy 1\n              - str\n"
                                       + "        - str:\n            equal:\n              get_property:\n"
                                       + "              - testFilter\n              - xxx";
        final Object o = new YamlUtil().yamlToObject(nodeFilterStr, Object.class);
        final UploadNodeFilterInfo nodeFilterData = new NodeFilterUploadCreator().createNodeFilterData(o);
        assertNotNull(nodeFilterData);
        final Map<String, UploadNodeFilterCapabilitiesInfo> capabilities = nodeFilterData.getCapabilities();
        assertTrue(MapUtils.isEmpty(capabilities));
        final List<UploadNodeFilterPropertyInfo> properties = nodeFilterData.getProperties();
        assertEquals(4, properties.size());
    }

    @Test
    public void testSampleNodeFiler1Property() {
        String nodeFilterStr = "                       properties:\n"
                                       + "                         - TimeOut: [{ less_or_equal : { get_input: TimeOutFilter } }]";
        final Object o = new YamlUtil().yamlToObject(nodeFilterStr, Object.class);
        final UploadNodeFilterInfo nodeFilterData = new NodeFilterUploadCreator().createNodeFilterData(o);
        assertNotNull(nodeFilterData);
        final Map<String, UploadNodeFilterCapabilitiesInfo> capabilities = nodeFilterData.getCapabilities();
        assertTrue(MapUtils.isEmpty(capabilities));
        final List<UploadNodeFilterPropertyInfo> properties = nodeFilterData.getProperties();
        assertEquals(1, properties.size());
    }

}
