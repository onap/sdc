package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class VfModuleArtifactPayloadTest {

	private VfModuleArtifactPayload createTestSubject() {
		return new VfModuleArtifactPayload(new GroupDefinition());
	}

	@Test
	public void testConstructor() {
		new VfModuleArtifactPayload(new GroupInstance());
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
	
	@Test
	public void testcompareByGroupName() throws Exception {
		VfModuleArtifactPayload testSubject;
		GroupDefinition groupDefinition = new GroupDefinition();
		groupDefinition.setName("module-1234.545");
		VfModuleArtifactPayload vfModuleArtifactPayload1 = new VfModuleArtifactPayload(groupDefinition);
		GroupDefinition groupDefinition2 = new GroupDefinition();
		groupDefinition.setName("module-3424.546");
		VfModuleArtifactPayload vfModuleArtifactPayload2 = new VfModuleArtifactPayload(groupDefinition);
		// default test
		testSubject = createTestSubject();
		testSubject.compareByGroupName(vfModuleArtifactPayload1, vfModuleArtifactPayload2);
		testSubject.compareByGroupName(vfModuleArtifactPayload1, vfModuleArtifactPayload1);
	}
}