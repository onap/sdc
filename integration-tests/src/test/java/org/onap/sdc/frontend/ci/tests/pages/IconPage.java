/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.sdc.frontend.ci.tests.pages;

import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;

public class IconPage extends GeneralPageElements {

    private IconPage() {
        super();
    }

    public static void clickOnIcon(ResourceCategoryEnum iconName) {
        GeneralUIUtils.getWebElementByTestID(iconStringBuilder(iconName) + DataTestIdEnum.ServiceMetadataEnum.ICON.getValue())
                .click();
    }

    private static String iconStringBuilder(ResourceCategoryEnum icon) {
        String iconName = icon.getSubCategory();
        String[] splitedIconName = iconName.split(" ");
        splitedIconName[0] = splitedIconName[0].toLowerCase();

        StringBuilder sb = new StringBuilder();
        for (String word : splitedIconName) {
            sb.append(word);
        }

        return sb.toString();
    }

}
