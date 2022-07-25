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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.NonManoArtifactType;
import org.openecomp.sdc.be.config.NonManoConfiguration;
import org.openecomp.sdc.be.config.NonManoFolderType;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import com.datastax.oss.driver.shaded.guava.common.collect.Lists;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OnboardedCsarInfoTest {

    private OnboardedCsarInfo csarInfo;

    @Mock
    private User user;

    private static final String CSAR_UUID = "csarUUID";
    private static final String PAYLOAD_NAME = "mock_service.csar";
    private static final String RESOURCE_NAME = "resourceName";
    private static final String MAIN_TEMPLATE_NAME = "Definitions/tosca_mock_vf.yaml";
    private static final String NEW_NODE_NAME = "new_db";
    private static final String NODE_TYPE = "tosca.nodes.Compute";
    private static final String DELIVER_FOR = "tosca.nodes.Root";

    @BeforeEach
    public void setup() throws ZipException, URISyntaxException {
        // given
        csarInfo = createCsarInfo(PAYLOAD_NAME, MAIN_TEMPLATE_NAME);

        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }
    
    private OnboardedCsarInfo createCsarInfo(final String csarFileName, final String mainTemplateName) throws URISyntaxException, ZipException {
      final File csarFile = new File(OnboardedCsarInfoTest.class.getClassLoader().getResource(csarFileName).toURI());
      final Map<String, byte[]> payload = ZipUtils.readZip(csarFile, false);
      String mainTemplateContent = new String(payload.get(mainTemplateName));

      return new OnboardedCsarInfo(user, CSAR_UUID, payload, RESOURCE_NAME,
              mainTemplateName, mainTemplateContent, true);
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
    public void setUpdateTest() {

        csarInfo.setUpdate(true);
        assertTrue(csarInfo.isUpdate());

        csarInfo.setUpdate(false);
        assertFalse(csarInfo.isUpdate());
    }

    @Test
    public void csarCheckTypesInfoTest() {

        // when
        Map<String, NodeTypeInfo> nodeTypeInfoMap = csarInfo.extractTypesInfo();
        NodeTypeInfo nodeTypeInfo = nodeTypeInfoMap.get(NODE_TYPE);

        // then
        assertNotNull(nodeTypeInfo);
        assertTrue(nodeTypeInfo.getDerivedFrom().contains(DELIVER_FOR));
        assertEquals(NODE_TYPE, nodeTypeInfo.getType());

        assertEquals(MAIN_TEMPLATE_NAME, csarInfo.getMainTemplateName());
        assertEquals(csarInfo.getMainTemplateName(), nodeTypeInfo.getTemplateFileName());
        
        Map<String, Object> dataTypes = csarInfo.getDataTypes();
        assertTrue(dataTypes.containsKey("tosca.datatypes.testDataType.FromMainTemplate"));
        assertTrue(dataTypes.containsKey("tosca.datatypes.testDataType.FromGlobalSub"));
    }

    @Test
    public void getSoftwareInformationPathTest() {
        final NonManoConfiguration nonManoConfigurationMock = Mockito.mock(NonManoConfiguration.class);
        final CsarInfo csarInfo = new OnboardedCsarInfo(nonManoConfigurationMock);
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
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateCsarInfoEtsiVnf() throws URISyntaxException, ZipException {
        final CsarInfo csarInfo = createCsarInfo("etsi_vnf.csar", "Definitions/MainServiceTemplate.yaml");
        
        final String nodeTypeInSubstitutionMapping = (String) ((Map<String, Object>)((Map<String, Object>)csarInfo.getMappedToscaMainTemplate().get("topology_template")).get("substitution_mappings")).get("node_type");
        assertTrue(((Map<String, Object>) csarInfo.getMappedToscaMainTemplate().get("node_types")).containsKey(nodeTypeInSubstitutionMapping));
        
        assertTrue(csarInfo.extractTypesInfo().isEmpty());
    }
    
    @Test
    public void testCreateCsarInfoVnfWithNodeTypeInGlobalSub() throws URISyntaxException, ZipException {
        final CsarInfo csarInfo = createCsarInfo("nodeTypeInGlobalSub.csar", "Definitions/MainServiceTemplate.yaml");

        assertEquals(1, csarInfo.extractTypesInfo().size());
        final NodeTypeInfo nodeTypeInfo = csarInfo.extractTypesInfo().get("tosca.nodes.l3vpn");
        assertNotNull(nodeTypeInfo);
        assertEquals("Definitions/GlobalSubstitutionTypesServiceTemplate.yaml", nodeTypeInfo.getTemplateFileName());
        assertEquals("tosca.nodes.l3vpn", nodeTypeInfo.getType());        
        assertEquals(Lists.newArrayList("tosca.nodes.Root"), nodeTypeInfo.getDerivedFrom());        
    }
}
