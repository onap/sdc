package org.openecomp.sdc.ci.tests.verificator;

import static org.testng.Assert.assertTrue;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;

public class PropertyVerificator {

	
	public static void validateEditVFCPropertiesPopoverFields(PropertyTypeEnum propertyType){
			String propertyValue = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_VALUE.getValue()).getAttribute("value");
			String propertyDescription = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_DESCRIPTION.getValue()).getAttribute("value");
			assertTrue(propertyType.getUpdateValue().equals(propertyValue), String.format("Property Value of type %s. Expected: %s, Actual: %s ",  propertyType.getType().toString(), propertyType.getUpdateValue(), propertyValue));
			assertTrue(propertyType.getUpdateDescription().equals(propertyDescription), String.format("Property Description of type %s. Expected: %s, Actual: %s ", propertyType.getType().toString(), propertyType.getUpdateDescription(), propertyDescription));
	}
}