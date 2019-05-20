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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ServiceDistributionArtifactsBuilderTest extends BeConfDependentTest {

	@InjectMocks
	ServiceDistributionArtifactsBuilder testSubject;
	
	@Mock
    ToscaOperationFacade toscaOperationFacade;

	@Before
	public void setUpMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
	}


	@Test
	public void testGetInterfaceLifecycleOperation() throws Exception {
		InterfaceLifecycleOperation result;

		// default test
		result = testSubject.getInterfaceLifecycleOperation();
	}

	@Test
	public void testSetInterfaceLifecycleOperation() throws Exception {
		InterfaceLifecycleOperation interfaceLifecycleOperation = null;

		// default test
		testSubject.setInterfaceLifecycleOperation(interfaceLifecycleOperation);
	}

	@Test
	public void testResolveWorkloadContext() throws Exception {
		String workloadContext = "";
		String result;

		// default test
		result = Deencapsulation.invoke(testSubject, "resolveWorkloadContext", new Object[] { workloadContext });
	}

	@Test
	public void testBuildResourceInstanceForDistribution() throws Exception {
		Service service = new Service();
		String distributionId = "";
		String workloadContext = "";
		INotificationData result;

		// test 1
		workloadContext = "mock";
		result = testSubject.buildResourceInstanceForDistribution(service, distributionId, workloadContext);

		// test 2
		workloadContext = null;
		result = testSubject.buildResourceInstanceForDistribution(service, distributionId, workloadContext);
	}

	@Test
	public void testBuildServiceForDistribution() throws Exception {
		INotificationData notificationData = Mockito.mock(INotificationData.class);
		Service service = new Service();
		service.setDeploymentArtifacts(new HashMap<>());
		service.setToscaArtifacts(new HashMap<>());
		INotificationData result;

		// default test
		result = testSubject.buildServiceForDistribution(notificationData, service);
	}

	@Test(expected = NullPointerException.class)
	public void testConvertServiceArtifactsToArtifactInfo() throws Exception {
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
		result = Deencapsulation.invoke(testSubject, "convertServiceArtifactsToArtifactInfo", service);
		service.setToscaArtifacts(toscaArtifacts);
		result = Deencapsulation.invoke(testSubject, "convertServiceArtifactsToArtifactInfo", service);
	}
	
	@Test
	public void testConvertRIsToJsonContanier() throws Exception {
		Service service = new Service();
		List<ComponentInstance> resourceInstances = new LinkedList<>();
		List<JsonContainerResourceInstance> result;

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.nullable(String.class), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(new Resource()));
		// default test
		result = Deencapsulation.invoke(testSubject, "convertRIsToJsonContanier", service);
		
		resourceInstances.add(new ComponentInstance());
		service.setComponentInstances(resourceInstances);
		result = Deencapsulation.invoke(testSubject, "convertRIsToJsonContanier", service);
	}

	@Test
	public void testFillJsonContainer() throws Exception {
		JsonContainerResourceInstance jsonContainer = new JsonContainerResourceInstance(new ComponentInstance(),
				new LinkedList<>());
		Resource resource = new Resource();

		// default test
		Deencapsulation.invoke(testSubject, "fillJsonContainer", jsonContainer, resource);
	}

	@Test
	public void testConvertToArtifactsInfoImpl() throws Exception {
		Service service = new Service();
		ComponentInstance resourceInstance = new ComponentInstance();
		List<ArtifactInfoImpl> result;

		// default test
		result = Deencapsulation.invoke(testSubject, "convertToArtifactsInfoImpl", service, resourceInstance);
	}

	@Test
	public void testSetCategories() throws Exception {
		JsonContainerResourceInstance jsonContainer = null;
		List<CategoryDefinition> categories = null;

		// test 1
		categories = null;
		LinkedList<CategoryDefinition> linkedList = new LinkedList<>();
		linkedList.add(new CategoryDefinition());
		LinkedList<ArtifactInfoImpl> artifacts = new LinkedList<>();
		Deencapsulation.invoke(testSubject, "setCategories",
				new JsonContainerResourceInstance(new ComponentInstance(), artifacts), linkedList);
	}

	@Test
	public void testGetArtifactsWithPayload() throws Exception {
		ComponentInstance resourceInstance = new ComponentInstance();
		Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
		resourceInstance.setDeploymentArtifacts(deploymentArtifacts);
		List<ArtifactDefinition> result;

		// default test
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
		Service service = new Service();
		boolean result;

		// default test
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
		Map<String, ArtifactDefinition> deploymentArtifacts = null;
		boolean result;

		// default test
		result = Deencapsulation.invoke(testSubject, "isContainsPayload", new Object[] { Map.class });
	}
}