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

package org.openecomp.sdc.be.components.distribution.engine;

import fj.data.Either;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ServiceDistributionArtifactsBuilderTest extends BeConfDependentTest {

	@InjectMocks
	private ServiceDistributionArtifactsBuilder testInstance;

	@Mock
	ToscaOperationFacade toscaOperationFacade;

	private ServiceDistributionArtifactsBuilder createTestSubject() {
		return testInstance;
	}

	@Before
	public void setUpMock() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetInterfaceLifecycleOperation() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		InterfaceLifecycleOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInterfaceLifecycleOperation();
	}

	@Test
	public void testSetInterfaceLifecycleOperation() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		InterfaceLifecycleOperation interfaceLifecycleOperation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInterfaceLifecycleOperation(interfaceLifecycleOperation);
	}

	@Test
	public void testResolveWorkloadContext() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		String workloadContext = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "resolveWorkloadContext", new Object[] { workloadContext });
	}

	@Test
	public void testBuildResourceInstanceForDistribution() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		Service service = new Service();
		String distributionId = "";
		String workloadContext = "";
		INotificationData result;

		// test 1
		testSubject = createTestSubject();
		workloadContext = "mock";
		result = testSubject.buildResourceInstanceForDistribution(service, distributionId, workloadContext);

		// test 2
		testSubject = createTestSubject();
		workloadContext = null;
		result = testSubject.buildResourceInstanceForDistribution(service, distributionId, workloadContext);
	}

	@Test
	public void testBuildResourceInstanceForDistributionArtifactsTimeout() throws Exception {
		Service service = new Service();
		int deploymentArtifactTimeout = 60;
		ComponentInstance ci = createComponentInstance("ci");
		ci.setDeploymentArtifacts(createDeploymentArtifactList(deploymentArtifactTimeout));
		service.setComponentInstances(Arrays.asList(ci));

		when(toscaOperationFacade.getToscaElement(Mockito.eq(ci.getComponentUid()), Mockito.any(ComponentParametersView.class)))
				.thenReturn(Either.left(createDefaultResource()));

		INotificationData notificationData = testInstance.buildResourceInstanceForDistribution(service, "did", "workload");
		assertThat(notificationData.getResources().get(0).getArtifacts().get(0).getArtifactTimeout())
				.isEqualTo(deploymentArtifactTimeout);
	}

	private Map<String, ArtifactDefinition> createDeploymentArtifactList(int timeout) {
		Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
		deploymentArtifacts.put("label", createArtifactDefinition("1", timeout));
		return deploymentArtifacts;
	}

	private Resource createDefaultResource() {
		Resource ciResource = new Resource();
		ciResource.setInvariantUUID("res");
		return ciResource;
	}

	private ComponentInstance createComponentInstance(String cuid) {
		ComponentInstance ci = new ComponentInstance();
		ci.setComponentUid(cuid);
		return ci;
	}

	private ArtifactDefinition createArtifactDefinition(String id, int timeout) {
		ArtifactDefinition artifactDefinition = new ArtifactDefinition();
		artifactDefinition.setTimeout(60);
		artifactDefinition.setUniqueId(id);
		artifactDefinition.setEsId(id);
		return artifactDefinition;
	}

	@Test
	public void testBuildServiceForDistribution() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		INotificationData notificationData = new INotificationDataMock();
		Service service = new Service();
		service.setDeploymentArtifacts(new HashMap<>());
		service.setToscaArtifacts(new HashMap<>());
		INotificationData result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.buildServiceForDistribution(notificationData, service);
	}

	@Test(expected = NullPointerException.class)
	public void testConvertServiceArtifactsToArtifactInfo() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		Service service = new Service();
		service.setDeploymentArtifacts(new HashMap<>());
		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifactDefinition = new ArtifactDefinition();
		ArtifactDefinition artifactDefinition2 = new ArtifactDefinition();
		artifactDefinition.setArtifactType(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
		artifactDefinition2.setArtifactType(ArtifactTypeEnum.TOSCA_CSAR.getType());
		toscaArtifacts.put("mock", artifactDefinition);
		toscaArtifacts.put("mock2", artifactDefinition2);
		service.setToscaArtifacts(new HashMap<>());
		List<ArtifactInfoImpl> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertServiceArtifactsToArtifactInfo", service);
		service.setToscaArtifacts(toscaArtifacts);
		result = Deencapsulation.invoke(testSubject, "convertServiceArtifactsToArtifactInfo", service);
	}
	
	@Test(expected=NullPointerException.class)
	public void testConvertRIsToJsonContanier() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		Service service = new Service();
		List<ComponentInstance> resourceInstances = new LinkedList<>();
		List<JsonContainerResourceInstance> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertRIsToJsonContanier", service);
		
		resourceInstances.add(new ComponentInstance());
		service.setComponentInstances(resourceInstances);
		result = Deencapsulation.invoke(testSubject, "convertRIsToJsonContanier", service);
	}

	@Test
	public void testFillJsonContainer() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		JsonContainerResourceInstance jsonContainer = new JsonContainerResourceInstance(new ComponentInstance(),
				new LinkedList<>());
		Resource resource = new Resource();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "fillJsonContainer", jsonContainer, resource);
	}

	@Test
	public void testConvertToArtifactsInfoImpl() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		Service service = new Service();
		ComponentInstance resourceInstance = new ComponentInstance();
		List<ArtifactInfoImpl> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertToArtifactsInfoImpl", service, resourceInstance);
	}

	@Test
	public void testSetCategories() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		JsonContainerResourceInstance jsonContainer = null;
		List<CategoryDefinition> categories = null;

		// test 1
		testSubject = createTestSubject();
		categories = null;
		Deencapsulation.invoke(testSubject, "setCategories",
				new Object[] { JsonContainerResourceInstance.class, List.class });
	}

	@Test
	public void testGetArtifactsWithPayload() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		ComponentInstance resourceInstance = new ComponentInstance();
		Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
		resourceInstance.setDeploymentArtifacts(deploymentArtifacts);
		List<ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getArtifactsWithPayload", resourceInstance);
		deploymentArtifacts.put("mock", new ArtifactDefinition());
		result = Deencapsulation.invoke(testSubject, "getArtifactsWithPayload", resourceInstance);
	}

	@Test
	public void testBuildResourceInstanceArtifactUrl() throws Exception {
		Service service = new Service();
		service.setSystemName("mock");
		service.setVersion("mock");
		ComponentInstance resourceInstance = new ComponentInstance();
		resourceInstance.setNormalizedName("mock");
		String artifactName = "mock";
		String result;

		// default test
		result = ServiceDistributionArtifactsBuilder.buildResourceInstanceArtifactUrl(service, resourceInstance,
				artifactName);
	}

	@Test
	public void testBuildServiceArtifactUrl() throws Exception {
		Service service = new Service();
		String artifactName = "mock";
		String result;

		// default test
		result = ServiceDistributionArtifactsBuilder.buildServiceArtifactUrl(service, artifactName);
	}

	@Test
	public void testVerifyServiceContainsDeploymentArtifacts() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		Service service = new Service();
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.verifyServiceContainsDeploymentArtifacts(service);
		Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
		deploymentArtifacts.put("mock", new ArtifactDefinition());
		service.setDeploymentArtifacts(deploymentArtifacts);
		result = testSubject.verifyServiceContainsDeploymentArtifacts(service);
		List<ComponentInstance> resourceInstances = new LinkedList<>();
		resourceInstances.add(new ComponentInstance());
		service.setComponentInstances(resourceInstances);
		service.setDeploymentArtifacts(null);
		result = testSubject.verifyServiceContainsDeploymentArtifacts(service);
	}

	@Test
	public void testIsContainsPayload() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		Map<String, ArtifactDefinition> deploymentArtifacts = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "isContainsPayload", new Object[] { Map.class });
	}

	private class INotificationDataMock implements INotificationData {

		@Override
		public String getDistributionID() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServiceName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServiceVersion() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServiceUUID() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServiceDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServiceInvariantUUID() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<JsonContainerResourceInstance> getResources() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<ArtifactInfoImpl> getServiceArtifacts() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getWorkloadContext() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setDistributionID(String distributionId) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setServiceName(String serviceName) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setServiceVersion(String serviceVersion) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setServiceUUID(String serviceUUID) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setServiceDescription(String serviceDescription) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setServiceInvariantUUID(String serviceInvariantUuid) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setResources(List<JsonContainerResourceInstance> resource) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setServiceArtifacts(List<ArtifactInfoImpl> artifacts) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setWorkloadContext(String workloadContext) {
			// TODO Auto-generated method stub

		}

	}
}
