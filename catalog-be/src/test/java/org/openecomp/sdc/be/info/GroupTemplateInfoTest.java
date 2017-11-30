package org.openecomp.sdc.be.info;

import org.junit.Test;


public class GroupTemplateInfoTest {

	private GroupTemplateInfo createTestSubject() {
		return new GroupTemplateInfo();
	}

	
	@Test
	public void testGetGroupName() throws Exception {
		GroupTemplateInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupName();
	}

	
	@Test
	public void testSetGroupName() throws Exception {
		GroupTemplateInfo testSubject;
		String groupName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupName(groupName);
	}

	
	@Test
	public void testIsBase() throws Exception {
		GroupTemplateInfo testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isBase();
	}

	
	@Test
	public void testSetBase() throws Exception {
		GroupTemplateInfo testSubject;
		boolean isBase = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setBase(isBase);
	}

	
	@Test
	public void testGetArtifactTemplateInfo() throws Exception {
		GroupTemplateInfo testSubject;
		ArtifactTemplateInfo result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactTemplateInfo();
	}

	
	@Test
	public void testSetArtifactTemplateInfo() throws Exception {
		GroupTemplateInfo testSubject;
		ArtifactTemplateInfo artifactTemplateInfo = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactTemplateInfo(artifactTemplateInfo);
	}
}