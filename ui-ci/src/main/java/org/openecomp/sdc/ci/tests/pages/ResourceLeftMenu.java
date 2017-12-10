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

package org.openecomp.sdc.ci.tests.pages;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;

public class ResourceLeftMenu implements ComponentLeftMenu {

	public void moveToGeneralScreen() throws Exception {
		GeneralUIUtils.moveToStep(StepsEnum.GENERAL);
	}

	public void moveToIconScreen() throws Exception {
		GeneralUIUtils.moveToStep(StepsEnum.ICON);
	}

	public void moveToDeploymentArtifactScreen() {
		GeneralUIUtils.moveToStep(StepsEnum.DEPLOYMENT_ARTIFACT);
	}

	public void moveToInformationalArtifactScreen() {
		GeneralUIUtils.moveToStep(StepsEnum.INFORMATION_ARTIFACT);
	}

	public void moveToPropertiesScreen() throws Exception {
		GeneralUIUtils.moveToStep(StepsEnum.PROPERTIES);
	}

	public void moveToCompositionScreen() throws Exception {
		GeneralUIUtils.moveToStep(StepsEnum.COMPOSITION);
	}

	public void moveToActivityLogScreen() throws Exception {
		GeneralUIUtils.moveToStep(StepsEnum.ACTIVITY_LOG);
	}

	public void moveToDeploymentViewScreen() throws Exception {
		GeneralUIUtils.moveToStep(StepsEnum.DEPLOYMENT_VIEW);
	}

	public void moveToToscaArtifactsScreen() {
		GeneralUIUtils.moveToStep(StepsEnum.TOSCA_ARTIFACTS);
	}
	
	public void moveToInputsScreen() {
		GeneralUIUtils.moveToStep(StepsEnum.INPUTS);
	}
	
	public void moveToPropertiesAssignmentScreen() {
		GeneralUIUtils.moveToStep(StepsEnum.PROPERTIES_ASSIGNMENT);
	}
}
