package org.openecomp.sdc.uici.tests.execute.generalTests;

import static org.testng.AssertJUnit.assertTrue;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.uici.tests.execute.base.SetupCDTest;
import org.openecomp.sdc.uici.tests.utilities.GeneralUIUtils;
import org.testng.annotations.Test;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum;

public class GeneralTests  extends SetupCDTest{
	
	@Test
	public void filterVFCMT() throws Exception {
		Resource resource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFCMT, NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_CALL_CONTROL , UserRoleEnum.DESIGNER, true).left().value();
		assertTrue(!GeneralUIUtils.isElementPresent(resource.getName()));
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.MainMenue.CATALOG.getValue()).click();
		assertTrue(!GeneralUIUtils.isElementPresent(resource.getName()));
	}

}
