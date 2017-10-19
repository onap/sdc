package org.openecomp.sdc.be.dao.jsongraph.types;

import javax.annotation.Generated;

import org.junit.Test;


public class VertexTypeEnumTest {

	private VertexTypeEnum createTestSubject() {
		return VertexTypeEnum.ADDITIONAL_INFORMATION;
	}

	
	@Test
	public void testGetName() throws Exception {
		VertexTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testGetClassOfJson() throws Exception {
		VertexTypeEnum testSubject;
		Class result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getClassOfJson();
	}

	
	@Test
	public void testGetByName() throws Exception {
		String name = "";
		VertexTypeEnum result;

		// default test
		result = VertexTypeEnum.getByName(name);
	}
}