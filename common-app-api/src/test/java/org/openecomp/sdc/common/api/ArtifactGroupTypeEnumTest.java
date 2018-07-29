package org.openecomp.sdc.common.api;

import org.junit.Test;

import java.util.List;


public class ArtifactGroupTypeEnumTest {

	private ArtifactGroupTypeEnum createTestSubject() {
		return  ArtifactGroupTypeEnum.DEPLOYMENT;
	}

	
	@Test
	public void testGetType() throws Exception {
		ArtifactGroupTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		ArtifactGroupTypeEnum testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(ArtifactGroupTypeEnum.DEPLOYMENT.getType());
	}

	
	@Test
	public void testFindType() throws Exception {
		String type = "";
		ArtifactGroupTypeEnum result;

		// default test
		result = ArtifactGroupTypeEnum.findType(type);
	}

	
	@Test
	public void testGetAllTypes() throws Exception {
		List<String> result;

		// default test
		result = ArtifactGroupTypeEnum.getAllTypes();
	}
}