package org.openecomp.sdc.be.resources.data;

import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;

public class ServiceArtifactsDataCollectionTest {

	private ServiceArtifactsDataCollection createTestSubject() {
		return new ServiceArtifactsDataCollection();
	}

	
	@Test
	public void testGetServiceArtifactDataMap() throws Exception {
		ServiceArtifactsDataCollection testSubject;
		Map<String, List<ESArtifactData>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceArtifactDataMap();
	}

	
	@Test
	public void testSetServiceArtifactDataMap() throws Exception {
		ServiceArtifactsDataCollection testSubject;
		Map<String, List<ESArtifactData>> serviceArtifactDataMap = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceArtifactDataMap(serviceArtifactDataMap);
	}

	
	@Test
	public void testGetNodeTemplateArtifacts() throws Exception {
		ServiceArtifactsDataCollection testSubject;
		String nodeTemplateName = "";
		List<ESArtifactData> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNodeTemplateArtifacts(nodeTemplateName);
	}
}