package org.openecomp.sdc.be.resources.data;

import mockit.Deencapsulation;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

public class ServiceCategoryDataTest {

	private ServiceCategoryData createTestSubject() {
		return new ServiceCategoryData();
	}

	@Test
	public void testCtor() throws Exception {
		new ServiceCategoryData(new HashedMap());
		new ServiceCategoryData("mock");
	}
	
	@Test
	public void testCreateUniqueId() throws Exception {
		ServiceCategoryData testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "createUniqueId");
	}
}