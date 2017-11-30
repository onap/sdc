package org.openecomp.sdc.be.info;

import java.util.List;

import org.junit.Test;


public class ArtifactTemplateInfoTest {

	private ArtifactTemplateInfo createTestSubject() {
		return new ArtifactTemplateInfo();
	}

	
	@Test
	public void testGetType() throws Exception {
		ArtifactTemplateInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		ArtifactTemplateInfo testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetFileName() throws Exception {
		ArtifactTemplateInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFileName();
	}

	
	@Test
	public void testSetFileName() throws Exception {
		ArtifactTemplateInfo testSubject;
		String fileName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFileName(fileName);
	}

	
	@Test
	public void testGetEnv() throws Exception {
		ArtifactTemplateInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnv();
	}

	
	@Test
	public void testSetEnv() throws Exception {
		ArtifactTemplateInfo testSubject;
		String env = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEnv(env);
	}

	
	@Test
	public void testGetRelatedArtifactsInfo() throws Exception {
		ArtifactTemplateInfo testSubject;
		List<ArtifactTemplateInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelatedArtifactsInfo();
	}

	
	@Test
	public void testSetRelatedArtifactsInfo() throws Exception {
		ArtifactTemplateInfo testSubject;
		List<ArtifactTemplateInfo> relatedArtifactsInfo = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelatedArtifactsInfo(relatedArtifactsInfo);
	}

	
	@Test
	public void testGetGroupName() throws Exception {
		ArtifactTemplateInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupName();
	}

	
	@Test
	public void testSetGroupName() throws Exception {
		ArtifactTemplateInfo testSubject;
		String groupName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupName(groupName);
	}

	
	@Test
	public void testIsBase() throws Exception {
		ArtifactTemplateInfo testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isBase();
	}

	
	@Test
	public void testSetBase() throws Exception {
		ArtifactTemplateInfo testSubject;
		boolean isBase = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setBase(isBase);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		ArtifactTemplateInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		ArtifactTemplateInfo testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testToString() throws Exception {
		ArtifactTemplateInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	
}