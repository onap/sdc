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

package org.openecomp.sdc.be.model;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.log.api.ILogConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ComponentInstanceTest {

	private ComponentInstance createTestSubject() {
		return new ComponentInstance();
	}

	@Test
	public void testCtor() throws Exception {
		assertNotNull(new ComponentInstance(new ComponentInstanceDataDefinition()));
	}
	
	@Test
	public void testCapabilities() throws Exception {
		ComponentInstance testSubject;
		Map<String, List<CapabilityDefinition>> capabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilities(capabilities);
		assertNull(testSubject.getCapabilities());
	}

	@Test
	public void testRequirements() throws Exception {
		ComponentInstance testSubject;
		Map<String, List<RequirementDefinition>> requirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirements(requirements);
		assertNull(testSubject.getRequirements());
	}

	@Test
	public void testDeploymentArtifacts() throws Exception {
		ComponentInstance testSubject = createTestSubject();
		Map<String, ArtifactDefinition> deploymentArtifacts = null;
		testSubject.setDeploymentArtifacts(deploymentArtifacts);
		assertNull(testSubject.getDeploymentArtifacts());
	}

	@Test
	public void testSafeGetDeploymentArtifacts() {
		ComponentInstance testSubject = createTestSubject();
		assertEquals(0, testSubject.safeGetDeploymentArtifacts().size());

		Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap();
        deploymentArtifacts.put("test", new ArtifactDefinition());
        testSubject.setDeploymentArtifacts(deploymentArtifacts);
        assertEquals(1, testSubject.safeGetDeploymentArtifacts().size());
	}

	@Test
	public void testSafeGetInformationalArtifacts() throws Exception {
        ComponentInstance testSubject = createTestSubject();
        assertEquals(0, testSubject.safeGetInformationalArtifacts().size());

        Map<String, ArtifactDefinition> informationArtifacts = new HashMap();
        informationArtifacts.put("test", new ArtifactDefinition());
        testSubject.setArtifacts(informationArtifacts);
        assertNull(testSubject.safeGetInformationalArtifacts());
	}

    @Test
    public void testSafeGetArtifacts() throws Exception {
        ComponentInstance testSubject = createTestSubject();
        assertEquals(0, testSubject.safeGetArtifacts().size());

        Map<String, ArtifactDefinition> artifacts = new HashMap();
		artifacts.put("test", new ArtifactDefinition());
        testSubject.setArtifacts(artifacts);
		assertEquals(1, testSubject.safeGetArtifacts().size());
    }

	@Test
	public void testArtifacts() throws Exception {
		ComponentInstance testSubject;
		Map<String, ArtifactDefinition> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
		assertNull(testSubject.getArtifacts());
	}

	@Test
	public void testSetGroupInstances() throws Exception {
		ComponentInstance testSubject;
		List<GroupInstance> groupInstances = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupInstances(groupInstances);
		assertNull(testSubject.getGroupInstances());
	}

	@Test
	public void testGetActualComponentUid() throws Exception {
		ComponentInstance testSubject = createTestSubject();
		testSubject.setOriginType(OriginTypeEnum.ServiceSubstitution);
		testSubject.setSourceModelUid("sourceModelUid");
		testSubject.setComponentUid("componentUid");
		assertEquals("sourceModelUid", testSubject.getActualComponentUid());

		testSubject.setOriginType(OriginTypeEnum.VFC);
		assertEquals("componentUid", testSubject.getActualComponentUid());
	}

	@Test
	public void testIsArtifactExists() throws Exception {
		ComponentInstance testSubject;

		// default test
		testSubject = createTestSubject();
		assertFalse(testSubject.isArtifactExists(null, ""));
		Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap();
		deploymentArtifacts.put("test", new ArtifactDefinition());
		testSubject.setDeploymentArtifacts(deploymentArtifacts);
		testSubject.setArtifacts(deploymentArtifacts);
		assertTrue(testSubject.isArtifactExists(null, "test"));

		testSubject = createTestSubject();
		assertFalse(testSubject.isArtifactExists(ArtifactGroupTypeEnum.DEPLOYMENT, ""));
		testSubject.setDeploymentArtifacts(deploymentArtifacts);
		assertTrue(testSubject.isArtifactExists(ArtifactGroupTypeEnum.DEPLOYMENT, "test"));
	}

	@Test
	public void testAddInterface() throws Exception {
		ComponentInstance testSubject = createTestSubject();
		assertNull(testSubject.getInterfaces());
		testSubject.addInterface("test", new InterfaceDefinition());
		assertEquals(1, testSubject.getInterfaces().size());
	}

	@Test
	public void testGetComponentMetadataForSupportLog() throws Exception {
		ComponentInstance testSubject = createTestSubject();
		testSubject.setName("testName");
		testSubject.setToscaPresentationValue(JsonPresentationFields.VERSION, "1.0");
		testSubject.setSourceModelUuid("sourceModelUuid");
		assertEquals(3, testSubject.getComponentMetadataForSupportLog().size());
		assertEquals("testName", testSubject.getComponentMetadataForSupportLog().get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_NAME));
		assertEquals("1.0", testSubject.getComponentMetadataForSupportLog().get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_VERSION));
		assertEquals("sourceModelUuid", testSubject.getComponentMetadataForSupportLog().get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_UUID));
	}

	@Test
	public void testIsCreatedFromCsar() throws Exception {
		ComponentInstance testSubject = createTestSubject();
		testSubject.setCreatedFrom(CreatedFrom.CSAR);
		assertTrue(testSubject.isCreatedFromCsar());
		testSubject.setCreatedFrom(CreatedFrom.UI);
		assertFalse(testSubject.isCreatedFromCsar());}
}
