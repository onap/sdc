package org.openecomp.sdc.be.info;

import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.model.GroupProperty;


public class GroupDefinitionInfoTest {

	private GroupDefinitionInfo createTestSubject() {
		return new GroupDefinitionInfo();
	}

	
	@Test
	public void testGetInvariantUUID() throws Exception {
		GroupDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInvariantUUID();
	}

	
	@Test
	public void testSetInvariantUUID() throws Exception {
		GroupDefinitionInfo testSubject;
		String invariantUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInvariantUUID(invariantUUID);
	}

	
	@Test
	public void testGetName() throws Exception {
		GroupDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		GroupDefinitionInfo testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		GroupDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		GroupDefinitionInfo testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetGroupUUID() throws Exception {
		GroupDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupUUID();
	}

	
	@Test
	public void testSetGroupUUID() throws Exception {
		GroupDefinitionInfo testSubject;
		String groupUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupUUID(groupUUID);
	}

	
	@Test
	public void testGetVersion() throws Exception {
		GroupDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testSetVersion() throws Exception {
		GroupDefinitionInfo testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	
	@Test
	public void testGetCustomizationUUID() throws Exception {
		GroupDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCustomizationUUID();
	}

	
	@Test
	public void testSetCustomizationUUID() throws Exception {
		GroupDefinitionInfo testSubject;
		String customizationUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCustomizationUUID(customizationUUID);
	}

	
	@Test
	public void testGetIsBase() throws Exception {
		GroupDefinitionInfo testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsBase();
	}

	
	@Test
	public void testSetIsBase() throws Exception {
		GroupDefinitionInfo testSubject;
		Boolean isBase = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIsBase(isBase);
	}

	
	@Test
	public void testGetArtifacts() throws Exception {
		GroupDefinitionInfo testSubject;
		List<ArtifactDefinitionInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	
	@Test
	public void testSetArtifacts() throws Exception {
		GroupDefinitionInfo testSubject;
		List<ArtifactDefinitionInfo> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		GroupDefinitionInfo testSubject;
		List<? extends GroupProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		GroupDefinitionInfo testSubject;
		List<? extends GroupProperty> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	
	@Test
	public void testGetGroupInstanceUniqueId() throws Exception {
		GroupDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupInstanceUniqueId();
	}

	
	@Test
	public void testSetGroupInstanceUniqueId() throws Exception {
		GroupDefinitionInfo testSubject;
		String groupInstanceUniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupInstanceUniqueId(groupInstanceUniqueId);
	}

	
	@Test
	public void testToString() throws Exception {
		GroupDefinitionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}