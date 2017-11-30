package org.openecomp.sdc.common.api;

import java.util.List;

import org.junit.Test;


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
		testSubject.setType(type);
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