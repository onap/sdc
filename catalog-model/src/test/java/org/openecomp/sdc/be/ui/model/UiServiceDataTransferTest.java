package org.openecomp.sdc.be.ui.model;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.model.ArtifactDefinition;


public class UiServiceDataTransferTest {

	private UiServiceDataTransfer createTestSubject() {
		return new UiServiceDataTransfer();
	}

	
	@Test
	public void testGetMetadata() throws Exception {
		UiServiceDataTransfer testSubject;
		UiServiceMetadata result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadata();
	}

	
	@Test
	public void testSetMetadata() throws Exception {
		UiServiceDataTransfer testSubject;
		UiServiceMetadata metadata = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMetadata(metadata);
	}

	
	@Test
	public void testGetServiceApiArtifacts() throws Exception {
		UiServiceDataTransfer testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceApiArtifacts();
	}

	
	@Test
	public void testSetServiceApiArtifacts() throws Exception {
		UiServiceDataTransfer testSubject;
		Map<String, ArtifactDefinition> serviceApiArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceApiArtifacts(serviceApiArtifacts);
	}
}