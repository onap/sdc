package org.openecomp.sdc.ci.tests.verificator;

import static org.testng.Assert.assertTrue;

import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;

import com.aventstack.extentreports.Status;

public class PropertiesAssignmentVerificator {
	
	public static void validateFilteredPropertiesCount(int propertiesCount, String propertyLocation){
		int actualPropertiesCount = GeneralUIUtils.getWebElementsListByContainsClassName(propertyLocation).size();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating. Expected properties count: %s , Actual: %s", propertiesCount, actualPropertiesCount));
		String errMsg = String.format("Properties amount not as expected, expected: %s ,Actual: %s", propertiesCount, actualPropertiesCount);
		assertTrue(actualPropertiesCount == propertiesCount, errMsg);
	}
}
