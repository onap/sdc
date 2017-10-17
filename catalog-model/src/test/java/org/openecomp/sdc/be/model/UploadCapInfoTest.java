package org.openecomp.sdc.be.model;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class UploadCapInfoTest {

	private UploadCapInfo createTestSubject() {
		return new UploadCapInfo();
	}

	
	@Test
	public void testGetNode() throws Exception {
		UploadCapInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode();
	}

	
	@Test
	public void testSetNode() throws Exception {
		UploadCapInfo testSubject;
		String node = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNode(node);
	}

	
	@Test
	public void testGetValidSourceTypes() throws Exception {
		UploadCapInfo testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValidSourceTypes();
	}

	
	@Test
	public void testSetValidSourceTypes() throws Exception {
		UploadCapInfo testSubject;
		List<String> validSourceTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValidSourceTypes(validSourceTypes);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		UploadCapInfo testSubject;
		List<UploadPropInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		UploadCapInfo testSubject;
		List<UploadPropInfo> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}