package org.openecomp.sdc.be.resources.data;

import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class CapabilityDataTest {

	private CapabilityData createTestSubject() {
		return new CapabilityData();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		CapabilityData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		CapabilityData testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		CapabilityData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		CapabilityData testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetType() throws Exception {
		CapabilityData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		CapabilityData testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetValidSourceTypes() throws Exception {
		CapabilityData testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValidSourceTypes();
	}

	
	@Test
	public void testSetValidSourceTypes() throws Exception {
		CapabilityData testSubject;
		List<String> validSourceTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValidSourceTypes(validSourceTypes);
	}

	
	@Test
	public void testGetCreationTime() throws Exception {
		CapabilityData testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationTime();
	}

	
	@Test
	public void testSetCreationTime() throws Exception {
		CapabilityData testSubject;
		Long creationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationTime(creationTime);
	}

	
	@Test
	public void testGetModificationTime() throws Exception {
		CapabilityData testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	
	@Test
	public void testSetModificationTime() throws Exception {
		CapabilityData testSubject;
		Long modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	
	@Test
	public void testGetMinOccurrences() throws Exception {
		CapabilityData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMinOccurrences();
	}

	
	@Test
	public void testSetMinOccurrences() throws Exception {
		CapabilityData testSubject;
		String minOccurrences = "";

		// test 1
		testSubject = createTestSubject();
		minOccurrences = null;
		testSubject.setMinOccurrences(minOccurrences);

		// test 2
		testSubject = createTestSubject();
		minOccurrences = "";
		testSubject.setMinOccurrences(minOccurrences);
	}

	
	@Test
	public void testGetMaxOccurrences() throws Exception {
		CapabilityData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMaxOccurrences();
	}

	
	@Test
	public void testSetMaxOccurrences() throws Exception {
		CapabilityData testSubject;
		String maxOccurrences = "";

		// test 1
		testSubject = createTestSubject();
		maxOccurrences = null;
		testSubject.setMaxOccurrences(maxOccurrences);

		// test 2
		testSubject = createTestSubject();
		maxOccurrences = "";
		testSubject.setMaxOccurrences(maxOccurrences);
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		CapabilityData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testToString() throws Exception {
		CapabilityData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}