package org.openecomp.sdc.ci.tests.verificator;

import static org.testng.Assert.assertTrue;


import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;

public class PropertiesAssignmentVerificator {
	
	public static void validateFilteredPropertiesCount(int propertiesCount, String propertyLocation){
		int actualPropertiesCount = GeneralUIUtils.getWebElementsListByContainsClassName(propertyLocation).size();
		String errMsg = String.format("Properties amount not as expected, expected: %s ,Actual: %s", propertiesCount, actualPropertiesCount);
		assertTrue(actualPropertiesCount == propertiesCount, errMsg);
	}
}
