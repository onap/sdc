package org.openecomp.sdc.be.components.distribution.engine;

import javax.annotation.Generated;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;

public class ServiceDistributionArtifactsBuilderTest {

	private ServiceDistributionArtifactsBuilder createTestSubject() {
		return new ServiceDistributionArtifactsBuilder();
	}

	
	@Test
	public void testBuildResourceInstanceArtifactUrl() throws Exception {
		Service service = new Service();
		ComponentInstance resourceInstance = new ComponentInstance();
		String artifactName = "";
		String result;

		// default test
		result = ServiceDistributionArtifactsBuilder.buildResourceInstanceArtifactUrl(service, resourceInstance,
				artifactName);
	}

	
	
	
	@Test
	public void testBuildServiceArtifactUrl() throws Exception {
		Service service = new Service();;
		String artifactName = "";
		String result;

		// default test
		result = ServiceDistributionArtifactsBuilder.buildServiceArtifactUrl(service, artifactName);
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
	public void testVerifyServiceContainsDeploymentArtifacts() throws Exception {
		ServiceDistributionArtifactsBuilder testSubject;
		Service service = new Service();;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.verifyServiceContainsDeploymentArtifacts(service);
	}
}