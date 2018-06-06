package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;


public class ComponentMetadataDefinitionTest {

	private ComponentMetadataDefinition createTestSubject() {
		return new ComponentMetadataDefinition();
	}
	
	@Test
	public void testCtor() throws Exception {
		new ComponentMetadataDefinition(new ResourceMetadataDataDefinition());
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
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		obj = new Object();
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
		ComponentMetadataDefinition testSubject2 = createTestSubject();
		testSubject.componentMetadataDataDefinition = new ResourceMetadataDataDefinition();
		result = testSubject.equals(testSubject2);
		Assert.assertEquals(false, result);
		testSubject2.componentMetadataDataDefinition = new ResourceMetadataDataDefinition();
		result = testSubject.equals(testSubject2);
		Assert.assertEquals(true, result);
	}
}