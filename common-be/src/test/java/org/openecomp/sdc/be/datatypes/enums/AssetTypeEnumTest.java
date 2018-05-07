package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class AssetTypeEnumTest {

	private AssetTypeEnum createTestSubject() {
		return AssetTypeEnum.PRODUCTS;
	}

	@Test
	public void testGetValue() throws Exception {
		AssetTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testGetCorrespondingComponent() throws Exception {
		AssetTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCorrespondingComponent();
	}

	@Test
	public void testConvertToComponentTypeEnum() throws Exception {
		String assetType = "products";
		ComponentTypeEnum result;

		// default test
		result = AssetTypeEnum.convertToComponentTypeEnum(assetType);
	}
}