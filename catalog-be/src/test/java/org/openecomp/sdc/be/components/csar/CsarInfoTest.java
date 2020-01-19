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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.config.NonManoArtifactType;
import org.openecomp.sdc.be.config.NonManoConfiguration;
import org.openecomp.sdc.be.config.NonManoFolderType;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

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
    public void setup() throws ZipException, URISyntaxException {
        // given
        final File csarFile = new File(CsarInfoTest.class.getClassLoader().getResource(PAYLOAD_NAME).toURI());
        final Map<String, byte[]> payload = ZipUtils.readZip(csarFile, false);
        String mainTemplateContent = new String(payload.get(MAIN_TEMPLATE_NAME));

        csarInfo = new CsarInfo(user, CSAR_UUID, payload, RESOURCE_NAME,
                MAIN_TEMPLATE_NAME, mainTemplateContent, true);
    }

    @Test
    public void add2TimesTheSameNodeTest() {

        try {
            // when
            csarInfo.addNodeToQueue(NEW_NODE_NAME);
            csarInfo.addNodeToQueue(NEW_NODE_NAME);
            fail("AddNodeToQueue not throw the exception!");
        } catch (ByActionStatusComponentException e) {
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

    @Test
    public void getSoftwareInformationPathTest() {
        final NonManoConfiguration nonManoConfigurationMock = Mockito.mock(NonManoConfiguration.class);
        final CsarInfo csarInfo = new CsarInfo(nonManoConfigurationMock);
        final NonManoFolderType testNonManoFolderType = new NonManoFolderType();
        testNonManoFolderType.setLocation("sw-location-test");
        testNonManoFolderType.setType("informational-test");
        when(nonManoConfigurationMock.getNonManoType(NonManoArtifactType.ONAP_SW_INFORMATION)).thenReturn(testNonManoFolderType);
        final Map<String, byte[]> csarFileMap = new HashMap<>();
        final String expectedPath = testNonManoFolderType.getPath() + "/" + "software-file.yaml";
        csarFileMap.put(expectedPath, new byte[0]);
        csarInfo.setCsar(csarFileMap);
        final Optional<String> softwareInformationPath = csarInfo.getSoftwareInformationPath();
        assertThat("The software information yaml path should be present", softwareInformationPath.isPresent(), is(true));
        softwareInformationPath.ifPresent(path -> {
            assertThat("The software information yaml ", path, is(equalTo(expectedPath)));
        });
    }

    @Test
    public void getSoftwareInformationPathTest_emptyCsar() {
        csarInfo.setCsar(new HashMap<>());
        final Optional<String> softwareInformationPath = csarInfo.getSoftwareInformationPath();
        assertThat("The software information yaml path should not be present", softwareInformationPath.isPresent(), is(false));
    }
}
