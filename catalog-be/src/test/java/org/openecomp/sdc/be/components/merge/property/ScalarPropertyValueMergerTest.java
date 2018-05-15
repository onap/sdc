package org.openecomp.sdc.be.components.merge.property;

import java.util.List;

import org.junit.Test;

import mockit.Deencapsulation;

public class ScalarPropertyValueMergerTest {

	private ScalarPropertyValueMerger createTestSubject() {
		return new ScalarPropertyValueMerger();
	}

	@Test
	public void testGetInstance() throws Exception {
		PropertyValueMerger result;

		// default test
		result = ScalarPropertyValueMerger.getInstance();
	}

	@Test
	public void testMerge() throws Exception {
	ScalarPropertyValueMerger testSubject;Object oldVal = null;
	Object newVal = null;
	List<String> getInputNamesToMerge = null;
	Object result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "merge", new Object[]{Object.class, Object.class, List.class});
	}
}