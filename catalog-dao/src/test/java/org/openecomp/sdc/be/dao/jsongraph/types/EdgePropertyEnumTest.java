package org.openecomp.sdc.be.dao.jsongraph.types;

import javax.annotation.Generated;

import org.junit.Test;


public class EdgePropertyEnumTest {

	private EdgePropertyEnum createTestSubject() {
		return EdgePropertyEnum.STATE;
	}

	
	@Test
	public void testGetProperty() throws Exception {
		EdgePropertyEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperty();
	}

	
	@Test
	public void testGetByProperty() throws Exception {
		String property = "";
		EdgePropertyEnum result;

		// default test
		result = EdgePropertyEnum.getByProperty(property);
	}
}