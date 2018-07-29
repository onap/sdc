package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

import java.util.List;

public class FilterKeyEnumTest {

	private FilterKeyEnum createTestSubject() {
		return FilterKeyEnum.CATEGORY;
	}

	@Test
	public void testGetName() throws Exception {
		FilterKeyEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testGetAllFilters() throws Exception {
		List<String> result;

		// default test
		result = FilterKeyEnum.getAllFilters();
	}

	@Test
	public void testGetValidFiltersByAssetType() throws Exception {
		ComponentTypeEnum assetType = null;
		List<String> result;

		// default test
		result = FilterKeyEnum.getValidFiltersByAssetType(ComponentTypeEnum.RESOURCE);
		result = FilterKeyEnum.getValidFiltersByAssetType(ComponentTypeEnum.SERVICE);
		result = FilterKeyEnum.getValidFiltersByAssetType(ComponentTypeEnum.SERVICE_INSTANCE);
		result = FilterKeyEnum.getValidFiltersByAssetType(assetType);
	}
}