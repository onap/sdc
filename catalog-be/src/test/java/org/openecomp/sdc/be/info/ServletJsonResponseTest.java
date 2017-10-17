package org.openecomp.sdc.be.info;

import javax.annotation.Generated;

import org.junit.Test;


public class ServletJsonResponseTest {

	private ServletJsonResponse createTestSubject() {
		return new ServletJsonResponse();
	}

	
	@Test
	public void testGetDescription() throws Exception {
		ServletJsonResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		ServletJsonResponse testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetSource() throws Exception {
		ServletJsonResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSource();
	}

	
	@Test
	public void testSetSource() throws Exception {
		ServletJsonResponse testSubject;
		String source = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSource(source);
	}
}