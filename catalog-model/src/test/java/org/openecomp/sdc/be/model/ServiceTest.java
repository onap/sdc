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

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import java.util.Map;

public class ServiceTest {

	protected static ConfigurationManager configurationManager;
	static Configuration.EnvironmentContext environmentContext = new Configuration.EnvironmentContext();

	@BeforeClass
	public static void init() {
		String appConfigDir = "src/test/resources/config";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);
		configurationManager = new ConfigurationManager(configurationSource);

		Configuration configuration = new Configuration();

		configuration.setJanusGraphInMemoryGraph(true);
		environmentContext.setDefaultValue("General_Revenue-Bearing");
		configuration.setEnvironmentContext(environmentContext);

		configurationManager.setConfiguration(configuration);
	}

	private Service createTestSubject() {
		return new Service();
	}
	
	@Test
	public void testCtor() throws Exception {
		new Service(new ComponentMetadataDefinition());
	}
	
	@Test
	public void testGetServiceApiArtifacts() throws Exception {
		Service testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceApiArtifacts();
	}

	
	@Test
	public void testSetServiceApiArtifacts() throws Exception {
		Service testSubject;
		Map<String, ArtifactDefinition> serviceApiArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceApiArtifacts(serviceApiArtifacts);
	}

	
	@Test
	public void testGetProjectCode() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProjectCode();
	}

	
	@Test
	public void testGetForwardingPaths() throws Exception {
		Service testSubject;
		Map<String, ForwardingPathDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getForwardingPaths();
	}

	
	@Test
	public void testSetForwardingPaths() throws Exception {
		Service testSubject;
		Map<String, ForwardingPathDataDefinition> forwardingPaths = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setForwardingPaths(forwardingPaths);
	}

	
	@Test
	public void testAddForwardingPath() throws Exception {
		Service testSubject;
		ForwardingPathDataDefinition forwardingPathDataDefinition = new ForwardingPathDataDefinition();
		ForwardingPathDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addForwardingPath(forwardingPathDataDefinition);
	}

	
	@Test
	public void testSetProjectCode() throws Exception {
		Service testSubject;
		String projectName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setProjectCode(projectName);
	}

	
	@Test
	public void testGetDistributionStatus() throws Exception {
		Service testSubject;
		DistributionStatusEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatus();
	}

	
	@Test
	public void testSetDistributionStatus() throws Exception {
		Service testSubject;
		DistributionStatusEnum distributionStatus = null;

		// test 1
		testSubject = createTestSubject();
		distributionStatus = null;
		testSubject.setDistributionStatus(distributionStatus);
		testSubject.setDistributionStatus(DistributionStatusEnum.DISTRIBUTED);
	}

	
	@Test
	public void testSetEcompGeneratedNaming() throws Exception {
		Service testSubject;
		Boolean ecompGeneratedNaming = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setEcompGeneratedNaming(ecompGeneratedNaming);
	}

	
	@Test
	public void testIsEcompGeneratedNaming() throws Exception {
		Service testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isEcompGeneratedNaming();
	}

	
	@Test
	public void testSetNamingPolicy() throws Exception {
		Service testSubject;
		String namingPolicy = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNamingPolicy(namingPolicy);
	}

	
	@Test
	public void testGetNamingPolicy() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNamingPolicy();
	}

	
	@Test
	public void testGetEnvironmentContext() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnvironmentContext();
	}

	
	@Test
	public void testSetEnvironmentContext() throws Exception {
		Service testSubject;
		String environmentContext = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEnvironmentContext(environmentContext);
	}

	
	@Test
	public void testSetServiceType() throws Exception {
		Service testSubject;
		String serviceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceType(serviceType);
	}

	
	@Test
	public void testGetServiceType() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceType();
	}

	
	@Test
	public void testSetServiceRole() throws Exception {
		Service testSubject;
		String serviceRole = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceRole(serviceRole);
	}

	
	@Test
	public void testGetServiceRole() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceRole();
	}

	


	
	@Test
	public void testToString() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testSetSpecificComponetTypeArtifacts() throws Exception {
		Service testSubject;
		Map<String, ArtifactDefinition> specificComponentTypeArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setSpecificComponetTypeArtifacts(specificComponentTypeArtifacts);
	}
}
