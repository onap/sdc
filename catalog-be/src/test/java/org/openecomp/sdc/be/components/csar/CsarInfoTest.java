/*-
 * ============LICENSE_START===============================================
 * ONAP SDC
 * ========================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=================================================
 */

package org.openecomp.sdc.be.components.csar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.util.ZipUtil;

@RunWith(MockitoJUnitRunner.class)
public class CsarInfoTest {

    private CsarInfo csarInfo;

    @Mock
    private User user;

    private static final String CSAR_UUID = "csarUUID";
    private static final String PAYLOAD_NAME = "mock_service.csar";
    private static final String RESOURCE_NAME = "resourceName";
    private static final String MAIN_TEMPLATE_NAME = "Definitions/tosca_mock_vf.yaml";

    private static final String NEW_NODE_NAME = "new_db";
    private static final String NODE_TYPE = "tosca.nodes.Compute";
    private static final String DELIVER_FOR = "tosca.nodes.Root";

    @Before
    public void setup() throws IOException, URISyntaxException {

        // given
        Map<String, byte[]> payload = loadPayload(PAYLOAD_NAME);
        String main_template_content = new String(payload.get(MAIN_TEMPLATE_NAME));

        csarInfo = new CsarInfo(user, CSAR_UUID, payload, RESOURCE_NAME,
                MAIN_TEMPLATE_NAME, main_template_content, true);
    }

    @Test
    public void add2TimesTheSameNodeTest() {

        try {
            // when
            csarInfo.addNodeToQueue(NEW_NODE_NAME);
            csarInfo.addNodeToQueue(NEW_NODE_NAME);
            fail("AddNodeToQueue not throw the exception!");
        } catch (ComponentException e) {
            List<String> expectParam = Arrays.asList(NEW_NODE_NAME, RESOURCE_NAME);

            // then
            assertEquals(ActionStatus.CFVC_LOOP_DETECTED, e.getActionStatus());
            assertTrue(Arrays.stream(e.getParams()).allMatch(expectParam::contains));
        }
    }

    @Test
    public void addMultipleTimesNodeTest() {

        // when
        csarInfo.addNodeToQueue(NEW_NODE_NAME);
        csarInfo.removeNodeFromQueue();
        csarInfo.addNodeToQueue(NEW_NODE_NAME);
    }

    @Test
    public void csarCheckNodeTypesInfoTest() {

        // when
        Map<String, NodeTypeInfo> nodeTypeInfoMap = csarInfo.extractNodeTypesInfo();
        NodeTypeInfo nodeTypeInfo = nodeTypeInfoMap.get(NODE_TYPE);

        // then
        assertNotNull(nodeTypeInfo);
        assertTrue(nodeTypeInfo.getDerivedFrom().contains(DELIVER_FOR));
        assertEquals(NODE_TYPE, nodeTypeInfo.getType());

        assertEquals(MAIN_TEMPLATE_NAME, csarInfo.getMainTemplateName());
        assertEquals(csarInfo.getMainTemplateName(), nodeTypeInfo.getTemplateFileName());
    }

    private Map<String, byte[]> loadPayload(String payloadName) throws IOException, URISyntaxException {

        Path path = Paths.get(getClass().getResource("/" + payloadName).toURI());
        byte[] data = Files.readAllBytes(path);

        return ZipUtil.readZip(data);
    }
}
