package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import org.junit.Test;


public class RequirementImplDataTest {

	private RequirementImplData createTestSubject() {
		return new RequirementImplData();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		RequirementImplData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetCreationTime() throws Exception {
		RequirementImplData testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationTime();
	}

	
	@Test
	public void testSetCreationTime() throws Exception {
		RequirementImplData testSubject;
		Long creationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationTime(creationTime);
	}

	
	@Test
	public void testGetModificationTime() throws Exception {
		RequirementImplData testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	
	@Test
	public void testSetModificationTime() throws Exception {
		RequirementImplData testSubject;
		Long modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		RequirementImplData testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		RequirementImplData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetName() throws Exception {
		RequirementImplData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		RequirementImplData testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetPosX() throws Exception {
		RequirementImplData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPosX();
	}

	
	@Test
	public void testSetPosX() throws Exception {
		RequirementImplData testSubject;
		String posX = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPosX(posX);
	}

	
	@Test
	public void testGetPosY() throws Exception {
		RequirementImplData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPosY();
	}

	
	@Test
	public void testSetPosY() throws Exception {
		RequirementImplData testSubject;
		String posY = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPosY(posY);
	}

	
	@Test
	public void testToString() throws Exception {
		RequirementImplData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}