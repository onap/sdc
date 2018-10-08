package org.openecomp.sdc.be.datamodel.utils;

import org.junit.Test;
import org.openecomp.sdc.be.datamodel.api.CategoryTypeEnum;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

public class NodeTypeConvertUtilsTest {

	@Test
	public void testGetCategoryNodeTypeByComponentParam() throws Exception {
		// test 1
		for (ComponentTypeEnum comp : ComponentTypeEnum.values()) {
			for (CategoryTypeEnum cat : CategoryTypeEnum.values()) {
				NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(comp, cat);
			}
		}

	}
}