package org.openecomp.sdc.be.components.distribution.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;


public class VfModuleArtifactPayloadTest {

	private VfModuleArtifactPayload createTestSubject() {
		return new VfModuleArtifactPayload(new GroupDefinition());
	}

	

	
	@Test
	public void testGetArtifacts() throws Exception {
		VfModuleArtifactPayload testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	
	@Test
	public void testSetArtifacts() throws Exception {
		VfModuleArtifactPayload testSubject;
		List<String> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		VfModuleArtifactPayload testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		VfModuleArtifactPayload testSubject;
		List<GroupInstanceProperty> properties = new ArrayList<>();

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}