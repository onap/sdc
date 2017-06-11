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

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.WebElement;

public class ToscaArtifactsPage extends DeploymentArtifactPage {

	public ToscaArtifactsPage() {

	}
	
	public static String getArtifactName(int row){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ToscaArtifactsScreenEnum.ARTIFACT_NAME.getValue() + row).getText();
	}
	
	public static String getArtifactType(int row){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ToscaArtifactsScreenEnum.ARTIFACT_TYPE.getValue() + row).getText();
	}
	
	public static String getArtifactVersion(int row){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ToscaArtifactsScreenEnum.ARTIFACT_VERSION.getValue() + row).getText();
	}
	
	public static WebElement getArtifactDetails(int row){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ToscaArtifactsScreenEnum.ARTIFACT_DETAILS.getValue() + row);
	}

	public static void downloadCsar() {
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ToscaArtifactsScreenEnum.DOWNLOAD_CSAR.getValue());
		GeneralUIUtils.ultimateWait();
	}

}
