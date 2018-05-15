package org.openecomp.sdc.be.components.merge.property;

import java.util.List;

import org.junit.Test;

import mockit.Deencapsulation;

public class ComplexPropertyValueMergerTest {

	private ComplexPropertyValueMerger createTestSubject() {
		return new ComplexPropertyValueMerger();
	}

	@Test
	public void testGetInstance() throws Exception {
		PropertyValueMerger result;

		// default test
		result = ComplexPropertyValueMerger.getInstance();
	}

	@Test
	public void testMerge() throws Exception {
		ComplexPropertyValueMerger testSubject;
		Object oldVal = null;
		Object newVal = null;
		List<String> someStrings = null;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "merge", new Object[] { Object.class, Object.class, List.class });
	}
}