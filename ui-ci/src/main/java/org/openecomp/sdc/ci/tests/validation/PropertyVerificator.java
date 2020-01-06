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

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;

import static org.testng.Assert.assertTrue;

public class PropertyVerificator {


    private PropertyVerificator() {
    }

    public static void validateEditVFCPropertiesPopoverFields(PropertyTypeEnum propertyType) {
        String propertyValue = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_VALUE.getValue()).getAttribute("value");
        String propertyDescription = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_DESCRIPTION.getValue()).getAttribute("value");
        assertTrue(propertyType.getUpdateValue().equals(propertyValue), String.format("Property Value of type %s. Expected: %s, Actual: %s ", propertyType.getType().toString(), propertyType.getUpdateValue(), propertyValue));
        assertTrue(propertyType.getUpdateDescription().equals(propertyDescription), String.format("Property Description of type %s. Expected: %s, Actual: %s ", propertyType.getType().toString(), propertyType.getUpdateDescription(), propertyDescription));
    }
}
