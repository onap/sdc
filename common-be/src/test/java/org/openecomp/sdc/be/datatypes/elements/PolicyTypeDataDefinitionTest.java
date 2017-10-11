package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class PolicyTypeDataDefinitionTest {

	private PolicyTypeDataDefinition createTestSubject() {
		return new PolicyTypeDataDefinition();
	}

	
	@Test
	public void testGetType() throws Exception {
		PolicyTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		PolicyTypeDataDefinition testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetVersion() throws Exception {
		PolicyTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testSetVersion() throws Exception {
		PolicyTypeDataDefinition testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	
	@Test
	public void testGetTargets() throws Exception {
		PolicyTypeDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTargets();
	}

	
	@Test
	public void testSetTargets() throws Exception {
		PolicyTypeDataDefinition testSubject;
		List<String> members = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTargets(members);
	}

	
	@Test
	public void testGetMetadata() throws Exception {
		PolicyTypeDataDefinition testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadata();
	}

	
	@Test
	public void testSetMetadata() throws Exception {
		PolicyTypeDataDefinition testSubject;
		Map<String, String> metadata = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMetadata(metadata);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		PolicyTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		PolicyTypeDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		PolicyTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		PolicyTypeDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetCreationTime() throws Exception {
		PolicyTypeDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationTime();
	}

	
	@Test
	public void testSetCreationTime() throws Exception {
		PolicyTypeDataDefinition testSubject;
		Long creationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationTime(creationTime);
	}

	
	@Test
	public void testGetModificationTime() throws Exception {
		PolicyTypeDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	
	@Test
	public void testSetModificationTime() throws Exception {
		PolicyTypeDataDefinition testSubject;
		Long modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	
	@Test
	public void testToString() throws Exception {
		PolicyTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetDerivedFrom() throws Exception {
		PolicyTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedFrom();
	}

	
	@Test
	public void testSetDerivedFrom() throws Exception {
		PolicyTypeDataDefinition testSubject;
		String derivedFrom = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedFrom(derivedFrom);
	}

	
	@Test
	public void testIsHighestVersion() throws Exception {
		PolicyTypeDataDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isHighestVersion();
	}

	
	@Test
	public void testSetHighestVersion() throws Exception {
		PolicyTypeDataDefinition testSubject;
		boolean isLatestVersion = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setHighestVersion(isLatestVersion);
	}
}