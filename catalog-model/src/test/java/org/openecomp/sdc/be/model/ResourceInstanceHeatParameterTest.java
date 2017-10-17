package org.openecomp.sdc.be.model;

import javax.annotation.Generated;

import org.junit.Test;


public class ResourceInstanceHeatParameterTest {

	private ResourceInstanceHeatParameter createTestSubject() {
		return new ResourceInstanceHeatParameter();
	}

	
	@Test
	public void testGetValueUniqueId() throws Exception {
		ResourceInstanceHeatParameter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValueUniqueId();
	}

	
	@Test
	public void testSetValueUniqueId() throws Exception {
		ResourceInstanceHeatParameter testSubject;
		String valueUniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setValueUniqueId(valueUniqueId);
	}

	
	@Test
	public void testToString() throws Exception {
		ResourceInstanceHeatParameter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}