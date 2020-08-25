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

public class ResourceLeftMenu implements ComponentLeftMenu {

    public void moveToGeneralScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.GENERAL);
    }

    public void moveToIconScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.ICON);
    }

    public void moveToDeploymentArtifactScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.DEPLOYMENT_ARTIFACT);
    }

    public void moveToInformationalArtifactScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.INFORMATION_ARTIFACT);
    }

    public void moveToPropertiesScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.PROPERTIES);
    }

    public void moveToCompositionScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.COMPOSITION);
    }

    public void moveToActivityLogScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.ACTIVITY_LOG);
    }

    public void moveToDeploymentViewScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.DEPLOYMENT_VIEW);
    }

    public void moveToToscaArtifactsScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.TOSCA_ARTIFACTS);
    }

    public void moveToInputsScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.INPUTS);
    }

    public void moveToPropertiesAssignmentScreen() {
        GeneralUIUtils.moveToStep(DataTestIdEnum.StepsEnum.PROPERTIES_ASSIGNMENT);
    }
}
