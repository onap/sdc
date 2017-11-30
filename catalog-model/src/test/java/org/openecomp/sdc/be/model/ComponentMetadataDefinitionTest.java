package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;


public class ComponentMetadataDefinitionTest {

	private ComponentMetadataDefinition createTestSubject() {
		return new ComponentMetadataDefinition();
	}

	
	@Test
	public void testGetMetadataDataDefinition() throws Exception {
		ComponentMetadataDefinition testSubject;
		ComponentMetadataDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadataDataDefinition();
	}

	
	@Test
	public void testHashCode() throws Exception {
		ComponentMetadataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		ComponentMetadataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
	}
}