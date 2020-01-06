/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.ci.tests.validation;

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class PropertiesAssignmentVerificator {

    private PropertiesAssignmentVerificator() {
    }

    public static void validateFilteredPropertiesCount(int propertiesCount, String propertyLocation) {
        int actualPropertiesCount = GeneralUIUtils.getWebElementsListByContainsClassName(propertyLocation).size();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating. Expected properties count: %s , Actual: %s", propertiesCount, actualPropertiesCount));
        String errMsg = String.format("Properties amount not as expected, expected: %s ,Actual: %s", propertiesCount, actualPropertiesCount);
        assertTrue(actualPropertiesCount == propertiesCount, errMsg);
    }

    public static void validatePropertyValue(String expectedPropertyName, String expectedPropertyValue) {
        String actualPropertyValue = GeneralUIUtils.getWebElementByTestID(expectedPropertyName).getAttribute("value");
        assertTrue(expectedPropertyValue.equals(actualPropertyValue), String.format("Validating the value of property/input %s. Expected: %s, Actual: %s ", expectedPropertyName, expectedPropertyValue, actualPropertyValue));
    }

    public static void validatePropertyValueIsNull(String expectedPropertyName) {
        String actualPropertyValue = GeneralUIUtils.getWebElementByTestID(expectedPropertyName).getAttribute("value");
        assertNull(actualPropertyValue, String.format("Validating the value of property/input %s. Expected: empty, Actual: %s ", expectedPropertyName, actualPropertyValue));
    }

    public static void validateListPropertyValue(DataTestIdEnum.PropertiesAssignmentScreen prefix, String expectedPropertyName, String expectedPropertyValue, int index) {
        String listElement = prefix.getValue() + expectedPropertyName + "." + String.valueOf(index);
        String actualPropertyValue = GeneralUIUtils.getWebElementByTestID(listElement).getAttribute("value");
        assertTrue(expectedPropertyValue.equals(actualPropertyValue), String.format("Validating the %s list element value of property %s. Expected: %s, Actual: %s ", index, expectedPropertyName, expectedPropertyValue, actualPropertyValue));
    }

    public static void validateBooleanPropertyValue(String expectedPropertyName, String expectedPropertyValue) {
        String actualPropertyValue = GeneralUIUtils.getSelectedElementFromDropDown(expectedPropertyName).getText();
        assertTrue(expectedPropertyValue.equals(actualPropertyValue), String.format("Validating the value of property %s. Expected: %s, Actual: %s ", expectedPropertyName, expectedPropertyValue, actualPropertyValue));
    }
}
