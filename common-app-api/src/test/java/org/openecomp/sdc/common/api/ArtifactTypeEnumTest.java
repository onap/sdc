package org.openecomp.sdc.common.api;

import java.util.List;

import org.junit.Test;


public class ArtifactTypeEnumTest {

	private ArtifactTypeEnum createTestSubject() {
		return ArtifactTypeEnum.AAI_SERVICE_MODEL;
	}

	
	@Test
	public void testGetType() throws Exception {
		ArtifactTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		ArtifactTypeEnum testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testFindType() throws Exception {
		String type = "";
		ArtifactTypeEnum result;

		// default test
		result = ArtifactTypeEnum.findType(type);
	}

	
	@Test
	public void testGetAllTypes() throws Exception {
		List<String> result;

		// default test
		result = ArtifactTypeEnum.getAllTypes();
	}
}