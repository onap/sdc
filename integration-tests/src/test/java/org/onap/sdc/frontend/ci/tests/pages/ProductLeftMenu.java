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
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;

/**
 * @author al714h
 */

public class ProductLeftMenu implements ComponentLeftMenu {

    public void moveToGeneralScreen() throws Exception {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.GENERAL);
    }

    public void moveToIconScreen() throws Exception {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.ICON);
    }

    public void moveToCompositionScreen() throws Exception {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.COMPOSITION);
    }

    public void moveToHierarchyScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.HIERARCHY);
    }
}
